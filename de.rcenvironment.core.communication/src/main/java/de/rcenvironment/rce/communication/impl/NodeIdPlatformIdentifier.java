/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import java.io.IOException;

import de.rcenvironment.core.communication.model.NodeInformation;
import de.rcenvironment.core.communication.model.internal.NodeInformationHolder;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A {@link PlatformIdentifier} based on a persistent globally unique identifier.
 * 
 * @author Robert Mischke
 */
public class NodeIdPlatformIdentifier implements PlatformIdentifier {

    private static final long serialVersionUID = -82480269867222031L;

    private String nodeId;

    private transient NodeInformation metaInformationHolder;

    /**
     * Creates a {@link PlatformIdentifier} object from the persistent platform id.
     * 
     * @param id the persistent id to use
     */
    public NodeIdPlatformIdentifier(String id) {
        this.nodeId = id;
        // note: this is done internally as it the dependency is needed on deserialization anyway
        attachMetaInformationHolder();
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String getAssociatedDisplayName() {
        if (metaInformationHolder == null) {
            return "<unknown>";
        }
        String displayName = metaInformationHolder.getDisplayName();
        if (displayName == null) {
            return "<unknown>";
        }
        return displayName;
    }

    @Override
    public String resolveHost() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHost() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPlatformNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an independent clone of this identifier.
     * 
     * @return the clone
     *
     * @see java.lang.Object#clone()
     */
    public NodeIdPlatformIdentifier clone() {
        return new NodeIdPlatformIdentifier(nodeId);
    }

    // hook into deserialization to attach the metadata information holder
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        attachMetaInformationHolder();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof NodeIdPlatformIdentifier) {
            return nodeId.equals(((NodeIdPlatformIdentifier) object).nodeId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }

    @Override
    public String toString() {
        String displayName = metaInformationHolder.getDisplayName();
        if (displayName == null) {
            displayName = "<unnamed>";
        }
        return String.format("'%s' [%s]", displayName, nodeId);
    }

    /**
     * Access method for unit tests.
     * @return the assigned {@link NodeInformationHolder}
     */
    protected NodeInformation getMetaInformationHolder() {
        return metaInformationHolder;
    }

    /**
     * Assigns the shared {@link NodeInformationHolder} to the internal field based on the node id.
     */
    private void attachMetaInformationHolder() {
        this.metaInformationHolder = NodeInformationRegistryImpl.getInstance().getWritableNodeInformation(nodeId);
    }
}
