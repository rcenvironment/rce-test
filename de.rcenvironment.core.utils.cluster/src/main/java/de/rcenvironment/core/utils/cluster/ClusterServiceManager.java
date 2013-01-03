/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster;


/**
 * Creates {@link ClusterService}s.
 * 
 * @author Doreen Seider
 */
public interface ClusterServiceManager {

    /**
     * Returns {@link ClusterService} which connects to the host via SSH. If no one was
     * created for given system, host, port, and user a new one will be created, otherwise the
     * existing cached one will be returned.
     * 
     * @param system target queuing system.
     * @param host target host
     * @param port target server
     * @param sshAuthUser given SSH user
     * @param sshAuthPhrase given SSH password
     * @return {@link ClusterService}
     */
    ClusterService retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem system, String host, int port,
        String sshAuthUser, String sshAuthPhrase);
    
}
