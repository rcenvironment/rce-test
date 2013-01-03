/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model.impl;

import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Default {@link NetworkNodeInformation} implementation.
 * 
 * @author Robert Mischke
 */
public class NetworkNodeInformationImpl implements NetworkNodeInformation {

    // TODO made Serializable for rapid prototyping; change for production
    private static final long serialVersionUID = 6729868652469869965L;

    private String nodeId;

    private String displayName;

    private boolean isWorkflowHost;

    private String softwareVersion;

    private String protocolVersion;

    private transient NodeIdentifier wrappedNodeId;

    /**
     * Default constructor for bean-style construction.
     */
    public NetworkNodeInformationImpl() {
        // NOP
    }

    /**
     * Convenience constructor.
     * 
     * @param nodeIdentifier
     */
    public NetworkNodeInformationImpl(NodeIdentifier nodeIdentifier) {
        this.nodeId = nodeIdentifier.getNodeId();
    }

    public NetworkNodeInformationImpl(String id) {
        this.nodeId = id;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    // setter for bean-style construction
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean getIsWorkflowHost() {
        return isWorkflowHost;
    }

    public void setIsWorkflowHost(boolean isWorkflowHost) {
        this.isWorkflowHost = isWorkflowHost;
    }

    @Override
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    @Override
    public String getNativeProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public synchronized NodeIdentifier getWrappedNodeId() {
        // create the wrapped object on-the-fly to support bean-style construction
        if (wrappedNodeId == null) {
            wrappedNodeId = PlatformIdentifierFactory.fromNodeId(nodeId);
        }
        return wrappedNodeId;
    }

    @Override
    public String getLogDescription() {
        String name = displayName;
        if (displayName == null) {
            displayName = "<unnamed>";
        }
        return String.format("%s [%s]", name, nodeId);
    }

    // NOTE: only intended for use in unit tests; not for production use!
    private String getInternalFingerprint() {
        return String.format("%s#%s#%s#%s", nodeId, displayName, softwareVersion, protocolVersion);
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s/%s", nodeId, displayName, softwareVersion, protocolVersion);
    }

    @Override
    // NOTE: only intended for unit tests; not for production use!
    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkNodeInformationImpl)) {
            return false;
        }
        return getInternalFingerprint().equals(((NetworkNodeInformationImpl) obj).getInternalFingerprint());
    }

    @Override
    // NOTE: only intended for unit tests; not for production use!
    public int hashCode() {
        return getInternalFingerprint().hashCode();
    }

}
