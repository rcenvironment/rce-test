/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.cluster.internal;

import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.core.utils.cluster.ClusterService;
import de.rcenvironment.core.utils.cluster.ClusterServiceManager;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;
import de.rcenvironment.core.utils.cluster.torque.internal.TorqueClusterService;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfiguration;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfigurationFactory;

/**
 * Implementation of {@link ClusterServiceManager}.
 * @author Doreen Seider
 */
public class ClusterServiceManagerImpl implements ClusterServiceManager {

    private static final String SEPARATOR = "!§$%&";
    
    private Map<String, ClusterService> informationServices = new HashMap<String, ClusterService>();

    @Override
    public synchronized ClusterService retrieveSshBasedClusterJobInformationService(ClusterQueuingSystem system, String host,
        int port, String sshAuthUser, String sshAuthPhrase) {
        
        String informationServiceId = createIdentifier(system, host, port, sshAuthUser);
        ClusterService informationService;
        
        if (informationServices.containsKey(informationServiceId)) {
            informationService = informationServices.get(informationServiceId);
        } else {
            switch (system) {
            case TORQUE:
                SshSessionConfiguration sshConfiguration = SshSessionConfigurationFactory
                    .createSshSessionConfigurationWithAuthPhrase(host, port, sshAuthUser, sshAuthPhrase);
                informationService = new TorqueClusterService(sshConfiguration);
                informationServices.put(informationServiceId, informationService);
                break;
            default:
                throw new UnsupportedOperationException("Cluster queuing system not supported: " + system);
            }            
        }
        return informationService;
    }
    
    private String createIdentifier(ClusterQueuingSystem system, String host, int port, String sshAuthUser) {
        StringBuffer buffer = new StringBuffer(host);
        buffer.append(SEPARATOR);
        buffer.append(port);
        buffer.append(SEPARATOR);
        buffer.append(system.toString());
        buffer.append(SEPARATOR);
        buffer.append(sshAuthUser);
        return buffer.toString();
    }

}
