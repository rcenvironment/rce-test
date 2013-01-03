/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import junit.framework.TestCase;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Unit test for the implementation of <code>RMISinkImpl</code>.
 * 
 * Note: Some tests may fail if you have a firewall enabled which blocks RMI.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Tobias Menden
 * @author Robert Mischke (removed use of EasyMock for serialized ServiceCallRequest)
 */
public class RMISinkImplTest extends TestCase {

    private final String targetServiceName = "service";

    private final String returnValue = "returnValue";

    private ServiceCallRequest request1;

    private ServiceHandler myActivator = null;

    private int rmiPort;

    @Override
    protected void setUp() throws Exception {
        // build dummy request; only targetServiceName is relevant
        PlatformIdentifier piFrom = PlatformIdentifierFactory.fromHostAndNumber("127.0.0.1", 1);
        PlatformIdentifier piTo = PlatformIdentifierFactory.fromHostAndNumber("127.0.0.1", 2);
        request1 = new ServiceCallRequest(piFrom, piTo, targetServiceName, null, "dummyMethod", null);

        myActivator = new ServiceHandler();
        rmiPort = RMITestConstants.guessFreePort();
        myActivator.bindConfigurationService(RMIMockFactory.getInstance().getConfigurationService(rmiPort));
        myActivator.bindPlatformService(RMIMockFactory.getInstance().getPlatformService());
        myActivator.bindServiceCallHandler(new DummyServiceCallHandler());
        myActivator.activate(RMIMockFactory.getInstance().getBundleContextMock());
    }

    @Override
    protected void tearDown() throws Exception {
        myActivator.deactivate();
    }

    /**
     * Test.
     */
    public void testStartStopForSuccess() {
        // start and stop are already tested in in setUp and tearDown
    }

    /**
     * Test.
     * 
     * @throws Exception if an exception occur.
     */
    public void testStartForFailure() throws Exception {
        // already started
        try {
            RMISinkImpl.start(RMIMockFactory.getInstance().getConfigurationService(rmiPort).getConfiguration(
                RMITestConstants.BUNDLE_SYMBOLIC_NAME, RMIConfiguration.class), ServiceHandler.getServiceCallHandler());
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }
    }

    /**
     * Test for sanity.
     * 
     * @throws RemoteException Thrown if Registry could not be accessed.
     */
    public void testStartForSanity() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(rmiPort);
        String[] functions = registry.list();
        assertEquals(functions[0], RMISinkImpl.RMI_METHOD_NAME);
    }

    /**
     * Test call.
     * 
     * @throws Exception Thrown on error.
     */
    public void testCall() throws Exception {

        Registry registry = LocateRegistry.getRegistry(RMITestConstants.LOCALHOST, rmiPort);

        RMISink stub = (RMISink) registry.lookup(RMISinkImpl.RMI_METHOD_NAME);

        ServiceCallResult obj = stub.call(request1);
        assertEquals(returnValue, (String) obj.getReturnValue());

    }

    /**
     * Test {@link ServiceCallHandler} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyServiceCallHandler implements ServiceCallHandler {

        @Override
        public ServiceCallResult handle(ServiceCallRequest serviceCallRequest) throws CommunicationException {
            if (serviceCallRequest.getService().equals(request1.getService())) {
                return new ServiceCallResult(returnValue);
            }
            return null;
        }

    }
}
