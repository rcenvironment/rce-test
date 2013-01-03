/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.transport.spi;

import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;

/**
 * Abstract base class for the {@link NetworkConnection} implementations of network transports.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractNetworkConnection implements NetworkConnection {

    protected ServerContactPoint associatedSCP;

    private NetworkNodeInformation remoteNodeInformation;

    private boolean initiatedByRemote = false;

    private String connectionId;

    @Override
    public NetworkNodeInformation getRemoteNodeInformation() {
        return remoteNodeInformation;
    }

    @Override
    public void setRemoteNodeInformation(NetworkNodeInformation nodeInformation) {
        this.remoteNodeInformation = nodeInformation;
    }

    @Override
    public void setAssociatedSCP(ServerContactPoint networkContactPoint) {
        this.associatedSCP = networkContactPoint;
    }

    @Override
    public boolean getInitiatedByRemote() {
        return initiatedByRemote;
    }

    @Override
    public void setInitiatedByRemote(boolean value) {
        this.initiatedByRemote = value;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public void setConnectionId(String id) {
        if (connectionId != null) {
            throw new IllegalArgumentException("Duplicate id assignment");
        }
        connectionId = id;
    }

    @Override
    public String toString() {
        // TODO improve
        return String.format("Connection %s (inv=%s)", connectionId, initiatedByRemote);
    }
}
