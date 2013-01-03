/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.testutils;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.rcenvironment.core.communication.configuration.NodeConfigurationService;
import de.rcenvironment.core.communication.connection.NetworkConnectionListener;
import de.rcenvironment.core.communication.connection.NetworkTrafficListener;
import de.rcenvironment.core.communication.connection.internal.NetworkConnectionServiceImpl;
import de.rcenvironment.core.communication.management.internal.CommunicationManagementServiceImpl;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.routing.internal.NetworkFormatter;
import de.rcenvironment.core.communication.routing.internal.NetworkRoute;
import de.rcenvironment.core.communication.routing.internal.NetworkRoutingServiceImpl;
import de.rcenvironment.core.communication.routing.internal.TopologyMap;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Provides a simulated/"virtual" node instance. Intended for use in integration testing; a major
 * use case is setting up networks of virtual nodes to test network dynamics and routing behavior.
 * 
 * @author Robert Mischke
 */
public class VirtualInstance extends VirtualInstanceSkeleton implements CommonVirtualInstanceControl {

    private static volatile boolean rememberRuntimePeersAfterRestart;

    private CommunicationManagementServiceImpl managementService;

    private NetworkRoutingServiceImpl routingService;

    private NetworkConnectionServiceImpl connectionService;

    /**
     * Creates a virtual instance with the given log/display name.
     * 
     * @param logName the log/display name to use
     */
    public VirtualInstance(String id, String logName) {
        super(id, logName);
        // get configuration service stub
        NodeConfigurationService configurationService = getConfigurationServiceStub();
        // create test request handler
        TestStringRequestPayloadHandler requestPayloadHandler = new TestStringRequestPayloadHandler(configurationService.getLocalNodeId());
        // create, link & start connection service
        connectionService = new NetworkConnectionServiceImpl();
        connectionService.bindNodeConfigurationService(configurationService);
        connectionService.bindRequestPayloadHandler(requestPayloadHandler);
        connectionService.activate();
        // create, link & start routing service
        routingService = new NetworkRoutingServiceImpl();
        routingService.bindNetworkConnectionService(connectionService);
        routingService.bindNodeConfigurationService(configurationService);
        routingService.activate();
        // create, link & start management service
        managementService = new CommunicationManagementServiceImpl();
        managementService.bindNetworkConnectionService(connectionService);
        managementService.bindNetworkRoutingService(routingService);
        managementService.bindNodeConfigurationService(configurationService);
        managementService.activate();
    }

    public static void setRememberRuntimePeersAfterRestarts(boolean rememberRuntimePeers) {
        VirtualInstance.rememberRuntimePeersAfterRestart = rememberRuntimePeers;
    }

    /**
     * Convenience method to send a payload to another node.
     * 
     * @param messageContent the request payload
     * @param targetNodeId the id of the destination node
     * @return a {@link Future} providing the response
     * @throws CommunicationException on messaging errors
     * @throws InterruptedException on interruption
     * @throws ExecutionException on internal errors
     * @throws SerializationException on serialization failure
     */
    public Future<NetworkResponse> performRoutedRequest(Serializable messageContent, NodeIdentifier targetNodeId)
        throws CommunicationException, InterruptedException, ExecutionException, SerializationException {
        return getRoutingService().performRoutedRequest(messageContent, targetNodeId);
    }

    /**
     * Convenience method to send a payload to another node. This method blocks until it has
     * received a response, or the timeout was exceeded.
     * 
     * @param messageContent the request payload
     * @param targetNodeId the id of the destination node
     * @param timeoutMsec the maximum time to wait for the response
     * @return the response
     * @throws CommunicationException on messaging errors
     * @throws InterruptedException on interruption
     * @throws TimeoutException on timeout
     * @throws ExecutionException on internal errors
     * @throws SerializationException on serialization failure
     */
    public NetworkResponse performRoutedRequest(Serializable messageContent, NodeIdentifier targetNodeId, int timeoutMsec)
        throws CommunicationException, InterruptedException, ExecutionException, TimeoutException, SerializationException {
        return performRoutedRequest(messageContent, targetNodeId).get(timeoutMsec, TimeUnit.MILLISECONDS);
    }

    @Override
    public void addNetworkConnectionListener(NetworkConnectionListener listener) {
        getConnectionService().addConnectionListener(listener);
    }

    @Override
    public void addNetworkTrafficListener(NetworkTrafficListener listener) {
        getConnectionService().addTrafficListener(listener);
    }

