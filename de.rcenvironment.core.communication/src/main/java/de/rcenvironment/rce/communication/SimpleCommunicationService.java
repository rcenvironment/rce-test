/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import java.util.Set;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.core.communication.model.NetworkStateModel;
import de.rcenvironment.core.communication.model.NodeInformationRegistry;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;

/**
 * Class serving as an abstraction of the {@link CommunicationService} regarding the OSGi API.
 * 
 * @author Doreen Seider
 * @author Heinrich Wendel
 */
public class SimpleCommunicationService {

    private static CommunicationService communicationService = ServiceUtils.createNullService(CommunicationService.class);

    private static PlatformService platformService = ServiceUtils.createNullService(PlatformService.class);

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void unbindPlatformService(PlatformService oldPlatformService) {
        platformService = ServiceUtils.createNullService(PlatformService.class);
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void unbindCommunicationService(CommunicationService oldCommunicationService) {
        communicationService = ServiceUtils.createNullService(CommunicationService.class);
    }

    /**
     * Returns all known RCE nodes that could be providing accessible services. More precisely, the
     * returned set contains the local node and all request-reachable nodes that have declared
     * themselves to be "workflow hosts".
     * 
     * @return the local node and all reachable "workflow host" nodes
     */
    public synchronized Set<PlatformIdentifier> getAvailableNodes() {
        return communicationService.getAvailableNodes(false);
    }

    /**
     * Returns a disconnected snapshot of the current network state. Changes to the network state
     * will not affect the returned model.
     * 
     * @return a disconnected model of the current network
     */
    public synchronized NetworkStateModel getCurrentNetworkState() {
        return communicationService.getCurrentNetworkState();
    }

    /**
     * Checks if the specified {@link PlatformIdentifier} represent the local RCE platform.
     * 
     * @param platformIdentifier a {@link PlatformIdentifier} that should be compared to the local
     *        RCE {@link PlatformIdentifier}.
     * @return True or false.
     */
    public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
        return platformService.isLocalPlatform(platformIdentifier);
    }

    /**
     * @return the local {@link NodeInformationRegistry}
     */
    public NodeInformationRegistry getNodeInformationRegistry() {
        return NodeInformationRegistryImpl.getInstance();
    }

    public PlatformIdentifier getLocalNodeId() {

        return platformService.getPlatformIdentifier();
    }
}
