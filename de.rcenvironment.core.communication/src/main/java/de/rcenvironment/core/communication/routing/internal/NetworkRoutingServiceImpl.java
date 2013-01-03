/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.configuration.NodeConfigurationService;
import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.NetworkConnectionListener;
import de.rcenvironment.core.communication.connection.NetworkConnectionService;
import de.rcenvironment.core.communication.connection.NetworkRequestHandler;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.impl.NetworkRequestImpl;
import de.rcenvironment.core.communication.model.impl.NetworkResponseImpl;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.routing.NetworkRoutingService;
import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A implementation of the {@link NetworkRoutingService} interface.
 * 
 * @author Phillip Kroll
 * @author Robert Mischke
 */
public class NetworkRoutingServiceImpl implements NetworkRoutingService {

    /**
     * Implementation of the listener interface as inner class.
     * 
     * @author Phillip Kroll
     */
    private class NetworkConnectionListenerAdapter implements NetworkConnectionListener {

        @Override
        public void onOutgoingConnectionEstablished(NetworkConnection connection) {}

        @Override
        public void onOutgoingConnectionTerminated(NetworkConnection connection) {}
    }

    /**
     * Initial listener for topology changes. This is the only listener that receives change events
     * from the routing layer, and delegates these events to external listeners. Each callback to an
     * external listener is performed in a separate thread to prevent blocking listeners from
     * affecting the calling code.
     * 
     * @author Robert Mischke
     */
    private class NetworkTopologyChangeListenerAdapter implements NetworkTopologyChangeListener {

        public void onNetworkTopologyChanged() {
            TopologyMap topologyMap = protocolManager.getTopologyMap();
            log.debug(String.format("Topology change detected; " + ownNodeInformation.getWrappedNodeId()
                + " is now aware of %d node(s), %d connection(s)",
                topologyMap.getNodeCount(), topologyMap.getLinkCount()));
            synchronized (topologyChangeListeners) {
                for (final NetworkTopologyChangeListener listener : topologyChangeListeners) {
                    // decouple from listeners by creating an asynchronous task for each
                    threadPool.execute(new Runnable() {

                        @Override
                        @TaskDescription("Topology change callback")
                        public void run() {
                            listener.onNetworkTopologyChanged();
                        }
                    });
                }
            }
        }
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @author krol_ph
     */
    private class NetworkRequestHandlerAdapter implements NetworkRequestHandler {

        @Override
        public boolean isApplicable(NetworkRequest request) {
            return MetaDataWrapper.createRouting().matches(request.accessRawMetaData());
        }

        @Override
        public NetworkResponse handleRequest(NetworkRequest request, NodeIdentifier sourceId) {

            // TODO check & probably remove; was used for old test code
            // String messageId = MetaDataWrapper.wrap(request.accessRawMetaData()).getMessageId();
            // protocolManager.addToMessageBuffer(messageId, request.getDeserializedContent());

            // Is the message a link state advertisement?
            if (MetaDataWrapper.createLsaMessage().matches(request.accessRawMetaData())) {
                // let the protocol handle the incoming LSA
                Serializable deserializedContent;
                try {
                    deserializedContent = request.getDeserializedContent();
                } catch (SerializationException e) {
                    log.error("Failed to deserialize incoming LSA", e);
                    return NetworkResponseFactory.generateExceptionAtDestinationResponse(request, e);
                }
                Serializable optionalLsaResponse =
                    protocolManager.handleLinkStateAdvertisement(deserializedContent, request.accessRawMetaData());
                byte[] optionalLsaBytes = MessageUtils.serializeSafeObject(optionalLsaResponse);
                return new NetworkResponseImpl(optionalLsaBytes, request.getRequestId(), NetworkResponse.RESULT_CODE_SUCCESS);
            }

            // Is the message a routed message?
            if (MetaDataWrapper.createRouted().matches(request.accessRawMetaData())) {
                return handleRoutedRequest(request);
            }

            log.error("Message matched no handler path: " + request.accessRawMetaData());

            return null;
        }

    }

    /**
     * Request handler for incoming connection health checks.
     * 
     * @author Robert Mischke
     */
    private class HealthCheckRequestHandler implements NetworkRequestHandler {

        private final MetaDataWrapper healthCheckMetadata = MetaDataWrapper.createHealthCheckMetadata();

        @Override
        public boolean isApplicable(NetworkRequest request) {
            return healthCheckMetadata.matches(request.accessRawMetaData());
        }

        @Override
        public NetworkResponse handleRequest(NetworkRequest request, NodeIdentifier lastHopNodeId) {
            // send back content token
            return NetworkResponseFactory.generateSuccessResponse(request, request.getContentBytes());
        }

    }

