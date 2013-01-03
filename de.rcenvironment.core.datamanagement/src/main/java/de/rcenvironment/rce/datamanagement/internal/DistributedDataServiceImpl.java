/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.DataService;
import de.rcenvironment.rce.datamanagement.DistributedDataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;

/**
 * Implementation of the {@link DistributedDataService}.
 *
 * @author Doreen Seider
 */
abstract class DistributedDataServiceImpl implements DistributedDataService {

    private static final Log LOGGER = LogFactory.getLog(DistributedMetaDataServiceImpl.class);
    
    protected CommunicationService communicationService;
    
    protected BundleContext context;
    
    @Override
    public void deleteReference(User user, DataReference dataReference) throws AuthorizationException {

        DataService dataService = (DataService) communicationService.getService(DataService.class,
                                                                                    dataReference.getPlatformIdentifier(), context);
        try {
            dataService.deleteReference(user, dataReference);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to delete reference on platform: " + dataReference.getPlatformIdentifier(), e);
        }
    }

    @Override
    public DataReference deleteRevision(User user, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException {
        
        DataService dataService = (DataService) communicationService.getService(DataService.class,
                                                                                    dataReference.getPlatformIdentifier(), context);
        try {
            return dataService.deleteRevision(user, dataReference, revisionNumber);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to delete revision on platform: " + dataReference.getPlatformIdentifier(), e);
            return null;
        }
    }

    @Override
    public DataReference branch(User user, DataReference sourceDataReference, Integer sourceRevision,
            PlatformIdentifier repositoryPlatform) throws AuthorizationException {
        
        DataService dataService = (DataService) communicationService.getService(DataService.class, repositoryPlatform, context);
        try {
            return dataService.branch(user, sourceDataReference, sourceRevision);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to branch reference on platform: " + sourceDataReference.getPlatformIdentifier(), e);
            return null;
        }
    }

}
