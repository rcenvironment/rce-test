/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.torque.internal;

import de.rcenvironment.core.utils.cluster.internal.ModifyableClusterJobInformation;


/**
 * Holds information about specified times belonging to a cluster job.
 * @author Doreen Seider
 */
public class ClusterJobTimesInformation {
    
    private static final String EMPTY_SPACE = "   ";
    
    private String jobId;

    private String remainingTime = ModifyableClusterJobInformation.NO_VALUE_SET;
    
    private String startTime = ModifyableClusterJobInformation.NO_VALUE_SET;
    
    private String queueTime = ModifyableClusterJobInformation.NO_VALUE_SET;
    
    public String getJobId() {
        return jobId;
    }

    public String getRemainingTime() {
        return remainingTime;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public String getQueueTime() {
        return queueTime;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public void setQueueTime(String queueTime) {
        this.queueTime = queueTime;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Cluster queue information: ");
        builder.append(jobId);
        builder.append(EMPTY_SPACE);
        builder.append(remainingTime);
        builder.append(EMPTY_SPACE);
        builder.append(startTime);
        builder.append(EMPTY_SPACE);
        builder.append(queueTime);
        return builder.toString();
    }

}
