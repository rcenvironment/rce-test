/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * Unit test for <code>RMIConfiguration</code>.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class RMIConfigurationTest extends TestCase {

    /**
     * The communication bundle activator.
     */
    private ServiceHandler myActivator = null;
    
    /**
     * The class under test.
     */
    private RMIConfiguration myConfiguration = null;

    private int rmiPort;
    
    @Override
    protected void setUp() throws Exception {
        myActivator = new ServiceHandler();
        rmiPort = RMITestConstants.guessFreePort();
        final ConfigurationService configurationService = RMIMockFactory.getInstance().getConfigurationService(rmiPort);
        myActivator.bindConfigurationService(configurationService);
        myActivator.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
        myActivator.bindServiceCallHandler(RMIMockFactory.getInstance().getServiceCallHandler());
        myActivator.activate(RMIMockFactory.getInstance().getBundleContextMock());
        myConfiguration = configurationService.getConfiguration(RMITestConstants.BUNDLE_SYMBOLIC_NAME, RMIConfiguration.class);
    }

    @Override
    protected void tearDown() throws Exception {
        myActivator.deactivate();
        myActivator = null;
        myConfiguration = null;
//        final int oneThousand = 1000;
//        Thread.sleep(oneThousand);
    }
    
    /**
     * 
     * Test.
     *
     */
    public void testGetRegistryPortForSanity() {
        assertEquals(myConfiguration.getRegistryPort(), rmiPort);
        // test default port value
        final RMIConfiguration configuration = new RMIConfiguration();
        assertEquals(configuration.getRegistryPort(), RMITestConstants.DEFAULT_PORT);
    }

    /**
     * 
     * Test.
     * @throws Exception if an error occurs.
     *
     */
    public void testGetRegistryPortForFailure() throws Exception {
        final CountDownLatch startLatch = new CountDownLatch(1);
        ServerSocket portBlockingServer = null;
        try {
            // start a server on the RMI socket to block the port
            final ServerSocket serverSocket = new ServerSocket(rmiPort);
            new Thread() {
                public void run() {
                    try {
                        startLatch.countDown();
                        serverSocket.accept();
                    } catch (IOException e) {
                        e = null;
                    }
                }
            }.start();
            // busy wait for port to be acquired
            while (!serverSocket.isBound()) {
                // do nothing - following lines to avoid Checkstyle errors
                int i = 0;
                i++;
            }
            portBlockingServer = serverSocket;
        } catch (BindException e) {
            // if the ServerSocket could not be acquired, the port is already blocked
            startLatch.countDown();
        }
        startLatch.await();
        try {
            // in order to stop the the RMI sink before starting again
            myActivator.deactivate();
            myActivator = new ServiceHandler();
            myActivator.bindConfigurationService(RMIMockFactory.getInstance().getBrokenConfigurationService(rmiPort));
            myActivator.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
            myActivator.activate(RMIMockFactory.getInstance().getBundleContextMock());
            myConfiguration = new RMIConfiguration();
            assertEquals(myConfiguration.getRegistryPort(), RMITestConstants.DEFAULT_PORT);
            myActivator.deactivate();
            
            // in order to be able stop the RMI sink in tearDown
            myActivator.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
            myActivator.bindConfigurationService(RMIMockFactory.getInstance().getBrokenConfigurationService(rmiPort));
            myActivator.activate(RMIMockFactory.getInstance().getBundleContextMock());
        } finally {
            if (portBlockingServer != null) {
                portBlockingServer.close();
            }
        }
    }

    /**
     * 
     * Test.
     *
     */
    public void testSetRegistryPortForSanity() {
        final RMIConfiguration configuration = new RMIConfiguration();
        assertEquals(configuration.getRegistryPort(), RMITestConstants.DEFAULT_PORT);
        final int port1 = 1;
        configuration.setRegistryPort(port1);
        assertEquals(port1, configuration.getRegistryPort());
        final int bit16 = 16;
        final int port65535 = (int) Math.pow(2, bit16) - 1;
        configuration.setRegistryPort(port65535);
        assertEquals(port65535, configuration.getRegistryPort());
    }

    /**
     * 
     * Test.
     *
     */
    public void testSetRegistryPortForFailure() {
        final RMIConfiguration configuration = new RMIConfiguration();
        final int port0 = 0;
        try {
            configuration.setRegistryPort(port0);
            fail();
        } catch (IllegalArgumentException e) {
            e = null;
        }
        assertEquals(configuration.getRegistryPort(), RMITestConstants.DEFAULT_PORT);
        final int bit16 = 16;
        final int port65536 = (int) Math.pow(2, bit16);
        try {
            configuration.setRegistryPort(port65536);
            fail();
        } catch (IllegalArgumentException e) {
            e = null;
        }
        assertEquals(configuration.getRegistryPort(), RMITestConstants.DEFAULT_PORT);
    }
    
}
