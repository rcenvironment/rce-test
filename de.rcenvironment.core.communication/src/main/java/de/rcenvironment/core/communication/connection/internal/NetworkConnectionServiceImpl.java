/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
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
import de.rcenvironment.core.communication.connection.NetworkTrafficListener;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NetworkResponseHandler;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.RawNetworkResponseHandler;
import de.rcenvironment.core.communication.model.impl.NetworkRequestImpl;
import de.rcenvironment.core.communication.model.impl.NetworkResponseImpl;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.routing.internal.NetworkFormatter;
import de.rcenvironment.core.communication.routing.internal.WaitForResponseCallable;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Default implementation of {@link NetworkConnectionService} which also provides the
 * {@link NetworkConnectionEndpointHandler} interface.
 * 
 * @author Robert Mischke
 */
public class NetworkConnectionServiceImpl implements NetworkConnectionService {

    private static final boolean VERBOSE_LOGGING = false;

    /**
     * String constant to satisfy CheckStyle; alternatively, introduce several String.format()
     * calls.
     */
    private static final String SINGLE_QUOTE = "'";

    private NetworkNodeInformation ownNodeInformation;

    private Map<String, NetworkTransportProvider> transportProviders;

    private List<NetworkConnectionListener> connectionListeners;

    private List<NetworkTrafficListener> trafficListeners;

    private NodeInformationRegistryImpl nodeInformationRegistry;

    private List<NetworkRequestHandler> requestHandlerChain;

    private final Log logger = LogFactory.getLog(getClass());

    private SharedThreadPool threadPool = SharedThreadPool.getInstance();

    private NodeConfigurationService configurationService;

    private NetworkConnectionEndpointHandlerImpl connectionEndpointHandler;

    private final BrokenConnectionListenerImpl brokenConnectionListener;

    private RequestPayloadHandler requestPayloadHandler;

    private final Set<NetworkConnection> outgoingConnections;

    private final Map<NetworkConnection, ConnectionState> connectionStates = Collections
        .synchronizedMap(new WeakHashMap<NetworkConnection, ConnectionState>());

    private final Random random = new Random();

    private final MetaDataWrapper healthCheckMetadata = MetaDataWrapper.createHealthCheckMetadata();

    /**
     * Main implementation of {@link NetworkConnectionEndpointHandler}.
     * 
     * @author Robert Mischke
     */
    private class NetworkConnectionEndpointHandlerImpl implements NetworkConnectionEndpointHandler {

        @Override
        public NetworkNodeInformation exchangeNodeInformation(NetworkNodeInformation senderNodeInformation) {
            // logger.debug(String.format("Incoming connection from node '%s' (%s); sending identification '%s' (%s)",
            // senderNodeInformation.getLogName(), senderNodeInformation.getNodeIdentifier(),
            // ownNodeInformation.getLogName(),
            // ownNodeInformation.getNodeIdentifier()));
            nodeInformationRegistry.updateFrom(senderNodeInformation);
            return ownNodeInformation;
        }

        @Override
        public void onRemoteInitiatedConnectionEstablished(NetworkConnection connection, ServerContactPoint serverContactPoint) {
            if (!connection.getInitiatedByRemote()) {
                throw new IllegalStateException("Consistency error");
            }
            registerNewOutgoingConnection(connection);
            logger.debug(String.format("Remote-initiated connection '%s' established from '%s' to '%s' via local SCP %s",
                connection, ownNodeInformation.getLogDescription(), connection.getRemoteNodeInformation().getLogDescription(),
                serverContactPoint));
        }

        @Override
        public void onIncomingConnectionClosed(NetworkConnection connection, ServerContactPoint serverContactPoint) {
            // TODO this is not being called yet
            // TODO check related outgoing connections here?
        }

