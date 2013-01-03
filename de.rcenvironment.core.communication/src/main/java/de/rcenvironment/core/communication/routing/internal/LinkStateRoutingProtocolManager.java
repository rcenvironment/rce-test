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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.connection.NetworkConnectionListener;
import de.rcenvironment.core.communication.connection.NetworkConnectionService;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NetworkResponseHandler;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Implementation of a link state based routing table.
 * 
 * @author Phillip Kroll
 * @author Robert Mischke
 */
public class LinkStateRoutingProtocolManager {

    /**
     * 
     */
    private static final int DEFAULT_TIME_TO_LIVE = 200;

    /**
     * TODO Write comment. 
     *
     * @author Robert Mischke
     */
    private class ConnectionEventListenerImpl implements NetworkConnectionListener {

        @Override
        public void onOutgoingConnectionEstablished(final NetworkConnection connection) {
            synchronized (topologyMap) {

                log.debug("Registering connection " + connection.getConnectionId() + " at node " + ownNodeId);

                registerNewConnection(connection);

                if (!connection.getInitiatedByRemote()) {
                    // only reply with an LSA batch if the connection was self-initiated
                    // TODO check: is this metadata still necessary?
                    Map<String, String> metaData = MetaDataWrapper.createEmpty().
                        addTraceItem(ownNodeId.toString()).
                        setTopicLsa().setCategoryRouting().getInnerMap();

                    LinkStateAdvertisementBatch payloadLsaCache = topologyMap.generateLsaBatchOfAllNodes();

                    log.debug("Sending initial LSA batch into connection " + connection.getConnectionId());

                    byte[] lsaBytes = MessageUtils.serializeSafeObject(payloadLsaCache);
                    connectionService.sendRequest(lsaBytes, metaData, connection, new NetworkResponseHandler() {

                        @Override
                        public void onResponseAvailable(NetworkResponse response) {
                            if (!response.isSuccess()) {
                                log.warn("Failed to send initial LSA batch via connection " + connection.getConnectionId() + ": Code "
                                    + response.getResultCode());
                                return;
                            }
                            Serializable deserializedContent;
                            try {
                                deserializedContent = response.getDeserializedContent();
                                if (deserializedContent instanceof LinkStateAdvertisementBatch) {
                                    handleLinkStateAdvertisementCacheResponse(deserializedContent);
                                    broadcastLsa();
                                } else {
                                    log.error("Unexpected response to initial LSA batch: " + deserializedContent);
                                }
                            } catch (SerializationException e) {
                                log.error("Failed to deserialize response to initial LSA batch", e);
                            }
                        }

                    });
                } else {
                    // for a remote-initiated connection, an update LSA is sufficient
                    broadcastLsa();
                }
            }
            onTopologyChanged();
        }

        @Override
        public void onOutgoingConnectionTerminated(NetworkConnection connection) {
            unregisterClosedConnection(connection);
            onTopologyChanged();
        }

    }

    private static int timeToLive = DEFAULT_TIME_TO_LIVE;

    private static final int MESSAGE_BUFFER_SIZE = 50;

    private static final boolean DEBUG_DUMP_INITIAL_LSA_BATCHES = false;


    /**
     * TODO Enter comment.
     */
    public volatile boolean sendCompactLsaLists = false;

    private final Log log = LogFactory.getLog(getClass());

    private final TopologyMap topologyMap;

    private final NetworkNodeInformation ownNodeInformation;

    private final NodeIdentifier ownNodeId;

    private final NetworkConnectionService connectionService;

    private NetworkTopologyChangeListener topologyChangeListener;

    private final Map<String, Serializable> messageBuffer = new LinkedHashMap<String, Serializable>(MESSAGE_BUFFER_SIZE);

    private final NetworkStats networkStats;

    private final Map<String, NetworkConnection> connectionsById = new HashMap<String, NetworkConnection>();



    // private final LinkStateAdvertisementBatch lsaCache = new LinkStateAdvertisementBatch();

    // @Deprecated
    

