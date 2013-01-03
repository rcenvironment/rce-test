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
 * Callback interface for messaging events; intended for monitoring and testing.
 * 
 * @author Robert Mischke
 */
public interface NetworkTrafficListener {

    /**
     * Called when the connection service has received a {@link NetworkRequest}, but has not
     * processed it yet; intended for monitoring and debugging.
     * 
     * @param request the received request
     * @param sourceId the id of the last hop this request was received from
     */
    void onRequestReceived(NetworkRequest request, NodeIdentifier sourceId);

    /**
     * Called after the connection service has processed a received {@link NetworkRequest}, and is
     * about to send the generated response back to the caller; intended for monitoring and
     * debugging.
     * 
     * @param response the generated response; may be null if no handler could process the request
     * @param request the received request
     * @param sourceId the id of the last hop the request was received from
     */
    void onResponseGenerated(NetworkResponse response, NetworkRequest request, NodeIdentifier sourceId);

    // TODO review: add onRequestGenerated/onResponseReceived callbacks? trigger on each hop or only
    // on start/end of route? add source/destination parameters? -- misc_ro
}
