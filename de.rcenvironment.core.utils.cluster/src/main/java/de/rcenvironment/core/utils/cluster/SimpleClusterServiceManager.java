/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.utils.cluster;

import de.rcenvironment.commons.ServiceUtils;


/**
 * Simple service implementation of {@link ClusterServiceManager}.
 *
 * @author Doreen Seider
 */
public class SimpleClusterServiceManager implements ClusterServiceManager {

    private static ClusterServiceManager serviceManager = ServiceUtils.createNullService(ClusterServiceManager.class);
    
    protected void bindClusterServiceManager(ClusterServiceManager newServiceManager) {
        serviceManager = newServiceManager;
    }

    protected void unbindClusterServiceManager(ClusterServiceManager oldServiceManager) {
        serviceManager = ServiceUtils.createNullService(ClusterServiceManager.class);
    }
    
    @Override
    public ClusterService retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem system, String host, int port,
        String sshUser, String sshPasswd) {
        return serviceManager.retrieveSshBasedClusterJobInformationService(system, host, port, sshUser, sshPasswd);
    }
}
