/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * Service handler for the RMI communication bundle {@link Bundle}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class ServiceHandler {

    private static ServiceCallHandler nullServiceCallHandler = ServiceUtils.createNullService(ServiceCallHandler.class);

    private static PlatformService nullPlatformService = ServiceUtils.createNullService(PlatformService.class);

    private static ConfigurationService nullConfigurationService = ServiceUtils.createNullService(ConfigurationService.class);

    private static RMIConfiguration rmiConfiguration;

    private static String bundleSymbolicName;

    private static ServiceCallHandler serviceCallHandler = nullServiceCallHandler;

    private static PlatformService platformService = nullPlatformService;

    private static ConfigurationService configurationService = nullConfigurationService;

    protected void activate(BundleContext context) throws CommunicationException {
        bundleSymbolicName = context.getBundle().getSymbolicName();
        // initialize the configuration of the bundle
        rmiConfiguration = configurationService.getConfiguration(bundleSymbolicName, RMIConfiguration.class);
        // disabled RMI registry creation as it is only used by the old communication layer,
        // and produces port collisions when starting more than one instance
        // TODO remove completely when done
        // RMISinkImpl.start(rmiConfiguration, serviceCallHandler);
    }

    protected void deactivate() {
        // RMISinkImpl.stop();
    }

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    protected void bindServiceCallHandler(ServiceCallHandler newServiceCallHandler) {
        serviceCallHandler = newServiceCallHandler;
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    public static String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public static ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public static ServiceCallHandler getServiceCallHandler() {
        return serviceCallHandler;
    }

    public static PlatformService getPlatformService() {
        return platformService;
    }
}
