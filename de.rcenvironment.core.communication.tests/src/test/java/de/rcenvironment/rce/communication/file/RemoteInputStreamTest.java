/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file;

import static de.rcenvironment.rce.communication.CommunicationTestHelper.FILE_CONTACT;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.LOCAL_PLATFORM;
import static de.rcenvironment.rce.communication.CommunicationTestHelper.URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationTestHelper;
import de.rcenvironment.rce.communication.file.internal.RemoteFileConnectionSupport;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnectionFactory;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.internal.CommunicationType;

/**
 * 
 * Test cases for {@link RemoteInputStream}.
 * 
 * @author Doreen Seider
 */
public class RemoteInputStreamTest {

    private User user = EasyMock.createNiceMock(User.class);

    /**
     * Set up.
     * 
     * @throws Exception if an error occurs.
     */
    @Before
    public void setUp() throws Exception {

        CommunicationTestHelper.activateCommunicationContactMap();

        CommunicationContactMap.removeAllMappings();
        CommunicationContactMap.setMapping(CommunicationType.FILE_TRANSFER, LOCAL_PLATFORM, FILE_CONTACT);

        ServiceReference ref = EasyMock.createNiceMock(ServiceReference.class);

        RemoteFileConnectionFactory factoryMock = EasyMock.createNiceMock(RemoteFileConnectionFactory.class);
        EasyMock.expect(factoryMock.createRemoteFileConnection(user, new URI(URI))).andReturn(new DummyRemoteFileConnection());
        EasyMock.replay(factoryMock);

        BundleContext contextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(contextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();
        EasyMock.expect(contextMock.getAllServiceReferences(EasyMock.eq(RemoteFileConnectionFactory.class.getName()),
            EasyMock.eq((String) null)))
            .andReturn(new ServiceReference[] { ref }).anyTimes();
        EasyMock.expect(contextMock.getService(ref)).andReturn(factoryMock).anyTimes();
        EasyMock.replay(contextMock);

        new RemoteFileConnectionSupport().activate(contextMock);
    }

    /**
     * Test.
     * 
     * @throws Exception if an error occured.
     * */
    @Test
    public void test() throws Exception {
        RemoteInputStream remoteStream = new RemoteInputStream(user, new URI(URI));
        try {
            remoteStream.read();
            fail();
        } catch (RuntimeException e) {
            assertEquals("read2", e.getMessage());
        }

        try {
            byte[] b = new byte[7];
            remoteStream.read(b, 0, 7);
            fail();
        } catch (RuntimeException e) {
            assertEquals("read1", e.getMessage());
        }

        try {
            remoteStream.skip(7);
            fail();
        } catch (RuntimeException e) {
            assertEquals("skip", e.getMessage());
        }

        try {
            remoteStream.close();
            fail();
        } catch (RuntimeException e) {
            assertEquals("close", e.getMessage());
        }
    }

    /**
     * Test {@link RemoteFileConnection} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyRemoteFileConnection implements RemoteFileConnection {

        private static final long serialVersionUID = 1L;

        @Override
        public void close() throws IOException {
            throw new RuntimeException("close");
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            throw new RuntimeException("read1");
        }

        @Override
        public int read() throws IOException {
            throw new RuntimeException("read2");
        }

        @Override
        public long skip(long n) throws IOException {
            throw new RuntimeException("skip");
        }

    }
}
