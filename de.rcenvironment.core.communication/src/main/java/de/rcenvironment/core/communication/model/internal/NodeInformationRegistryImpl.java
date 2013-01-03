/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model.internal;

import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeInformation;
import de.rcenvironment.core.communication.model.NodeInformationRegistry;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;

/**
 * Central registry for information gathered about nodes.
 * 
 * @author Robert Mischke
 */
public class NodeInformationRegistryImpl implements NodeInformationRegistry {

    private static final NodeInformationRegistryImpl sharedInstance = new NodeInformationRegistryImpl();

    private Map<String, NodeInformationHolder> idToHolderMap = new HashMap<String, NodeInformationHolder>();

    // TODO this shared map does not properly reflect multi-instance tests; changes required?
    public static NodeInformationRegistryImpl getInstance() {
        return sharedInstance;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.core.communication.model.NodeInformationRegistry#getNodeInformation(java.lang.String)
     */
    @Override
    public NodeInformation getNodeInformation(String id) {
        return getWritableNodeInformation(id);
    }

    /**
     * Provides direct, write-enabled access to {@link NodeInformationHolder}s. Not part of the
     * {@link NodeInformationRegistry} interface as it is intended for bundle-internal use only.
     * 
     * @param id the id of the relevant node
     * @return the writable {@link NodeInformationHolder}
     */
    public NodeInformationHolder getWritableNodeInformation(String id) {
        synchronized (idToHolderMap) {
            NodeInformationHolder holder = idToHolderMap.get(id);
            if (holder == null) {
                holder = new NodeInformationHolder();
                idToHolderMap.put(id, holder);
            }
            return holder;
        }
    }

    /**
     * Updates the associated information for a node from a received or locally-generated
     * {@link NetworkNodeInformation} object.
     * 
     * @param remoteNodeInformation the object to update from
     */
    public void updateFrom(NetworkNodeInformation remoteNodeInformation) {
        String nodeId = remoteNodeInformation.getWrappedNodeId().getNodeId();
        NodeInformationHolder writableNodeInformation = getWritableNodeInformation(nodeId);
        writableNodeInformation.setDisplayName(remoteNodeInformation.getDisplayName());
        writableNodeInformation.setIsWorkflowHost(remoteNodeInformation.getIsWorkflowHost());
    }

    /**
     * Updates the associated information for a node from a received or locally-generated
     * {@link PlatformIdentityInformation} object.
     * 
     * @param identityInformation the object to update from
     */
    public void updateFrom(PlatformIdentityInformation identityInformation) {
        String nodeId = identityInformation.getPersistentNodeId();
        NodeInformationHolder writableNodeInformation = getWritableNodeInformation(nodeId);
        writableNodeInformation.setDisplayName(identityInformation.getDisplayName());
        writableNodeInformation.setIsWorkflowHost(identityInformation.getIsWorkflowHost());
    }

}
