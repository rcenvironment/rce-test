/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.service.internal;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;

/**
 * Test cases for {@link ServiceRemoteFileConnectionFactory}.
 * 
 * @author Doreen Seider
 */
public class ServiceRemoteFileConnectionFactoryTest {

    private final UUID dmUuid = UUID.randomUUID();
    
    private final String nodeId = "node-id";
    
    private final String uri = "rce://" + nodeId + "/" + dmUuid + "/7";

    private User user = EasyMock.createNiceMock(User.class);

    private ServiceRemoteFileConnectionFactory factory;

    /** Set up.
     * @throws Exception if an error occurred.
     */
    @Before
    public void setUp() throws Exception {
        factory = new ServiceRemoteFileConnectionFactory();
        factory.bindCommunicationService(new DummyCommunicationService());
        factory.activate(EasyMock.createNiceMock(BundleContext.class));
    }

    /**
     * Test.
     * @throws Exception if the test fails.
     */
    @Test
    public void test() throws Exception {
        RemoteFileConnection conncetion = factory.createRemoteFileConnection(user, new URI(uri));
        assertNotNull(conncetion);

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
                return EasyMock.createNiceMock(FileService.class);
            }
            return null;
        }

    }

}
