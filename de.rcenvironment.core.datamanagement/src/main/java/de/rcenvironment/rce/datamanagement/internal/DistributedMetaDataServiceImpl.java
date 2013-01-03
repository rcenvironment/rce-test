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
import de.rcenvironment.rce.datamanagement.DistributedMetaDataService;
import de.rcenvironment.rce.datamanagement.MetaDataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Implementation of the {@link DistributedMetaDataService}.
 *
 * @author Doreen Seider
 */
public class DistributedMetaDataServiceImpl implements DistributedMetaDataService {
    
    private static final Log LOGGER = LogFactory.getLog(DistributedMetaDataServiceImpl.class);
    
    private CommunicationService communicationService;
    
    private BundleContext context;
    
    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }
    
    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    @Override
    public void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
            MetaDataSet metaDataSet, boolean includeRevisionIndependent) throws AuthorizationException {
        MetaDataService metaDataService = (MetaDataService) communicationService.getService(MetaDataService.class,
                                                                                            dataReference.getPlatformIdentifier(), context);
        try {
            metaDataService.updateMetaDataSet(proxyCertificate, dataReference, revisionNumber, metaDataSet, includeRevisionIndependent);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to get reference on platform: " + dataReference.getPlatformIdentifier(), e);
        }
        
    }

    @Override
    public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
        throws AuthorizationException {
        MetaDataService metaDataService = (MetaDataService) communicationService.getService(MetaDataService.class,
                                                                                            dataReference.getPlatformIdentifier(), context);
        try {
            return metaDataService.getMetaDataSet(proxyCertificate, dataReference, revisionNumber);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to get reference on platform: " + dataReference.getPlatformIdentifier(), e);
            return null;
        }
    }

}
