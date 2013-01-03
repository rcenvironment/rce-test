/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection;

import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;

/**
 * Service interface for methods that are required by the <b>receiving/incoming</b> end of a network
 * connection. Depending on the transport implementation, these methods may be provided as remote
 * services.
 * 
 * @author Robert Mischke
 */
public interface NetworkConnectionEndpointHandler {

    /**
     * Provides a two-way exchange of node information with a single method call.
     * 
     * @param nodeInformation the caller's node information
     * @return the receiver's node information
     */
    NetworkNodeInformation exchangeNodeInformation(NetworkNodeInformation nodeInformation);

    /**
     * Reports the creation of a remote-initiated connection, i.e. a connection that allows
     * <b>messages</b> to be initiated from node B to node A after A initiated a <b>physical network
     * connection</b> to B.
     * 
     * Note that such a connection is only required if B wants to *initiate* message calls to A; it
     * is not required for B to *reply* (in a RPC sense) to messages sent by A.
     * 
     * @param connection the new connection
     * @param serverContactPoint the contact point that was used to contact the local node
     */
    void onRemoteInitiatedConnectionEstablished(NetworkConnection connection, ServerContactPoint serverContactPoint);

    /**
     * TODO review; probably obsolete.
     * 
     * @param connection the affected connection; TODO breaks connection==outgoing convention
     * @param serverContactPoint the associated {@link ServerContactPoint}
     */
    @Deprecated
    void onIncomingConnectionClosed(NetworkConnection connection, ServerContactPoint serverContactPoint);

    /**
     * Called for every {@link NetworkRequest} received by this node, regardless of the local node
     * is the final destination or not.
     * 
     * @param request the received request
     * @param sourceId the {@link NodeIdentifier} of the last hop the request was received from
     * @return the generated or forwarded {@link NetworkResponse}
     */
    NetworkResponse onRawRequestReceived(NetworkRequest request, NodeIdentifier sourceId);

    /**
     * Called for every received {@link NetworkRequest} that has the local node as its final
     * destination.
     * 
     * @param request the received request
     * @return the locally generated {@link NetworkResponse}
     */
    NetworkResponse onRequestArrivedAtDestination(NetworkRequest request);

}
