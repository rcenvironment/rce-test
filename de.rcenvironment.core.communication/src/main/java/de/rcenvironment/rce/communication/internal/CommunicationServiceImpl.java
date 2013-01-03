/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.core.communication.management.CommunicationManagementService;
import de.rcenvironment.core.communication.model.NetworkStateModel;
import de.rcenvironment.core.communication.model.NetworkStateNode;
import de.rcenvironment.core.communication.model.internal.NodeInformationHolder;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;
import de.rcenvironment.core.communication.routing.NetworkRoutingService;
import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.core.communication.utils.NetworkContactPointUtils;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.ReachabilityChecker;
import de.rcenvironment.rce.communication.impl.ReachabilityCheckerImpl;
import de.rcenvironment.rce.communication.service.RemoteServiceHandler;
import de.rcenvironment.rce.communication.service.internal.RemoteServiceCallBridge;

/**
 * Implementation of the {@link CommunicationService}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class CommunicationServiceImpl implements CommunicationService, NetworkTopologyChangeListener {

    private static final String REMOVED_FROM_KNOWN_PLATFORMS = "; it will be removed from list of known platforms";

    private static final String SERVICE_NOT_AVAILABLE_ERROR = "The requested service is not available: ";

    private static final Log LOGGER = LogFactory.getLog(CommunicationServiceImpl.class);

    private static Set<PlatformIdentifier> cachedAvailableNodes;

    private RemoteServiceHandler remoteServiceHandler;

    private PlatformService platformService;

    private BundleContext context;

    private CommunicationConfiguration configuration;

    private boolean usingNewCommunicationLayer;

    private CommunicationManagementService newManagementService;

    private NetworkRoutingService newRoutingService;

    private NetworkStateModel cachedNetworkState;

    private NodeInformationRegistryImpl nodeInformationRegistry;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
        // TODO copy this to simplify the migration; can be removed later
        configuration = platformService.getConfiguration();
        nodeInformationRegistry = NodeInformationRegistryImpl.getInstance();
        usingNewCommunicationLayer = configuration.getUseNewCommunicationLayer();
        LOGGER.debug("Using new communication layer: " + usingNewCommunicationLayer);
        if (usingNewCommunicationLayer) {
            RemoteServiceCallBridge.setNewRoutingService(newRoutingService);
            try {
                // for now, trigger this from here; may be moved to management service itself
                // TODO run this asynchronously?
                newManagementService.startUpNetwork();
            } catch (CommunicationException e) {
                LOGGER.error("Error while starting up network layer", e);
            }
        }
    }

    protected void deactivate(BundleContext bundleContext) {
        cachedAvailableNodes = null;
        if (usingNewCommunicationLayer) {
            // for now, trigger this from here; may be moved to management service itself
            newManagementService.shutDownNetwork();
        }
    }

    protected void bindRemoteServiceHandler(RemoteServiceHandler newRemoteServiceHandler) {
        remoteServiceHandler = newRemoteServiceHandler;
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void bindCommunicationManagementService(CommunicationManagementService managementService) {
        this.newManagementService = managementService;
    }

    protected void bindNetworkRoutingService(NetworkRoutingService routingService) {
        this.newRoutingService = routingService;
    }

    @Override
    public synchronized Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
        if (forceRefresh || cachedAvailableNodes == null) {
            cachedAvailableNodes = determineAvailableNodes(true);
        }
        return cachedAvailableNodes;
    }

    @Override
    public synchronized NetworkStateModel getCurrentNetworkState() {
        // TODO reduce scope of synchronization
        if (cachedNetworkState == null) {
            cachedNetworkState = createNetworkStateModel();
        }
        return cachedNetworkState;
    }

    @Override
    // TODO apply generics -- misc_ro
    public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext) {
        return getService(iface, null, platformIdentifier, bundleContext);
    }

    @Override
    // TODO apply generics -- misc_ro
    public Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier,
        BundleContext bundleContext) {

        if (platformIdentifier == null || platformService.isLocalPlatform(platformIdentifier)) {
            return getLocalService(iface, properties, bundleContext);
        } else {
            return remoteServiceHandler.createServiceProxy(platformIdentifier, iface, null, properties);
        }
    }

    @Override
    public void addRuntimeNetworkPeer(String contactPointDefinition) throws CommunicationException {
        newManagementService.connectToRuntimePeer(NetworkContactPointUtils.parseStringRepresentation(contactPointDefinition));
    }

    @Override
    @AllowRemoteAccess
    public void checkReachability(ReachabilityChecker checker) {
        checker.checkForReachability(platformService.getPlatformIdentifier());
    }

    private Set<PlatformIdentifier> determineAvailableNodes(boolean restrictToWorkflowHostsAndSelf) {
        if (usingNewCommunicationLayer) {
            Set<PlatformIdentifier> nodeIds = newRoutingService.getReachableNodes(restrictToWorkflowHostsAndSelf);
            retrieveUnknownNodeNames(nodeIds);
            return nodeIds;
        } else {
            return retrieveReachingPlatforms(retrieveReachablePlatforms());
        }
    }

    private NetworkStateModel createNetworkStateModel() {
        NetworkStateModel model = new NetworkStateModel();
        for (PlatformIdentifier node : newRoutingService.getReachableNodes(false)) {
            String nodeId = node.getNodeId();
            NetworkStateNode treeNode = new NetworkStateNode(nodeId);
            treeNode.setDisplayName(nodeInformationRegistry.getNodeInformation(nodeId).getDisplayName());
            treeNode.setIsWorkflowHost(nodeInformationRegistry.getNodeInformation(nodeId).isWorkflowHost());
            model.addNode(nodeId, treeNode);
        }
        model.getNode(platformService.getPlatformIdentifier().getNodeId()).setIsLocalNode(true);
        return model;
    }

    /**
     * Fetches display names of nodes that are not known yet.
     * 
     * TODO obsolete; remove if the message never shows up during testing
     * 
     * @param nodeIds
     */
    @Deprecated
    private void retrieveUnknownNodeNames(Set<PlatformIdentifier> nodeIds) {
        for (PlatformIdentifier node : nodeIds) {
            String nodeId = node.getNodeId();
            NodeInformationHolder metaInformationHolder =
                NodeInformationRegistryImpl.getInstance().getWritableNodeInformation(nodeId);
            if (metaInformationHolder.getDisplayName() == null) {
                LOGGER.warn("No name information for node " + nodeId + "; setting '<unknown>' placeholder");
                metaInformationHolder.setDisplayName("<unknown>");
            }
        }
    }

    private Set<PlatformIdentifier> retrieveReachablePlatforms() {
        List<PlatformIdentifier> visited = new ArrayList<PlatformIdentifier>();
        visited.add(platformService.getPlatformIdentifier());
        return recursivelyGetReachablePlatforms(visited.get(0), visited);
    }

    private Set<PlatformIdentifier> recursivelyGetReachablePlatforms(PlatformIdentifier platformIdentifier,
        Collection<PlatformIdentifier> visisted) {

        Set<PlatformIdentifier> platforms = new HashSet<PlatformIdentifier>();

        // Get Target Platforms
        PlatformService ps = (PlatformService) getService(PlatformService.class, platformIdentifier, context);
        try {
            // get the platform identifier from remote because it could contain a name which is only
            // configured locally
            platformIdentifier = ps.getPlatformIdentifier();

            // Recurse
            for (PlatformIdentifier remoteIdentifier : ps.getRemotePlatforms()) {
                if (!visisted.contains(remoteIdentifier)) {
                    visisted.add(remoteIdentifier);
                    platforms.addAll(recursivelyGetReachablePlatforms(remoteIdentifier, visisted));
                }
            }
            // if getRemotePlatforms could be called and no Exception was thrown, this platform is
            // reachable and thus add the platform to known platforms
            platforms.add(platformIdentifier);
        } catch (RuntimeException e) {
            LOGGER.warn("platform not reachable: " + platformIdentifier + REMOVED_FROM_KNOWN_PLATFORMS, e);
        }

        return platforms;
    }

    private Set<PlatformIdentifier> retrieveReachingPlatforms(Set<PlatformIdentifier> reachablePlatforms) {

        // TODO review: terminology - what exactly is "reaching" here? - misc_ro
        Set<PlatformIdentifier> reachingPlatforms = new HashSet<PlatformIdentifier>();
        ReachabilityChecker checker = new ReachabilityCheckerImpl();

        for (PlatformIdentifier pi : reachablePlatforms) {
            try {
                if (!platformService.isLocalPlatform(pi)) {
                    CommunicationService remoteCommService = (CommunicationService) getService(CommunicationService.class, pi, context);
                    remoteCommService.checkReachability(checker);
                }
                reachingPlatforms.add(pi);
            } catch (UndeclaredThrowableException e) {
                LOGGER.warn("Error while trying to contact platform " + pi + REMOVED_FROM_KNOWN_PLATFORMS, e);
            }
            // fetch remote identity information; not stored or used yet, for testing only
            try {
                if (!platformService.isLocalPlatform(pi)) {
                    PlatformService remotePlatformService = (PlatformService) getService(PlatformService.class, pi, context);
                    PlatformIdentityInformation remoteIdentityInfo = remotePlatformService.getIdentityInformation();
                    if (remoteIdentityInfo != null) {
                        LOGGER.debug("Contacted " + pi + " and found platform id " + remoteIdentityInfo.getPersistentNodeId()
                            + " with name " + remoteIdentityInfo.getDisplayName());
                    } else {
                        LOGGER.warn("Contacted " + pi + " but received 'null' as its identity information");
                    }
                }
            } catch (UndeclaredThrowableException e) {
                LOGGER.warn("Error while trying to fetch remote identity information for " + pi, e);
            }
        }
        return reachingPlatforms;
    }

    private Object getLocalService(Class<?> iface, Map<String, String> properties, BundleContext bundleContext) {

        ServiceReference serviceReference;

        if (properties != null && properties.size() > 0) {
            try {
                ServiceReference[] serviceReferences = bundleContext.getServiceReferences(iface.getName(),
                    ServiceUtils.constructFilter(properties));
                if (serviceReferences != null) {
                    serviceReference = serviceReferences[0];
                } else {
                    throw new IllegalStateException(SERVICE_NOT_AVAILABLE_ERROR + iface.getName());
                }
            } catch (InvalidSyntaxException e) {
                throw new IllegalStateException();
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(SERVICE_NOT_AVAILABLE_ERROR + iface.getName());
            }
        } else {
            serviceReference = bundleContext.getServiceReference(iface.getName());
        }

        if (serviceReference != null) {
            Object service = bundleContext.getService(serviceReference);
            if (service != null) {
                return service;
            } else {
                throw new IllegalStateException(SERVICE_NOT_AVAILABLE_ERROR + iface.getName());
            }
        } else {
            throw new IllegalStateException(SERVICE_NOT_AVAILABLE_ERROR + iface.getName());
        }
    }

    @Override
    public synchronized void onNetworkTopologyChanged() {
        // invalidate platform cache
        cachedAvailableNodes = null;
        cachedNetworkState = null;
        LOGGER.debug("Received network topology change event; invalidated local caches");
    }

    @Override
    public String getNetworkInformation() {
        return newRoutingService.getNetworkSummary();
    }

}
