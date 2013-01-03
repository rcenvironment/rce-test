/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;
import de.rcenvironment.rce.configuration.testutils.PersistentSettingsServiceDefaultStub;

/**
 * Test cases for {@link PlatformServiceImpl}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class PlatformServiceImplTest {

    private static final int EXPECTED_PLATFORM_ID_LENGTH = 32;

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");

    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remotehost:0");

    private final String serviceBindAddress = "127.0.0.8";

    private final String externalHostAddress = "127.0.0.7";

    private PlatformServiceImpl service;

    private BundleContext contextMock;

    /**
     * Set up.
     * 
     * @throws Exception if an error occur.
     **/
    @Before
    public void setUp() throws Exception {
        service = new PlatformServiceImpl();
        service.bindConfigurationService(new DummyConfigurationService());
        service.bindPersistentSettingsService(new PersistentSettingsServiceDefaultStub());

        contextMock = EasyMock.createNiceMock(BundleContext.class);
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("bundle").anyTimes();
        EasyMock.replay(bundleMock);
        EasyMock.expect(contextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.replay(contextMock);
        service.activate(contextMock);
    }

    /** Test. @throws UnknownHostException Thrown on error. */
    @Test
    public void testGetPlatformIdentifier() throws UnknownHostException {
        PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumber(externalHostAddress, 0);
        assertEquals(pi, service.getLegacyPlatformIdentifier());
    }

    /** Test. */
    @Test
    public void testGetServiceBindAddress() {
        assertEquals(serviceBindAddress, service.getServiceBindAddress());
    }

    /** Test. */
    @Test
    public void testGetRemotePlatforms() {
        Set<PlatformIdentifier> instances = new HashSet<PlatformIdentifier>();
        instances.add(pi1);
        instances.add(pi2);
        assertEquals(instances, service.getRemotePlatforms());
    }

    /** Test. */
    @Test
    @Ignore("This feature is deprecated, and this test uses an arbitrary routable IP for testing; delete after migration")
    public void testIsLocalPlatform() {
        PlatformIdentifier localIdentifier = service.getPlatformIdentifier();
        assertTrue(service.isLocalPlatform(localIdentifier));
        assertTrue(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber("localhost", 0)));
        assertTrue(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber("127.0.0.1", 0)));
        assertFalse(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber(localIdentifier.getHost(), 2)));
        String anyHost = "85.64.145.98";
        if (!localIdentifier.getHost().equals(anyHost)) {
            assertFalse(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber(anyHost, localIdentifier.getPlatformNumber())));
        }
        final String customHostname = "localhosrt";
        assertFalse(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber(customHostname, 0)));
        ConfigurationService configMock = EasyMock.createNiceMock(ConfigurationService.class);
        EasyMock.expect(configMock.getPlatformHost()).andReturn(customHostname).anyTimes();
        EasyMock.replay(configMock);
        service.bindConfigurationService(configMock);
        final String customHostname2 = "localhost";
        assertTrue(service.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumber(customHostname2, 0)));

    }

    /**
     * Tests the returned {@link PlatformIdentityInformation}.
     */
    @Test
    public void testGetIdentityInformation() {
        PlatformIdentityInformation platformInf = service.getIdentityInformation();
        // basic test: check that the persistent id is defined and of the expected length
        assertEquals(EXPECTED_PLATFORM_ID_LENGTH, platformInf.getPersistentNodeId().length());
    }

    /**
     * Test {@link ConfigurationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            CommunicationConfiguration config = new CommunicationConfiguration();
            config.setRemotePlatforms(new Vector<String>() {

                {
                    add("localhost:0");
                    add("remotehost:0");
                    add("brokenhost1");
                }
            });
            config.setExternalAddress(externalHostAddress);
            config.setBindAddress(serviceBindAddress);
            return (T) config;
        }

        @Override
        public boolean getIsWorkflowHost() {
            return false;
        }

        @Override
        public String getPlatformHost() {
            return "";
        }

        @Override
        public String getPlatformName() {
            return "";
        }

        @Override
        public int getPlatformNumber() {
            return 0;
        }

    }
}
