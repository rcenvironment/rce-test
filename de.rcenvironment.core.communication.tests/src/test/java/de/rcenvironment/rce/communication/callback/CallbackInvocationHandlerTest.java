/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.internal.ServiceCallSenderSupport;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;

/**
 * Test case for {@link CallbackInvocationHandler}.
 * 
 * @author Doreen Seider
 */
public class CallbackInvocationHandlerTest {

    private final CallbackObject callbackObject = new DummyObject();

    private final String bundleID = "bundleID";

    private final String objectID = "callMe";

    private final String puffParam = "knaller";

    private final Integer puffIterations = 5;

    private final String puffMethod = "makePuff";

    private final String pengMethod = "makePeng";

    private final String throwMethod = "throwSomething";

    private final String puff1RV = "puff1";

    private final String puff2RV = "puff2";

    private final String pengRV = "peng";

    private final ServiceCallResult puffResult = new ServiceCallResult(puff1RV);

    private final ServiceCallResult pengResult = new ServiceCallResult(pengRV);

    private final PlatformIdentifier piLocal = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    private final PlatformIdentifier piRemote = PlatformIdentifierFactory.fromHostAndNumberString("remotehost:1");

    /**
     * Test.
     * 
     * @throws Throwable if an error occurs.
     * */
    @Test
    public void test() throws Throwable {
        CallbackInvocationHandler handler = new CallbackInvocationHandler(callbackObject, objectID, piLocal, piRemote);

        Method method = CallbackProxy.class.getMethod("getObjectIdentifier", new Class[] {});
        assertEquals(objectID, handler.invoke(new Object(), method, new Object[] {}));

        method = CallbackProxy.class.getMethod("getHomePlatform", new Class[] {});
        assertEquals(piLocal, handler.invoke(new Object(), method, new Object[] {}));

        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(bundleID).anyTimes();
        EasyMock.replay(bundleMock);

        BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(contextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        String filter = "(" + ServiceCallSenderFactory.PROTOCOL + "=de.rcenvironment.rce.communication.rmi)";
        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(ServiceCallSenderFactory.class.getName()), EasyMock.eq(filter)))
            .andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(new DummyServiceCallSenderFactory()).anyTimes();
        EasyMock.replay(contextMock);

        CommunicationContactMap map = new CommunicationContactMap();
        map.bindConfigurationService(new DummyConfigurationService());
        map.activate(contextMock);

        new ServiceCallSenderSupport().activate(contextMock);

        method = DummyInterface.class.getMethod(puffMethod, new Class[] { String.class });
        assertEquals(puff1RV, handler.invoke(new Object(), method, new Object[] { puffParam }));

        method = DummyInterface.class.getMethod(puffMethod, new Class[] { Integer.class });
        assertEquals(puff2RV, handler.invoke(new Object(), method, new Object[] { puffIterations }));

        method = DummyInterface.class.getMethod(pengMethod, new Class[] {});
        assertEquals(pengRV, handler.invoke(new Object(), method, null));

        method = DummyInterface.class.getMethod(throwMethod, new Class[] {});

        try {
            handler.invoke(new Object(), method, new Object[] {});
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    /**
     * Test implementation of {@link ConfigurationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            if (identifier.equals(bundleID) && clazz == CommunicationConfiguration.class) {
                CommunicationConfiguration config = new CommunicationConfiguration();
                config.setServiceCallContacts(new Vector<String>() {

                    {
                        add("0.0.0.0/0:1=127.0.0.1:de.rcenvironment.rce.communication.rmi:1098");
                    }
                });
                return (T) config;
            }
            return null;
        }

    }

    /**
     * Test {@link ServiceCallSenderFactory} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyServiceCallSenderFactory implements ServiceCallSenderFactory {

        @Override
        public ServiceCallSender createServiceCallSender(NetworkContact contact) throws CommunicationException {
            return new DummyServiceCallSender();
        }

    }

    /**
     * Test {@link ServiceCallSender} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyServiceCallSender implements ServiceCallSender {

        @Override
        public void initialize(NetworkContact contact) throws CommunicationException {}

        @Override
        public ServiceCallResult send(ServiceCallRequest serviceCallRequest) throws CommunicationException {
            if (serviceCallRequest.getRequestedPlatform().equals(piLocal)
                && serviceCallRequest.getService().equals(CallbackService.class.getCanonicalName())
                && serviceCallRequest.getServiceMethod().equals("callback")
                && serviceCallRequest.getServiceProperties() == null
                && serviceCallRequest.getParameterList().get(0).equals(objectID)) {

                if (serviceCallRequest.getParameterList().get(1).equals(pengMethod)
                    && ((ArrayList<Serializable>) serviceCallRequest.getParameterList().get(2)).size() == 0) {
                    return pengResult;
                } else if (serviceCallRequest.getParameterList().get(1).equals(puffMethod)
                    && ((ArrayList<Serializable>) serviceCallRequest.getParameterList().get(2)).size() == 1
                    && ((ArrayList<Serializable>) serviceCallRequest.getParameterList().get(2)).get(0).equals(puffParam)) {
                    return puffResult;
                } else if (serviceCallRequest.getParameterList().get(1).equals(throwMethod)
                    && ((ArrayList<Serializable>) serviceCallRequest.getParameterList().get(2)).size() == 0) {
                    throw new NullPointerException();
                }
            }
            return null;
        }

    }

    /**
     * Dummy interface.
     * 
     * @author Doreen Seider
     */
    private interface DummyInterface extends CallbackObject {

        @Callback
        Object makePuff(String string);

        Object makePuff(Integer iteration);

        @Callback
        Object makePeng();

        @Callback
        void throwSomething();

    }

    /**
     * Dummy object.
     * 
     * @author Doreen Seider
     */
    private class DummyObject implements DummyInterface {

        private static final String ERROR_MESSAGE = "should never be called, because annotated to be callback remotely.";

        @Override
        public Object makePuff(String string) {
            throw new RuntimeException(ERROR_MESSAGE);
        }

        @Override
        public Object makePeng() {
            throw new RuntimeException(ERROR_MESSAGE);
        }

        @Override
        public void throwSomething() {
            throw new RuntimeException(ERROR_MESSAGE);
        }

        @Override
        public Object makePuff(Integer iteration) {
            return puff2RV;
        }

        @Override
        public Class<?> getInterface() {
            return DummyInterface.class;
        }

    }
}