    /**
     * Constructor needs to get services injected.
     * @param communicationService
     * @param platformService
     */
    public LinkStateRoutingProtocolManager(NetworkNodeInformation ownNodeInformation, NetworkConnectionService connectionService) {
        this.ownNodeInformation = ownNodeInformation;
        this.ownNodeId = ownNodeInformation.getWrappedNodeId();
        this.connectionService = connectionService;
        this.topologyMap = new TopologyMap(ownNodeInformation);
        this.networkStats = new NetworkStats();
        connectionService.addConnectionListener(new ConnectionEventListenerImpl());
        // initialize topology with self
        TopologyNode ownNode = topologyMap.addNode(ownNodeId);
        ownNode.setDisplayName(ownNodeInformation.getDisplayName());
        ownNode.setIsWorkflowHost(ownNodeInformation.getIsWorkflowHost());
        // initialize own sequence number
        ownNode.invalidateSequenceNumber();
        // TODO when called from constructor, listeners have no chance of registering before
        onTopologyChanged();
    }

    /**
     * Broadcast the information that this node is shutting down, and will disappear from the
     * network.
     * 
     * @throws CommunicationException The communication exception.
     */
    public void announceShutdown() throws CommunicationException {
        broadcastLsa(topologyMap.generateShutdownLSA());
    }

    /**
     * Test if a message with a given message id has been received recently.
     * 
     * @param messageId The id of the message.
     * @return A boolean. 
     */
    public boolean messageReivedById(String messageId) {
        return messageBuffer.containsKey(messageId);
    }

    /**
     * Test if a message has recently been received that had a given content.
     * 
     * @param messageContent The content.
     * @return A boolean.
     */
    public boolean messageReivedByContent(Serializable messageContent) {
        return messageBuffer.containsValue(messageContent);
    }

    /**
     * Find a route to destination.
     *  
     * @param destination The destination node.
     * @return A boolean.
     */
    public NetworkRoute getRouteTo(NodeIdentifier destination) {
        networkStats.incShortestPathComputations();
        return topologyMap.getShortestPath(getOwner(), destination);
    }

    /**
     * This method is called, when the routing service receives an {@link LinkStateAdvertisement}
     * from a remote instance.
     *
     * @param messageContent The message content.
     * @param metaData The meta data.
     * @return an optional {@link Serializable} response or null
     */
    public Serializable handleLinkStateAdvertisement(Serializable messageContent, Map<String, String> metaData) {

        boolean topologyChanged = false;

        synchronized (topologyMap) {

            if (messageContent instanceof LinkStateAdvertisementBatch) {
                // TODO re-introduce the startup flag when LSA batches are used elsewhere, too?
                return handleLinkStateAdvertisementCacheRequest(messageContent);
            }

            // sanity check
            if (!(messageContent instanceof LinkStateAdvertisement)) {
                throw new IllegalStateException("Received a non-LSA in handleLinkStateAdvertisement()");
            }

            LinkStateAdvertisement lsa = (LinkStateAdvertisement) messageContent;

            networkStats.incReceivedLSAs();
            networkStats.incHopCountOfReceivedLSAs(MetaDataWrapper.wrap(metaData).getHopCount());

            // TODO review: currently not sent; see LSA batch handling above
            // if (LinkStateAdvertisement.REASON_STARTUP.equals(lsa.getReason())) {
            // }

            // if the received LSA was accepted
            if (topologyMap.update(lsa)) {

                topologyChanged = true;

                // TODO Dynamically adjust maximum time to live for LSAs
                networkStats.setMaxTimeToLive(getTimeToLive());

                // // fill cache
                // synchronized (lsaCache) {
                // lsaCache.put(lsa.getOwner(), lsa);
                // }

                /**
                 * @Protocol Forward lsa
                 */
                broadcastLsa(lsa, metaData);

                // NetworkFormatter.nodeList(topologyMap));

                // synchronized (lsaCache) {
                // LinkStateAdvertisementCache clonedCache;
                // clonedCache = new LinkStateAdvertisementCache(lsaCache);
                // return clonedCache;
                // }
            } else {
                networkStats.incRejectedLSAs();
                networkStats.incHopCountOfRejectedLSAs(MetaDataWrapper.wrap(metaData).getHopCount());
                // send null response
            }
        }
        if (topologyChanged) {
            onTopologyChanged();
        }
        return null;
    }

