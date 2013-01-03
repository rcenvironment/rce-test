/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import junit.framework.TestCase;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;

/**
 * Unit test for {@link SOAPServiceCallSenderFactory}.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class SOAPServiceCallSenderFactoryTest extends TestCase {

    private static final NetworkContact CONTACT = new NetworkContact(
        "127.0.0.1", "de.rcenvironment.rce.communication.soap", 1099);

    private SOAPServiceCallSenderFactory myRequestSenderFactory = null;

    @Override
    protected void setUp() throws Exception {
        myRequestSenderFactory = new SOAPServiceCallSenderFactory();
        myRequestSenderFactory.bindConfigurationService(SOAPMockFactory.getInstance().getConfigurationService(true));
        myRequestSenderFactory.bindPlatformService(SOAPMockFactory.getInstance().getPlatformService());
        myRequestSenderFactory.bindJettyService(new JettyServiceImplDummy());
        myRequestSenderFactory.bindServiceCallHandler(new ServiceCallHandlerDummy());
        myRequestSenderFactory.activate(SOAPMockFactory.getInstance().getBundleContextMock());
        myRequestSenderFactory.bindConfigurationService(SOAPMockFactory.getInstance().getConfigurationService(false));
        myRequestSenderFactory.activate(SOAPMockFactory.getInstance().getBundleContextMock());
    }

    @Override
    protected void tearDown() throws Exception {
        myRequestSenderFactory.deactivate();
        myRequestSenderFactory = null;
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testCreateRequestSenderForSuccess() throws Exception {
        myRequestSenderFactory.createServiceCallSender(CONTACT);
        assertTrue(true);
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testCreateRequestSenderForSanity() throws Exception {
        ServiceCallSender communicator = myRequestSenderFactory.createServiceCallSender(CONTACT);
        assertNotNull(communicator);
        assertTrue(communicator instanceof SOAPServiceCallSender);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testCreateRequestSenderForFailure() throws Exception {
        try {
            myRequestSenderFactory.createServiceCallSender(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
