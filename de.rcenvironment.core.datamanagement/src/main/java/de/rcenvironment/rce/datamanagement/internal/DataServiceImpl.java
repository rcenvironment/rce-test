/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.datamanagement.DataService;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.MetaDataService;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.ParentRevision;
import de.rcenvironment.rce.datamanagement.commons.Revision;

/**
 * Implementation of {@link DataService}.
 * 
 * @author Juergen Klein
 */
abstract class DataServiceImpl implements DataService {
    
    protected static final String PASSED_USER_IS_NOT_VALID = "Passed user representation is not valid.";

    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    
    private static final String MINUS = "-";

    protected CommunicationService communicationService;
    
    protected PlatformService platformService;
    
    protected BundleContext context;
        
    @Override
    public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision)
        throws AuthorizationException {
        
        DataReference dataReference = null;
        DataReference newestSourceDataReference = null;
        PlatformIdentifier platformId = sourceDataReference.getPlatformIdentifier();
        
        QueryService qs = (QueryService) communicationService.getService(QueryService.class, platformId, context);
        newestSourceDataReference = qs.getReference(proxyCertificate, sourceDataReference.getIdentifier());
        if (newestSourceDataReference == null) {
            throw new IllegalStateException("Could not retrieve the newest data reference from catalog at: " + platformId);
        }
        
        // get the meta data of the source data reference
        MetaDataService mds = (MetaDataService) communicationService.getService(MetaDataService.class, platformId, context);
        MetaDataSet metaDataSet = mds.getMetaDataSet(proxyCertificate, newestSourceDataReference, sourceRevision);
        
        if (metaDataSet == null) {
            throw new IllegalStateException("Meta data set of data reference to branch could not be retrieved at: " + platformId);
        }
        metaDataSet = setRequiredMetaData(metaDataSet, proxyCertificate);
        metaDataSet = setCreatorOnMetaData(metaDataSet, proxyCertificate);

        // get the input stream of the revision
        Object object = null;
        if (sourceDataReference.getDataType().equals(DataReferenceType.fileObject)) {
            FileDataService fds = (FileDataService) communicationService.getService(FileDataService.class, platformId, context);
            object = fds.getStreamFromDataReference(proxyCertificate, newestSourceDataReference, sourceRevision);
            if (object == null) {
                throw new IllegalStateException("Could not retrieve input stream: "
                                                + newestSourceDataReference + MINUS + sourceRevision + "@" + platformId);
            }
            
        } else {
            throw new IllegalArgumentException("Not supported data type: " + sourceDataReference.getDataType());
        }
        
        // get the local data backend service
        DataBackend dataBackend = BackendSupport.getDataBackend(newestSourceDataReference.getDataType());
        
        // create the new data reference
        UUID newUUID = UUID.randomUUID();
        URI localLocation = dataBackend.suggestLocation(newUUID, DataReference.FIRST_REVISION);
        
        ParentRevision parentRevision = new ParentRevision(newestSourceDataReference.getIdentifier(), 
                                                           newestSourceDataReference.getPlatformIdentifier(), 
                                                           sourceRevision);
        dataReference = new DataReference(newestSourceDataReference.getDataType(), 
                                          newUUID, 
                                          platformService.getPlatformIdentifier(), 
                                          parentRevision);
        
        // write to data backend
        long fileSize = dataBackend.put(localLocation, object);
        metaDataSet = setFileLengthOnMetaData(metaDataSet, fileSize);
        
        // get the local catalog service and store new data reference and its meta data set
        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        catalogBackend.storeReference(dataReference);
        catalogBackend.storeMetaDataSet(dataReference.getIdentifier(), DataReference.FIRST_REVISION, metaDataSet, true);
        
        return dataReference;
    }

    @Override
    public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        
        DataReference catalogDataReference = catalogBackend.getReference(dataReference.getIdentifier());
        if (catalogDataReference == null) {
            throw new IllegalArgumentException("Data reference not available in catalog: " + dataReference.getIdentifier());
        }
        if (!catalogBackend.isLockedDataReference(catalogDataReference.getIdentifier())) {
            catalogBackend.lockDataReference(catalogDataReference.getIdentifier(), proxyCertificate);
            checkOutOfSync(dataReference, catalogDataReference);
    
            catalogBackend.deleteReference(catalogDataReference.getIdentifier());
            catalogBackend.releaseLockedDataReference(catalogDataReference.getIdentifier(), proxyCertificate);
            de.rcenvironment.rce.datamanagement.backend.DataBackend dataService = 
                BackendSupport.getDataBackend(catalogDataReference.getDataType());
            
            for (Revision revision : catalogDataReference) {
                URI locationToDelete = revision.getLocation();
                dataService.delete(locationToDelete);
            }
        } else {
            throw new IllegalStateException("Requested data reference locked: " + dataReference);
        }
    }

    @Override
    public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException {

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        final DataReference catalogDataReference = catalogBackend.getReference(dataReference.getIdentifier());
        if (catalogDataReference == null) {
            throw new IllegalArgumentException("Data reference not available in cataloge: " + dataReference.getIdentifier());
        }

        if (!catalogDataReference.isValidRevision(revisionNumber)) {
            throw new IllegalArgumentException("Revision not available in cataloge: " + dataReference + MINUS + revisionNumber);
        }

        if (! catalogBackend.isLockedDataReference(catalogDataReference.getIdentifier())) {
            
            catalogBackend.lockDataReference(catalogDataReference.getIdentifier(), proxyCertificate);
            
            checkOutOfSync(dataReference, catalogDataReference);
    
            int highestRevisionNumber = catalogDataReference.getHighestRevisionNumber();
            if (highestRevisionNumber == revisionNumber) {
                throw new IllegalArgumentException("Deleting the newest revision is not allowed: "
                                                   + dataReference + MINUS + revisionNumber);
            }
    
            URI locationToDelete = catalogDataReference.getLocation(revisionNumber);
    
            final DataReference dataReferenceWithDeletedRevision = catalogDataReference;
            dataReferenceWithDeletedRevision.removeRevision(revisionNumber);
            catalogBackend.updateRevisions(dataReferenceWithDeletedRevision);
    
            DataBackend dataBackend = BackendSupport.getDataBackend(dataReferenceWithDeletedRevision.getDataType());
            
            dataBackend.delete(locationToDelete);
            catalogBackend.releaseLockedDataReference(catalogDataReference.getIdentifier(), proxyCertificate);
            return dataReferenceWithDeletedRevision.clone();
        } else {
            throw new IllegalStateException("Requested data reference locked: " + dataReference);
        }
    }

    protected MetaDataSet setRequiredMetaData(MetaDataSet metaDataSet, User proxyCertificate) {
        metaDataSet.setValue(MetaData.AUTHOR, proxyCertificate.getUserId());
        metaDataSet.setValue(MetaData.DATE, getDate());
        return metaDataSet;
    }
    
    protected MetaDataSet setCreatorOnMetaData(MetaDataSet metaDataSet, User proxyCertificate) {
        metaDataSet.setValue(MetaData.CREATOR, proxyCertificate.getUserId());
        return metaDataSet;
    }

    protected MetaDataSet setFileLengthOnMetaData(MetaDataSet metaDataSet, long fileSize) {
        metaDataSet.setValue(MetaData.SIZE, Long.toString(fileSize));
        return metaDataSet;
    }

    /**
     * Checks if the given {@link DataReference} is older than the corresponding {@link DataReference} in the
     * catalog.
     * 
     * @param dataReference
     *            {@link DataReference} to check.
     * @param catalogDataReference
     *            Corresponding {@link DataReference} in the catalog.
     */
    protected synchronized void checkOutOfSync(DataReference dataReference, DataReference catalogDataReference) {
        int highestRevisionNumber = dataReference.getHighestRevisionNumber();
        int catalogHighestRevisionNumber = catalogDataReference.getHighestRevisionNumber();
        if (highestRevisionNumber < catalogHighestRevisionNumber) {
            throw new IllegalStateException(MessageFormat.format("Out of sync with dataReference {1}: highest local revision {2}"
                + " < highest revision in catalogue {3}", dataReference, highestRevisionNumber, catalogDataReference));
        }
    }

    private String getDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

}
