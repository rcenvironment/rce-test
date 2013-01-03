/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.torque.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.core.utils.cluster.ClusterJobInformation;
import de.rcenvironment.core.utils.cluster.ClusterJobInformation.ClusterJobState;
import de.rcenvironment.core.utils.cluster.internal.DistributedClusterJobSourceServiceImplTest;
import de.rcenvironment.core.utils.cluster.internal.ModifyableClusterJobInformation;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfiguration;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfigurationFactory;

/**
 * Test cases for {@link TorqueClusterService}.
 * @author Doreen Seider
 */
public class TorqueClusterServiceTest {
    
    private TorqueClusterService informationService;
    
    private DistributedClusterJobSourceServiceImplTest helperTestClass
        = new DistributedClusterJobSourceServiceImplTest();
    
    /** Set up. */
    @Before
    public void setUp() {
        String randomString = RandomStringUtils.random(5);
        SshSessionConfiguration sshConfiguration = SshSessionConfigurationFactory
            .createSshSessionConfigurationWithAuthPhrase(helperTestClass.localHost, helperTestClass.port, randomString, randomString);
        informationService = new TorqueClusterService(sshConfiguration);
        informationService.bindClusterJobSourceService(helperTestClass.createDistributedClusterJobSourceInformationService());
    }
    
    /**
     * Test. 
     * @throws IOException if an error occurs 
     **/
    @Test
    public void testParseStdoutForClusterJobInformation() throws IOException {
        final String stdout = IOUtils.toString(getClass().getResourceAsStream("/qstat"));
        
        Map<String, ClusterJobInformation> jobInformation = informationService.parseStdoutForClusterJobInformation(stdout);
        
        assertEquals(4, jobInformation.size());
        
        ClusterJobInformation information = jobInformation.get("506.bssc075dl");
        assertEquals("job", information.getJobName());
        assertEquals(ClusterJobState.Running, information.getJobState());
        assertEquals("qtest", information.getQueue());
        assertEquals("seid_do", information.getUser());
        
        information = jobInformation.get("507.bssc075dl");
        assertEquals("jib", information.getJobName());
        assertEquals(ClusterJobState.Queued, information.getJobState());
        assertEquals("mem", information.getQueue());
        assertEquals("litz_ma", information.getUser());
        
        information = jobInformation.get("508.bssc075dl");
        assertEquals("job", information.getJobName());
        assertEquals(ClusterJobState.Completed, information.getJobState());
        assertEquals("qtest", information.getQueue());
        assertEquals("seid_do", information.getUser());
        
        information = jobInformation.get("509.bssc075dl");
        assertEquals("jab", information.getJobName());
        assertEquals(ClusterJobState.Queued, information.getJobState());
        assertEquals("fast", information.getQueue());
        assertEquals("sipp_ja", information.getUser());
    }
    
    /**
     * Test. 
     * @throws IOException if an error occurs 
     **/
    @Test
    public void testParseStdoutForClusterJobTimesInformation() throws IOException {
        final String stdout = IOUtils.toString(getClass().getResourceAsStream("/showq"));
        
        Map<String, ClusterJobTimesInformation> jobTimesInformation = informationService.parseStdoutForClusterJobTimesInformation(stdout);
        
        assertEquals(3, jobTimesInformation.size());
        
        ClusterJobTimesInformation information = jobTimesInformation.get("606");
        assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getQueueTime());
        assertEquals("Tue Aug 28 16:54:22", information.getStartTime());
        assertEquals("00:30:02", information.getRemainingTime());
        
        information = jobTimesInformation.get("569");
        assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getQueueTime());
        assertEquals("Thu Aug 23 14:15:11", information.getStartTime());
        assertEquals("94:20:20:45", information.getRemainingTime());
        
        information = jobTimesInformation.get("607");
        assertEquals("Tue Aug 28 17:54:24", information.getQueueTime());
        assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getStartTime());
        assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getRemainingTime());
        
    }
    
    /**
     * Test.
     **/
    @Test
    public void testEnhanceClusterJobInformation() {
        String jobId0 = RandomStringUtils.random(5);
        String jobId1 = RandomStringUtils.random(5);
        String jobId2 = helperTestClass.localJobId;
        String jobId3 = helperTestClass.remoteJobId;
        String jobId4 = RandomStringUtils.random(5);
        
        String[] jobIds = new String[] { jobId0, jobId1, jobId2, jobId3 };
        
        Map<String, ClusterJobInformation> jobInformation = new HashMap<String, ClusterJobInformation>();
        Map<String, ClusterJobTimesInformation> jobTimesInformation = new HashMap<String, ClusterJobTimesInformation>();

        for (String jobId : jobIds) {
            ModifyableClusterJobInformation information = new ModifyableClusterJobInformation();
            information.setJobId(jobId);
            jobInformation.put(jobId, information);
            if (jobId.equals(jobId1) || jobId.equals(jobId3)) {
                ClusterJobTimesInformation timesInformation = new ClusterJobTimesInformation();
                timesInformation.setJobId(jobId);
                jobTimesInformation.put(jobId, timesInformation);
            }
        }
        
        String queueTime1 = RandomStringUtils.random(5);
        jobTimesInformation.get(jobId1).setQueueTime(queueTime1);
        
        String remainingTime3 = RandomStringUtils.random(5);
        String startTime3 = RandomStringUtils.random(5);
        jobTimesInformation.get(jobId3).setRemainingTime(remainingTime3);
        jobTimesInformation.get(jobId3).setStartTime(startTime3);
        
        Set<ClusterJobInformation> resultJobInformation = informationService
            .enhanceClusterJobInformation(jobInformation, jobTimesInformation);
        
        assertEquals(jobInformation.size(), resultJobInformation.size());
        for (ClusterJobInformation information : resultJobInformation) {
            if (information.getJobId().equals(jobId0)) {
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getQueueTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getRemainingTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getStartTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getSubmittedFrom());
            } else if (information.getJobId().equals(jobId1)) {
                assertEquals(queueTime1, information.getQueueTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getRemainingTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getStartTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getSubmittedFrom());
            } else if (information.getJobId().equals(jobId2)) {
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getQueueTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getRemainingTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getStartTime());
                assertEquals(helperTestClass.localSource, information.getSubmittedFrom());
            } else if (information.getJobId().equals(jobId3)) {
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getQueueTime());
                assertEquals(remainingTime3, information.getRemainingTime());
                assertEquals(startTime3, information.getStartTime());
                assertEquals(ModifyableClusterJobInformation.NO_VALUE_SET, information.getSubmittedFrom());
            }
        }
        assertFalse(resultJobInformation.contains(jobId4));
        
    }
}
