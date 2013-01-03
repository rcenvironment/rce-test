/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import junit.framework.TestCase;

/**
 * Test cases for the {@link ServiceHandler}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Tobias Menden
 */
public final class ServiceHandlerTest extends TestCase {

    private ServiceHandler serviceHandler;
    
    private int rmiPort;

    @Override
    protected void setUp() throws Exception {
        serviceHandler = new ServiceHandler();
        rmiPort = RMITestConstants.guessFreePort();
    }

    /**
     * Test.
     * 
     * @throws Exception
     *             if the test fails.
     * 
     */
    public void test() throws Exception {

        assertNotNull(ServiceHandler.getConfigurationService());
        assertNotNull(ServiceHandler.getPlatformService());
        assertNotNull(ServiceHandler.getServiceCallHandler());
        
        serviceHandler.bindConfigurationService(RMIMockFactory.getInstance().getConfigurationService(rmiPort));
        serviceHandler.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
        serviceHandler.bindServiceCallHandler(RMIMockFactory.getInstance().getServiceCallHandler());
        serviceHandler.activate(RMIMockFactory.getInstance().getBundleContextMock());
        
        assertNotNull(ServiceHandler.getBundleSymbolicName());
        assertTrue(ServiceHandler.getBundleSymbolicName().equals(RMITestConstants.BUNDLE_SYMBOLIC_NAME));
        assertNotNull(ServiceHandler.getConfigurationService());
        assertNotNull(ServiceHandler.getPlatformService());
        assertNotNull(ServiceHandler.getServiceCallHandler());
        
        serviceHandler.deactivate();
    }
}
