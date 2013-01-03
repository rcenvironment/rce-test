/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend;

import java.util.Collection;
import java.util.UUID;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Interface of the data management catalog backend.
 * 
 * @author Juergen Klein
 */
public interface CatalogBackend {

    /**
     * Key for a service property.
     */
    String PROVIDER = "de.rcenvironment.rce.datamanagement.backend.catalog.provider";
    
    /**
     * Stores a new {@link DataReference} in the catalog. The given
     * {@link DataReference} is not modified by this method.
     * 
     * @param reference
     *            {@link DataReference} to store in catalog.
     */
    void storeReference(DataReference reference);
    
    /**
     * Deletes a {@link DataReference} and its included {@link Revision}s and {@link MetaData} from the catalog. The given
     * {@link DataReference} is not modified by this method.
     * 
     * @param dataReferenceId
     *            Guid of DataReference to delete from the catalog.
     *            
     * @return <code>true</code> if a {@link DataReference} is deleted, otherwise <code>false</code>.
     */
    boolean deleteReference(UUID dataReferenceId);
    
    /**
     * Searches the catalog for a {@link DataReference} with the given identifier and returns it.
     * 
     * @param dataReferenceId
     *            The identifier of {@link DataReference} to search for.
     * @return found {@link DataReference} or <code>null</code> if there is none.
     */
    DataReference getReference(UUID dataReferenceId);
    
    /**
     * Merges changes made to the {@link Revision}s of a {@link DataReference} into the catalog. Deleted {@link Revision}s
     * are removed from the catalog and added {@link Revision}s are stored in the catalog. Changes made
     * on a stored {@link Revision} are not merged. The given {@link DataReference} is not modified by this method.
     * 
     * @param dataReference
     *            {@link DataReference} to update in the catalog.
     */
    void updateRevisions(DataReference dataReference);
    
    /**
     * Executes a {@link Query} for {@link DataReference}s hold by this database which matches the given query.
     * 
     * @param query
     *            {@link Query} to be executed.
     * @param maxResults
     *            Maximal number of {@link DataReference}s to search for. <code>0</code> means no limit.
     * @return result of {@link Query}.
     */
    Collection<DataReference> executeQuery(Query query, int maxResults);

    /**
     * Executes a {@link Query} for {@link MetaData}s hold by this database which matches the given
     * query.
     * 
     * @param query
     *            {@link Query} to be executed.
     * @param maxResults
     *            Maximal number of {@link DataReference}s to search for. <code>0</code> means no limit.
     * @return result of {@link Query}
     */
    MetaDataResultList executeMetaDataQuery(Query query, Integer maxResults);

    /**
     * Returns {@link MetaDataSet} of a specified {@link DataReference} and {@link Revision}.
     * 
     * @param dataReferenceId
     *            Identifier of {@link DataReference} for which {@link MetaDataSet} is retrieved.
     * @param revisionNr
     *            Revision number > 0
     * @return retrieved {@link MetaDataSet}.
     */
    MetaDataSet getMetaDataSet(UUID dataReferenceId, int revisionNr);
    
    /**
     * Stores a {@link MetaDataSet} of a specified {@link DataReference} and {@link Revision}. Existing {@link MetaData} will be
     * deleted first.
     * 
     * @param dataReferenceId
     *            Identifier of {@link DataReference} for which {@link MetaDataSet} is stored.
     * @param revisionNr
     *            Revision number > 0
     * @param metaDataSet
     *            {@link MetaDataSet} to store
     * @param includeRevisionIndependent
     *            if <code>true</code>revision independent data is stored as well.
     */
    void storeMetaDataSet(UUID dataReferenceId, int revisionNr, MetaDataSet metaDataSet, boolean includeRevisionIndependent);
    
    /**
     * Checks if a {@link DataReference} is locked to avoid dirty reading/writing on it.
     * 
     * @param dataReferenceId Identifier of the {@link DataReference} to check.
     * @return <code>true</code> if the DataReference is locked, <code>false</code> otherwise
     */
    boolean isLockedDataReference(UUID dataReferenceId);
    
    /**
     * Releases a lock on a {@link DataReference}.
     * 
     * @param dataReferenceId Identifier of the {@link DataReference} to unlock.
     * @param proxyCertificate The {@link User} of the user who wants to release the lock.
     * 
     * @return <code>true</code> if a {@link DataReference} is released, otherwise <code>false</code>
     */
    boolean releaseLockedDataReference(UUID dataReferenceId, User proxyCertificate);
    
    /**
     * Locks a DataReference for longer running operations to avoid dirty reading/writing on it.
     * 
     * @param dataReferenceId Identifier of the {@link DataReference} to lock.
     * @param proxyCertificate The ProxyCertificate of the user who wants to lock the {@link DataReference}.
     */
    void lockDataReference(UUID dataReferenceId, User proxyCertificate);

}
