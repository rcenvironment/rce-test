/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import junit.framework.TestCase;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;

/**
 * Unit test for <code>RMIServiceCallSenderFactory</code>.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class RMIServiceCallSenderFactoryTest extends TestCase {

//    /**
//     * RMI port.
//     */
//    public static final int RMI_PORT = RMITestConstants.guessFreePort();
    
//    /**
//     * Test communication contact.
//     */
//    public static final CommunicationContact CONTACT = new CommunicationContact("127.0.0.1",
//                                                                                "de.rcenvironment.rce.communication.rmi",
//                                                                                RMI_PORT);

    /**
     * The RMI communication bundle activator.
     */
    private ServiceHandler myActivator = null;

    /**
     * The class under test.
     */
    private RMIServiceCallSenderFactory myRequestSenderFactory = null;
    
    private int rmiPort;
    
    @Override
    protected void setUp() throws Exception {
        myActivator = new ServiceHandler();
        rmiPort = RMITestConstants.guessFreePort();
        myActivator.bindConfigurationService(RMIMockFactory.getInstance().getConfigurationService(rmiPort));
        myActivator.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
        myActivator.activate(RMIMockFactory.getInstance().getBundleContextMock());
        myRequestSenderFactory = new RMIServiceCallSenderFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        myActivator.deactivate();
        myActivator = null;
        myRequestSenderFactory = null;
//        final int oneThousand = 1000;
//        Thread.sleep(oneThousand);
    }

    /**
     * 
     * Test.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testCreateRequestSenderForSuccess() throws Exception {
        final NetworkContact contact = new NetworkContact("127.0.0.1",
            "de.rcenvironment.rce.communication.rmi",
            rmiPort);
        myRequestSenderFactory.createServiceCallSender(contact);
        assertTrue(true);

    }

    /**
     * 
     * Test.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testCreateRequestSenderForSanity() throws Exception {
        final NetworkContact contact = new NetworkContact("127.0.0.1",
            "de.rcenvironment.rce.communication.rmi",
            rmiPort);
        ServiceCallSender communicator = myRequestSenderFactory.createServiceCallSender(contact);
        assertNotNull(communicator);
        assertTrue(communicator instanceof RMIServiceCallSender);

    }

    /**
     * 
     * Test.
     * 
     * @throws Exception
     *             if the test fails.
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
