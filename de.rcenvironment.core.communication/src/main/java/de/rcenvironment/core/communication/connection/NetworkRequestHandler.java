/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;

/**
 * Callback interface for incoming connection-level requests.
 * 
 * @author Robert Mischke
 */
public interface NetworkRequestHandler {

    /**
     * Tests whether this handler is applicable for the given request.
     * 
     * @param request the request
     * @return true if this handler is applicable.
     */
    boolean isApplicable(NetworkRequest request);

    /**
     * Each implementation of this method must determine whether it can handle the received request.
     * If this is the case, it must return a non-null response; otherwise, it must return null.
     * 
     * @param request the received request
     * @param lastHopNodeId the node id of the immediate neighbor the request was received from
     * @return the generated response, if the request could be handled; null, otherwise
     */
    NetworkResponse handleRequest(NetworkRequest request, NodeIdentifier lastHopNodeId);
}
