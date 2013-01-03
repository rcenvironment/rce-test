/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.internal;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import de.rcenvironment.core.utils.cluster.ClusterService;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;

/**
 * Test cases for {@link ClusterServiceManagerImpl}.
 * @author Doreen Seider
 */
public class ClusterServiceHandlerImplTest {

    private ClusterServiceManagerImpl handler = new ClusterServiceManagerImpl();
    
    /**
     * Test. 
     * @throws IOException on error
     **/
    @Test
    public void testCreateSshBasedClusterJobInformationService() throws IOException {
        String host0 = RandomStringUtils.random(5);
        int port0 = 9;
        String user0 = RandomStringUtils.random(5);
        String passwd0 = RandomStringUtils.random(5);

        String host1 = RandomStringUtils.random(5);
        int port1 = 10;
        String user1 = RandomStringUtils.random(5);
        String passwd1 = RandomStringUtils.random(5);

        ClusterService service0 = handler.retrieveSshBasedClusterJobInformationService(
            ClusterQueuingSystem.TORQUE, host0, port0, user0, passwd0);
        
        ClusterService service1 = handler.retrieveSshBasedClusterJobInformationService(
            ClusterQueuingSystem.TORQUE, host0, port0, user0, passwd1);
        
        ClusterService service2 = handler.retrieveSshBasedClusterJobInformationService(
            ClusterQueuingSystem.TORQUE, host0, port0, user1, passwd0);
        
        ClusterService service3 = handler.retrieveSshBasedClusterJobInformationService(
            ClusterQueuingSystem.TORQUE, host0, port1, user0, passwd0);
        
        ClusterService service4 = handler.retrieveSshBasedClusterJobInformationService(
            ClusterQueuingSystem.TORQUE, host1, port0, user0, passwd0);

        assertSame(service0, service1);
        assertNotSame(service0, service2);
        assertNotSame(service0, service3);
        assertNotSame(service0, service4);
        assertNotSame(service2, service3);
        assertNotSame(service3, service4);
        
    }
}
