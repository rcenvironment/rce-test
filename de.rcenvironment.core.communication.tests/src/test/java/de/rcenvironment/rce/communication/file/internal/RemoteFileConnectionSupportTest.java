/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.internal;

import static de.rcenvironment.rce.communication.CommunicationTestHelper.FILE_CONTACT;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.LOCAL_PLATFORM;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.URI;

import java.net.URI;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationTestHelper;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnectionFactory;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.internal.CommunicationType;

/**
 * Test cases for {@link RemoteFileConnectionSupport}.
 * 
 * @author Doreen Seider
 */
public class RemoteFileConnectionSupportTest extends TestCase {

    private final String filter = "(" + RemoteFileConnectionFactory.PROTOCOL + "=de.rcenvironment.rce.communication)";

    private BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);

    private RemoteFileConnectionSupport support;

    private User cert = EasyMock.createNiceMock(User.class);

    @Override
    protected void setUp() throws Exception {
        support = new RemoteFileConnectionSupport();

        CommunicationTestHelper.activateCommunicationContactMap();

        CommunicationContactMap.removeAllMappings();
        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, LOCAL_PLATFORM, FILE_CONTACT);
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testGetRemoteFileConnectionForSuccess() throws Exception {

        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("de.rcenvironment.rce.communication").anyTimes();
        EasyMock.replay(bundleMock);

        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);

        RemoteFileConnectionFactory factoryMock = EasyMock.createStrictMock(RemoteFileConnectionFactory.class);
        EasyMock.expect(factoryMock.createRemoteFileConnection(cert, new URI(URI)))
            .andReturn(EasyMock.createNiceMock(RemoteFileConnection.class));
        EasyMock.replay(factoryMock);

        contextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] { bundleMock }).anyTimes();
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(RemoteFileConnectionFactory.class.getName()),
            EasyMock.eq((String) null))).andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(factoryMock).anyTimes();
        EasyMock.replay(contextMock);

        support.activate(contextMock);

        RemoteFileConnection connection = RemoteFileConnectionSupport.getRemoteFileConnection(cert, new URI(URI));
        assertNotNull(connection);
    }

    /**
     * 
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    public void testGetRemoteInputStreamForFailure() throws Exception {
        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);
        try {
            RemoteFileConnectionSupport.getRemoteFileConnection(cert, new URI(URI));
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(RemoteFileConnectionFactory.class.getName()),
            EasyMock.eq(filter))).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);
        try {
            RemoteFileConnectionSupport.getRemoteFileConnection(cert, new URI(URI));
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }

        EasyMock.reset(contextMock);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(RemoteFileConnectionFactory.class.getName()),
            EasyMock.eq(filter))).andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(null).anyTimes();
        EasyMock.replay(contextMock);
        support.activate(contextMock);
        try {
            RemoteFileConnectionSupport.getRemoteFileConnection(cert, new URI(URI));
            fail();
        } catch (CommunicationException e) {
            assertTrue(true);
        }
    }
}
