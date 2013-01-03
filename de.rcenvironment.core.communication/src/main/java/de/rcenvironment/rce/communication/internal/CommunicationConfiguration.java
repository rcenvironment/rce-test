/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Class providing the configuration of the communication bundle. Additionally it defines the
 * default configuration.
 * 
 * @author Frank Kautz
 * @author Doreen Seider
 * @author Tobias Menden
 * @author Robert Mischke
 */
public class CommunicationConfiguration {

    /**
     * The interval between connection health/liveliness checks.
     */
    public static final int CONNECTION_HEALTH_CHECK_INTERVAL_MSEC = 15 * 1000;

    /**
     * Defines the maximum random delay ("jitter") that is waited before each individual connection
     * health check. This randomness serves to avoid all connections being checked at once, and
     * always in the same order. The interval for the random delay is [0;
     * CONNECTION_HEALTH_CHECK_MAX_JITTER_MSEC]. The maximum value should be smaller than
     * (CONNECTION_HEALTH_CHECK_INTERVAL_MSEC - CONNECTION_HEALTH_CHECK_TIMEOUT_MSEC) to avoid
     * overlapping checks for the same connection.
     * 
     */
    public static final int CONNECTION_HEALTH_CHECK_MAX_JITTER_MSEC = 9 * 1000;

    /**
     * The maximum response time for an individual connection health check.
     */
    public static final int CONNECTION_HEALTH_CHECK_TIMEOUT_MSEC = 5 * 1000;

    /**
     * The number of consecutive health check failures before a connection is considered "broken".
     */
    public static final int CONNECTION_HEALTH_CHECK_FAILURE_LIMIT = 3;

    /**
     * Default request/response timeout on the sender side.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT_MSEC = 30000;

    /**
     * Default timeout for waiting for the response while forwarding.
     */
    public static final int DEFAULT_FORWARDING_TIMEOUT_MSEC = 30000;

    private List<String> serviceCallContacts = new Vector<String>();

    private List<String> fileTransferContacts = new Vector<String>();

    private List<String> remotePlatforms = new Vector<String>();

    private String bindAddress = "0.0.0.0";

    private String externalAddress = "127.0.0.1";

    private boolean useNewCommunicationLayer = true;

    private List<String> providedContactPoints = new ArrayList<String>();

    private List<String> remoteContactPoints = new ArrayList<String>();

    private int requestTimeoutMsec = DEFAULT_REQUEST_TIMEOUT_MSEC;

    private int forwardingTimeoutMsec = DEFAULT_FORWARDING_TIMEOUT_MSEC;

    public void setServiceCallContacts(List<String> value) {
        this.serviceCallContacts = value;
    }

    public void setFileTransferContacts(List<String> value) {
        this.fileTransferContacts = value;
    }

    public void setRemotePlatforms(List<String> value) {
        this.remotePlatforms = value;
    }

    public List<String> getServiceCallContacts() {
        return serviceCallContacts;
    }

    public List<String> getFileTransferContacts() {
        return fileTransferContacts;
    }

    public List<String> getRemotePlatforms() {
        return remotePlatforms;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getExternalAddress() {
        return externalAddress;
    }

    public void setExternalAddress(String externalIpAddress) {
        this.externalAddress = externalIpAddress;
    }

    public boolean getUseNewCommunicationLayer() {
        return useNewCommunicationLayer;
    }

    public void setUseNewCommunicationLayer(boolean useNewNetworkLayer) {
        this.useNewCommunicationLayer = useNewNetworkLayer;
    }

    public List<String> getProvidedContactPoints() {
        return providedContactPoints;
    }

    public void setProvidedContactPoints(List<String> providedContactPoints) {
        this.providedContactPoints = providedContactPoints;
    }

    public List<String> getRemoteContactPoints() {
        return remoteContactPoints;
    }

    public void setRemoteContactPoints(List<String> remoteContactPoints) {
        this.remoteContactPoints = remoteContactPoints;
    }

    public int getRequestTimeoutMsec() {
        return requestTimeoutMsec;
    }

    public void setRequestTimeoutMsec(int requestTimeoutMsec) {
        this.requestTimeoutMsec = requestTimeoutMsec;
    }

    public int getForwardingTimeoutMsec() {
        return forwardingTimeoutMsec;
    }

    public void setForwardingTimeoutMsec(int forwardingTimeoutMsec) {
        this.forwardingTimeoutMsec = forwardingTimeoutMsec;
    }

}
