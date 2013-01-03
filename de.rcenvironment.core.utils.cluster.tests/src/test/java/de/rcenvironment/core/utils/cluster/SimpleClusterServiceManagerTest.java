/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test case for {@link SimpleClusterServiceManager}.
 * @author Doreen Seider
 */
public class SimpleClusterServiceManagerTest {

    private ClusterService clusterInformationServiceMock = EasyMock.createNiceMock(ClusterService.class);
    
    /**
     * Test for {@link SimpleClusterServiceManager}.
     * @throws IOException on error
     */
    @Test
    public void testCreateSshBasedClusterJobInformationService() throws IOException {
        String randomString = RandomStringUtils.random(5);
        
        SimpleClusterServiceManager handler = new SimpleClusterServiceManager();
        try {
            handler.retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem.TORQUE, randomString, 8, randomString, randomString);
            fail();
        } catch (IllegalStateException e1) {
            assertTrue(true);
        }
        handler.bindClusterServiceManager(new ClusterInformationServiceStub());
        
        handler = new SimpleClusterServiceManager();
        ClusterService clusterInformationService = handler
            .retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem.TORQUE, randomString, 9, randomString, randomString);
        assertEquals(clusterInformationServiceMock, clusterInformationService);
        
    }
    
    /**
     * Stub implementation of {@link ClusterServiceManager}.
     * @author Doreen Seider
     */
    private class ClusterInformationServiceStub implements ClusterServiceManager {

        @Override
        public ClusterService retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem system, String host, int port,
            String sshAuthUser, String sshAuthPhrase) {
            return clusterInformationServiceMock;
        }
        
    }
}
