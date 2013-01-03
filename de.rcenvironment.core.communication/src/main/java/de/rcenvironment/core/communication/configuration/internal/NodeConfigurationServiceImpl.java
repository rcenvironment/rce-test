/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.configuration.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.configuration.NodeConfigurationService;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;
import de.rcenvironment.core.communication.utils.NetworkContactPointUtils;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Default {@link NodeConfigurationService} implementation.
 * 
 * @author Robert Mischke
 */
public class NodeConfigurationServiceImpl implements NodeConfigurationService {

    // TODO temporary hardcoded default for actual RCE instances; see Mantis #8074
    private static final int STARTUP_INITIAL_CONNECT_DELAY_MSEC = 2500;

    private final List<NetworkContactPoint> serverContactPoints;

    private final List<NetworkContactPoint> initialNetworkPeers;

    private final Log log = LogFactory.getLog(getClass());

    private NetworkNodeInformationImpl localNodeInformation;

    private CommunicationConfiguration configuration;

    public NodeConfigurationServiceImpl() {
        serverContactPoints = new ArrayList<NetworkContactPoint>();
        initialNetworkPeers = new ArrayList<NetworkContactPoint>();
    }

    @Override
    public NodeIdentifier getLocalNodeId() {
        return localNodeInformation.getWrappedNodeId();
    }

    @Override
    public NetworkNodeInformation getLocalNodeInformation() {
        return localNodeInformation;
    }

    @Override
    public List<NetworkContactPoint> getServerContactPoints() {
        return Collections.unmodifiableList(serverContactPoints);
    }

    @Override
    public List<NetworkContactPoint> getInitialNetworkContactPoints() {
        return Collections.unmodifiableList(initialNetworkPeers);
    }

    @Override
    public long getDelayBeforeStartupConnectAttempts() {
        // TODO temporary hardcoded default for actual RCE instances; see Mantis #8074
        return STARTUP_INITIAL_CONNECT_DELAY_MSEC;
    }

    @Override
    public int getRequestTimeoutMsec() {
        return configuration.getRequestTimeoutMsec();
    }

    @Override
    public long getForwardingTimeoutMsec() {
        return configuration.getForwardingTimeoutMsec();
    }

    protected void bindPlatformService(PlatformService platformService) {
        createLocalNodeInformation(platformService);
        parseNetworkConfiguration(platformService);
    }

    private void createLocalNodeInformation(PlatformService platformService) {
        PlatformIdentityInformation identityInformation = platformService.getIdentityInformation();
        localNodeInformation = new NetworkNodeInformationImpl(identityInformation.getPersistentNodeId());
        localNodeInformation.setDisplayName(identityInformation.getDisplayName());
        // TODO temporary
        localNodeInformation.setIsWorkflowHost(identityInformation.getIsWorkflowHost());
    }

    private void parseNetworkConfiguration(PlatformService platformService) {
        configuration = platformService.getConfiguration();
        // "provided" (server) NCPs
        List<String> serverContactPointDefs = configuration.getProvidedContactPoints();
        log.debug("Parsing " + serverContactPointDefs.size() + " provided contact points");
        for (String contactPointDef : serverContactPointDefs) {
            NetworkContactPoint ncp;
            try {
                ncp = NetworkContactPointUtils.parseStringRepresentation(contactPointDef);
                log.debug("Adding configured server NCP " + ncp);
                serverContactPoints.add(ncp);
            } catch (IllegalArgumentException e) {
                log.error("Unable to parse contact point definition: " + contactPointDef);
            }
        }
        // "remote" (client) NCPs
        List<String> remoteContactPointDefs = configuration.getRemoteContactPoints();
        log.debug("Parsing " + remoteContactPointDefs.size() + " remote contact points");
        for (String contactPointDef : remoteContactPointDefs) {
            NetworkContactPoint ncp;
            try {
                ncp = NetworkContactPointUtils.parseStringRepresentation(contactPointDef);
                log.debug("Adding configured remote NCP " + ncp);
                initialNetworkPeers.add(ncp);
            } catch (IllegalArgumentException e) {
                log.error("Unable to parse contact point definition: " + contactPointDef);
            }
        }
    }

}
