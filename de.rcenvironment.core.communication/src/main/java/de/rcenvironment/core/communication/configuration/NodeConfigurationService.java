/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.configuration;

import java.util.List;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Configuration management service for the local node. It serves to decouple the communication
 * classes from the low-level {@link CommunicationConfiguration} class, which simplifies the
 * configuration of integration tests.
 * 
 * @author Robert Mischke
 */
public interface NodeConfigurationService {

    /**
     * @return the identifier of the local node
     */
    NodeIdentifier getLocalNodeId();

    /**
     * @return a {@link NetworkNodeInformation} object for the local node
     */
    NetworkNodeInformation getLocalNodeInformation();

    /**
     * @return the list of "provided" {@link NetworkContactPoint}s for the local node; these are the
     *         {@link NetworkContactPoint}s that the local node listens on as a "server"
     */
    List<NetworkContactPoint> getServerContactPoints();

    /**
     * @return the list of "remote" {@link NetworkContactPoint}s for the local node; these are the
     *         {@link NetworkContactPoint}s that the local node connects to as a "client"
     */
    List<NetworkContactPoint> getInitialNetworkContactPoints();

    /**
     * @return the delay (in milliseconds) before connections to the configured
     *         "remote contact points" are attempted
     */
    long getDelayBeforeStartupConnectAttempts();

    /**
     * @return the timeout for a request/response cycle on the initiating node
     */
    int getRequestTimeoutMsec();

    /**
     * @return the timeout for a request/response cycle on a forwarding/routing node
     */
    long getForwardingTimeoutMsec();

}