    @Override
    public synchronized void addInitialNetworkPeer(NetworkContactPoint contactPoint) {
        VirtualInstanceState currentState = getCurrentState();
        if (currentState == VirtualInstanceState.STARTED) {
            // FIXME transitional code; rewrite calls
            log.warn("addInitialNetworkPeer() called for an instance in the STARTED state; change to addRuntimeNetworkPeer()");
            addRuntimeNetworkPeer(contactPoint);
            return;
        }
        if (currentState != VirtualInstanceState.INITIAL) {
            throw new IllegalStateException("Initial peers can only be added in the INITIAL state");
        }
        super.addInitialNetworkPeer(contactPoint);
    }

    /**
     * Adds and connects to a {@link NetworkContactPoint}.
     * 
     * @param contactPoint the {@link NetworkContactPoint} to add and connect to
     */
    public synchronized void addRuntimeNetworkPeer(NetworkContactPoint contactPoint) {
        if (getCurrentState() != VirtualInstanceState.STARTED) {
            throw new IllegalStateException("Runtime peers can only be added in the STARTED state (is " + getCurrentState() + ")");
        }
        if (rememberRuntimePeersAfterRestart) {
            // add this as an initial peer for next network startup
            super.addInitialNetworkPeer(contactPoint);
        }
        managementService.asyncConnectToNetworkPeer(contactPoint);
    }

    public String getFormattedNetworkGraph() {
        return NetworkFormatter.summary(getRoutingService().getProtocolManager().getTopologyMap());
    }

    public String getFormattedNetworkStats() {
        return NetworkFormatter.networkStats(getRoutingService().getProtocolManager().getNetworkStats());
    }

    /**
     * Test method: verifies same reporded topology hashes for all known nodes.
     * 
     * TODO verify description
     * 
     * @return true if all hashes are consistent
     */
    public boolean hasSameTopologyHashesForAllNodes() {
        return getRoutingService().getProtocolManager().getTopologyMap().hasSameTopologyHashesForAllNodes();
    }

    /**
     * Test method: determine the messaging route to the given node.
     * 
     * @param destination the destination node
     * @return the calculated {@link NetworkRoute}
     */
    public NetworkRoute getRouteTo(VirtualInstance destination) {
        return getRoutingService().getProtocolManager().getRouteTo(destination.getConfigurationService().getLocalNodeId());
    }

    /**
     * Test method: returns the internal {@link TopologyMap}.
     * 
     * @return the internal topology map
     */
    public TopologyMap getTopologyMap() {
        return getRoutingService().getProtocolManager().getTopologyMap();
    }

    /**
     * Test method; probably obsolete.
     * 
     * @param messageId a message id
     * @return whether this message was received (?)
     */
    @Deprecated
    public boolean checkMessageReceivedById(String messageId) {
        return getRoutingService().getProtocolManager().messageReivedById(messageId);
    }

    /**
     * Test method; probably obsolete.
     * 
     * @param messageContent a message content
     * @return whether this content was received (?)
     */
    @Deprecated
    // check the response received by the initiating node instead
    public boolean checkMessageReceivedByContent(Serializable messageContent) {
        return getRoutingService().getProtocolManager().messageReivedByContent(messageContent);
    }

    @Override
    public void registerNetworkTransportProvider(NetworkTransportProvider provider) {
        connectionService.addNetworkTransportProvider(provider);
    }

    /**
     * Provide unit/integration test access to the management service.
     * 
     * @return The management service.
     */
    public CommunicationManagementServiceImpl getManagementService() {
        return managementService;
    }

    /**
     * Provide unit/integration test access to the routing service.
     * 
     * @return The routing service.
     */
    public NetworkRoutingServiceImpl getRoutingService() {
        return routingService;
    }

    /**
     * Provide unit/integration test access to the connection service.
     * 
     * @return The connection service.
     */
    public NetworkConnectionServiceImpl getConnectionService() {
        return connectionService;
    }

    /**
     * @return The configuration service.
     */
    public NodeConfigurationService getConfigurationService() {
        return super.getConfigurationServiceStub();
    }

    @Override
    protected void performStartup() throws InterruptedException, CommunicationException {
        managementService.startUpNetwork();
    }

    @Override
    protected void performShutdown() throws InterruptedException {
        managementService.shutDownNetwork();
    }

    @Override
    protected void performSimulatedCrash() throws InterruptedException {
        // simply shut down the network; this should not send any "goodbye" messages etc.
        managementService.simulateUncleanShutdown();
    }

    /**
     * @return the node identifier of this virtual instance
     */
    public NodeIdentifier getNodeId() {
        return nodeInformation.getWrappedNodeId();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return getNodeId().toString();
    }
}
