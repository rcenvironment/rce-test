/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * 
 * Factory for mock objects used by this bundle's test.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public final class SOAPMockFactory {

    /**
     * The instance of this singleton class.
     */
    private static SOAPMockFactory instance = null;

    /**
     * Getter.
     * 
     * @return the instance of this singleton object.
     * @throws Exception if an error occurs.
     */
    public static SOAPMockFactory getInstance() throws Exception {
        if (instance == null) {
            instance = new SOAPMockFactory();
        }

        return instance;
    }

    /**
     * Getter.
     * @return the bundle context mock.
     */
    public BundleContext getBundleContextMock() {

        Bundle bundleMock = createSOAPBundleMock(SOAPTestConstants.BUNDLE_SYMBOLIC_NAME);
        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.replay(bundleContextMock);

        return bundleContextMock;
    }

    
    /**
     * Getter.
     * @return The configuration service mock object.
     * @param protocol Set the configuration https true/false.
     */
    public ConfigurationService getConfigurationService(Boolean protocol) {

        SOAPConfiguration mySoapConfiguration = new SOAPConfiguration();
        mySoapConfiguration.setPort(SOAPTestConstants.PORT);
        ConfigurationService configurationServiceMock = EasyMock.createNiceMock(ConfigurationService.class);
        EasyMock.expect(configurationServiceMock.getConfiguration(
            SOAPTestConstants.BUNDLE_SYMBOLIC_NAME, SOAPConfiguration.class)).andReturn(mySoapConfiguration).anyTimes();
        EasyMock.replay(configurationServiceMock);
        
        return configurationServiceMock;
    }
    
    /**
     * Getter.
     * @return a platform service mock object.
     */
    public PlatformService getPlatformService() {
        
        PlatformService platformServiceMock = EasyMock.createNiceMock(PlatformService.class);
        EasyMock.expect(platformServiceMock.getPlatformIdentifier()).andReturn(SOAPTestConstants.LOCAL_PLATFORM).anyTimes();
        PlatformIdentifier requestPlatform = SOAPTestConstants.REQUEST.getRequestedPlatform();
        EasyMock.expect(platformServiceMock.isLocalPlatform(requestPlatform)).andReturn(true).anyTimes();
        EasyMock.replay(platformServiceMock);
        
        return platformServiceMock;
    }
                    
    /**
     * Creates a SOAP bundle mock object.
     * 
     * @param bundleSymbolicName The symbolic name of the bundle.
     * @return The created mock.
     */
    private Bundle createSOAPBundleMock(String bundleSymbolicName) {
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(bundleSymbolicName).anyTimes();
        EasyMock.replay(bundleMock);
        return bundleMock;
    }

}
