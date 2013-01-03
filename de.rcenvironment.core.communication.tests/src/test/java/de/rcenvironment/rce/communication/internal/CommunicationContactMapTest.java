/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationTestHelper;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;

/**
 * Unit test for <code>CommunicationContactMap</code>.
 * 
 * @author Doreen Seider
 */
public class CommunicationContactMapTest extends TestCase {

    private static final PlatformIdentifier DEFAULT_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber("192.168.0.100", 1);

    private static final PlatformIdentifier GENERIC_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber("$host", 1);

    private static final PlatformIdentifier IN_NETWORK_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber("192.168.0.0/24", -1);

    private static final PlatformIdentifier NOT_EXISTING_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber("192.168.0.100", 3);

    private static final NetworkContact CONTACT = new NetworkContact("127.0.0.1", "de.rcenvironment.rce.communication.rmi",
        1099);

    private static final NetworkContact GENERIC_CONTACT = new NetworkContact("$host", "de.rcenvironment.rce.communication.rmi",
        1099);

    private final String bundleID1 = "bundleID1";

    private final String bundleID2 = "bundleID2";

    private BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);

    private CommunicationContactMap map;

    @Override
    public void setUp() throws Exception {
        CommunicationTestHelper.activateCommunicationContactMap();

        map = new CommunicationContactMap();
        map.bindConfigurationService(new DummyConfigurationService());
    }

    /**
     * Test.
     * 
     * @throws Exception if an error occurs.
     **/
    public void testActivate() throws Exception {
        Bundle bundleMock1 = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock1.getSymbolicName()).andReturn(bundleID1).anyTimes();
        EasyMock.replay(bundleMock1);

        Bundle bundleMock2 = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock2.getSymbolicName()).andReturn(bundleID2).anyTimes();
        EasyMock.replay(bundleMock2);

        EasyMock.expect(contextMock.getBundle()).andReturn(bundleMock1);
        EasyMock.expect(contextMock.getBundle()).andReturn(bundleMock2);
        EasyMock.replay(contextMock);

        map.activate(contextMock);
        map.activate(contextMock);
    }

    /**
     * Test.
     * 
     * @throws Exception if an error occurs.
     **/
    public void testIfServiceWasUnbound() throws Exception {
        map.unbindConfigurationService(new DummyConfigurationService());
        map.deactivate(contextMock);
        try {
            CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

    }

    /**
     * 
     * Test for success.
     * 
     */
    public void testSetMappingForSuccess() {
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM, CONTACT);
    }

    /**
     * 
     * Test for success.
     * 
     */
    public void testSetMappingsForSuccess() {
        Map<PlatformIdentifier, NetworkContact> entries = new HashMap<PlatformIdentifier, NetworkContact>();
        entries.put(DEFAULT_PLATFORM, CONTACT);
        entries.put(DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.setMappings(CommunicationType.SERVICE_CALL, entries);
        CommunicationContactMap.setMappings(CommunicationType.FILE_TRANSFER, entries);
    }

    /**
     * 
     * Test for success.
     * 
     */
    public void testRemoveAllMappingsEntriesForSucces() {
        CommunicationContactMap.removeAllMappings();
    }

    /**
     * 
     * Test for success.
     * 
     * @throws Exception if the test fails.
     */
    public void testGetContactForSuccess() throws Exception {
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);

        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
    }

    /**
     * Test for failure.
     */
    public void testGetContactForFailure() {
        try {
            CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, NOT_EXISTING_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }
        try {
            CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, NOT_EXISTING_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }
    }

    /**
     * 
     * Test for success.
     * 
     */
    public void testRemoveMappingForSuccess() {
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.removeMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.removeMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
    }

    /**
     * 
     * Test for failure.
     * 
     * @throws Exception if the test fails.
     */
    public void testRemoveMappingForFailure() throws Exception {

        NetworkContact contact = CommunicationContactMap
            .removeMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
        assertNull(contact);

        contact = CommunicationContactMap.removeMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
        assertNull(contact);

    }

    /**
     * 
     * Test for sanity.
     * 
     * @throws Exception if the test fails.
     */
    public void testMappingForSanity() throws Exception {

        CommunicationContactMap.removeAllMappings();
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, IN_NETWORK_PLATFORM, CONTACT);

        NetworkContact contact;
        contact = CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
        assertEquals(CONTACT, contact);

        try {
            CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        contact = CommunicationContactMap.removeMapping(CommunicationType.SERVICE_CALL, IN_NETWORK_PLATFORM);
        assertEquals(CONTACT, contact);

        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.removeAllMappings();

        try {
            CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, IN_NETWORK_PLATFORM, CONTACT);

        contact = CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
        assertEquals(CONTACT, contact);

        try {
            CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        contact = CommunicationContactMap.removeMapping(CommunicationType.FILE_TRANSFER, IN_NETWORK_PLATFORM);
        assertEquals(CONTACT, contact);

        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM, CONTACT);
        CommunicationContactMap.removeAllMappings();

        try {
            CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, GENERIC_PLATFORM, GENERIC_CONTACT);
        contact = CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
        assertEquals(new NetworkContact(DEFAULT_PLATFORM.resolveHost(), CONTACT.getProtocol(), CONTACT.getPort()), contact);

        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, GENERIC_PLATFORM, GENERIC_CONTACT);
        contact = CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
        assertEquals(new NetworkContact(DEFAULT_PLATFORM.resolveHost(), CONTACT.getProtocol(), CONTACT.getPort()), contact);

        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, GENERIC_PLATFORM, CONTACT);
        contact = CommunicationContactMap.getContact(CommunicationType.SERVICE_CALL, DEFAULT_PLATFORM);
        assertEquals(CONTACT, contact);

        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, GENERIC_PLATFORM, CONTACT);
        contact = CommunicationContactMap.getContact(CommunicationType.FILE_TRANSFER, DEFAULT_PLATFORM);
        assertEquals(CONTACT, contact);
    }

    /**
     * Test {@link ConfigurationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            CommunicationConfiguration commConfig = new CommunicationConfiguration();
            if (identifier.equals(bundleID1)) {
                commConfig.setFileTransferContacts(new Vector<String>() {

                    {
                        add("0.0.0.0/0:1=127.0.0.1:de.rcenvironment.rce.communication:1099");
                        add("1.2.4.0:2=127.0.0.1:de.rcenvironment.rce.communication:1099");
                    }
                });

                commConfig.setServiceCallContacts(new Vector<String>() {

                    {
                        add("0.0.0.0/0:1=127.0.0.1:de.rcenvironment.rce.communication.rmi:1099");
                        add("1.2.4.0:2=127.0.0.1:de.rcenvironment.rce.communication.soap:1089");
                    }
                });
            } else if (identifier.equals(bundleID2)) {
                commConfig.setFileTransferContacts(new Vector<String>() {

                    {
                        add("0.0.0.0/0:1127.0.0.1:de.rcenvironment.rce.communication:1099");
                        add("1.2.4.0:2=127.0.0.1:de.rcenvironment.rce.communication:1099");
                    }
                });

                commConfig.setServiceCallContacts(new Vector<String>() {

                    {
                        add("0.0.0.0/0:hust=127.0.0.1:de.rcenvironment.rce.communication.rmi:1099");
                        add("1.2.4.0:2=127.0.0.1:de.rcenvironment.rce.communication.soap:1089");
                    }
                });
            }

            return (T) commConfig;
        }

    }
}
