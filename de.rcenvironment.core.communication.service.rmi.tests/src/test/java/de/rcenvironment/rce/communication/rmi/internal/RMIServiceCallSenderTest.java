/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * 
 * Unit test for the <code>RMIServiceSender</code> implementation of the <code>ServiceSender</code>
 * interface.
 * 
 * Note: Some tests may fail if you have a firewall enabled which blocks RMI.
 * 
 * @author Heinrich Wendel
 */
public class RMIServiceCallSenderTest extends TestCase {

    /**
     * RMI RCE-Call function name.
     */
    private static final String RCE_CALL = "RCE-Call";

    /**
     * Dummy.
     */
    private RMISink myServer = null;

    /**
     * Registry instance.
     */
    private Registry myRegistry = null;

    /**
     * The class under test.
     */
    private RMIServiceCallSender myRequestSender = null;
    
    private int rmiPort;

    @Override
    protected void setUp() throws Exception {
        myServer = new RMISinkDummy();
        
        rmiPort = RMITestConstants.guessFreePort();
        myRegistry = LocateRegistry.createRegistry(rmiPort);
        myRegistry.bind(RCE_CALL, myServer);

        initialize(RMITestConstants.LOCALHOST, rmiPort);
    }

    /**
     * Set the contact.
     * 
     * @param host
     *            Host to use.
     * @param port
     *            Port to use.
     * @throws CommunicationException
     *             if an error occurs
     * 
     */
    protected void initialize(String host, Integer port) throws CommunicationException {
        NetworkContact contact = new NetworkContact(host, RMITestConstants.RMI_PROTOCOL, port);
        myRequestSender = new RMIServiceCallSender();
        myRequestSender.initialize(contact);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            myRegistry.unbind(RCE_CALL);
            UnicastRemoteObject.unexportObject(myRegistry, true);
        } catch (NotBoundException e) {
            assertTrue(true);
        }
//        final int oneThousand = 1000;
//        Thread.sleep(oneThousand);
    }

    /**
     * Test method for success.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testInitializeForSuccess() throws Exception {
        initialize(RMITestConstants.LOCALHOST, rmiPort);
        assertTrue(true);

    }

    /**
     * Test method for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testInitializeFailure() throws Exception {
        try {
            new RMIServiceCallSender().initialize(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test for success.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testSendForSuccess() throws Exception {
        try {
            myRequestSender.send(RMITestConstants.REQUEST);
            assertTrue(true);
        } catch (CommunicationException e) {
            fail();
        }
    }

    /**
     * Test method for sanity.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testSendForSanity() throws Exception {
        ServiceCallResult commResult = myRequestSender.send(RMITestConstants.REQUEST);
        assertEquals((String) commResult.getReturnValue(), RMITestConstants.RETURN_VALUE);

    }

    /**
     * 
     * Test for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testSendForFailure() throws Exception {

        // Null CommRequest
        try {
            myRequestSender.send(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // RemoteException thrown
        try {
            myRequestSender.send(RMITestConstants.UNKNOWN_METHOD_REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        // Wrong port
        final int wrongPort = 1100;
        try {
            initialize(RMITestConstants.LOCALHOST, wrongPort);
            myRequestSender.send(RMITestConstants.REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        // Wrong Host
        try {
            initialize("wrongHost", rmiPort);
            myRequestSender.send(RMITestConstants.REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        // Server not bound
        try {
            myRegistry.unbind(RCE_CALL);
            initialize(RMITestConstants.LOCALHOST, rmiPort);
            myRequestSender.send(RMITestConstants.REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        // Registry not bound
        try {
            UnicastRemoteObject.unexportObject(myRegistry, true);
            initialize(RMITestConstants.LOCALHOST, rmiPort);
            myRequestSender.send(RMITestConstants.REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

    }

}
