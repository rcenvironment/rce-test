/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.callback.CallbackObject;
import de.rcenvironment.rce.communication.callback.CallbackProxy;
import de.rcenvironment.rce.communication.callback.CallbackProxyService;
import de.rcenvironment.rce.communication.callback.CallbackService;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;

/**
 * Test cases for {@link CleanJob}.
 * 
 * @author Doreen Seider
 */
public class CleanJobTest {

    private CleanJob job;

    private final BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:2");

    private final String id1 = "id1";

    private final String id2 = "id2";

    private Map<String, WeakReference<Object>> objects = new HashMap<String, WeakReference<Object>>();

    private Map<String, Long> ttls = new HashMap<String, Long>();

    private Map<String, PlatformIdentifier> platforms = new HashMap<String, PlatformIdentifier>();

    /** Set up. */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        job = new CleanJob();
        job.activate(contextMock);
        job.bindCommunicationService(new DummyCommunicatioService());
    }

    /** Test. */
    @Test
    public void testUnSchedule() {
        CleanJob.scheduleJob(CallbackService.class, objects, ttls, platforms);
        CleanJob.unscheduleJob(CallbackService.class);
    }

    /**
     * Test.
     * 
     * @throws JobExecutionException if an error occurs.
     **/
    @Test
    public void testExecuteForCallbackService() throws JobExecutionException {
        testExecute(CallbackService.class);
    }

    /**
     * Test.
     * 
     * @throws JobExecutionException if an error occurs.
     **/
    @Test
    public void testExecuteForCallbackProxyService() throws JobExecutionException {
        testExecute(CallbackProxyService.class);
    }

    private void testExecute(Class<?> iface) throws JobExecutionException {
        JobDataMap jobDataMapMock = EasyMock.createMock(JobDataMap.class);
        EasyMock.expect(jobDataMapMock.get(CleanJob.SERVICE)).andReturn(iface).anyTimes();
        EasyMock.expect(jobDataMapMock.get(CleanJob.WEAK_MAP)).andReturn(objects).anyTimes();
        EasyMock.expect(jobDataMapMock.get(CleanJob.TTL_MAP)).andReturn(ttls).anyTimes();
        EasyMock.expect(jobDataMapMock.get(CleanJob.PLATFORMS_MAP)).andReturn(platforms).anyTimes();
        EasyMock.replay(jobDataMapMock);

        JobDetail jobDetailMock = EasyMock.createMock(JobDetail.class);
        EasyMock.expect(jobDetailMock.getJobDataMap()).andReturn(jobDataMapMock).anyTimes();
        EasyMock.replay(jobDetailMock);

        JobExecutionContext jobContextMock = EasyMock.createNiceMock(JobExecutionContext.class);
        EasyMock.expect(jobContextMock.getJobDetail()).andReturn(jobDetailMock).anyTimes();
        EasyMock.replay(jobContextMock);

        job.execute(jobContextMock);

        Object o = new Object();
        objects.put(id1, new WeakReference<Object>(o));
        platforms.put(id1, pi1);
        ttls.put(id1, new Date(System.currentTimeMillis() + CleanJob.TTL).getTime());

        job.execute(jobContextMock);

        platforms.put(id1, pi2);
        /*
         * Does not make sense (any more?). Method does not throw RuntimeException. try {
         * 
         * job.execute(jobContextMock); fail(); } catch (RuntimeException e) { assertTrue(true); }
         */
        o = null;
        System.gc();
        job.execute(jobContextMock);

        o = new Object();
        objects.put(id2, new WeakReference<Object>(o));
        platforms.put(id2, pi2);
        ttls.put(id2, new Date(System.currentTimeMillis() - CleanJob.TTL).getTime());

        job.execute(jobContextMock);

    }

    /**
     * Test {@link CommunicationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicatioService extends MockCommunicationService {

        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            Object service = null;
            if (iface == CallbackService.class && platformIdentifier.equals(pi1) && bundleContext == contextMock) {
                service = new DummyCallbackService1();
            } else if (iface == CallbackService.class && platformIdentifier.equals(pi2) && bundleContext == contextMock) {
                service = new DummyCallbackService2();
            } else if (iface == CallbackProxyService.class && platformIdentifier.equals(pi1) && bundleContext == contextMock) {
                service = new DummyCallbackProxyService1();
            } else if (iface == CallbackProxyService.class && platformIdentifier.equals(pi2) && bundleContext == contextMock) {
                service = new DummyCallbackProxyService2();
            }
            return service;
        }

    }

    /**
     * Test {@link CallbackService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCallbackService1 implements CallbackService {

        @Override
        public String addCallbackObject(Object callBackObject, PlatformIdentifier platformIdentifier) {
            return null;
        }

        @Override
        public Object callback(String objectIdentifier, String methodName, List<? extends Serializable> parameters)
            throws CommunicationException {
            return null;
        }

        @Override
        public Object getCallbackObject(String objectIdentifier) {
            return null;
        }

        @Override
        public void setTTL(String objectIdentifier, Long ttl) {}

        @Override
        public Object createCallbackProxy(CallbackObject callbackObject, String objectIdentifier, PlatformIdentifier proxyHome) {
            return null;
        }

        @Override
        public String getCallbackObjectIdentifier(Object callbackObject) {
            return null;
        }

    }

    /**
     * Test {@link CallbackService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCallbackService2 implements CallbackService {

        @Override
        public String addCallbackObject(Object callBackObject, PlatformIdentifier platformIdentifier) {
            return null;
        }

        @Override
        public Object callback(String objectIdentifier, String methodName, List<? extends Serializable> parameters)
            throws CommunicationException {
            return null;
        }

        @Override
        public Object getCallbackObject(String objectIdentifier) {
            return null;
        }

        @Override
        public void setTTL(String objectIdentifier, Long ttl) {
            throw new RuntimeException("fail");

        }

        @Override
        public Object createCallbackProxy(CallbackObject callbackObject, String objectIdentifier, PlatformIdentifier proxyHome) {
            return null;
        }

        @Override
        public String getCallbackObjectIdentifier(Object callbackObject) {
            return null;
        }

    }

    /**
     * Test {@link CallbackProxyService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCallbackProxyService1 implements CallbackProxyService {

        @Override
        public void addCallbackProxy(CallbackProxy callBackProxy) {}

        @Override
        public Object getCallbackProxy(String objectIdentifier) {
            return null;
        }

        @Override
        public void setTTL(String objectIdentifier, Long ttl) {}

    }

    /**
     * Test {@link CallbackProxyService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCallbackProxyService2 implements CallbackProxyService {

        @Override
        public void addCallbackProxy(CallbackProxy callBackProxy) {}

        @Override
        public Object getCallbackProxy(String objectIdentifier) {
            return null;
        }

        @Override
        public void setTTL(String objectIdentifier, Long ttl) {
            throw new RuntimeException("fail");
        }

    }
}
