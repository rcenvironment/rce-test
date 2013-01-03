/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.transport.virtual;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.connection.internal.ConnectionClosedException;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.RawNetworkResponseHandler;
import de.rcenvironment.core.communication.model.impl.NetworkRequestImpl;
import de.rcenvironment.core.communication.model.impl.NetworkResponseImpl;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.transport.spi.AbstractNetworkConnection;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * The {@link NetworkConnection} implementation of {@link VirtualNetworkTransportProvider}.
 * 
 * TODO the internal content serialization/deserialization is obsolete; it is not handled outside of
 * the transports
 * 
 * @author Robert Mischke
 */
public class VirtualNetworkConnection extends AbstractNetworkConnection {

    protected final Log log = LogFactory.getLog(getClass());

    private NetworkConnectionEndpointHandler service;

    private NetworkNodeInformation ownNodeInformation;

    private AtomicBoolean closedFlag = new AtomicBoolean(false);

    private SharedThreadPool threadPool = SharedThreadPool.getInstance();

    /**
     * TODO krol_ph: enter comment!
     * 
     */
    public VirtualNetworkConnection(NetworkNodeInformation ownNodeInformation, NetworkConnectionEndpointHandler service,
        ServerContactPoint remoteSCP) {
        this.service = service;
        this.ownNodeInformation = ownNodeInformation;
        this.associatedSCP = remoteSCP;
        // this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void sendRequest(final NetworkRequest request, final RawNetworkResponseHandler responseHandler, int timeoutMsec) {
        // TODO add local timeout
        // TODO send NetworkResponseHandler connection failure response on invalid destination

        // sanity check to avoid exceptions in async code
        if (request == null || responseHandler == null) {
            throw new NullPointerException();
        }

        Callable<NetworkResponse> task = new Callable<NetworkResponse>() {

            @Override
            public NetworkResponse call() throws Exception {
                try {
                    try {
                        checkReadyToSend();
                    } catch (ConnectionClosedException e) {
                        responseHandler.onConnectionBroken(request, VirtualNetworkConnection.this);
                        throw e;
                    }
                    return simulateRoundTrip(request, responseHandler);
                } catch (RuntimeException e) {
                    String nodeId = ownNodeInformation.getWrappedNodeId().getNodeId();
                    NetworkResponse errorResponse =
                        NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, nodeId, e);
                    responseHandler.onResponseAvailable(errorResponse);
                    // responseHandler.onRequestFailure(request, VirtualNetworkConnection.this, e);
                    // TODO review: keep throwing this exception?
                    throw new CommunicationException("Failed to simulate request-response loop (request id: '" + request.getRequestId()
                        + "')", e);
                }
            }

            private NetworkResponse simulateRoundTrip(final NetworkRequest request, final RawNetworkResponseHandler responseHandler)
                throws SerializationException {

                // clone the associated node identifier
                // TODO is this actually necessary? embed sender UUID in metadata instead?
                final NodeIdentifier virtualSenderId = ownNodeInformation.getWrappedNodeId().clone();

                // create a detached clone of the request
                NetworkRequestImpl clonedRequest = createDetachedClone(request);

                // invoke the connection service on the "receiving" side and fetch the response
                NetworkResponse generatedResponse = service.onRawRequestReceived(clonedRequest, virtualSenderId);
                // create a detached clone of the response
                NetworkResponse clonedResponse = createDetachedClone(generatedResponse);
                responseHandler.onResponseAvailable(clonedResponse);
                return clonedResponse;
            }
        };

        // TODO rework to plain runnable; no Future needed
        threadPool.submit(task);
    }

    private void checkReadyToSend() throws ConnectionClosedException {
        if (!isReadyToSend()) {
            throw new ConnectionClosedException("Connection id " + getConnectionId() + " is closed");
        }
        if (!associatedSCP.isAcceptingMessages()) {
            throw new ConnectionClosedException("Connection id " + getConnectionId() + " is broken");
        }
    }

    @Override
    public void close() {
        log.debug("Closing connection (remote=" + getRemoteNodeInformation().getLogDescription() + ", NCP=" + associatedSCP);
        closedFlag.set(true);
    }

    private boolean isReadyToSend() {
        if (closedFlag.get()) {
            return false;
        }
        return true;
    }

    private NetworkRequestImpl createDetachedClone(NetworkRequest request) {
        byte[] originalContent = request.getContentBytes();
        final byte[] detachedContentBytes = Arrays.copyOf(originalContent, originalContent.length);

        // clone the received metadata; should be safe as it is a String/String map
        final Map<String, String> clonedRequestMetaData = MetaDataWrapper.wrap(request.accessRawMetaData()).cloneData();

        NetworkRequestImpl clonedRequest = new NetworkRequestImpl(detachedContentBytes, clonedRequestMetaData, request.getRequestId());
        return clonedRequest;
    }

    private NetworkResponseImpl createDetachedClone(NetworkResponse response) throws SerializationException {
        // clone the received metadata; should be safe as it is a String/String map
        final Map<String, String> clonedResponseMetaData = MetaDataWrapper.wrap(response.accessRawMetaData()).cloneData();
        // clone content byte array
        byte[] originalContentBytes = response.getContentBytes();
        byte[] detachedContentBytes = Arrays.copyOf(originalContentBytes, originalContentBytes.length);

        NetworkResponseImpl clonedResponse = new NetworkResponseImpl(detachedContentBytes, clonedResponseMetaData);
        return clonedResponse;
    }

    @Deprecated
    // TODO review: delete? -- misc_ro
    private Serializable createDetachedMessageBody(Serializable originalBody) {
        if (originalBody == null) {
            return null;
        }
        // simulate a remote call by serializing the original message body,
        // then deserializing it again. this is similar to a "clone" call,
        // but provides a stronger test of serializability. -- misc_ro
        Serializable deserializedBody;
        try {
            final byte[] serializedBody = MessageUtils.serializeObject(originalBody);
            deserializedBody = (Serializable) MessageUtils.deserializeObject(serializedBody);
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to create detached copy of message body: " + originalBody, e);
        }
        return deserializedBody;
    }
}
