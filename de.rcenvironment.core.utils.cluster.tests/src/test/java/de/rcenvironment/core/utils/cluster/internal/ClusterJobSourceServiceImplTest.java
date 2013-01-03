/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.internal;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;

/**
 * Test cases for {@link ClusterJobSourceServiceImpl}.
 * @author Doreen Seider
 */
public class ClusterJobSourceServiceImplTest {

    private ClusterJobSourceServiceImpl service = new ClusterJobSourceServiceImpl();
    
    /** Test. */
    @Test
    public void test() {
        String host = RandomStringUtils.random(5);
        int port1 = 1;
        String jobId1 = RandomStringUtils.random(5);
        String source1 = RandomStringUtils.random(5);
        int port2 = 2;
        String jobId2 = RandomStringUtils.random(5);
        String source2 = RandomStringUtils.random(5);
        String jobId3 = RandomStringUtils.random(5);
        String source3 = RandomStringUtils.random(5);
        
        service.addSourceInformation(ClusterQueuingSystem.TORQUE, host, port1, jobId1, source1);
        service.addSourceInformation(ClusterQueuingSystem.TORQUE, host, port1, jobId2, source2);
        service.addSourceInformation(ClusterQueuingSystem.TORQUE, host, port2, jobId3, source3);
        
        Map<String, String> information = service.getSourceInformation(ClusterQueuingSystem.TORQUE, host, port1);
        
        assertEquals(2, information.size());
        assertEquals(source1, information.get(jobId1));
        assertEquals(source2, information.get(jobId2));
        
        information = service.getSourceInformation(ClusterQueuingSystem.TORQUE, host, port2);
        
        assertEquals(1, information.size());
        assertEquals(source3, information.get(jobId3));
        
        service.removeSourceInformation(ClusterQueuingSystem.TORQUE, host, port2, jobId3);

        information = service.getSourceInformation(ClusterQueuingSystem.TORQUE, host, port2);
        
        assertEquals(0, information.size());
    }
}
