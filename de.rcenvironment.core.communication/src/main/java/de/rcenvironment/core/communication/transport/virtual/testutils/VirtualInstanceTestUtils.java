/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.transport.virtual.testutils;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.testutils.NetworkContactPointGenerator;
import de.rcenvironment.core.communication.testutils.VirtualInstance;
import de.rcenvironment.core.communication.testutils.VirtualInstanceGroup;
import de.rcenvironment.core.communication.testutils.VirtualInstanceState;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Utilities for {@link VirtualInstance} tests. Mostly superseded by the new {@link VirtualTopology}
 * class.
 * 
 * TODO move to de.rcenvironment.core.communication.testutils
 * 
 * @author Phillip Kroll
 * @author Robert Mischke
 */
public class VirtualInstanceTestUtils {

    private static final int UPPER_UNIQUE_TOKEN_LIMIT = (int) 10e+6;

    private final Log log = LogFactory.getLog(getClass());

    private final Random randomGenerator = new Random();

    private NetworkTransportProvider transportProvider;

    private NetworkContactPointGenerator contactPointGenerator;

    public VirtualInstanceTestUtils(NetworkTransportProvider transportProvider, NetworkContactPointGenerator contactPointGenerator) {
        super();
        this.transportProvider = transportProvider;
        this.contactPointGenerator = contactPointGenerator;
    }

    /**
     * Create a set of initialized and started virtual instances.
     * 
     * @param size the number of instances to generate
     * @return the generated instances
     * @throws InterruptedException on interruption
     */
    @Deprecated
    public VirtualInstance[] spawnDefaultInstances(int size) throws InterruptedException {
        // legacy default behaviour
        return spawnDefaultInstances(size, false, true);
    }

    /**
     * Create a set of initialized and optionally started virtual instances.
     * 
     * @param size the number of instances to generate
     * @param useDuplexTransport true if duplex connections should be allowed
     * @param startInstances true if the instances should be started automatically
     * @return the generated instances
     * @throws InterruptedException on interruption
     */
    public VirtualInstance[] spawnDefaultInstances(int size, boolean useDuplexTransport, boolean startInstances)
        throws InterruptedException {

        if (size < 2) {
            throw new IllegalArgumentException("Illegal number of instances: " + size);
        }

        VirtualInstance[] instances = new VirtualInstance[size];
        for (int i = 1; i <= size; i++) {
            instances[i - 1] = new VirtualInstance(String.format("RCE-%03d", i), "RCE Instance " + i);
        }

        VirtualInstanceGroup group = new VirtualInstanceGroup(instances);

        // add transport provider to every instance
        group.registerNetworkTransportProvider(transportProvider);

        // a server contact point for everyone
        for (VirtualInstance vi : instances) {
            vi.addServerConfigurationEntry(contactPointGenerator.createContactPoint());
        }

        if (startInstances) {
            startAll(instances);
        }

        return instances;
    }

    /**
     * Sets the target state of all instances to "started" and waits until the state changes have
     * finished.
     * 
     * @param instances the instances to start
     * @throws InterruptedException on interruption
     */
    public void startAll(VirtualInstance[] instances) throws InterruptedException {
        VirtualInstanceGroup group = new VirtualInstanceGroup(instances);
        group.setTargetState(VirtualInstanceState.STARTED);
        group.waitForStateChangesToFinish();
    }

    /**
     * Log description of an instance.
     * 
     * @param instances the pool of instances
     * @param index the index within the pool of instances
     * @return the log description with display name and id
     */
    public String getFormattedName(VirtualInstance[] instances, int index) {
        return getFormattedName(instances[index]);
    }

    /**
     * Log description of an instance.
     * 
     * @param instance the instance
     * @return the log description with display name and id
     */
    @Deprecated
    public String getFormattedName(VirtualInstance instance) {
        return String.format("%s (%s)",
            instance.getConfigurationService().getLocalNodeId(),
            instance.getConfigurationService().getLocalNodeInformation().getLogDescription());
    }

    /**
     * @return a string with a random component
     */
    public String generateUniqueMessageToken() {
        return String.format("Unique message token: %s", randomGenerator.nextInt(UPPER_UNIQUE_TOKEN_LIMIT));
    }

    /**
     * Returns a random instance from a set of instances.
     * 
     * @param instances the set to choose from
     * @return the randomly chosen instance
     */
    public VirtualInstance getRandomInstance(VirtualInstance[] instances) {
        return instances[randomGenerator.nextInt(instances.length)];
    }

    /**
     * Returns a random instance from a set of instances, with the option to specify exclusions.
     * 
     * @param instances the set to choose from
     * @param not the instances to exclude as candidates
     * @return the randomly chosen instance
     */
    public VirtualInstance getRandomInstance(VirtualInstance[] instances, VirtualInstance... not) {
        int i = 0;
        int rand = randomGenerator.nextInt(instances.length);
        boolean run = true;
        while (run) {
            if (i++ > instances.length * 2) {
                break;
            }
            rand = randomGenerator.nextInt(instances.length);
            run = false;
            for (VirtualInstance vi : not) {
                run |= vi.equals(instances[rand]);
            }
        }
        return instances[rand];
    }

