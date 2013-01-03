/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.service.internal;

import java.io.IOException;
import java.net.URI;

import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnectionFactory;

/**
 * Implementation of the {@link RemoteFileConnectionFactory} creating
 * {@link ServiceRemoteFileConnection} objects.
 * 
 * @author Doreen Seider
 */
public class ServiceRemoteFileConnectionFactory implements RemoteFileConnectionFactory {

    private BundleContext context;

    private CommunicationService communicationService;
    
    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    @Override
    public RemoteFileConnection createRemoteFileConnection(User cert, URI uri) throws IOException {
        return new ServiceRemoteFileConnection(cert, uri, communicationService, context);
    }

}