    /**
     * TODO Robert Mischke: Enter comment!
     * 
     * @param messageContent The message content.
     * @return The link state advertisement batch.
     */
    public Serializable handleLinkStateAdvertisementCacheRequest(Serializable messageContent) {

        boolean topologyChanged = false;
        LinkStateAdvertisementBatch response;

        synchronized (topologyMap) {
            // sanity check
            if (!(messageContent instanceof LinkStateAdvertisementBatch)) {
                throw new IllegalStateException("Message content of wrong type.");
            }

            LinkStateAdvertisementBatch lsaCache = (LinkStateAdvertisementBatch) messageContent;

            if (DEBUG_DUMP_INITIAL_LSA_BATCHES) {
                // TODO add origin/sender information
                String dump = String.format("Processing LSA cache at %s (as incoming request):", ownNodeId);
                for (NodeIdentifier id : lsaCache.keySet()) {
                    dump += "\n" + id + " -> " + lsaCache.get(id);
                }
                log.debug(dump);
            }

            LinkStateAdvertisementBatch lsaCacheNew = new LinkStateAdvertisementBatch();

            // TODO increment stats
            for (LinkStateAdvertisement lsa : lsaCache.values()) {
                if (topologyMap.update(lsa)) {
                    topologyChanged = true;
                    // update main cache
                    // lsaCache.put(lsa.getOwner(), lsa);
                    // lsaCacheNew.put(lsa.getOwner(), lsa);
                    broadcastLsa(lsa);
                }

            }

            // return lsaCache;
            response = topologyMap.generateLsaBatchOfAllNodes();
        }
        if (topologyChanged) {
            onTopologyChanged();
        }
        return response;
    }

    /**
     * TODO Robert Mischke: Enter comment!
     * 
     * @param messageContent The message content.
     */
    public void handleLinkStateAdvertisementCacheResponse(Serializable messageContent) {
        boolean topologyChanged = false;
        synchronized (topologyMap) {
            // sanity check
            if (!(messageContent instanceof LinkStateAdvertisementBatch)) {
                log.warn("Message content was of wrong type.");
                return;
            }

            LinkStateAdvertisementBatch lsaCache = (LinkStateAdvertisementBatch) messageContent;

            if (DEBUG_DUMP_INITIAL_LSA_BATCHES) {
                // TODO add origin/sender information
                String dump = String.format("Processing LSA cache at %s (as incoming response):", ownNodeId);
                for (NodeIdentifier id : lsaCache.keySet()) {
                    dump += "\n" + id + " -> " + lsaCache.get(id);
                }
                log.debug(dump);
            }

            // LinkStateAdvertisementCache lsaCacheNew = new LinkStateAdvertisementCache();

            for (LinkStateAdvertisement lsa : lsaCache.values()) {
                if (topologyMap.update(lsa)) {
                    topologyChanged = true;
                    // lsaCacheNew.put(lsa.getOwner(), lsa);
                    broadcastLsa(lsa);
                }

            }
        }
        if (topologyChanged) {
            onTopologyChanged();
        }
    }

    /**
     * Send link state advertisement of the own node.
     * 
     * @return The message id.
     */
    public String broadcastLsa() {
        // extract fresh LSA from topology map
        // synchronized (lsaCache) {
        LinkStateAdvertisement ownLsa = topologyMap.generateLsa();
        // lsaCache.put(ownNodeId, ownLsa);
        return broadcastLsa(ownLsa);
        // }
    }

    /**
     * Send a given LSA to all neighbors.
     * 
     * @param lsa
     * @throws CommunicationException
     * @return The message id.
     */
    private String broadcastLsa(LinkStateAdvertisement lsa) {
        return broadcastLsa(lsa, MetaDataWrapper.createRouting().setTopicLsa().getInnerMap());
    }

    /**
     * Send any link state advertisement.
     * 
     * @param lsa The link state advertisement.
     * @param metaData The meta data.
     * @throws CommunicationException The communication exception.
     * @return The message id.
     */
    private String broadcastLsa(LinkStateAdvertisement lsa, Map<String, String> metaData) {

        byte[] lsaBytes = MessageUtils.serializeSafeObject(lsa);
        String messageId = "";

        // TODO Message Id might not be necessary anymore
        if (MetaDataWrapper.wrap(metaData).getMessageId().equals("")) {
            messageId = MetaDataWrapper.wrap(metaData).
                setMessageId(generateUniqueMessageId(ownNodeId, ownNodeId, lsa)).
                getMessageId();
        }

        // update metadata
        MetaDataWrapper.wrap(metaData).
            incHopCount().
            addTraceItem(ownNodeId.toString()).
            setTopicLsa().
            setCategoryRouting();

        List<TopologyNode> neighbors = new ArrayList<TopologyNode>(topologyMap.getSuccessors());
        // Use a randomized list
        Collections.shuffle(neighbors);

        // iterate over all neighbor nodes of the current node
        for (TopologyNode neighbor : neighbors) {
            // split horizon
            // if (neighbor.equals(lsa.getOwner())) { continue; }

            networkStats.incSentLSAs();
            networkStats.incHopCountOfSentLSAs(MetaDataWrapper.wrap(metaData).getHopCount());

            sendToNeighbor(lsaBytes, metaData, neighbor.getNodeIdentifier());
        }

        return messageId;
    }

