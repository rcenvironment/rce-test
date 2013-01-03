/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.internal;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.cluster.ClusterJobSourceService;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;
import de.rcenvironment.core.utils.cluster.DistributedClusterJobSourceService;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;

/**
 * Implementation of {@link DistributedClusterJobSourceService}.
 * @author Doreen Seider
 */
public class DistributedClusterJobSourceServiceImpl implements DistributedClusterJobSourceService {

    private static final Log LOGGER = LogFactory.getLog(DistributedClusterJobSourceServiceImpl.class);
    
    private static CommunicationService communicationService;
    
    private static PlatformService platformService;
    
    private static ClusterJobSourceService clusterJobSourceService;

    private static BundleContext context;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }
    
    protected void bindClusterJobSourceService(ClusterJobSourceService newClusterJobSourceService) {
        clusterJobSourceService = newClusterJobSourceService;
    }
    
    @Override
    public Map<String, String> getSourceInformation(PlatformIdentifier platform, ClusterQueuingSystem system, String host, int port) {
        if (platform == null) {
            return clusterJobSourceService.getSourceInformation(system, host, port);
        } else {
            ClusterJobSourceService remoteSourceInformationService = (ClusterJobSourceService)
                communicationService.getService(ClusterJobSourceService.class, platform, context);
            try {
                return remoteSourceInformationService.getSourceInformation(system, host, port);
            } catch (RuntimeException e) {
                LOGGER.error(MessageFormat.format("Failed get cluster job source information from remote platform: %s", platform), e);
                return new HashMap<String, String>();
            }
        }
    }

    @Override
    public Map<String, String> getSourceInformation(ClusterQueuingSystem system, String host, int port) {
        
        Map<String, String> sourceInformation = new HashMap<String, String>();
        
        sourceInformation.putAll(getSourceInformation(null, system, host, port));
        
        for (PlatformIdentifier platform : platformService.getRemotePlatforms()) {
            sourceInformation.putAll(getSourceInformation(platform, system, host, port));
        }
        return sourceInformation;
    }


}
