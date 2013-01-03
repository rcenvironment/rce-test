/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.model;

/**
 * A disconnected representation of a network node, in the sense that changes to the actual, live
 * network state will not automatically affect instances of this class. It is intended to provide
 * stable "snapshot" representations of the actual network state.
 * 
 * @author Robert Mischke
 */
public class NetworkStateNode {

    private String nodeId;

    private String displayName = "<unknown>"; // safe default

    private boolean isLocalNode;

    private boolean isWorkflowHost;

    public NetworkStateNode(String nodeId) {
        this.setNodeId(nodeId);
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isLocalNode() {
        return isLocalNode;
    }

    public void setIsLocalNode(boolean isLocalNode) {
        this.isLocalNode = isLocalNode;
    }

    public boolean isWorkflowHost() {
        return isWorkflowHost;
    }

    public void setIsWorkflowHost(boolean isWorkflowHost) {
        this.isWorkflowHost = isWorkflowHost;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkStateNode)) {
            return false;
        }
        return nodeId.equals(((NetworkStateNode) obj).nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
}
