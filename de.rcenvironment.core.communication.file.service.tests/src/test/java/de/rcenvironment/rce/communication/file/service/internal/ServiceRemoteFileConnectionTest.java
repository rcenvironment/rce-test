/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.service.internal;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection.FileType;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;

/**
 * Test cases for {@link ServiceRemoteFileConnection}.
 * 
 * @author Doreen Seider
 */
public class ServiceRemoteFileConnectionTest extends TestCase {

    private final UUID dmUuid = UUID.randomUUID();
    
    private final String nodeId = "node-id";
    
    private final String uri = "rce://" + nodeId + "/" + dmUuid + "/7";

    private User user = EasyMock.createNiceMock(User.class);
    
    /**
     * Test.
     * @throws Exception if the test fails.
     */
    @Test
    public void test() throws Exception {

        RemoteFileConnection connection = new ServiceRemoteFileConnection(user, new URI(uri), new DummyCommunicationService(),
            EasyMock.createNiceMock(BundleContext.class));
        int read = connection.read();
        final int minusOne = -1;
        assertTrue(read > minusOne);
        byte[] b = new byte[5];
        read = connection.read(b, 0, 5);
        connection.skip(5);
        assertEquals(5, read);
        connection.close();
    }
    
    /**
     * Dummy {@link CommunicationService} implementation.
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            if (platformIdentifier.equals(PlatformIdentifierFactory.fromNodeId(nodeId)) && iface == FileService.class) {
                return new DummeFileService();
            }
            return null;
        }
        
        /**
         * Dummy {@link FileService} implementation.
         * @author Doreen Seider
         */
        private class DummeFileService implements FileService {

            private final String testUUID = "snoopy";
            
            @Override
            public void close(String uuid) throws IOException {
            }

            @Override
            public String open(User certificate, FileType type, String file) throws IOException {
                return testUUID;
            }

            @Override
            public int read(String uuid) throws IOException {
                if (uuid.equals(testUUID)) {
                    return 5;
                }
                return 0;
            }

            @Override
            public byte[] read(String uuid, Integer len) throws IOException {
                if (uuid.equals(testUUID)) {
                    return new byte[len];
                }
                return new byte[0];
            }
            
            @Override
            public long skip(String uuid, Long n) throws IOException {
                if (uuid.equals(testUUID)) {
                    return n;
                }
                return 0;
            }
            
        }

    }
}
