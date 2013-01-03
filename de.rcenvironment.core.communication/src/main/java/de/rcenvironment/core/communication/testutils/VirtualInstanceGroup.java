/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.testutils;

import de.rcenvironment.core.communication.connection.NetworkConnectionListener;
import de.rcenvironment.core.communication.connection.NetworkTrafficListener;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;

/**
 * Utility class to simplify the management of virtual node instances. Any methods called are
 * delegated to each node in the group.
 * 
 * @author Robert Mischke
 */
public class VirtualInstanceGroup implements CommonVirtualInstanceControl {

    private CommonVirtualInstanceControl[] instances;

    /**
     * Creates a new group with the given nodes.
     * 
     * Note: There is no specific reason against dynamically adding/removing instances; add this
     * feature when necessary.
     * 
     * @param instances the dynamic-length list of instances to add
     */
    public VirtualInstanceGroup(CommonVirtualInstanceControl... instances) {
        this.instances = instances;
    }

    @Override
    public void setTargetState(VirtualInstanceState state) throws InterruptedException {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.setTargetState(state);
        }
    }

    @Override
    public void waitForStateChangesToFinish() throws InterruptedException {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.waitForStateChangesToFinish();
        }
    }

    @Override
    public void start() throws InterruptedException {
        // first set all target states, then wait for all changes to finish; this results in better
        // parallelization than looping and calling start() on each instance -- misc_ro
        setTargetState(VirtualInstanceState.STARTED);
        waitForStateChangesToFinish();
    }

    @Override
    public void simulateCrash() throws InterruptedException {
        // first set all target states, then wait for all changes to finish; this results in better
        // parallelization than looping and calling simulateCrash() on each instance -- misc_ro
        setTargetState(VirtualInstanceState.SIMULATED_CRASHING);
        waitForStateChangesToFinish();
    }

    @Override
    public void shutDown() throws InterruptedException {
        // first set all target states, then wait for all changes to finish; this results in better
        // parallelization than looping and calling shutDown() on each instance -- misc_ro
        setTargetState(VirtualInstanceState.STOPPED);
        waitForStateChangesToFinish();
    }

    @Override
    public void registerNetworkTransportProvider(NetworkTransportProvider provider) {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.registerNetworkTransportProvider(provider);
        }
    }

    @Override
    public void addInitialNetworkPeer(NetworkContactPoint contactPoint) {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.addInitialNetworkPeer(contactPoint);
        }
    }

    @Override
    public void addNetworkConnectionListener(NetworkConnectionListener listener) {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.addNetworkConnectionListener(listener);
        }
    }

    @Override
    public void addNetworkTrafficListener(NetworkTrafficListener listener) {
        for (CommonVirtualInstanceControl instance : instances) {
            instance.addNetworkTrafficListener(listener);
        }
    }

}
