/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing.internal;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.testutils.TestNetworkTrafficListener;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;

/**
 * Provides tools to generate human readable string representations of routing components (such as
 * {@link TopologyMap}) that are useful for debugging and monitoring.
 * 
 * @author Phillip Kroll
 */
public final class NetworkFormatter {

    private NetworkFormatter() {
        // no instance
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param networkGraph The network Graph
     * @return A string representation for debugging/displaying.
     */
    public static String linkList(TopologyMap networkGraph) {
        return linkList(networkGraph.getAllLinks());
    }

    /**
     * Formats a collection of links to a string.
     * 
     * @param linkCollection The collection of links.
     * @return A string representation for debugging/displaying.
     */
    public static String linkList(Collection<TopologyLink> linkCollection) {
        String result = "";
        List<TopologyLink> linkList = new ArrayList<TopologyLink>(linkCollection);
        Collections.sort(linkList);

        for (TopologyLink link : linkList) {
            result += String.format("  %s --[%s]--> %s (Hash=%s)\n",
                link.getSource().getNodeId(),
                link.getConnectionId(),
                link.getDestination().getNodeId(),
                link.hashCode()
            );
        }
        return result;
    }

    /**
     * Formats a list of nodes to a string.
     * 
     * @param networkGraph The network graph.
     * @return A string representation for debugging/displaying.
     */
    public static String nodeList(TopologyMap networkGraph) {
        String result = "";
        List<TopologyNode> networkNodes = new ArrayList<TopologyNode>(networkGraph.getNodes());
        Collections.sort(networkNodes);

        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        for (TopologyNode networkNode : networkNodes) {
            String nodeIdInfo = networkNode.getNodeIdentifier().getNodeId();
            // mark local node
            if (nodeIdInfo.equals(networkGraph.getLocalNodeId().getNodeId())) {
                nodeIdInfo += "*";
            }
            result +=
                String.format("  [%9$s] %1$s, --%4$s-> * --%5$s->, '%8$s', Seq=%2$s, Created=%3$tk:%3$tM:%3$tS, Conv=%6$s, Hash=%7$s\n",
                    nodeIdInfo,
                    // TODO quick & dirty; improve
                    networkNode.getSequenceNumber() + " (" + timestampFormat.format(new Date(networkNode.getSequenceNumber()))
                        + ")",
                    networkNode.getCreatedTime(),
                    networkGraph.getPredecessors(networkNode).size(),
                    networkGraph.getSuccessors(networkNode).size(),
                    (networkNode.getLastGraphHashCode() == networkGraph.hashCode()),
                    networkNode.hashCode(),
                    networkNode.getDisplayName(),
                    networkNode.getIsWorkflowHost()
                );
        }
        return result;
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param networkGraph The network graph.
     * @return A string representation for debugging/displaying.
     */
    public static String graphMetaData(TopologyMap networkGraph) {
        return String.format("  Local Node Id: %s, Nodes: %s, Links: %s, Fully conv.: %s, Hash=%s\n",
            networkGraph.getLocalNodeId().getNodeId(),
            networkGraph.getNodeCount(),
            networkGraph.getLinkCount(),
            networkGraph.hasSameTopologyHashesForAllNodes(),
            networkGraph.hashCode()
        );
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param networkGraph The network graph.
     * @return A string representation for debugging/displaying.
     */
    public static String summary(TopologyMap networkGraph) {
        return String.format("Topology Metadata:\n%sKnown Nodes:\n%sLinks:\n%s",
            NetworkFormatter.graphMetaData(networkGraph),
            NetworkFormatter.nodeList(networkGraph),
            NetworkFormatter.linkList(networkGraph)
        );
    }

    /**
     * Formats message to string.
     * 
     * @param messageContent The message content.
     * @param metaData The meta data of the message.
     * @return A string representation for debugging/displaying.
     */
    public static String message(Serializable messageContent, Map<String, String> metaData) {
        MetaDataWrapper handler = MetaDataWrapper.wrap(metaData);
        return String.format("Src='%s', Dest='%s', Body='%s', HopC=%d, MsgId='%s', Trace='%s'",
            handler.getSender(),
            handler.getReceiver(),
            messageContent.toString(),
            handler.getHopCount(),
            handler.getMessageId(),
            handler.getTrace());
    }

    /**
     * Formats LSA to string.
     * 
     * @param lsa The links state advertisement.
     * @return A string representation for debugging/displaying.
     */
    public static String lsa(LinkStateAdvertisement lsa) {
        return String.format("owner=%s, links=%s, seq=%s, type=%s, hash=%s\n%s",
            lsa.getOwner(),
            lsa.getLinks().size(),
            lsa.getSequenceNumber(),
            lsa.getReason(),
            lsa.getGraphHashCode(),
            linkList(lsa.getLinks()));
    }

    /**
     * Formats LSA cache to string.
     * 
     * @param lsaCache  The LSA cache.
     * @return A string representation for debugging/displaying.
     */
    public static String lsaCache(LinkStateAdvertisementBatch lsaCache) {
        String result = String.format("size=%s\n", lsaCache.size());
        for (LinkStateAdvertisement lsa : lsaCache.values()) {
            result += lsa(lsa) + "\n";
        }
        return result;
    }

    /**
     * Formats a network rout to string.
     * 
     * @param networkRoute The network route.
     * @return A string representation for debugging/displaying.
     */
    public static String networkRoute(NetworkRoute networkRoute) {
        String result =
            String.format("length: %s, %s, time: %s ms", networkRoute.getPath().size(), networkRoute.getSource().getNodeId(),
                networkRoute.getComputationalEffort());
        for (TopologyLink link : networkRoute.getPath()) {
            result += String.format(" --> %s", link.getDestination().getNodeId());
        }
        return result;
    }

    /**
     * Formats network statistics to string.
     * 
     * @param networkStats The network statistics.
     * @return A string representation for debugging/displaying.
     */
    public static String networkStats(NetworkStats networkStats) {
        return String.format(
            "\nSuccessful communications: %s\n"
                + "Failed communications: %s\n\n"
                + "LSAs send:     %s\n"
                + "LSAs received: %s\n"
                + "LSAs rejected: %s\n\n"
                + "Max received hop count:     %s\n"
                + "Max time to live:           %s\n"
                + "Number of computed routes:  %s\n\n"
                + "Average hop count of send LSAs:     %s\n"
                + "Average hop count of received LSAs: %s\n"
                + "Average hop count of rejected LSAs: %s\n",

            networkStats.getSuccessfulCommunications(),
            networkStats.getFailedCommunications(),

            networkStats.getSentLSAs(),
            networkStats.getReceivedLSAs(),
            networkStats.getRejectedLSAs(),

            networkStats.getMaxReceivedHopCount(),
            networkStats.getMaxTimeToLive(),
            networkStats.getShortestPathComputations(),

            networkStats.averageHopCountOfSentLSAs(),
            networkStats.averageHopCountOfReceivedLSAs(),
            networkStats.averageHopCountOfRejectedLSAs()
        );
    }

    /**
     * Formats network response to string.
     * 
     * @param networkResponse The network response.
     * @return A string representation for debugging/displaying.
     */
    public static String networkResponseToString(NetworkResponse networkResponse) {
        return String.format("id=%s, succ=%s, code=%s, header=%s",
            networkResponse.getRequestId(),
            networkResponse.isSuccess(),
            networkResponse.getResultCode(),
            networkResponse.accessRawMetaData().toString());
    }

    /**
     * Formats traffic listener to string.
     * 
     * @param listener The network traffic listener.
     * @param instanceCount The number of instances.
     * @return A string representation for debugging/displaying.
     */
    public static String globalNetworkTraffic(TestNetworkTrafficListener listener, int instanceCount) {

        if (instanceCount <= 0) {
            throw new IllegalArgumentException("Argument must be >=1");
        }

        return String.format("Total requests sent:                %d\n"
            + "Average requests sent per node:     %d\n"
            + "Total LSA messages sent:            %d\n"
            + "Average LSA messages sent per node: %d\n"
            + "Total routed messages sent:         %d\n"
            + "Largest observed hop count:         %d (%d)\n"
            + "Unsuccessful responses received:    %d\n",
            listener.getRequestCount(),
            listener.getRequestCount() / instanceCount,
            listener.getLsaMessages(),
            listener.getLsaMessages() / instanceCount,
            listener.getRoutedMessages(),
            listener.getLargestObservedHopCount(),
            instanceCount,
            listener.getUnsuccessfulResponses()
        );

    }

}
