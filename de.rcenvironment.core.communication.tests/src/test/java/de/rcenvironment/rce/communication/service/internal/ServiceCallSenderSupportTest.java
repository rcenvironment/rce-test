/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.internal;

import static de.rcenvironment.rce.communication.CommunicationTestHelper.INSTANCE;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.LOCAL_PLATFORM;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.METHOD;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.REQUEST;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.SERVICE;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.SERVICE_CONTACT;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationTestHelper;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.internal.CommunicationType;
import de.rcenvironment.rce.communication.internal.RoutingServiceImpl;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;

/**
 * Unit test for <code>ServiceCallSenderSupport</code>.
 * 
 * @author Doreen Seider
 */
public class ServiceCallSenderSupportTest extends TestCase {

    private final String filter = "(" + ServiceCallSenderFactory.PROTOCOL + "=de.rcenvironment.rce.communication.rmi)";

    private final ServiceCallRequest failingRequest = new ServiceCallRequest(PlatformIdentifierFactory.fromHostAndNumber("192.168.13.1",
        INSTANCE), PlatformIdentifierFactory.fromHostAndNumber("remotehost", INSTANCE), SERVICE, "", METHOD, null);

    private BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);

    private ServiceCallSenderSupport support;

    @Override
    public void setUp() throws Exception {
        CommunicationTestHelper.activateCommunicationContactMap();

        // set default RoutingServiceImpl to fulfill dependency
        ServiceCallSenderSupport.bindRoutingService(new RoutingServiceImpl());
        support = new ServiceCallSenderSupport();

        CommunicationContactMap.removeAllMappings();
        CommunicationContactMap.setMapping(CommunicationType.SERVICE_CALL, LOCAL_PLATFORM, SERVICE_CONTACT);
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testGetServiceCallSenderForSuccess() throws Exception {

        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("de.rcenvironment.rce.communication.rmi").anyTimes();
        EasyMock.replay(bundleMock);

        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);

        ServiceCallSenderFactory factoryMock = EasyMock.createNiceMock(ServiceCallSenderFactory.class);
        EasyMock.expect(factoryMock.createServiceCallSender(SERVICE_CONTACT))
            .andReturn(EasyMock.createNiceMock(ServiceCallSender.class)).anyTimes();
        EasyMock.replay(factoryMock);

        contextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] { bundleMock }).anyTimes();
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(ServiceCallSenderFactory.class.getName()),
            EasyMock.eq(filter))).andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(factoryMock).anyTimes();
        EasyMock.replay(contextMock);

        support.activate(contextMock);

        ServiceCallSender serviceCaller = ServiceCallSenderSupport.getServiceCallSender(REQUEST);
        assertNotNull(serviceCaller);

    }

    /**
     * Test.
     * 
     * @throws Exception if an error occurs.
     */
    public void testGetRequestSenderForFailure() throws Exception {
        try {
            ServiceCallSenderSupport.getServiceCallSender(failingRequest);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);

        try {
            ServiceCallSenderSupport.getServiceCallSender(REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(ServiceCallSenderFactory.class.getName()),
            EasyMock.eq(filter))).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);

        try {
            ServiceCallSenderSupport.getServiceCallSender(REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(ServiceCallSenderFactory.class.getName()),
            EasyMock.eq(filter))).andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);

        try {
            ServiceCallSenderSupport.getServiceCallSender(REQUEST);
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }
    }

}
