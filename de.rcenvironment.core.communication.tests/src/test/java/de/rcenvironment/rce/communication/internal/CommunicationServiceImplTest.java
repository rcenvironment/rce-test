/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.core.communication.management.CommunicationManagementService;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.ReachabilityChecker;
import de.rcenvironment.rce.communication.impl.PlatformIdentityInformationImpl;
import de.rcenvironment.rce.communication.impl.ReachabilityCheckerImpl;
import de.rcenvironment.rce.communication.service.RemoteServiceHandler;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;

/**
 * Test cases for the {@link CommunicationServiceImpl}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke (adaptations)
 */
public class CommunicationServiceImplTest {

    private static final int SAFETY_NET_TEST_TIMEOUT = 10000;

    private CommunicationServiceImpl communicationService;

    private BundleContext contextMock;

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");

    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");

    private final PlatformIdentifier pi3 = PlatformIdentifierFactory.fromHostAndNumberString("notReachable:0");

    private final Object serviceInstance = new Object();

    private final Class<?> iface = Serializable.class;

    private final Map<String, String> serviceProperties = new HashMap<String, String>();

    /** Setup. */
    @Before
    public void setUp() {
        serviceProperties.put("piti", "platsch");
        communicationService = new CommunicationServiceImpl();
        communicationService.bindRemoteServiceHandler(new DummyRemoteServiceHandler());
        communicationService.bindPlatformService(new DummyPlatformServiceLocal());
        communicationService.bindCommunicationManagementService(EasyMock.createMock(CommunicationManagementService.class));

        contextMock = EasyMock.createNiceMock(BundleContext.class);
        ServiceReference ifaceReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getServiceReference(iface.getName())).andReturn(ifaceReferenceMock).anyTimes();
        ServiceReference[] referencesDummy = new ServiceReference[2];
        referencesDummy[0] = ifaceReferenceMock;
        try {
            EasyMock.expect(contextMock.getServiceReferences(iface.getName(),
                ServiceUtils.constructFilter(serviceProperties))).andReturn(referencesDummy).anyTimes();
        } catch (InvalidSyntaxException e) {
            fail();
        }
        EasyMock.expect(contextMock.getService(ifaceReferenceMock)).andReturn(serviceInstance).anyTimes();
        ServiceReference platformReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getServiceReference(PlatformService.class.getName())).andReturn(platformReferenceMock).anyTimes();
        EasyMock.expect(contextMock.getService(platformReferenceMock)).andReturn(new DummyPlatformServiceLocal()).anyTimes();

        EasyMock.replay(contextMock);

        communicationService.activate(contextMock);

    }

    /** Test. */
    @Test(timeout = SAFETY_NET_TEST_TIMEOUT)
    @Ignore
    // FIXME test is currently broken - adapt/fix (misc_ro, 2012-11-09)
    public void testGetPlatforms() {
        Set<PlatformIdentifier> platforms = communicationService.getAvailableNodes(false);
        assertEquals(1, platforms.size());
        assertTrue(platforms.contains(pi1));

        platforms = communicationService.getAvailableNodes(true);
        assertEquals(1, platforms.size());
        assertTrue(platforms.contains(pi1));

        communicationService.deactivate(contextMock);

        platforms = communicationService.getAvailableNodes(true);
        assertEquals(1, platforms.size());
        assertTrue(platforms.contains(pi1));

        communicationService.bindPlatformService(new DummyPlatformServiceLocal2());

        BundleContext context = EasyMock.createNiceMock(BundleContext.class);
        ServiceReference platformReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(context.getServiceReference(PlatformService.class.getName())).andReturn(platformReferenceMock).anyTimes();
        EasyMock.expect(context.getService(platformReferenceMock)).andReturn(new DummyPlatformServiceLocal2()).anyTimes();

        EasyMock.replay(context);

        communicationService.activate(context);

        // return cached platforms
        platforms = communicationService.getAvailableNodes(false);
        assertEquals(1, platforms.size());
        assertTrue(platforms.contains(pi1));

        // return newly retrieved platforms
        platforms = communicationService.getAvailableNodes(true);
        assertEquals(1, platforms.size());
        assertTrue(platforms.contains(pi2));

    }

    /** Test. */
    @Test
    public void testCheckReachability() {
        communicationService.checkReachability(new ReachabilityCheckerImpl());
    }

    /**
     * Test.
     * 
     * @throws Exception if an error occur.
     **/
    @Test
    public void testGetService() throws Exception {

        Object service = communicationService.getService(iface, pi1, contextMock);
        assertEquals(serviceInstance, service);

        service = communicationService.getService(iface, pi2, contextMock);
        assertEquals(serviceInstance, service);

        service = communicationService.getService(iface, null, contextMock);
        assertEquals(serviceInstance, service);

        service = communicationService.getService(iface, serviceProperties, pi1, contextMock);
        assertEquals(serviceInstance, service);

        service = communicationService.getService(iface, serviceProperties, pi2, contextMock);
        assertEquals(serviceInstance, service);

        service = communicationService.getService(iface, serviceProperties, null, contextMock);
        assertEquals(serviceInstance, service);

    }

    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceIfServiceRefArrayIsEmpty() {

        EasyMock.reset(contextMock);
        ServiceReference[] referencesDummy = new ServiceReference[0];
        try {
            EasyMock.expect(contextMock.getServiceReferences(iface.getName(),
                ServiceUtils.constructFilter(serviceProperties))).andReturn(referencesDummy).anyTimes();
        } catch (InvalidSyntaxException e) {
            fail();
        }
        EasyMock.replay(contextMock);

        Object service = communicationService.getService(iface, serviceProperties, pi1, contextMock);
        assertEquals(serviceInstance, service);

    }

    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceIfNoServiceAvailable() {

        EasyMock.reset(contextMock);
        try {
            EasyMock.expect(contextMock.getServiceReferences(iface.getName(),
                ServiceUtils.constructFilter(serviceProperties))).andReturn(null).anyTimes();
        } catch (InvalidSyntaxException e) {
            fail();
        }
        EasyMock.replay(contextMock);

        Object service = communicationService.getService(iface, serviceProperties, pi1, contextMock);
        assertEquals(serviceInstance, service);

    }

    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceIfServiceRefIsNull() {

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getServiceReference(iface.getName())).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);

        Object service = communicationService.getService(iface, pi1, contextMock);
        assertEquals(serviceInstance, service);

    }

    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceIfServiceIsNull() {

        EasyMock.reset(contextMock);
        ServiceReference referenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getServiceReference(iface.getName())).andReturn(referenceMock).anyTimes();
        EasyMock.replay(contextMock);

        Object service = communicationService.getService(iface, pi1, contextMock);
        assertEquals(serviceInstance, service);

    }

    /**
     * Dummy Remote Service Handler.
     * 
     * @author Doreen Seider
     */
    @SuppressWarnings("serial")
    private class DummyRemoteServiceHandler implements RemoteServiceHandler {

        @SuppressWarnings("unchecked")
        private <T> T createNullService(final Class<T> clazz) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz },
                new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
                        throw new UndeclaredThrowableException(new RuntimeException("Service not available"));
                    }
                });
        }

        @Override
        public Object createServiceProxy(PlatformIdentifier platformIdentifier, Class<?> serviceIface, Class<?>[] ifaces,
            Map<String, String> servicePropertiesString) {
            Object service = null;
            if (platformIdentifier.equals(pi1) && serviceIface == PlatformService.class) {
                service = new DummyPlatformServiceLocal();
            } else if (platformIdentifier.equals(pi2) && serviceIface == PlatformService.class) {
                service = new DummyPlatformServiceRemote();
            } else if (platformIdentifier.equals(pi3) && serviceIface == PlatformService.class) {
                service = createNullService(PlatformService.class);
            } else if (platformIdentifier.equals(pi1) && serviceIface == CommunicationService.class) {
                service = new DummyCommunicationService();
            } else if (platformIdentifier.equals(pi2) && serviceIface == CommunicationService.class) {
                service = new DummyBrokenCommunicationService();
            } else if (platformIdentifier.equals(pi3) && serviceIface == CommunicationService.class) {
                service = createNullService(CommunicationService.class);
            } else if (platformIdentifier.equals(pi2) && serviceIface == iface) {
                service = serviceInstance;
            }
            return service;
        }

        @Override
        public Object createServiceProxy(PlatformIdentifier platformIdentifier, Class<?> serviceIface, Class<?>[] ifaces,
            String servicePropertiesMap) {
            return null;
        }
    }

    /**
     * Dummy local platform service.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformServiceLocal extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi1;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<PlatformIdentifier> getRemotePlatforms() {
            return new HashSet<PlatformIdentifier>() {

                {
                    add(pi2);
                    add(pi3);
                }
            };
        }

        @Override
        public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
            if (platformIdentifier == pi1) {
                return true;
            }
            return false;
        }

        @Override
        public PlatformIdentityInformation getIdentityInformation() {
            return new PlatformIdentityInformationImpl("12345678123456781234567812345678", null, "Dummy Test Platform", true);
        }

        @Override
        public CommunicationConfiguration getConfiguration() {
            return new CommunicationConfiguration();
        }

    }

    /**
     * Dummy local platform service.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformServiceLocal2 extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi2;
        }

        @Override
        public Set<PlatformIdentifier> getRemotePlatforms() {
            return new HashSet<PlatformIdentifier>();
        }

        @Override
        public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
            if (platformIdentifier == pi2) {
                return true;
            }
            return false;
        }

    }

    /**
     * Dummy remote platform service.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformServiceRemote extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi2;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<PlatformIdentifier> getRemotePlatforms() {
            return new HashSet<PlatformIdentifier>() {

                {
                    add(pi1);
                }
            };
        }

        @Override
        public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
            return false;
        }

    }

    /**
     * Dummy implementation of {@link CommunicationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

    }

    /**
     * Dummy implementation of {@link CommunicationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyBrokenCommunicationService extends MockCommunicationService {

        @Override
        public void checkReachability(ReachabilityChecker checker) {
            throw new UndeclaredThrowableException(new NullPointerException());
        }

    }
}
