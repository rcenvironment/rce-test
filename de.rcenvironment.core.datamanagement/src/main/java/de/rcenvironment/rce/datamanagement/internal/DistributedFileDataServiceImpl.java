/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.datamanagement.DistributedFileDataService;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DistributableInputStream;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Implementation of the {@link DistributedFileDataService}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke (added stream buffering)
 */
public class DistributedFileDataServiceImpl extends DistributedDataServiceImpl implements DistributedFileDataService {

    // 128kb (arbitrarily chosen; BufferedInputStream default is 8kb)
    private static final int REMOTE_STREAM_BUFFER_SIZE = 128 * 1024;

    private static final Log LOGGER = LogFactory.getLog(DistributedMetaDataServiceImpl.class);

    private FileDataService fileDataService;

    private PlatformService platformService;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void bindFileDataService(FileDataService newFileDataService) {
        fileDataService = newFileDataService;
    }

    @Override
    public InputStream getStreamFromDataReference(User user, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException {

        FileDataService dataService = (FileDataService) communicationService.getService(FileDataService.class,
                                                                                        dataReference.getPlatformIdentifier(), context);
        try {
            InputStream rawStream = dataService.getStreamFromDataReference(user, dataReference, revisionNumber);
            return new BufferedInputStream(rawStream, REMOTE_STREAM_BUFFER_SIZE);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to get stream from reference on platform: " + dataReference.getPlatformIdentifier(), e);
            return null;
        }
    }

    @Override
    public DataReference newReferenceFromStream(User user, InputStream inputStream, MetaDataSet metaDataSet, PlatformIdentifier platform)
        throws AuthorizationException {

        if (platform == null) {
            platform = platformService.getPlatformIdentifier();
        }

        DataReference dataRef;
        
        if (!platformService.isLocalPlatform(platform)) {
            dataRef = fileDataService.newReferenceFromStream(user, inputStream, null);
        } else {
            dataRef = fileDataService.newReferenceFromStream(user, inputStream, metaDataSet);
        }
                
        if (!platformService.isLocalPlatform(platform)) {
//            DataReference localDataRef = dataRef;
            inputStream = new DistributableInputStream(user, dataRef, DataReference.FIRST_REVISION, inputStream);

            FileDataService dataService = (FileDataService) communicationService.getService(FileDataService.class, platform, context);
            try {
                dataRef = dataService.newReferenceFromStream(user, inputStream, metaDataSet);
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to create new reference from stream on platform: " + platform, e);
                return null;
                // removed due to deadlocks (timeouts). thus, currently file will remain on remote
                // host even it was not intent to be stored there
//            } finally {
//                fileDataService.deleteReference(user, localDataRef);
            }
        }
        
        return dataRef;
    }

    @Override
    public DataReference newRevisionFromStream(User user, DataReference dataReference, InputStream inputStream, MetaDataSet metaDataSet)
        throws AuthorizationException {

        DataReference dataRef;
        
        if (!platformService.isLocalPlatform(dataReference.getPlatformIdentifier())) {
            dataRef = fileDataService.newReferenceFromStream(user, inputStream, null);
        } else {
            dataRef = fileDataService.newReferenceFromStream(user, inputStream, metaDataSet);
        }
                
        if (!platformService.isLocalPlatform(dataReference.getPlatformIdentifier())) {
//            DataReference localDataRef = dataRef;
            inputStream = new DistributableInputStream(user, dataRef, DataReference.FIRST_REVISION, inputStream);

            FileDataService dataService = (FileDataService) communicationService.getService(FileDataService.class,
                                                                                            dataReference.getPlatformIdentifier(), context);
            
            try {
                dataRef = dataService.newRevisionFromStream(user, dataReference, inputStream, metaDataSet);
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to create new revision from stream on platform: " + dataReference.getPlatformIdentifier(), e);
                return null;
                // removed due to deadlocks (timeouts). thus, currently file will remain on remote
                // host even it was not intent to be stored there
//            } finally {
//                fileDataService.deleteRevision(user, localDataRef, localDataRef.getHighestRevisionNumber());
            }
        }
        

        return dataRef;
    }
}
