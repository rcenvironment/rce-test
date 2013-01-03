/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkStateModel;
import de.rcenvironment.rce.communication.service.RemoteServiceHandler;

/**
 * Convenient service serving as a distribute abstraction layer for the services of the
 * communication bundle: {@link PlatformService}, {@link RemoteServiceHandler}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public interface CommunicationService {

    /**
     * /** Returns all known RCE nodes that could be providing accessible services. More precisely,
     * the returned set contains the local node and all request-reachable nodes that have declared
     * themselves to be "workflow hosts".
     * 
     * @param forceRefresh if <code>false</code>, the results may be fetched from internal caches;
     *        if <code>true</code>, caching is disabled (TODO obsolete distinction?)
     * 
     * @return the local node and all reachable "workflow host" nodes
     */
    Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh);

    /**
     * Returns a disconnected snapshot of the current network state. Changes to the network state
     * will not affect the returned model.
     * 
     * @return a disconnected model of the current network
     */
    NetworkStateModel getCurrentNetworkState();

    /**
     * Returns an instance of a service registered with the given interface at the local OSGi
     * registry or a proxy of a remote service.
     * 
     * @param iface The interface of the service to get.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform the desired service
     *        is registered. <code>null</code> serves as the local one.
     * @param bundleContext The {@link BundleContext} to use for getting the service at the OSGi
     *        registry.
     * @return An instance of the service if local or a proxy of a service of remote.
     * @throws IllegalStateException if there is no appropriate service found at the local registry.
     *         For the remote case no check that the remote service exists is provided yet.
     */
    Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext) throws IllegalStateException;

    /**
     * Returns an instance of a service registered with the given interface at the local OSGi
     * registry or a proxy of a remote service.
     * 
     * @param iface The interface of the service to get.
     * @param properties The desired properties the service must have.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform the desired service
     *        is registered. <code>null</code> serves as the local one.
     * @param bundleContext The {@link BundleContext} to use for getting the service at the OSGi
     *        registry.
     * @return An instance of the service if local or a proxy of a service of remote.
     * @throws IllegalStateException if there is no appropriate service found at the local registry.
     *         For the remote case no check that the remote service exists is provided yet.
     */
    Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
        throws IllegalStateException;

    /**
     * Synchronously connects to a network peer at a given {@link NetworkContactPoint}.
     * 
     * @param contactPointDefinition the String representation of the {@link NetworkContactPoint} to
     *        connect to; the exact syntax is transport-specific, but is typically similar to
     *        "transportId:host:port"
     * @throws CommunicationException on connection errors
     */
    void addRuntimeNetworkPeer(String contactPointDefinition) throws CommunicationException;

    /**
     * Used to check if this platform is reachable by remote ones.
     * @param checker object used to check reachability.
     */
    void checkReachability(ReachabilityChecker checker);

    /**
     * @return a human-readable summary of the current network state; intended for logging and
     *         administrative output (for example, on an interactive console)
     */
    String getNetworkInformation();
}
