/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Future;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A service that provides message routing operations.
 * 
 * @author Robert Mischke
 */
public interface NetworkRoutingService {

    /**
     * Sends a routed {@link NetworkRequest}.
     * 
     * @param messageContent the message payload
     * @param receiver The receivers node id.
     * @return a {@link Future} representing the {@link NetworkResponse}
     * @throws SerializationException on serialization failure
     */
    Future<NetworkResponse> performRoutedRequest(Serializable messageContent, NodeIdentifier receiver) throws SerializationException;

    /**
     * Returns the (optionally filtered) nodes in the known topology.
     * 
     * @param restrictToWorkflowHostsAndSelf if true, only the local node and nodes that are marked
     *        as "workflow hosts" are returned; otherwise, all known nodes are returned
     * 
     *        TODO refer to central glossary for "workflow host"?
     * 
     * @return the determined set of node ids
     */
    Set<PlatformIdentifier> getReachableNodes(boolean restrictToWorkflowHostsAndSelf);

    /**
     * Broadcast the information that this node is shutting down, and should be removed from the
     * known network.
     */
    void announceShutdown();

    /**
     * Returns a human-readable summary of the current network state.
     * 
     * @return the network summary text representation
     */
    String getNetworkSummary();

}