    private static final boolean VERBOSE_LOGGING = false;

    private NetworkNodeInformation ownNodeInformation;

    private NetworkConnectionService connectionService;

    private final Log log = LogFactory.getLog(getClass());

    private NodeConfigurationService configurationService;

    private LinkStateRoutingProtocolManager protocolManager;

    private SharedThreadPool threadPool = SharedThreadPool.getInstance();

    private NetworkConnectionEndpointHandler connectionEndpointHandler;

    private String ownNodeId;

    private final List<NetworkTopologyChangeListener> topologyChangeListeners = new ArrayList<NetworkTopologyChangeListener>();

    /**
     * This is a new routing implementation that uses direct (blocking) responses instead of
     * asynchronous confirmation messages.
     * 
     * @param messageContent The message content
     * @param receiver The
     * @return the {@link Future} for fetching the {@link NetworkResponse} from
     * @throws SerializationException on serialization failure
     */
    @Override
    public Future<NetworkResponse> performRoutedRequest(final Serializable messageContent, final NodeIdentifier receiver)
        throws SerializationException {

        Map<String, String> metaData =
            MetaDataWrapper.createEmpty().setTopicRouted().setCategoryRouting().setTypeMessage()
                .setSender(ownNodeInformation.getWrappedNodeId()).setReceiver(receiver).addTraceItem(ownNodeId).getInnerMap();

        return sendToNextHop(MessageUtils.serializeObject(messageContent), metaData, receiver);
    }

    @Override
    public Set<PlatformIdentifier> getReachableNodes(boolean restrictToWorkflowHostsAndSelf) {
        TopologyMap topologyMap = protocolManager.getTopologyMap();
        return topologyMap.getIdsOfReachableNodes(restrictToWorkflowHostsAndSelf);
    }

    /**
     * TODO Restrict method visibility.
     * 
     * @return Returns the protocol.
     */
    public LinkStateRoutingProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param service The network connection service.
     */
    public void bindNetworkConnectionService(NetworkConnectionService service) {
        // do not allow rebinding for now
        if (this.connectionService != null) {
            throw new IllegalStateException();
        }
        this.connectionService = service;
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param service The configuration service.
     */
    public void bindNodeConfigurationService(NodeConfigurationService service) {
        // do not allow rebinding for now
        if (this.configurationService != null) {
            throw new IllegalStateException();
        }
        this.configurationService = service;
    }
    
    /**
     * TODO krol_ph: Enter comment!
     * 
     * @return The connection listener.
     */
    public NetworkConnectionListener createConnectionListener() {
        return new NetworkConnectionListenerAdapter();
    }

    /**
     * OSGi activate method.
     */
    public void activate() {
        ownNodeInformation = configurationService.getLocalNodeInformation();
        // TODO link these services in management service instead? -- misc_ro
        connectionService.addConnectionListener(this.createConnectionListener());
        connectionService.addRequestHandler(new NetworkRequestHandlerAdapter());
        connectionService.addRequestHandler(new HealthCheckRequestHandler());
        connectionEndpointHandler = connectionService.getConnectionEndpointHandler();
        ownNodeId = ownNodeInformation.getWrappedNodeId().getNodeId();
        protocolManager = new LinkStateRoutingProtocolManager(ownNodeInformation, connectionService);
        protocolManager.setTopologyChangeListener(new NetworkTopologyChangeListenerAdapter());
    }

    /**
     * TODO Enter comment!
     * 
     */
    public void deactivate() {
        // nothing so far
    }

    /**
     * When a node receives a message that is "tagged" as a routed/forwarded message, this method
     * can be used to handle the message. If the current node is not the destination node it
     * forwards the message to the next node on the route. Otherwise a confirmation is returned to
     * the sender and the method returns <code>true</code>
     * 
     * @param request The network request
     * @return The future of the network response.
     */
    private NetworkResponse handleRoutedRequest(NetworkRequest request) {
        MetaDataWrapper wrapper = MetaDataWrapper.wrap(request.accessRawMetaData());
        if (wrapper.getReceiver().getNodeId().equals(ownNodeId)) {
            // handle locally
            return connectionEndpointHandler.onRequestArrivedAtDestination(request);
        } else {
            // forward
            // TODO check TTL here
            wrapper = MetaDataWrapper.cloneAndWrap(request.accessRawMetaData());
            wrapper.incHopCount();
            NetworkResponse response = null;
            // TODO this blocks a thread for each forwarded request; improve in future version
            Future<NetworkResponse> responseFuture =
                sendToNextHop(request.getContentBytes(), wrapper.getInnerMap(), wrapper.getReceiver());
            try {
                response = responseFuture.get(configurationService.getForwardingTimeoutMsec(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn(String.format("Timeout while forwarding message from %s to %s at %s (ReqId=%s)", wrapper.getSender()
                    .getNodeId(), wrapper.getReceiver().getNodeId(), ownNodeId, request.getRequestId()));
                response = NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, ownNodeId, e);
            } catch (InterruptedException e) {
                log.warn(String.format("Interrupted while forwarding message from %s to %s at %s (ReqId=%s)", wrapper.getSender()
                    .getNodeId(), wrapper.getReceiver().getNodeId(), ownNodeId, request.getRequestId()), e);
                response = NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, ownNodeId, e);
            } catch (ExecutionException e) {
                log.warn(
                    String.format("Error while forwarding message from %s to %s at %s (ReqId=%s)", wrapper.getSender().getNodeId(),
                        wrapper.getReceiver().getNodeId(), ownNodeId, request.getRequestId()), e);
                response = NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, ownNodeId, e);
            }
            if (response == null) {
                log.warn(
                    String.format("NULL response after forwarding message from %s to %s at %s (ReqId=%s)", wrapper.getSender().getNodeId(),
                        wrapper.getReceiver().getNodeId(), ownNodeId, request.getRequestId()));
            }
            return response;
        }
    }

