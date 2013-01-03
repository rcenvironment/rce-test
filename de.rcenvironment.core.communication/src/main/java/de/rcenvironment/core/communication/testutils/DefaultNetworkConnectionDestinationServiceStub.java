/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.testutils;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;

/**
 * Service interface for methods that are usually required by the receiving end of a network
 * connection. Depending on the transport implementation, these methods may be provided as remote
 * services.
 * 
 * @author Robert Mischke
 */
public class DefaultNetworkConnectionDestinationServiceStub implements NetworkConnectionEndpointHandler {

    @Override
    public NetworkNodeInformation exchangeNodeInformation(NetworkNodeInformation nodeInformation) {
        return null;
    }

    @Override
    public void onRemoteInitiatedConnectionEstablished(NetworkConnection connection, ServerContactPoint serverContactPoint) {}

    @Override
    public void onIncomingConnectionClosed(NetworkConnection connection, ServerContactPoint serverContactPoint) {}

    @Override
    public NetworkResponse onRawRequestReceived(NetworkRequest request, NodeIdentifier sourceId) {
        return null;
    }

    @Override
    public NetworkResponse onRequestArrivedAtDestination(NetworkRequest request) {
        return null;
    }
}
