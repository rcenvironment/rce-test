/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.transport.virtual;

import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.NetworkConnectionIdFactory;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * A JVM-internal pseudo transport intended for unit testing.
 * 
 * @author Robert Mischke
 */
public class VirtualNetworkTransportProvider implements NetworkTransportProvider {

    /**
     * The transport id of this provider.
     */
    public static final String TRANSPORT_ID = "virtual";

    private Map<NetworkContactPoint, ServerContactPoint> virtualServices =
        new HashMap<NetworkContactPoint, ServerContactPoint>();

    private Map<NodeIdentifier, NetworkConnectionEndpointHandler> remoteInitiatedConnectionEndpointHandlerMap =
        new HashMap<NodeIdentifier, NetworkConnectionEndpointHandler>();

    private boolean supportRemoteInitiatedConnections;

    private NetworkConnectionIdFactory connectionIdFactory;

    /**
     * Constructor.
     * 
     * @param supportRemoteInitiatedConnections whether the transport should simulate support for
     *        passive/inverse connections or not
     */
    public VirtualNetworkTransportProvider(boolean supportRemoteInitiatedConnections, NetworkConnectionIdFactory connectionIdFactory) {
        this.supportRemoteInitiatedConnections = supportRemoteInitiatedConnections;
        this.connectionIdFactory = connectionIdFactory;
    }

    @Override
    public String getTransportId() {
        return TRANSPORT_ID;
    }

    @Override
    public synchronized NetworkConnection connect(NetworkContactPoint ncp, NetworkNodeInformation initiatingNodeInformation,
        boolean allowDuplex, NetworkConnectionEndpointHandler initiatingEndpointHandler, BrokenConnectionListener brokenConnectionListener)
        throws CommunicationException {
        // FIXME handle case of no matching server instance; causes a NPE in current implementation
        ServerContactPoint receivingSCP = virtualServices.get(ncp);
        if (!receivingSCP.isAcceptingMessages()) {
            // remote server was shut down or is simulating a crash
            throw new CommunicationException("Failed to open connection: Remote SCP is not accepting messages");
        }
        NetworkConnectionEndpointHandler receivingEndpointHandler = receivingSCP.getEndpointHandler();

        NetworkConnection activeConnection =
            new VirtualNetworkConnection(initiatingNodeInformation, receivingEndpointHandler, receivingSCP);
        NetworkNodeInformation receivingNodeInformation = receivingEndpointHandler.exchangeNodeInformation(initiatingNodeInformation);
        activeConnection.setRemoteNodeInformation(receivingNodeInformation);
        activeConnection.setConnectionId(connectionIdFactory.generateId(true));

        // TODO use brokenConnectionListener

        if (allowDuplex && supportRemoteInitiatedConnections) {
            NetworkConnection passiveConnection =
                new VirtualNetworkConnection(receivingNodeInformation, initiatingEndpointHandler, receivingSCP);
            passiveConnection.setRemoteNodeInformation(initiatingNodeInformation);
            passiveConnection.setConnectionId(connectionIdFactory.generateId(false));
            passiveConnection.setInitiatedByRemote(true);
            receivingEndpointHandler.onRemoteInitiatedConnectionEstablished(passiveConnection, receivingSCP);
        }

        return activeConnection;
    }

    @Override
    public boolean supportsRemoteInitiatedConnections() {
        return supportRemoteInitiatedConnections;
    }

    @Override
    public synchronized void startServer(ServerContactPoint scp) {
        // TODO naive implementation; check for collisions etc.
        virtualServices.put(scp.getNetworkContactPoint(), scp);
        scp.setAcceptingMessages(true);
    }

    @Override
    public synchronized void stopServer(ServerContactPoint scp) {
        scp.setAcceptingMessages(false);
        ServerContactPoint removed = virtualServices.remove(scp.getNetworkContactPoint());
        if (removed == null) {
            throw new IllegalStateException("No matching SCP registered: " + scp);
        }
    }
}
