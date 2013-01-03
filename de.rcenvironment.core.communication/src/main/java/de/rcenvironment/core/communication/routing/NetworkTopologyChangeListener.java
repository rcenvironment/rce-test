/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing;

/**
 * Listener interface for topology changes. The main events triggering topology changes are nodes
 * entering or leaving the network. Additionally, existing nodes may create new connections, or
 * existing connections may disappear.
 * 
 * @author Robert Mischke
 */
public interface NetworkTopologyChangeListener {

    /**
     * Signals that the known network topology has changed.
     */
    void onNetworkTopologyChanged();
}