    /**
     * Tests whether all instances consider themselves "converged".
     * 
     * TODO @krol_ph: definition of convergence
     * 
     * TODO refer to central glossary?
     * 
     * @param instances the instances to check
     * @return true if all instances are "converged"
     */
    public boolean allInstancesConverged(VirtualInstance[] instances) {
        for (int i = 1; i < instances.length; i++) {
            if (instances[i].getTopologyMap().hashCode() != instances[0].getTopologyMap().hashCode()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param instances the instances to use
     */
    @Deprecated
    public void batchAfterStartup(VirtualInstance[] instances) {
        // TODO remove method.
    }

    /**
     * @param instances the instances to use
     * @throws CommunicationException on messaging errors
     */
    public void batchSendLinkStateAdvertisement(VirtualInstance[] instances)
        throws CommunicationException {
        // send link state advertisements
        for (VirtualInstance vi : instances) {
            vi.getRoutingService().getProtocolManager().broadcastLsa();
        }

    }

    /**
     * @param instance the instance that should send an LSA
     * @throws CommunicationException on messaging errors
     */
    public void sendLSA(VirtualInstance instance) throws CommunicationException {
        instance.getRoutingService().getProtocolManager().broadcastLsa();
    }

    /**
     * Sets the TTL for multiple instances.
     * 
     * TODO review @krol_ph
     * 
     * @param instances the instances to use
     * @param value the new TTL to set
     */
    public void batchSetTimeToLive(VirtualInstance[] instances, int value) {
        for (VirtualInstance vi : instances) {
            vi.getRoutingService().getProtocolManager().setTimeToLive(value);
        }
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToChainTopology(VirtualInstance[] instances) {
        for (int i = 0; i < instances.length - 1; i++) {
            instances[i].addRuntimeNetworkPeer(instances[i + 1].getConfigurationService().getServerContactPoints().get(0));
        }
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToRingTopology(VirtualInstance[] instances) {
        connectToChainTopology(instances);
        instances[instances.length - 1].addRuntimeNetworkPeer(instances[0].getConfigurationService().getServerContactPoints().get(0));
    }

    /**
     * @param instances the instances to connect a subset of
     * @param min the start index of the range
     * @param max the end index of the range
     */
    public void connectToDoubleChainTopology(VirtualInstance[] instances, int min, int max) {
        for (int i = min; i <= max - 1; i++) {
            instances[i].addRuntimeNetworkPeer(instances[i + 1].getConfigurationService().getServerContactPoints().get(0));
            instances[i + 1].addRuntimeNetworkPeer(instances[i].getConfigurationService().getServerContactPoints().get(0));
        }
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToDoubleChainTopology(VirtualInstance[] instances) {
        connectToDoubleChainTopology(instances, 0, instances.length - 1);
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToDoubleRingTopology(VirtualInstance[] instances) {
        connectToDoubleRingTopology(instances, 0, instances.length - 1);
    }

    /**
     * @param instances the instances to connect a subset of
     * @param min the start index of the range
     * @param max the end index of the range
     */
    public void connectToDoubleRingTopology(VirtualInstance[] instances, int min, int max) {
        connectToDoubleChainTopology(instances, min, max);
        instances[max].addRuntimeNetworkPeer(instances[min].getConfigurationService().getServerContactPoints().get(0));
        instances[min].addRuntimeNetworkPeer(instances[max].getConfigurationService().getServerContactPoints().get(0));
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToDoubleStarTopology(VirtualInstance[] instances) {
        for (int i = 0; i < instances.length - 1; i++) {
            instances[i].addRuntimeNetworkPeer(instances[instances.length - 1].getConfigurationService().getServerContactPoints().get(0));
            instances[instances.length - 1].addRuntimeNetworkPeer(instances[i].getConfigurationService().getServerContactPoints().get(0));
        }
    }

    /**
     * @param instances the instances to connect
     */
    public void connectToInwardStarTopology(VirtualInstance[] instances) {
        NetworkContactPoint hubServerNCP = instances[0].getConfigurationService().getServerContactPoints().get(0);
        for (int i = 1; i < instances.length; i++) {
            instances[i].addRuntimeNetworkPeer(hubServerNCP);
        }
    }

    /**
     * Concatenate two random instances from two topologies.
     * 
     * @param instances1 the first topology
     * @param instances2 the second topology
     */
    public void randomlyConcatenateTopologies(VirtualInstance[] instances1, VirtualInstance[] instances2) {
        int index1 = randomGenerator.nextInt(instances1.length);
        int index2 = randomGenerator.nextInt(instances2.length);

        instances1[index1].addRuntimeNetworkPeer(instances2[index2].getConfigurationService().getServerContactPoints().get(0));
        instances2[index2].addRuntimeNetworkPeer(instances1[index1].getConfigurationService().getServerContactPoints().get(0));
    }

    /**
     * One-way concatenate two instances within a topology.
     * 
     * @param instances the topology
     * @param first the source node
     * @param second the target node
     */
    public void concatenateInstances(VirtualInstance[] instances, int first, int second) {
        instances[first].addRuntimeNetworkPeer(instances[second].getConfigurationService().getServerContactPoints().get(0));
    }

    /**
     * Two-way concatenate two instances within a topology.
     * 
     * @param instances the topology
     * @param first the source node
     * @param second the target node
     */
    public void doubleConcatenateInstances(VirtualInstance[] instances, int first, int second) {
        instances[first].addRuntimeNetworkPeer(instances[second].getConfigurationService().getServerContactPoints().get(0));
        instances[second].addRuntimeNetworkPeer(instances[first].getConfigurationService().getServerContactPoints().get(0));
    }

}
