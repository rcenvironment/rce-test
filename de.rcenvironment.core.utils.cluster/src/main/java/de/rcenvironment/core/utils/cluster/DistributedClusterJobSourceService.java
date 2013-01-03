/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster;

import java.util.Map;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Distributed service implementation of {@link ClusterJobSourceService}.
 * 
 * @author Doreen Seider
 */
public interface DistributedClusterJobSourceService {

    /**
     * @param platform platform to call.
     * @param system target queuing system.
     * @param host target host
     * @param port target server
     * @return all stored source information of given platform.
     */
    Map<String, String> getSourceInformation(PlatformIdentifier platform, ClusterQueuingSystem system, String host, int port);

    /**
     * @param system target queuing system.
     * @param host target host
     * @param port target server
     * @return all stored source information of all platforms.
     */
    Map<String, String> getSourceInformation(ClusterQueuingSystem system, String host, int port);
}