        @Override
        public NetworkResponse onRawRequestReceived(NetworkRequest request, NodeIdentifier sourceId) {

            // send "request received" event to listeners
            // TODO potential synchronization bottleneck; currently, only test listeners exist
            synchronized (connectionListeners) {
                for (NetworkTrafficListener listener : trafficListeners) {
                    listener.onRequestReceived(request, sourceId);
                }
            }

            // find matching handler
            // TODO optimize by using a map?
            NetworkRequestHandler matchingHandler = null;
            synchronized (requestHandlerChain) {
                for (NetworkRequestHandler handler : requestHandlerChain) {
                    if (handler.isApplicable(request)) {
                        matchingHandler = handler;
                        break;
                    }
                }
            }
            // actually *handle* the message outside the synchronized block
            NetworkResponse response = null;
            if (matchingHandler != null) {
                try {
                    response = matchingHandler.handleRequest(request, sourceId);
                } catch (RuntimeException e) {
                    // TODO improve: "while routing" is not really appropriate, as it might
                    // be a local request as well -- misc_ro
                    response = NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, ownNodeInformation.getNodeId(), e);
                }
            } else {
                Serializable loggableContent;
                try {
                    loggableContent = request.getDeserializedContent();
                } catch (SerializationException e) {
                    // used for logging only
                    loggableContent = "Failed to deserialize content: " + e;
                }
                logger.warn("No request handler matched for request id '" + request.getRequestId()
                    + "', generating failure response; string representation of request: "
                    + NetworkFormatter.message(loggableContent, request.accessRawMetaData()));

                response = new NetworkResponseImpl(null, request.getRequestId(), NetworkResponse.RESULT_CODE_NO_MATCHING_HANDLER);
            }

            // send "response generated" event to listeners
            // TODO potential synchronization bottleneck; currently, only test listeners exist
            synchronized (trafficListeners) {
                for (NetworkTrafficListener listener : trafficListeners) {
                    listener.onResponseGenerated(response, request, sourceId);
                }
            }

            return response;
        }

