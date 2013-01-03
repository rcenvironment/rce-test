/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.transport.spi;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.NetworkConnectionService;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Interface for pluggable transport providers.
 * 
 * TODO extend documentation
 * 
 * @author Robert Mischke
 */
public interface NetworkTransportProvider {

    /**
     * @return the unique transport id of this implementation; may be used to identify the OSGi
     *         bundle providing its implementation, so naming conventions should be observed
     */
    String getTransportId();

    /**
     * Connects to a remote node. This is a blocking call; all implementations must be thread-safe.
     * After this call returns, it is expected that the created {@link NetworkConnection} contains
     * the {@link NetworkNodeInformation} of the remote node. This is usually achieved by performing
     * a transport-native handshake where the remote node delegates to an implementation of
     * {@link NetworkConnectionEndpointHandler#exchangeNodeInformation(NetworkNodeInformation)}.
     * 
     * @param ncp the contact information for the remote server
     * @param ownNodeInformation the network-level information of the local node
     * @param allowInverseConnection whether to allow the remote node to use the same physical link
     *        to initiate network messages to the local node
     * @param connectionEndpointHandler the {@link NetworkConnectionEndpointHandler} to use for
     *        remote-initiated connections from the perspective of the connection target; may be
     *        null if <code>allowInverseConnections</code> is false
     * @param brokenConnectionListener listener for unexpected connection failure
     * @return the initialized {@link NetworkConnection}
     * @throws CommunicationException on connection or protocol failures
     */
    // TODO boolean parameter is redundant; remove -- misc_ro
    NetworkConnection connect(NetworkContactPoint ncp, NetworkNodeInformation ownNodeInformation, boolean allowInverseConnection,
        NetworkConnectionEndpointHandler connectionEndpointHandler, BrokenConnectionListener brokenConnectionListener)
        throws CommunicationException;

    /**
     * @return whether this transport supports remote-initiated connections, i.e. logical message
     *         links that can initiate messages over a physical connection that was initiated by the
     *         remote node
     */
    boolean supportsRemoteInitiatedConnections();

    /**
     * Starts a server matching this transport. This is a blocking call. See
     * {@link NetworkConnectionService#startServer(NetworkContactPoint)} for more information.
     * 
     * @param scp the {@link ServerContactPoint} to start and get configuration information from
     * @throws CommunicationException on startup errors
     */
    void startServer(ServerContactPoint scp) throws CommunicationException;

    /**
     * Stops a server matching this transport. This is a blocking call. See
     * {@link NetworkConnectionService#stopServer(NetworkContactPoint)} for more information.
     * 
     * @param scp the {@link ServerContactPoint} to stop and get configuration information from
     */
    void stopServer(ServerContactPoint scp);

}
