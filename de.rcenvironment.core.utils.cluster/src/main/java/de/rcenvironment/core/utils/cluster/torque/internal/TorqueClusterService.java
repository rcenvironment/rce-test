/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.torque.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.core.utils.cluster.ClusterService;
import de.rcenvironment.core.utils.cluster.ClusterJobInformation;
import de.rcenvironment.core.utils.cluster.ClusterJobInformation.ClusterJobState;
import de.rcenvironment.core.utils.cluster.ClusterJobStateChangeListener;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;
import de.rcenvironment.core.utils.cluster.DistributedClusterJobSourceService;
import de.rcenvironment.core.utils.cluster.internal.ModifyableClusterJobInformation;
import de.rcenvironment.core.utils.ssh.jsch.JschSessionFactory;
import de.rcenvironment.core.utils.ssh.jsch.SshParameterException;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfiguration;
import de.rcenvironment.core.utils.ssh.jsch.executor.JSchCommandLineExecutor;

/**
 * TORQUE implementation of {@link ClusterService}.
 * @author Doreen Seider
 */
public class TorqueClusterService implements ClusterService {

    private static final String REMOTE_WORK_DIR = "~";

    private static final int INDEX_JOBID = 0;
    
    private static final int INDEX_USER = 1;
    
    private static final int INDEX_QUEUE = 2;

    private static final int INDEX_JOBNAME = 3;

    private static final int INDEX_JOBSTATE = 9;
    
    private static final int INDEX_REMAININGTIME = 4;

    private static final int INDEX_STARTTIME = 5;
    
    private static final int INDEX_QUEUETIME = 5;
    
    private static final int SECTION_ACTIVE_JOBS = 0;
    
    private static final int SECTION_IDLE_JOBS = 1;
    
    private static final int SECTION_BLOCKED_JOBS = 2;
    
    private static DistributedClusterJobSourceService informationService
        = ServiceUtils.createNullService(DistributedClusterJobSourceService.class);
    
    private SshSessionConfiguration sshConfiguration;
    
    private Session jschSession;
    
    private volatile long latestFetch = 0;
    
    private Map<String, ClusterJobInformation> latestFetchedJobInformation;
    
    private Map<String, ClusterJobState> lastClusterJobStates = new HashMap<String, ClusterJobInformation.ClusterJobState>();
    
    private final Map<String, ClusterJobStateChangeListener> listeners = new HashMap<String, ClusterJobStateChangeListener>();

    private Timer fetchInformationTimer;
    
    public TorqueClusterService() {}
    
    public TorqueClusterService(SshSessionConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }
    
    protected void bindClusterJobSourceService(DistributedClusterJobSourceService newService) {
        informationService = newService;
    }
    
    protected void unbindDistributedClusterJobSourceInformationService(DistributedClusterJobSourceService oldService) {}
    
    @Override
    public Set<ClusterJobInformation> fetchClusterJobInformation() throws IOException {
        synchronized (this) {
            if (jschSession == null) {
                try {
                    jschSession = JschSessionFactory.setupSession(sshConfiguration.getDestinationHost(),
                        sshConfiguration.getPort(), sshConfiguration.getSshAuthUser(), null,
                        sshConfiguration.getSshAuthPhrase(), null);
                } catch (JSchException e) {
                    throw new IOException("Establishing connection to cluster failed", e);
                } catch (SshParameterException e) {
                    throw new IOException("Establishing connection to cluster failed", e);
                }
            }   
        }
        String stdout = executesCommand(jschSession, "qstat -a", REMOTE_WORK_DIR);
        Map<String, ClusterJobInformation> jobInformation = parseStdoutForClusterJobInformation(stdout);

        latestFetchedJobInformation = Collections.unmodifiableMap(jobInformation);
        latestFetch = new Date().getTime();
        
        stdout = executesCommand(jschSession, "showq", REMOTE_WORK_DIR);
        Map<String, ClusterJobTimesInformation> jobTimesInformation = parseStdoutForClusterJobTimesInformation(stdout);
        return enhanceClusterJobInformation(jobInformation, jobTimesInformation);
    }
    
