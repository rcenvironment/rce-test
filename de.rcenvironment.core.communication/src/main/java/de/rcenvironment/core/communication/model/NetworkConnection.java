/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;

/**
 * Abstraction of a directed network connection that allows request-response calls to a remote node.
 * The realization of the actual wire communication depends on the underlying network transport.
 * Implementations of this class must be reusable and thread-safe.
 * 
 * Note that this kind of connection may or may not correspond to a persistent, underlying network
 * connection. As a result, a {@link NetworkConnection} may continue to exist after the contacted
 * node has already become unreachable. This kind of situation is usually detected on the next
 * communication attempt (similar to a TCP connection).
 * 
 * TODO rename to "MessageChannel" for distinction from actual network-level connections? -- misc_ro
 * 
 * @author Robert Mischke
 */
public interface NetworkConnection {

    /**
     * Returns a JVM-unique id for this connection. If this id is to be shared across JVMs, it
     * should be joined with another id (for example, the node UUID) to make it globally unique.
     * 
     * @return the unique id
     */
    String getConnectionId();

    /**
     * @param id a JVM-unique id for this connection
     */
    void setConnectionId(String id);

    /**
     * @return general information about the node at the remote end of this connection
     */
    NetworkNodeInformation getRemoteNodeInformation();

    /**
     * @param nodeInformation information about the node at the remote end of this connection
     */
    void setRemoteNodeInformation(NetworkNodeInformation nodeInformation);

    /**
     * @return true if this connection was initiated (on the network level) by the remote node, i.e.
     *         if this is a "passive" connection from the local node's perspective
     * 
     * @see NetworkConnectionEndpointHandler#onRemoteInitiatedConnectionEstablished(NetworkConnection,
     *      ServerContactPoint)
     */
    boolean getInitiatedByRemote();

    /**
     * @param value true if this connection was initiated (on the network level) by the remote node,
     *        i.e. if this is a "passive" connection from the local node's perspective
     */
    void setInitiatedByRemote(boolean value);

    /**
     * Sends a {@link NetworkRequest} to the remote node via this connection. The response is
     * returned asynchronously.
     * 
     * @param request the request to send
     * @param responseHandler the response callback handler
     * @param timeoutMsec the timeout in milliseconds
     */
    void sendRequest(NetworkRequest request, RawNetworkResponseHandler responseHandler, int timeoutMsec);

    /**
     * Closes this (logical) network connection. Depending on the transport that created this
     * connection, this may or may not result in an action on the network level.
     */
    void close();

    /**
     * Method for associating a {@link ServerContactPoint} with a connection. Usage is
     * transport-specific.
     * 
     * TODO review: push down to subclasses?
     * 
     * @param networkContactPoint the {@link NetworkContactPoint} to associate
     */
    void setAssociatedSCP(ServerContactPoint networkContactPoint);

}