    /**
     * TODO Comment and rename!
     * 
     * @param messageContent
     * @param metaData
     * @param nodeIdentifier
     * @return
     * @throws CommunicationException The communication exception.
     */
    private void sendToNeighbor(byte[] messageContent, Map<String, String> metaData,
        final NodeIdentifier nodeIdentifier) {

        // compute route
        NetworkRoute route = getRouteTo(nodeIdentifier);

        // FIXME response generation; return Future?
        if (route.validate()) {
            // check if route is immediate, ie of length 1
            if (route.getLength() != 1) {
                // TODO change to CommunicationException?
                throw new IllegalStateException("Unexpected state: Route to neighbor has length " + route.getLength());
            }

            // if there is a route, use it
            sendTowardsNeighbor(messageContent, metaData, route.getFirstLink(), new NetworkResponseHandler() {

                @Override
                public void onResponseAvailable(NetworkResponse response) {
                    if (!response.isSuccess()) {
                        // TODO add cause to log entry
                        log.warn("Failed to send LSA to neighbor node " + nodeIdentifier);
                    }
                }
            });
        } else {
            log.warn("Route to " + nodeIdentifier + " failed to validate");
            // else fail
            // FIXME route.getFirstLink() can be null, which sometimes causes NPEs
            // FIXME when this happens, it blocks the test indefinitely; find the source and add a
            // timeout; the "if" is only a band-aid fix
            // TODO review
            // if (route != null) {
            // onCommunicationFailure(messageContent, metaData,
            // route.getFirstLink().getNetworkContactPoint());
            // }
        }
    }

    /**
     * Central method to send a remote, asynchronous messages to a {@link NetworkContactPoint}. No
     * routing is involved here.
     * 
     * @param messageBytes The message content.
     * @param metaData The message meta data.
     * @param link The link.
     * @param outerResponseHander 
     */
    // TODO move this to routing service? -- misc_ro
    // TODO better method name?
    public void sendTowardsNeighbor(final byte[] messageBytes,
        final Map<String, String> metaData, final TopologyLink link, final NetworkResponseHandler outerResponseHander) {

        NetworkConnection connection = getConnectionForLink(link);

        NetworkResponseHandler responseHandler = new NetworkResponseHandler() {

            @Override
            public void onResponseAvailable(NetworkResponse response) {
                if (!response.isSuccess()) {
                    Serializable loggableContent;
                    try {
                        loggableContent = response.getDeserializedContent();
                    } catch (SerializationException e) {
                        // used for logging only
                        loggableContent = "Failed to deserialize content: " + e;
                    }
                    log.warn(String.format("Received non-success response for request id '%s' at '%s': result code: %d, body: '%s'",
                        response.getRequestId(), ownNodeId, response.getResultCode(), loggableContent));
                }
                if (outerResponseHander != null) {
                    outerResponseHander.onResponseAvailable(response);
                } else {
                    log.warn("No outer response handler");
                }
            }

        };

        connectionService.sendRequest(messageBytes, metaData, connection, responseHandler);
    }

    private NetworkConnection getConnectionForLink(final TopologyLink link) {
        NetworkConnection connection = null;
        synchronized (connectionsById) {
            connection = connectionsById.get(link.getConnectionId());
        }
        if (connection == null) {
            throw new IllegalStateException("No registered connection for connection id " + link.getConnectionId());
        }
        return connection;
    }

    /**
     * Central method to build up a connection.
     * 
     * @param ncp
     * @param duplex
     * @return NetworkConnection
     * @throws CommunicationException The communication exception.
     * @throws InterruptedException The Interrupted exception.
     * @throws ExecutionException The execution exception.
     */
    private NetworkConnection establishConnection(NetworkContactPoint ncp, boolean duplex)
        throws CommunicationException, InterruptedException, ExecutionException {
        Future<NetworkConnection> future = connectionService.connect(ncp, false);
        return future.get();
    }