    @Override
    public String cancelClusterJobs(List<String> jobIds) throws IOException {
        StringBuilder commandBuilder = new StringBuilder("qdel ");
        for (String jobId : jobIds) {
            commandBuilder.append(" ");
            commandBuilder.append(jobId);
        }
        try {
            executesCommand(jschSession, commandBuilder.toString(), REMOTE_WORK_DIR);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return "";
    }

        
    @Override
    public void addClusterJobStateChangeListener(String jobId, final ClusterJobStateChangeListener listener) {
        synchronized (listeners) {
            listeners.put(jobId, listener);
            if (fetchInformationTimer == null) {
                fetchInformationTimer = new Timer("Fetch Cluster Job Information Timer", true);
                TimerTask fetchInformationTimerTask = new TimerTask() {
                    
                    @Override
                    public void run() {
                        final int oneSecond = 1000;
                        if (latestFetchedJobInformation == null || new Date().getTime() - latestFetch > FETCH_INTERVAL + oneSecond) {
                            try {
                                fetchClusterJobInformation();
                            } catch (IOException e) {
                                throw new RuntimeException("Fetching cluster job information failed", e);
                            }
                        }
                        notifyClusterJobStateChangeListener();
                    }
                };
                fetchInformationTimer.schedule(fetchInformationTimerTask, 0, ClusterService.FETCH_INTERVAL);                
            }
        }
    }
    
    private void notifyClusterJobStateChangeListener() {

        Set<String> listenersToRemove = new HashSet<String>();
        
        synchronized (listeners) {
            for (String jobId : listeners.keySet()) {
                ClusterJobInformation.ClusterJobState lastState = lastClusterJobStates.get(jobId);
                if (latestFetchedJobInformation.containsKey(jobId)) {
                    ClusterJobInformation.ClusterJobState latestState = latestFetchedJobInformation.get(jobId).getJobState();
                    if (lastState == null || !lastState.equals(latestState)) {
                        if (!listeners.get(jobId).onClusterJobStateChanged(latestState)) {
                            listenersToRemove.add(jobId);
                        }
                    }
                    lastState = latestState;
                } else {
                    if (!listeners.get(jobId).onClusterJobStateChanged(ClusterJobInformation.ClusterJobState.Unknown)) {
                        listenersToRemove.add(jobId);
                    }
                }
            }
            
            for (String jobId : listenersToRemove) {
                listeners.remove(jobId);
            }
            
            
            if (listeners.isEmpty()) {
                fetchInformationTimer.cancel();
                fetchInformationTimer = null;
            }
        }
    }
    
    private String executesCommand(Session ajschSession, String command, String remoteWorkDir)
        throws IOException {
        JSchCommandLineExecutor commandLineExecutor = new JSchCommandLineExecutor(ajschSession, remoteWorkDir);
        commandLineExecutor.start(command);
        InputStream stdoutStream = commandLineExecutor.getStdout();
        InputStream stderrStream = commandLineExecutor.getStderr();
        try {
            commandLineExecutor.waitForTermination();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        String stderr = IOUtils.toString(stderrStream);
        IOUtils.closeQuietly(stderrStream);
        if (stderr != null && !stderr.isEmpty()) {
            throw new IOException(stderr);
        }
        String stdout = IOUtils.toString(stdoutStream);
        IOUtils.closeQuietly(stdoutStream);
        return stdout;
    }

    protected Map<String, ClusterJobInformation> parseStdoutForClusterJobInformation(String stdout) {
        Map<String, ClusterJobInformation> jobInformation = new HashMap<String, ClusterJobInformation>();
        
        final String regex = "(-+)";

        boolean headerCompleted = false;
        boolean isHeader = false;
        
        
        String[] lines = stdout.split("\n");
        for (String line : lines) {
            headerCompleted = isHeader;
            String[] lineTokens = line.split("(\\s+)");
            if (headerCompleted) {
                ClusterJobInformation information = extractClusterJobInformation(lineTokens);
                jobInformation.put(information.getJobId(), information);
            } else {
                isHeader = true;
                for (String attribute : lineTokens) {
                    if (!attribute.matches(regex)) {
                        isHeader = false;
                        break;
                    }
                }
            }
        }
        return jobInformation;
    }
    
    protected Map<String, ClusterJobTimesInformation> parseStdoutForClusterJobTimesInformation(String stdout) {
        Map<String, ClusterJobTimesInformation> information = new HashMap<String, ClusterJobTimesInformation>();
        
        int section = SECTION_ACTIVE_JOBS;
        boolean inSection = false;
        boolean emptyRowPassed = false;
        
        String[] lines = stdout.split("\n");
        for (String line : lines) {
            String[] lineTokens = line.split("(\\s+)");
            if (inSection) {
                if (lineTokens.length <= 1) {
                    if (!emptyRowPassed) {
                        emptyRowPassed = true;                        
                    } else {
                        emptyRowPassed = false;
                        inSection = false;
                        section++;
                    }
                } else {
                    ClusterJobTimesInformation timesInformation = extractClusterJobTimesInformation(lineTokens, section);
                    information.put(timesInformation.getJobId(), timesInformation);
                }
            } else {
                for (String attribute : lineTokens) {
                    if (attribute.matches("JOBNAME")) {
                        inSection = true;
                        break;
                    }
                }
            }
        }

        return information;
    }
    
    protected Set<ClusterJobInformation> enhanceClusterJobInformation(Map<String, ClusterJobInformation> jobInformation,
        Map<String, ClusterJobTimesInformation> jobTimesInformation) {
        
        jobInformation = enhanceClusterJobInformationWithTimesInformation(jobInformation, jobTimesInformation);
        jobInformation = enhanceClusterJobInformationWithSubmissionSourceInformation(jobInformation);

        return new HashSet<ClusterJobInformation>(jobInformation.values());
    }
    
    private Map<String, ClusterJobInformation> enhanceClusterJobInformationWithTimesInformation(
        Map<String, ClusterJobInformation> jobInformation, Map<String, ClusterJobTimesInformation> jobTimesInformation) {
        
        for (ClusterJobInformation information : jobInformation.values()) {
            String jobName = information.getJobId().split("\\.")[0];
            if (jobTimesInformation.containsKey(jobName)) {
                ((ModifyableClusterJobInformation) information).setClusterJobTimesInformation(jobTimesInformation.get(jobName));
            } else {
                ((ModifyableClusterJobInformation) information).setClusterJobTimesInformation(new ClusterJobTimesInformation());
            }
        }
        return jobInformation;
    }
    
    private Map<String, ClusterJobInformation> enhanceClusterJobInformationWithSubmissionSourceInformation(
        Map<String, ClusterJobInformation> jobInformation) {

        Map<String, String> sourceInformation = informationService.getSourceInformation(ClusterQueuingSystem.TORQUE,
            sshConfiguration.getDestinationHost(), sshConfiguration.getPort());

        for (String jobId : sourceInformation.keySet()) {
            if (jobInformation.containsKey(jobId)) {
                ((ModifyableClusterJobInformation) jobInformation.get(jobId)).setWorkflowInformation(sourceInformation.get(jobId));
            }
        }

        return jobInformation;
        
    }
    
    private ClusterJobTimesInformation extractClusterJobTimesInformation(String[] lineTokens, int section) {
        ClusterJobTimesInformation information = new ClusterJobTimesInformation();
        
        information.setJobId(lineTokens[INDEX_JOBID]);

        switch (section) {
        case SECTION_ACTIVE_JOBS:
            information.setRemainingTime(lineTokens[INDEX_REMAININGTIME]);
            information.setStartTime(getTime(lineTokens, INDEX_STARTTIME));
            break;
        case SECTION_IDLE_JOBS:
        case SECTION_BLOCKED_JOBS:
        default:
            information.setQueueTime(getTime(lineTokens, INDEX_QUEUETIME));
            break;
        }
        
        return information;
    }
    
    private String getTime(String[] lineTokens, int startIndex) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = startIndex; i < lineTokens.length; i++) {
            stringBuffer.append(lineTokens[i]);
            stringBuffer.append(" ");
        }
        return stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length()).toString();
    }
    
    private ClusterJobInformation extractClusterJobInformation(String[] lineTokens) {
        ModifyableClusterJobInformation information = new ModifyableClusterJobInformation();
        
        information.setJobId(lineTokens[INDEX_JOBID]);
        information.setUser(lineTokens[INDEX_USER]);
        information.setQueue(lineTokens[INDEX_QUEUE]);
        information.setQueue(lineTokens[INDEX_QUEUE]);
        information.setJobName(lineTokens[INDEX_JOBNAME]);
        information.setJobState(getClusterJobState(lineTokens[INDEX_JOBSTATE]));
        
        return information;
    }
    
    private ClusterJobState getClusterJobState(String stateToken) {
        ClusterJobState state = ClusterJobState.Unknown;
        if (stateToken.equals("C"))  {
            state = ClusterJobState.Completed;
        } else if (stateToken.equals("E"))  {
            state = ClusterJobState.Exiting;
        } else if (stateToken.equals("H"))  {
            state = ClusterJobState.Held;
        } else if (stateToken.equals("Q"))  {
            state = ClusterJobState.Queued;
        } else if (stateToken.equals("R"))  {
            state = ClusterJobState.Running;
        } else if (stateToken.equals("T"))  {
            state = ClusterJobState.Moved;
        } else if (stateToken.equals("W"))  {
            state = ClusterJobState.Waiting;
        } else if (stateToken.equals("S"))  {
            state = ClusterJobState.Suspended;
        }
        return state;
    }

}
