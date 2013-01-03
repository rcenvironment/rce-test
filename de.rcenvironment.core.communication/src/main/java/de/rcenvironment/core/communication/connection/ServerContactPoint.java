/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Extension of a {@link NetworkContactPoint} to represent transport-specific implementations that
 * accept incoming connections. For example, a TCP-based {@link ServerContactPoint} might listen on
 * a TCP port and accept inbound connections.
 * 
 * @author Robert Mischke
 */
public class ServerContactPoint {

    private NetworkContactPoint networkContactPoint;

    private NetworkConnectionEndpointHandler endpointHandler;

    private boolean acceptingMessages = false;

    private NetworkTransportProvider transportProvider;

    public ServerContactPoint(NetworkTransportProvider transportProvider, NetworkContactPoint ncp,
        NetworkConnectionEndpointHandler endpointHandler) {
        this.transportProvider = transportProvider;
        this.networkContactPoint = ncp;
        this.endpointHandler = endpointHandler;
    }

    public NetworkContactPoint getNetworkContactPoint() {
        return networkContactPoint;
    }

    public void setNetworkContactPoint(NetworkContactPoint contactPoint) {
        this.networkContactPoint = contactPoint;
    }

    public NetworkConnectionEndpointHandler getEndpointHandler() {
        return endpointHandler;
    }

    public void setEndpointHandler(NetworkConnectionEndpointHandler endpointHandler) {
        this.endpointHandler = endpointHandler;
    }

    public synchronized boolean isAcceptingMessages() {
        return acceptingMessages;
    }

    public synchronized void setAcceptingMessages(boolean shutDown) {
        this.acceptingMessages = shutDown;
    }

    @Override
    public String toString() {
        return String.format("SCP (NCP='%s', listening=%s)", networkContactPoint, acceptingMessages);
    }

    public String getTransportId() {
        return getNetworkContactPoint().getTransportId();
    }

    /**
     * Starts accepting connections at the configured {@link NetworkContactPoint}.
     * 
     * Note that this method could be refactored away, but is kept for API symmetry with shutDown().
     * 
     * @throws CommunicationException on startup failure
     */
    public void start() throws CommunicationException {
        transportProvider.startServer(this);
    }

    /**
     * Stops accepting connections at the configured {@link NetworkContactPoint}. Whether inbound
     * connections are actively closed is transport-specific.
     */
    public void shutDown() {
        transportProvider.stopServer(this);
    }

}