    private Future<NetworkResponse> sendToNextHop(final byte[] messageBytes, Map<String, String> metaData,
        final NodeIdentifier receiver) {

        // TODO move routing into Callable for faster return of caller thread? -- misc_ro
        // try to find a route
        NetworkRoute route = protocolManager.getRouteTo(receiver);
        if (!route.validate()) {
            // create exception
            final CommunicationException cause =
                new CommunicationException(String.format("Found no route towards '%s' at '%s'", receiver,
                    ownNodeInformation.getWrappedNodeId()));
            // convert to Future containing failure response
            final NetworkRequest request = new NetworkRequestImpl(messageBytes, metaData);
            return threadPool.submit(new Callable<NetworkResponse>() {

                public NetworkResponse call() throws Exception {
                    return NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, ownNodeId, cause);
                };
            });
        }

        TopologyLink linkToNextHop = route.getFirstLink();
        if (VERBOSE_LOGGING) {
            log.debug(String.format("Sending routed message for '%s' towards '%s' via link '%s'",
                receiver, linkToNextHop.getDestination(), linkToNextHop.getConnectionId()));
        }

        WaitForResponseCallable responseCallable = new WaitForResponseCallable();
        protocolManager.sendTowardsNeighbor(messageBytes, metaData, linkToNextHop, responseCallable);
        return threadPool.submit(responseCallable);

        // TODO restore routing retry? (on higher call level?)

        // // if there is a route, use it
        // int routeRetries = 0;
        // while (route.validate()) {
        //
        // // forward message content to next network contact point on the route
        // WaitForResponseCallable responseCallable = new WaitForResponseCallable();
        // if (protocolManager.sendTowardsNeighbor(messageContent, metaData, route.getFirstLink(),
        // responseCallable)) {
        // return executorService.submit(responseCallable);
        // } else {
        // routeRetries++;
        // // TODO make limit a constant
        // if (routeRetries >= 3) {
        // break;
        // }
        // // try to get a new route.
        // // TODO add retry limit? -- misc_ro
        // route = protocolManager.getRouteTo(receiver);
        // }
        // }
        // throw new CommunicationException(String.format("'%s' could not find a route to '%s'.",
        // ownNodeId, receiver));
    }

    /**
     * Adds a new {@link NetworkTopologyChangeListener}. This method is not part of the service
     * interface; it is only meant to be used via OSGi-DS (whiteboard pattern) and integration
     * tests.
     * 
     * @param listener the listener
     */
    public void addNetworkTopologyChangeListener(NetworkTopologyChangeListener listener) {
        synchronized (topologyChangeListeners) {
            topologyChangeListeners.add(listener);
        }
    }

    /**
     * Removes a {@link NetworkTopologyChangeListener}. This method is not part of the service
     * interface; it is only meant to be used via OSGi-DS (whiteboard pattern) and integration
     * tests.
     * 
     * @param listener the listener
     */
    public void removeNetworkTopologyChangeListener(NetworkTopologyChangeListener listener) {
        synchronized (topologyChangeListeners) {
            topologyChangeListeners.remove(listener);
        }
    }

    @Override
    public void announceShutdown() {
        log.debug("Announcing shutdown to network peers");
        try {
            getProtocolManager().announceShutdown();
        } catch (CommunicationException e) {
            log.warn("Exception while announcing shutdown to network peers", e);
        }
    }

    @Override
    public String getNetworkSummary() {
        return NetworkFormatter.summary(protocolManager.getTopologyMap());
    }
}
