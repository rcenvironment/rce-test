/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.util.Collection;
import java.util.UUID;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Service that provides easy access to all catalogs by calling remote {@link QueryService}s in the
 * distributed system.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public interface DistributedQueryService {

    /**
     * Executes a {@link Query} within the whole distributed system and returns the result as a
     * {@link QueryResult}. It is first checked, if the user owns the permissions to execute a
     * query. The result does only contain {@link DataReference}s, where the user has a show
     * permission.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @return the result of the query.
     */
    Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults);

    /**
     * Executes a {@link Query} and returns the result as a {@link QueryResult}. It is first
     * checked, if the user owns the permissions to execute a query. The result does only contain
     * {@link DataReference}s, where the user has a show permission.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to execute the query. If
     *            <code>null</code> the query will be executed on the local platform.
     * @return the result of the query.
     */
    Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults, PlatformIdentifier platform);

    /**
     * Executes a {@link Query} within the whole distributed system and returns the result as a
     * {@link MetaDataResultList}. It is first checked, if the user owns the permissions to execute
     * a query. The result does only contain ids and metadata of the latest revisions of
     * {@link DataReference}s, where the user has a show permission.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @return the result of the query.
     */
    MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults);

    /**
     * Executes a {@link Query} and returns the result as a {@link MetaDataResultList}. It is first
     * checked, if the user owns the permissions to execute a query. The result does only contain
     * ids and metadata of the latest revisions of {@link DataReference}s, where the user has a show
     * permission.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to execute the query. If
     *            <code>null</code> the query will be executed on the local platform.
     * @return the result of the query.
     */
    MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults,
            PlatformIdentifier platform);

    /**
     * Retrieves a {@link DataReference} for a given guid from the Catalog.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReferenceGuid
     *            The guid of the {@link DataReference} to return.
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to query. If <code>null</code> the
     *            reference will be gotten from the local platform.
     * @return the found {@link DataReference} as a clone.
     * @throws AuthorizationException
     *             Thrown if user does not own the SHOW permission for the {@link DataReference}.
     */
    DataReference getReference(User proxyCertificate, UUID dataReferenceGuid, PlatformIdentifier platform)
        throws AuthorizationException;

    /**
     * Retrieves a {@link DataReference} for a given guid by querying the Catalogs of the statically
     * configured "known" platforms.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReferenceGuid
     *            The guid of the {@link DataReference} to return.
     * @return the found {@link DataReference} as a clone.
     * @throws AuthorizationException
     *             Thrown if user does not own the SHOW permission for the {@link DataReference}.
     */
    DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException;

    /**
     * Retrieves a {@link DataReference} for a given guid by querying the Catalogs of the given
     * platforms.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReferenceGuid
     *            The guid of the {@link DataReference} to return.
     * @param platforms
     *            the ids of the platforms to query
     * @return the found {@link DataReference} as a clone.
     * @throws AuthorizationException
     *             Thrown if user does not own the SHOW permission for the {@link DataReference}.
     */
    DataReference getReference(User proxyCertificate, UUID dataReferenceGuid, Collection<PlatformIdentifier> platforms)
        throws AuthorizationException;

}
