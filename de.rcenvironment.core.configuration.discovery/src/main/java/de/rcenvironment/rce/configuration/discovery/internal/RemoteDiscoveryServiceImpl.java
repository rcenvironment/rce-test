/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.discovery.internal;

import javax.jws.WebService;

import de.rcenvironment.rce.jetty.JettyServiceUtils;

/**
 * Defualt SOAP implementation of {@link RemoteDiscoveryService}.
 * 
 * @author Robert Mischke
 */
@WebService(endpointInterface = "de.rcenvironment.rce.configuration.discovery.internal.RemoteDiscoveryService",
    serviceName = "Discovery")
public class RemoteDiscoveryServiceImpl implements RemoteDiscoveryService {

    public String getReflectedCallerAddress() {
        return JettyServiceUtils.getClientIPForCurrentContext();
    }

}