        @Override
        public NetworkResponse onRequestArrivedAtDestination(NetworkRequest request) {
            NetworkResponse result;
            try {
                return requestPayloadHandler.handleRequest(request);
            } catch (RuntimeException e) {
                result = NetworkResponseFactory.generateExceptionAtDestinationResponse(request, e);
            } catch (CommunicationException e) {
                result = NetworkResponseFactory.generateExceptionAtDestinationResponse(request, e);
            } catch (SerializationException e) {
                result = NetworkResponseFactory.generateExceptionAtDestinationResponse(request, e);
            }
            return result;
        }

    }

    /**
     * Listener interface for unexpected connection failures.
     * 
     * @author Robert Mischke
     */
    private class BrokenConnectionListenerImpl implements BrokenConnectionListener {

        @Override
        public void onConnectionBroken(NetworkConnection connection) {
            if (connection.getInitiatedByRemote()) {
                logger.warn("onConnectionBroken called for remote-initiated connection " + connection.getConnectionId() + "; ignoring");
                return;
            }
            handleBrokenOutgoingConnection(connection);
        }

    }

    /**
     * Internal status information for a single connection.
     * 
     * @author Robert Mischke
     */
    private static final class ConnectionState {

        private int healthCheckFailureCount;

        /**
         * Lock object to prevent concurrent active health checks.
         */
        private final Object healthCheckInProgressLock = new Object();

    }

    /**
     * Default constructor.
     */
    public NetworkConnectionServiceImpl() {
        this.transportProviders = new HashMap<String, NetworkTransportProvider>();
        this.connectionListeners = new ArrayList<NetworkConnectionListener>();
        this.trafficListeners = new ArrayList<NetworkTrafficListener>();
        this.requestHandlerChain = new ArrayList<NetworkRequestHandler>();
        this.connectionEndpointHandler = new NetworkConnectionEndpointHandlerImpl();
        this.brokenConnectionListener = new BrokenConnectionListenerImpl();
        this.outgoingConnections = new HashSet<NetworkConnection>();
    }

    public NetworkConnectionEndpointHandler getConnectionEndpointHandler() {
        return connectionEndpointHandler;
    }

    @Override
    public Future<NetworkConnection> connect(final NetworkContactPoint ncp, final boolean allowDuplex) throws CommunicationException {
        final NetworkTransportProvider transportProvider = getTransportProvider(ncp.getTransportId());
        if (transportProvider == null) {
            throw new CommunicationException("Unknown transport id: " + ncp.getTransportId());
        }
        Callable<NetworkConnection> connectTask = new Callable<NetworkConnection>() {

            @Override
            @TaskDescription("Connect to remote node (low-level task)")
            public NetworkConnection call() throws Exception {
                NetworkConnection connection;
                try {
                    connection =
                        transportProvider
                            .connect(ncp, ownNodeInformation, allowDuplex, connectionEndpointHandler, brokenConnectionListener);
                } catch (RuntimeException e) {
                    // FIXME add internal event handling of connection failures?
                    logger.error("Failed to connect to " + ncp + " (local node: " + ownNodeInformation.getLogDescription() + ")", e);
                    throw e;
                }
                // on success
                NetworkNodeInformation remoteNodeInformation = connection.getRemoteNodeInformation();
                nodeInformationRegistry.updateFrom(remoteNodeInformation);
                registerNewOutgoingConnection(connection);
                logger.debug(String.format("Connection '%s' established from '%s' to '%s' using remote NCP %s", connection,
                    ownNodeInformation.getLogDescription(), remoteNodeInformation.getLogDescription(), ncp));
                return connection;
            }
        };
        return threadPool.submit(connectTask);
    }

    @Override
    public void closeAllOutgoingConnections() {
        Set<NetworkConnection> tempCopyofSet;
        synchronized (outgoingConnections) {
            // create a copy as the original set will be modified
            tempCopyofSet = new HashSet<NetworkConnection>(outgoingConnections);
        }
        for (NetworkConnection connection : tempCopyofSet) {
            closeOutgoingConnection(connection);
        }
        // verify that all connections have been closed and deregistered
        synchronized (outgoingConnections) {
            for (NetworkConnection connection : outgoingConnections) {
                logger.warn("Connection list not empty after closing all outgoing connections: " + connection);
            }
        }
    }

    @Override
    public void sendRequest(byte[] messageBytes, Map<String, String> metaData,
        NetworkConnection connection, final NetworkResponseHandler outerResponseHandler) {

        NetworkRequestImpl request = new NetworkRequestImpl(messageBytes, metaData);

        RawNetworkResponseHandler responseHandler = new RawNetworkResponseHandler() {

            @Override
            public void onResponseAvailable(NetworkResponse response) {
                outerResponseHandler.onResponseAvailable(response);
            }

            @Override
            public void onConnectionBroken(NetworkRequest request, NetworkConnection connection) {
                handleBrokenOutgoingConnection(connection);
                // send a proper response to the caller, instead of causing a timeout
                outerResponseHandler.onResponseAvailable(NetworkResponseFactory.generateExceptionWhileRoutingResponse(request,
                    ownNodeInformation.getNodeId(), new ConnectionClosedException("Connection " + connection.getConnectionId()
                        + " was broken and has been closed by " + ownNodeInformation.getNodeId())));
            }
        };
        connection.sendRequest(request, responseHandler, configurationService.getRequestTimeoutMsec());
    }

    @Override
    public Future<NetworkResponse> sendRequest(final byte[] messageBytes,
        final Map<String, String> metaData, final NetworkConnection connection) {

        WaitForResponseCallable responseCallable = new WaitForResponseCallable();
        // responseCallable.setLogMarker(ownNodeInformation.getWrappedNodeId().getNodeId() +
        // "/sendRequest");
        sendRequest(messageBytes, metaData, connection, responseCallable);
        return threadPool.submit(responseCallable);
    }

    @Override
    public void addRequestHandler(NetworkRequestHandler handler) {
        synchronized (requestHandlerChain) {
            requestHandlerChain.add(handler);
        }
    }

    @Override
    public void addConnectionListener(NetworkConnectionListener listener) {
        synchronized (connectionListeners) {
            connectionListeners.add(listener);
        }
    }

    /**
     * Adds a {@link NetworkTrafficListener}.
     * 
     * @param listener the new listener
     */
    public void addTrafficListener(NetworkTrafficListener listener) {
        synchronized (trafficListeners) {
            trafficListeners.add(listener);
        }
    }

    @Override
    public void removeConnectionListener(NetworkConnectionListener listener) {
        // TODO implement
    }

    public List<NetworkConnectionListener> getConnectionListeners() {
        return connectionListeners;
    }

    @Override
    public ServerContactPoint startServer(NetworkContactPoint ncp) throws CommunicationException {
        NetworkTransportProvider transportProvider = getTransportProvider(ncp.getTransportId());
        ServerContactPoint scp = new ServerContactPoint(transportProvider, ncp, connectionEndpointHandler);
        scp.start();
        return scp;
    }

    @Override
    public void triggerConnectionHealthChecks() {
        synchronized (outgoingConnections) {
            for (final NetworkConnection connection : outgoingConnections) {
                threadPool.execute(new Runnable() {

                    @Override
                    @TaskDescription("Connection health check")
                    public void run() {
                        // random delay ("jitter") to avoid all connections being checked at once
                        try {
                            Thread.sleep(random.nextInt(CommunicationConfiguration.CONNECTION_HEALTH_CHECK_MAX_JITTER_MSEC));
                            ConnectionState connectionState = connectionStates.get(connection);
                            // synchronize on lock object to prevent concurrent checks
                            synchronized (connectionState.healthCheckInProgressLock) {
                                if (VERBOSE_LOGGING) {
                                    logger.debug("Performing health check on " + connection);
                                }
                                boolean checkSuccessful = performConnectionHealthCheck(connection);
                                // keep synchronization on state object short
                                synchronized (connectionState) {
                                    if (checkSuccessful) {
                                        // log if this was a recovery
                                        if (connectionState.healthCheckFailureCount > 0) {
                                            logger.info(String.format(
                                                "Connection %s to %s passed its health check after %d previous failures",
                                                connection.getConnectionId(),
                                                connection.getRemoteNodeInformation().getWrappedNodeId(),
                                                connectionState.healthCheckFailureCount));
                                        }
                                        // reset counter
                                        connectionState.healthCheckFailureCount = 0;
                                    } else {
                                        // increase counter and log
                                        connectionState.healthCheckFailureCount++;
                                        logger.warn(String.format(
                                            "Connection %s to %s failed a health check (%d consecutive failures)",
                                            connection.getConnectionId(),
                                            connection.getRemoteNodeInformation().getWrappedNodeId(),
                                            connectionState.healthCheckFailureCount));
                                        // limit exceeded? -> consider broken
                                        // TODO CheckStyle: the following lines conflict with the
                                        // eclipse formatter
                                        if (connectionState.healthCheckFailureCount
                                            >= CommunicationConfiguration.CONNECTION_HEALTH_CHECK_FAILURE_LIMIT) {
                                            handleBrokenOutgoingConnection(connection);
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            logger.debug("Interruption during connection health check", e);
                        }
                    }
                });
            }
        }
    }

    /**
     * Registers a new {@link NetworkTransportProvider}. In a running application, this is called
     * via OSGi-DS; unit tests may call this method directly.
     * 
     * Adding more than one provider for a given transport id is considered an error and results in
     * an {@link IllegalStateException}.
     * 
     * @param provider the transport provider to add
     */
    public void addNetworkTransportProvider(NetworkTransportProvider provider) {
        logger.info("Registering transport provider for id '" + provider.getTransportId() + SINGLE_QUOTE);
        synchronized (transportProviders) {
            String id = provider.getTransportId();
            NetworkTransportProvider previous = transportProviders.put(id, provider);
            if (previous != null) {
                transportProviders.put(id, previous);
                throw new IllegalStateException("Duplicate transport for id '" + id + SINGLE_QUOTE);
            }
        }
    }

    /**
     * Removes a registered {@link NetworkTransportProvider}. Trying to remove a non-existing
     * provider results in an {@link IllegalStateException}.
     * 
     * @param provider the transport provider to remove
     */
    public void removeNetworkTransportProvider(NetworkTransportProvider provider) {
        logger.info("Unregistering transport provider for id '" + provider.getTransportId() + SINGLE_QUOTE);
        synchronized (transportProviders) {
            // consistency check
            NetworkTransportProvider removed = transportProviders.remove(provider.getTransportId());
            if (removed != provider) {
                throw new IllegalStateException("Transport to remove was not actually registered: " + provider);
            }
        }
    }

    /**
     * FIXME Should be called 'getNetworkTransportProvider'. --krol_ph
     * 
     * @param transportId
     * @return
     */
    private NetworkTransportProvider getTransportProvider(String transportId) {
        synchronized (transportProviders) {
            NetworkTransportProvider provider = transportProviders.get(transportId);
            if (provider == null) {
                throw new IllegalStateException("No transport registered for id " + transportId);
            }
            return provider;
        }
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param newService the service to bind
     */
    public void bindRequestPayloadHandler(RequestPayloadHandler newService) {
        this.requestPayloadHandler = newService;
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param newService the service to bind
     */
    public void bindNodeConfigurationService(NodeConfigurationService newService) {
        // do not allow rebinding for now
        if (this.configurationService != null) {
            throw new IllegalStateException();
        }
        this.configurationService = newService;
    }

    /**
     * OSGi-DS lifecycle method.
     */
    public void activate() {
        ownNodeInformation = configurationService.getLocalNodeInformation();
        nodeInformationRegistry = NodeInformationRegistryImpl.getInstance();
        synchronized (transportProviders) {
            int numTransports = transportProviders.size();
            logger.debug(String.format(
                "Activated network connection service; instance log name='%s'; node id='%s'; %d registered transport providers",
                ownNodeInformation.getLogDescription(),
                ownNodeInformation.getWrappedNodeId(), numTransports));
        }
    }

    /**
     * OSGi-DS lifecycle method.
     */
    public void deactivate() {
        logger.debug("Deactivating");
    }

    /**
     * Field access for unit tests.
     * 
     * @param nodeInformation
     */
    protected void setNodeInformation(NetworkNodeInformation nodeInformation) {
        ownNodeInformation = nodeInformation;
    }

    private void registerNewOutgoingConnection(NetworkConnection connection) {
        synchronized (connectionListeners) {
            for (NetworkConnectionListener listener : connectionListeners) {
                listener.onOutgoingConnectionEstablished(connection);
            }
        }
        connectionStates.put(connection, new ConnectionState());
        synchronized (outgoingConnections) {
            outgoingConnections.add(connection);
        }
    }

    private void closeOutgoingConnection(NetworkConnection connection) {
        connection.close();
        synchronized (outgoingConnections) {
            outgoingConnections.remove(connection);
        }
        synchronized (connectionListeners) {
            for (NetworkConnectionListener listener : connectionListeners) {
                listener.onOutgoingConnectionTerminated(connection);
            }
        }
    }

    /**
     * Performs a single request/response attempt with a random token. The receiver should reply
     * with the same token as the response content.
     * 
     * @param connection the connection to test
     * @return true if the check was successful
     * @throws InterruptedException on thread interruption
     */
    private boolean performConnectionHealthCheck(final NetworkConnection connection) throws InterruptedException {
        String randomToken = Integer.toString(random.nextInt());
        Future<NetworkResponse> future =
            sendRequest(MessageUtils.serializeSafeObject(randomToken), healthCheckMetadata.getInnerMap(), connection);
        try {
            NetworkResponse response = future.get(CommunicationConfiguration.CONNECTION_HEALTH_CHECK_TIMEOUT_MSEC, TimeUnit.MILLISECONDS);
            if (!response.isSuccess()) {
                logger.warn("Unexpected result: Received non-sucess response on connection health check for '" + connection + SINGLE_QUOTE);
                return false;
            }
            if (VERBOSE_LOGGING) {
                logger.debug("Health check on connection " + connection + " passed");
            }
            // verify that the response contained the same token; this check *should* never fail
            if (!randomToken.equals(response.getDeserializedContent())) {
                logger.warn("Received unexpected content on connection health check");
            }
            return true;
        } catch (ExecutionException e) {
            logger.debug("Exception during connection health check", e);
        } catch (SerializationException e) {
            logger.debug("Exception during connection health check", e);
        } catch (TimeoutException e) {
            logger.debug("Timeout during connection health check", e);
        }
        return false;
    }

    private void handleBrokenOutgoingConnection(NetworkConnection connection) {
        logger.warn("Closing broken connection to " + connection.getRemoteNodeInformation().getWrappedNodeId() + " (id="
            + connection.getConnectionId() + ")");
        synchronized (outgoingConnections) {
            outgoingConnections.remove(connection);
        }
        synchronized (connectionListeners) {
            for (NetworkConnectionListener listener : connectionListeners) {
                listener.onOutgoingConnectionTerminated(connection);
            }
        }
        // TODO possible optimization: find related requests waiting for response and cancel them
    }

}
