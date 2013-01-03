/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.callback.CallbackProxyService;
import de.rcenvironment.rce.communication.callback.CallbackService;

/**
 * {@link Job} which sets the time to live (TTL) for call back objects and call back proxy objects
 * which are held by {@link CallbackService} and {@link CallbackProxyService}. Additoonally it
 * removes objects and proxies which time to live is expired.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class CleanJob implements Job {

    /** Represents 5 min. */
    public static final int TTL = 600000;

    /** Represents 4 min. */
    public static final int UPDATE_INTERVAL = 8;

    /** Key for the service to call contained in the job context. */
    public static final String SERVICE = "de.rcenvironment.rce.communication.callback.service";

    /** Key for the weak object references map contained in the job context. */
    public static final String WEAK_MAP = "de.rcenvironment.rce.communication.callback.weak";

    /** Key for the TTL map contained in the job context. */
    public static final String TTL_MAP = "de.rcenvironment.rce.communication.callback.ttl";

    /** Key for the platforms (to call) map contained in the job context. */
    public static final String PLATFORMS_MAP = "de.rcenvironment.rce.communication.callback.platforms";

    private static BundleContext context;

    private static CommunicationService communicationService;

    private static Scheduler scheduler;

    /** Only called by OSGi. */
    @Deprecated
    public CleanJob() {}

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void unbindCommunicationService(CommunicationService oldCommunicationService) {
        communicationService = ServiceUtils.createNullService(CommunicationService.class);
    }

    /**
     * Schedules a clean job.
     * 
     * @param iface The service's iface to call for TTL update.
     * @param objects The referenced objects.
     * @param ttls The TTLs of the references objects.
     * @param platforms The platforms to call for TTL update.
     */
    @SuppressWarnings({ "rawtypes" })
    public static void scheduleJob(Class iface,
        Map<String, WeakReference<Object>> objects,
        Map<String, Long> ttls,
        Map<String, PlatformIdentifier> platforms) {

        try {
            synchronized (CleanJob.class) {
                if (scheduler == null) {
                    StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
                    try {
                        Properties props = new Properties();
                        InputStream propsStream = CleanJob.class.getResourceAsStream("/quartz.properties");
                        if (propsStream == null) {
                            throw new IOException("Null stream returned for quartz.properties");
                        }
                        props.load(propsStream);
                        schedulerFactory.initialize(props);
                        LogFactory.getLog(CleanJob.class).debug("Custom Quartz configuration loaded");
                    } catch (IOException e) {
                        LogFactory.getLog(CleanJob.class).error("Failed to load custom Quartz configuration", e);
                    }
                    scheduler = schedulerFactory.getScheduler();
                    scheduler.start();
                }
            }
            JobDetail jobDetail = new JobDetail(iface.getCanonicalName() + ".job", CleanJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(CleanJob.SERVICE, iface);
            jobDataMap.put(CleanJob.WEAK_MAP, objects);
            jobDataMap.put(CleanJob.TTL_MAP, ttls);
            jobDataMap.put(CleanJob.PLATFORMS_MAP, platforms);

            Trigger trigger = TriggerUtils.makeMinutelyTrigger(CleanJob.UPDATE_INTERVAL);
            trigger.setStartTime(new Date());
            trigger.setName(iface.getCanonicalName() + ".trigger");

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("remote callback feature is down", e);
        }

    }

    /**
     * Unschedules a clean job.
     * 
     * @param iface The service's iface to call for TTL update.
     */
    @SuppressWarnings({ "rawtypes" })
    public static void unscheduleJob(Class iface) {

        try {
            if (scheduler != null) {
                scheduler.unscheduleJob(iface.getCanonicalName() + ".trigger", null);
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("remote callback feature is down");
        }

    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
        Class iface = (Class) jobDataMap.get(SERVICE);
        Map<String, WeakReference<Object>> objects = (Map<String, WeakReference<Object>>) jobDataMap.get(WEAK_MAP);
        Map<String, PlatformIdentifier> platforms = (Map<String, PlatformIdentifier>) jobDataMap.get(PLATFORMS_MAP);
        Map<String, Long> ttls = (Map<String, Long>) jobDataMap.get(TTL_MAP);

        // remove all unreferenced and expired objects and renew TTL for all remaining objects
        synchronized (objects) {
            for (Iterator<String> iterator = objects.keySet().iterator(); iterator.hasNext();) {
                String id = iterator.next();
                if (objects.get(id).get() == null || new Date(ttls.get(id)).before(new Date())) {
                    iterator.remove();
                    ttls.remove(id);
                    platforms.remove(id);
                } else {
                    try {
                        if (iface == CallbackProxyService.class) {
                            CallbackService service = (CallbackService) communicationService.getService(CallbackService.class,
                                platforms.get(id), context);
                            service.setTTL(id, ttls.get(id));
                        } else if (iface == CallbackService.class) {
                            CallbackProxyService service = (CallbackProxyService) communicationService
                                .getService(CallbackProxyService.class, platforms.get(id), context);
                            service.setTTL(id, new Date(System.currentTimeMillis() + CleanJob.TTL).getTime());
                        }
                    } catch (RuntimeException e) {
                        // temporary fix for remote call failures;
                        // see https://www.sistec.dlr.de/mantis/view.php?id=6542
                        LogFactory.getLog(getClass()).warn("Failed to update TTL for id " + id + " via " + iface.getName(), e);
                    }
                }
            }
        }

    }

}
