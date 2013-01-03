/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.testutils;

import java.util.ArrayList;
import java.util.List;

import de.rcenvironment.core.communication.configuration.NodeConfigurationService;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Replacement {@link NodeConfigurationService} for {@link VirtualInstance} integrations tests.
 * Defines the configuration of {@link VirtualInstance}s.
 * 
 * @author Robert Mischke
 */
public class NodeConfigurationServiceTestStub implements NodeConfigurationService {

    private static final long TEST_INSTANCES_INITIAL_CONNECT_DELAY_MSEC = 300;

    private final NodeIdentifier localNodeId;

    private final NetworkNodeInformationImpl localNodeInformation;

    private final List<NetworkContactPoint> serverContactPoints;

    private final List<NetworkContactPoint> initialNetworkPeers;

    public NodeConfigurationServiceTestStub(String nodeId, String displayName) {
        localNodeId = PlatformIdentifierFactory.fromNodeId(nodeId);
        localNodeInformation = new NetworkNodeInformationImpl(localNodeId);
        localNodeInformation.setDisplayName(displayName);
        serverContactPoints = new ArrayList<NetworkContactPoint>();
        initialNetworkPeers = new ArrayList<NetworkContactPoint>();
    }

    @Override
    public NodeIdentifier getLocalNodeId() {
        return localNodeId;
    }

    @Override
    public NetworkNodeInformation getLocalNodeInformation() {
        return localNodeInformation;
    }

    @Override
    public List<NetworkContactPoint> getServerContactPoints() {
        return serverContactPoints;
    }

    @Override
    public List<NetworkContactPoint> getInitialNetworkContactPoints() {
        return initialNetworkPeers;
    }

    @Override
    public long getDelayBeforeStartupConnectAttempts() {
        return TEST_INSTANCES_INITIAL_CONNECT_DELAY_MSEC;
    }

    @Override
    public int getRequestTimeoutMsec() {
        // use the default value for tests, too; can be changed if useful
        return CommunicationConfiguration.DEFAULT_REQUEST_TIMEOUT_MSEC;
    }

    @Override
    public long getForwardingTimeoutMsec() {
        // use the default value for tests, too; can be changed if useful
        return CommunicationConfiguration.DEFAULT_FORWARDING_TIMEOUT_MSEC;
    }

    /**
     * Adds a server ("provided") {@link NetworkContactPoint}.
     * 
     * @param contactPoint the server {@link NetworkContactPoint} to add
     */
    public void addServerConfigurationEntry(NetworkContactPoint contactPoint) {
        serverContactPoints.add(contactPoint);
    }

    /**
     * Adds an initial neighbor ("remote") {@link NetworkContactPoint}.
     * 
     * @param contactPoint the server {@link NetworkContactPoint} to add
     */
    public void addInitialNetworkPeer(NetworkContactPoint contactPoint) {
        initialNetworkPeers.add(contactPoint);
    }

}