    private TopologyLink registerNewConnection(NetworkConnection connection) {
        String connectionId = connection.getConnectionId();
        synchronized (connectionsById) {
            // consistency check: there should be no connection with the same id already
            if (connectionsById.get(connectionId) != null) {
                // consistency error
                throw new IllegalStateException("Existing connection found for connection id " + connectionId);
            }
            connectionsById.put(connectionId, connection);
            // LOGGER.debug(String.format("Registered new connection %s in node %s",
            // connection.toString(),
            // ownNodeInformation.getLogName()));

            // NOTE: this replaces the obsolete "pingNetworkContactPoint" method -- misc_ro
            NodeIdentifier remoteNodeId = connection.getRemoteNodeInformation().getWrappedNodeId();

            // TODO restore onCommunicationSuccess callback (via traffic listener?)
            // onCommunicationSuccess("", MetaDataWrapper.createEmpty().getInnerMap(), connection,
            // remoteNodeId);

            // update graph model
            topologyMap.addNode(remoteNodeId);

            if (topologyMap.hasLinkForConnection(connection.getConnectionId())) {
                // unexpected state / consistency error
                throw new IllegalStateException("Found existing link for new connection " + connectionId);
            }

            // add newly discovered link to network model
            TopologyLink newLink = topologyMap.addLink(getOwner(), remoteNodeId, connection.getConnectionId());

            return newLink;
        }
    }

    private void unregisterClosedConnection(NetworkConnection connection) {
        synchronized (connectionsById) {
            String connectionId = connection.getConnectionId();

            // remove link from topology
            TopologyLink link = topologyMap.getLinkForConnection(connectionId);
            if (!topologyMap.removeLink(link)) {
                log.warn("Unexpected state: Closed connection had no link counterpart; id=" + connectionId);
            }

            // is there already a connection to this NCP?
            NetworkConnection registeredConnection = connectionsById.get(connectionId);
            if (registeredConnection == null) {
                log.warn("No registered connection for id " + connectionId);
                return;
            }
            if (registeredConnection != connection) {
                log.warn("Another connection is registered under id " + connectionId + "; ignoring unregistration");
                return;
            }
            connectionsById.remove(connectionId);
            log.debug(String.format("Unregistered connection %s from %s", connection.toString(),
                ownNodeInformation.getLogDescription()));
        }
        broadcastLsa();
    }

    /**
     * Event.
     * 
     * @param messageContent
     * @param metaData
     * @param ncp
     */
    private void onMaxTimeToLiveReached(Serializable messageContent, Map<String, String> metaData,
        NetworkContactPoint ncp) {
        networkStats.incFailedCommunications();

        log.debug(String.format(
            "'%s' reports that a message that was issued by '%s' exeeded the maximum time to life (%s).",
            ownNodeId, MetaDataWrapper.wrap(metaData).getSender(), timeToLive));
    }

    /**
     * 
     * TODO krol_ph: Enter comment!
     * 
     * @param messageId
     * @param messageContent
     */
    private void onMessageReceived(String messageId, Serializable messageContent) {

    }

    /**
     * @return Returns the owner.
     */
    public NodeIdentifier getOwner() {
        return ownNodeId;
    }

    /**
     * Get something that is guaranteed to be unique for every message.
     * 
     * @param sender
     * @param receiver
     * @param messageContent
     * @return
     */
    private String generateUniqueMessageId(NodeIdentifier sender, NodeIdentifier receiver,
        Serializable messageContent) {
        return Integer.toString(String.format("msg-id:%s%s%s%s",
            sender.getNodeId(),
            receiver.getNodeId(),
            messageContent.hashCode(),
            System.currentTimeMillis()
        ).hashCode());
    }

    /**
     * Add received messages to a buffer so that they can be accessed later on.
     * 
     * @param messageContent The message content.
     */
    protected void addToMessageBuffer(String messageId, Serializable messageContent) {
        messageBuffer.put(messageId, messageContent);
        onMessageReceived(messageId, messageContent);
    }

    public TopologyMap getTopologyMap() {
        return topologyMap;
    }

    /**
     * @return Returns the networkStats.
     */
    public NetworkStats getNetworkStats() {
        return networkStats;
    }

    /**
     * @return Returns the timeToLive.
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    /**
     * @param timeToLive The timeToLive to set.
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Map<String, Serializable> getMessageBuffer() {
        return messageBuffer;
    }

    public void setTopologyChangeListener(NetworkTopologyChangeListener topologyChangeListener) {
        this.topologyChangeListener = topologyChangeListener;
    }

    private void onTopologyChanged() {
        if (topologyChangeListener != null) {
            topologyChangeListener.onNetworkTopologyChanged();
        }
    }
}
