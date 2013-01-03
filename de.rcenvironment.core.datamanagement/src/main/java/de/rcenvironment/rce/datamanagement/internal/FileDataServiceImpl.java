/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.DistributableInputStream;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Implementation of the {@link FileDataService}.
 * 
 * @author Juergen Klein
 */
public class FileDataServiceImpl extends DataServiceImpl implements FileDataService {

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    @Override
    @AllowRemoteAccess
    public InputStream getStreamFromDataReference(User user, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException {
        Assertions.isTrue(user.isValid(), PASSED_USER_IS_NOT_VALID);

        DataBackend dataBackend = BackendSupport.getDataBackend(dataReference.getDataType());
        return new DistributableInputStream(user, dataReference, revisionNumber,
                                            (InputStream) dataBackend.get(dataReference.getLocation(revisionNumber)));
    }

    @Override
    @AllowRemoteAccess
    public DataReference newReferenceFromStream(User user, InputStream inputStream, MetaDataSet metaDataSet)
        throws AuthorizationException {
        Assertions.isTrue(user.isValid(), PASSED_USER_IS_NOT_VALID);

        if (metaDataSet == null) {
            metaDataSet = new MetaDataSet();
        }
        metaDataSet = setRequiredMetaData(metaDataSet, user);
        metaDataSet = setCreatorOnMetaData(metaDataSet, user);

        UUID uuid = UUID.randomUUID();

        // store input stream
        DataBackend dataBackend = BackendSupport.getDataBackend(DataReferenceType.fileObject);
        URI location = dataBackend.suggestLocation(uuid, DataReference.FIRST_REVISION);
        long submittedBytes = dataBackend.put(location, inputStream);

        // create a new data reference
        DataReference dataReference = new DataReference(DataReferenceType.fileObject, uuid,
                                                        platformService.getPlatformIdentifier());
        // .. and add the very first revision
        dataReference.addRevision(DataReference.FIRST_REVISION, location);

        // get the catalog backend  and store the newly created data reference
        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        catalogBackend.lockDataReference(dataReference.getIdentifier(), user);
        catalogBackend.storeReference(dataReference);
        metaDataSet = setFileLengthOnMetaData(metaDataSet, submittedBytes);
        catalogBackend.storeMetaDataSet(dataReference.getIdentifier(), DataReference.FIRST_REVISION, metaDataSet, true);
        catalogBackend.releaseLockedDataReference(dataReference.getIdentifier(), user);
        return dataReference;
    }

    @Override
    public DataReference newRevisionFromStream(User user, DataReference dataReference, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException {
        Assertions.isTrue(user.isValid(), PASSED_USER_IS_NOT_VALID);

        if (metaDataSet == null) {
            metaDataSet = new MetaDataSet();
        }
        metaDataSet = setRequiredMetaData(metaDataSet, user);

        UUID uuid = dataReference.getIdentifier();

        // get newest data reference from catalog
        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        final DataReference catalogDataReference = catalogBackend.getReference(dataReference.getIdentifier());
        if (catalogDataReference == null) {
            throw new IllegalArgumentException("Data reference not available in catalog: " + dataReference);
        }

        if (! catalogBackend.isLockedDataReference(catalogDataReference.getIdentifier())) {
            catalogBackend.lockDataReference(catalogDataReference.getIdentifier(), user);
            checkOutOfSync(dataReference, catalogDataReference);
    
            final int newRevisionNumber = catalogDataReference.getHighestRevisionNumber() + 1;
    
            DataBackend dataBackend = BackendSupport.getDataBackend(dataReference.getDataType());
            URI location = dataBackend.suggestLocation(uuid, newRevisionNumber);
    
            final DataReference dataReferenceWithAddedRevision = catalogDataReference;
            dataReferenceWithAddedRevision.addRevision(newRevisionNumber, location);
            catalogDataReference.addRevision(newRevisionNumber, location);
    
            // reserve new revision number in memory
            catalogBackend.updateRevisions(catalogDataReference);
    
            // write input stream to data backend
            Long submittedBytes = dataBackend.put(location, inputStream);
    
            // add the new revision
            dataReference.addRevision(newRevisionNumber, location);
    
            setFileLengthOnMetaData(metaDataSet, submittedBytes);
            catalogBackend.storeMetaDataSet(dataReferenceWithAddedRevision.getIdentifier(), newRevisionNumber, metaDataSet, false);
            
            catalogBackend.releaseLockedDataReference(catalogDataReference.getIdentifier(), user);
            
            return dataReferenceWithAddedRevision;
        } else {
            throw new IllegalStateException("Requested data reference locked: " + dataReference);
        }

    }

}
