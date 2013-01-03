/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.internal;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.cluster.ClusterJobSourceService;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceStubFactory;

/**
 * Test cases for {@link DistributedClusterJobSourceServiceImpl}.
 * @author Doreen Seider
 */
public class DistributedClusterJobSourceServiceImplTest {

    /** Test constants. */
    public int port = 3;

    /** Test constants. */
    public String localHost = RandomStringUtils.random(5);

    /** Test constants. */
    public String localJobId = RandomStringUtils.random(5);

    /** Test constants. */
    public String localSource = RandomStringUtils.random(5);

    /** Test constants. */
    public String remoteHost = RandomStringUtils.random(5);

    /** Test constants. */
    public String remoteJobId = RandomStringUtils.random(5);

    /** Test constants. */
    public String remoteSource = RandomStringUtils.random(5);
    
    private DistributedClusterJobSourceServiceImpl distrInformationService;
    
    private ClusterJobSourceService localInformationService;
    
    private ClusterJobSourceService remoteInformationService;
    
    private PlatformIdentifier remotePlatform;
    
    /**
     * Helper method used by other tests as well.
     * @return set up {@link DistributedClusterJobSourceServiceImpl} instance.
     */
    public DistributedClusterJobSourceServiceImpl createDistributedClusterJobSourceInformationService() {
        
        localInformationService = new ClusterJobSourceServiceImpl();
        localInformationService.addSourceInformation(ClusterQueuingSystem.TORQUE, localHost, port,
            localJobId, localSource);
        
        remoteInformationService = new ClusterJobSourceServiceImpl();
        remoteInformationService.addSourceInformation(ClusterQueuingSystem.TORQUE, remoteHost, port,
            remoteJobId, remoteSource);
        
        distrInformationService = new DistributedClusterJobSourceServiceImpl();
        distrInformationService.bindCommunicationService(new TestCommunicationService());
        
        remotePlatform = PlatformIdentifierFactory.fromHostAndNumber(RandomStringUtils.random(5), 2);
        Set<PlatformIdentifier> remotePlatforms = new HashSet<PlatformIdentifier>();
        remotePlatforms.add(remotePlatform);
        distrInformationService
            .bindPlatformService(PlatformServiceStubFactory.createHostAndNumberStub(RandomStringUtils.random(5), 1, remotePlatforms));
        
        distrInformationService.bindClusterJobSourceService(localInformationService);
        return distrInformationService;
    }
    
    /** Set up. */
    @Before
    public void setUp() {
        distrInformationService = createDistributedClusterJobSourceInformationService();
        
    }
    
    /** Test. */
    @Test
    public void testGetSourceInformationForSpecifiedPlatform() {
        
        Map<String, String> information = distrInformationService.getSourceInformation(null, ClusterQueuingSystem.TORQUE, localHost, port);
        assertEquals(1, information.size());
        assertEquals(localSource, information.get(localJobId));

        information = distrInformationService.getSourceInformation(null, ClusterQueuingSystem.TORQUE, remoteHost, port);
        assertEquals(0, information.size());

        information = distrInformationService.getSourceInformation(remotePlatform, ClusterQueuingSystem.TORQUE, remoteHost, port);
        
        assertEquals(1, information.size());
        assertEquals(remoteSource, information.get(remoteJobId));

        information = distrInformationService.getSourceInformation(remotePlatform, ClusterQueuingSystem.TORQUE, localHost, port);
        
        assertEquals(0, information.size());
    }
    
    /** Test. */
    @Test
    public void testGetSourceInformation() {
        
        Map<String, String> information = distrInformationService.getSourceInformation(ClusterQueuingSystem.TORQUE, localHost, port);
        assertEquals(1, information.size());
        assertEquals(localSource, information.get(localJobId));

        information = distrInformationService.getSourceInformation(ClusterQueuingSystem.TORQUE, remoteHost, port);
        assertEquals(1, information.size());
        assertEquals(remoteSource, information.get(remoteJobId));
    }
    
    /**
     * Custom Test {@link CommunicationService} implementation.
     * @author Doreen Seider
     */
    private class TestCommunicationService extends MockCommunicationService {
        
        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            return remoteInformationService;
        }
    }
}
