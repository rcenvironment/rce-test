/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import static de.rcenvironment.rce.communication.rmi.internal.RMITestConstants.BUNDLE_SYMBOLIC_NAME;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * 
 * Factory for mock objects used by this bundle's test.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public final class RMIMockFactory {

    /**
     * The instance of this singleton class.
     */
    private static RMIMockFactory instance = null;

    /**
     * 
     * Getter.
     * 
     * @return the instance of this singleton object.
     * @throws Exception if an error occurs.
     */
    public static RMIMockFactory getInstance() throws Exception {
        if (instance == null) {
            instance = new RMIMockFactory();
        }

        return instance;
    }

    /**
     * Getter.
     * 
     * @return the bundle context mock.
     */
    public BundleContext getBundleContextMock() {

        Bundle bundleMock = createRMIBundleMock(BUNDLE_SYMBOLIC_NAME);
        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.replay(bundleContextMock);

        return bundleContextMock;
    }

    /**
     * Getter.
     * 
     * @param rmiPort the rmi port
     * @return The configuration service mock object.
     */
    public ConfigurationService getConfigurationService(final int rmiPort) {

        RMIConfiguration rmiConfiguration = new RMIConfiguration();
        rmiConfiguration.setRegistryPort(rmiPort);
        ConfigurationService configurationMock = EasyMock.createNiceMock(ConfigurationService.class);
        EasyMock.expect(configurationMock.getConfiguration(BUNDLE_SYMBOLIC_NAME,
            RMIConfiguration.class)).andReturn(rmiConfiguration).anyTimes();
        EasyMock.replay(configurationMock);

        return configurationMock;
    }

    /**
     * Getter.
     * 
     * @return a platform service mock object.
     */
    public PlatformService getPlatformService() {

        PlatformService platformServiceMock = EasyMock.createNiceMock(PlatformService.class);
        EasyMock.expect(platformServiceMock.getPlatformIdentifier()).andReturn(RMITestConstants.LOCAL_PLATFORM).anyTimes();
        PlatformIdentifier requestPlatform = RMITestConstants.REQUEST.getRequestedPlatform();
        EasyMock.expect(platformServiceMock.isLocalPlatform(requestPlatform)).andReturn(true).anyTimes();
        EasyMock.replay(platformServiceMock);

        return platformServiceMock;
    }

    /**
     * Getter.
     * 
     * @return a information service mock object.
     */
    public ServiceCallHandler getServiceCallHandler() {

        ServiceCallHandler serviceCallHandlerMock = EasyMock.createNiceMock(ServiceCallHandler.class);

        ServiceCallResult serviceCallResultMock = EasyMock.createNiceMock(ServiceCallResult.class);
        EasyMock.expect(serviceCallResultMock.getReturnValue())
            .andReturn(RMITestConstants.RETURN_VALUE).anyTimes();
        EasyMock.replay(serviceCallResultMock);

        try {
            EasyMock.expect(serviceCallHandlerMock.handle(RMITestConstants.REQUEST)).andReturn(serviceCallResultMock).anyTimes();
        } catch (CommunicationException e) {
            e.getCause();
        }

        EasyMock.replay(serviceCallHandlerMock);

        return serviceCallHandlerMock;
    }

    /**
     * 
     * Getter.
     * 
     * @param rmiPort the rmi port
     * @return a configuration service mock object with broken configuration.
     */
    public ConfigurationService getBrokenConfigurationService(final int rmiPort) {

        ConfigurationService configurationMock = EasyMock.createNiceMock(ConfigurationService.class);

        RMIConfiguration rmiConfiguration = new RMIConfiguration();
        rmiConfiguration.setRegistryPort(rmiPort);

        EasyMock.expect(configurationMock.getConfiguration(BUNDLE_SYMBOLIC_NAME, RMIConfiguration.class))
            .andReturn(rmiConfiguration).anyTimes();
        EasyMock.replay(configurationMock);

        return configurationMock;
    }

    /**
     * 
     * Creates a RMI bundle mock object.
     * 
     * @param bundleSymbolicName The symbolic name of the bundle.
     * @return The created mock.
     */
    private Bundle createRMIBundleMock(String bundleSymbolicName) {
        // RMI bundle
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(bundleSymbolicName).anyTimes();
        EasyMock.replay(bundleMock);
        return bundleMock;
    }

}
