/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.gui.communication.views.model;

import de.rcenvironment.core.communication.model.NetworkStateNode;

/**
 * Tree representation of a "connection" between two {@link NetworkStateNode}s. The exact semantics are
 * up to the content provider.
 * 
 * @author Robert Mischke
 * 
 */
public class TopologyLink {

    private String linkId;

    private NetworkStateNode from;

    private NetworkStateNode to;

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public NetworkStateNode getFrom() {
        return from;
    }

    public void setFrom(NetworkStateNode from) {
        this.from = from;
    }

    public NetworkStateNode getTo() {
        return to;
    }

    public void setTo(NetworkStateNode to) {
        this.to = to;
    }
}
