/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A model representing a condensed, disconnected snapshot of the current network state. Changes to
 * the actual, live network state will not automatically affect this model.
 * 
 * @author Robert Mischke
 */
public class NetworkStateModel {

    private NetworkStateNode localNode;

    private Map<String, NetworkStateNode> nodes = new HashMap<String, NetworkStateNode>();

    // private Map<String, TopologyLink> links = new HashMap<String, TopologyLink>();

    public NetworkStateNode getLocalNode() {
        return localNode;
    }

    public void setLocalNode(NetworkStateNode localNode) {
        this.localNode = localNode;
    }

    /**
     * @return all tree nodes, in no particular order
     */
    public Collection<NetworkStateNode> getNodes() {
        return nodes.values();
    }

    /**
     * @param key the nodeId of the topology node
     * @return the tree node representing the matching topology node
     */
    public NetworkStateNode getNode(String key) {
        return nodes.get(key);
    }

    /**
     * @param key the nodeId of the topology node
     * @param value the tree node representing the matching topology node
     */
    public void addNode(String key, NetworkStateNode value) {
        nodes.put(key, value);
    }

}
