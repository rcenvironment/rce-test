/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.internal;

import static de.rcenvironment.rce.communication.CommunicationTestHelper.LOCAL_PLATFORM;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.REQUEST;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.SERVICE_CONTACT;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationTestHelper;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.callback.CallbackObject;
import de.rcenvironment.rce.communication.callback.CallbackProxy;
import de.rcenvironment.rce.communication.callback.CallbackProxyService;
import de.rcenvironment.rce.communication.callback.CallbackService;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.internal.CommunicationType;
import de.rcenvironment.rce.communication.internal.RoutingServiceImpl;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;

/**
 * Test cases for {@link RemoteServiceHandlerImpl}.
 * 
 * @author Doreen Seider
 */
public class RemoteServiceHandlerImplTest extends TestCase {

    private final String bundleID = "bundleID";

    private final int sum = 3;

    private final ServiceCallResult addResult = new ServiceCallResult(sum);

    private final ServiceCallResult callbackResult = new ServiceCallResult(new Serializable() {
    });

    private final String value = new String("value");

    private final ServiceCallResult valueResult = new ServiceCallResult(value);

    private final IOException exception = new IOException();

    private final ServiceCallResult exceptionResult = new ServiceCallResult(exception);

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    private RemoteServiceHandlerImpl serviceHandler;

    @Override
    public void setUp() throws Exception {
        CommunicationTestHelper.activateCommunicationContactMap();

        serviceHandler = new RemoteServiceHandlerImpl();
        serviceHandler.bindCallbackService(new DummyCallbackService());
        serviceHandler.bindCallbackProxyService(EasyMock.createNiceMock(CallbackProxyService.class));
        serviceHandler.bindPlatformService(new DummyPlatformService());

        CommunicationContactMap.removeAllMappings();
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, LOCAL_PLATFORM, SERVICE_CONTACT);

    }

    @Override
    public void tearDown() throws Exception {
        serviceHandler = null;
    }

    /** Test. **/
    public void testCreateServiceProxyForSuccess() {

        Object proxy = serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class, null, (String) null);
        assertTrue(proxy instanceof MethodCallerTestMethods);

        proxy = serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class,
            new Class<?>[] { BundleActivator.class, Bundle.class }, (Map<String, String>) null);
        assertTrue(proxy instanceof MethodCallerTestMethods);
        assertTrue(proxy instanceof BundleActivator);
        assertTrue(proxy instanceof Bundle);

        proxy = serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class, null, "(&(rumpel=false)(pumpel=true)");
        assertTrue(proxy instanceof MethodCallerTestMethods);

        proxy = serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class,
            null, REQUEST.getServiceProperties());
        assertTrue(proxy instanceof MethodCallerTestMethods);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("rumpel", "false");
        properties.put("pumpel", "true");

        proxy = serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class, null, properties);
        assertTrue(proxy instanceof MethodCallerTestMethods);

    }

    /**
     * Test.
     * 
     * @throws Exception if an error occurs.
     **/
    public void testProxy() throws Exception {

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

        // set default RoutingServiceImpl to fulfill dependency
        ServiceCallSenderSupport.bindRoutingService(new RoutingServiceImpl());
        ServiceCallSenderSupport serviceCallSenderSupport = new ServiceCallSenderSupport();
        serviceCallSenderSupport.activate(contextMock);

        MethodCallerTestMethods proxy = (MethodCallerTestMethods) serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class,
            null, (String) null);

        assertEquals(3, proxy.add(1, 2));
        proxy.callbackTest(new DummyObject());
        assertEquals(value, proxy.getValue());
        try {
            proxy.exceptionFunction();
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        proxy = (MethodCallerTestMethods) serviceHandler.createServiceProxy(pi, MethodCallerTestMethods.class,
            null, "");

        assertEquals(3, proxy.add(1, 2));
    }

    /** Test. */
    public void testCreateServiceProxyForFailure() {
        try {
            serviceHandler.createServiceProxy(null, MethodCallerTestMethods.class, null, (String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            serviceHandler.createServiceProxy(LOCAL_PLATFORM, null, null, (String) null);
            fail();
        } catch (IllegalArgumentException e) {
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
            ServiceCallResult result = null;
            if (serviceCallRequest.getRequestedPlatform().equals(pi)
                && serviceCallRequest.getService().equals(MethodCallerTestMethods.class.getCanonicalName())
                && serviceCallRequest.getServiceMethod().equals("add")
                && serviceCallRequest.getServiceProperties() == null
                && serviceCallRequest.getParameterList().get(0).equals(1)
                && serviceCallRequest.getParameterList().get(1).equals(2)
                && serviceCallRequest.getParameterList().size() == 2) {

                result = addResult;
            } else if (serviceCallRequest.getRequestedPlatform().equals(pi)
                && serviceCallRequest.getService().equals(MethodCallerTestMethods.class.getCanonicalName())
                && serviceCallRequest.getServiceMethod().equals("callbackTest")
                && serviceCallRequest.getServiceProperties() == null
                && (serviceCallRequest.getParameterList().get(0) instanceof CallbackProxy)
                && serviceCallRequest.getParameterList().size() == 1) {

                result = callbackResult;
            } else if (serviceCallRequest.getRequestedPlatform().equals(pi)
                && serviceCallRequest.getService().equals(MethodCallerTestMethods.class.getCanonicalName())
                && serviceCallRequest.getServiceMethod().equals("getValue")
                && serviceCallRequest.getServiceProperties() == null
                && serviceCallRequest.getParameterList().size() == 0) {

                result = valueResult;
            } else if (serviceCallRequest.getRequestedPlatform().equals(pi)
                && serviceCallRequest.getService().equals(MethodCallerTestMethods.class.getCanonicalName())
                && serviceCallRequest.getServiceMethod().equals("exceptionFunction")
                && serviceCallRequest.getServiceProperties() == null
                && serviceCallRequest.getParameterList().size() == 0) {

                result = exceptionResult;
            }
            return result;
        }

    }

    /**
     * Test object to call back.
     * 
     * @author Doreen Seider
     */
    private class DummyObject implements DummyInterface {

        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getInterface() {
            return DummyInterface.class;
        }

        @Override
        public Object makePeng() {
            return null;
        }

        @Override
        public Object makePuff(String string) {
            return null;
        }

    }

    /**
     * Dummy interface.
     * 
     * @author Doreen Seider
     */
    private interface DummyInterface extends CallbackObject {

        Object makePuff(String string);

        Object makePeng();
    }

    /**
     * Test implementation of the {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi;
        }

    }

    /**
     * Test {@link CallbackService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCallbackService implements CallbackService {

        private final String id = "id";

        @Override
        public String addCallbackObject(Object callBackObject, PlatformIdentifier platformIdentifier) {
            return null;
        }

        @Override
        public Object callback(String objectIdentifier, String methodName, List<? extends Serializable> parameters)
            throws CommunicationException {
            return null;
        }

        @Override
        public Object createCallbackProxy(CallbackObject callbackObject, String objectIdentifier, PlatformIdentifier proxyHome) {
            if (objectIdentifier == id) {
                return new CallbackProxy() {

                    @Override
                    public String getObjectIdentifier() {
                        return id;
                    }

                    @Override
                    public PlatformIdentifier getHomePlatform() {
                        return pi;
                    }
                };
            }
            return null;
        }

        @Override
        public Object getCallbackObject(String objectIdentifier) {
            return new Object();
        }

        @Override
        public String getCallbackObjectIdentifier(Object callbackObject) {
            return id;
        }

        @Override
        public void setTTL(String objectIdentifier, Long ttl) {}

    }
}
