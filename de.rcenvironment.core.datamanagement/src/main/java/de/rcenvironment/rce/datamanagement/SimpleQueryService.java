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

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Class providing easy access to all {@link QueryService}s in the distributed system.
 * 
 * @author Doreen Seider
 */
public class SimpleQueryService {

    private static DistributedQueryService queryService = ServiceUtils.createNullService(DistributedQueryService.class);

    private User certificate;

    /**
     * Only used by OSGi component instantiation.
     */
    @Deprecated
    public SimpleQueryService() {

    }

    public SimpleQueryService(User certificate) {
        this.certificate = certificate;
    }

    protected void bindDistributedQueryService(DistributedQueryService newQueryService) {
        queryService = newQueryService;
    }

    protected void unbindDistributedQueryService(DistributedQueryService newQueryService) {
        queryService = ServiceUtils.createNullService(DistributedQueryService.class);
    }

    /**
     * Executes a {@link Query} within the whole distributed system and returns the result as a
     * {@link QueryResult}. It is first checked, if the user owns the permissions to execute a
     * query. The result does only contain {@link DataReference}s, where the user has a show
     * permission.
     * 
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @return the result of the query.
     */
    public Collection<DataReference> executeQuery(Query query, Integer maxResults) {
        return queryService.executeQuery(certificate, query, maxResults);
    }

    /**
     * Executes a {@link Query} and returns the result as a {@link QueryResult}. It is first
     * checked, if the user owns the permissions to execute a query. The result does only contain
     * {@link DataReference}s, where the user has a show permission.
     * 
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to execute the query. If
     *            <code>null</code> the query will be executed on the local platform.
     * @return the result of the query.
     */
    public Collection<DataReference> executeQuery(Query query, Integer maxResults, PlatformIdentifier platform) {
        return queryService.executeQuery(certificate, query, maxResults, platform);
    }

    /**
     * Executes a {@link Query} within the whole distributed system and returns the result as a
     * {@link MetaDataResultList}. It is first checked, if the user owns the permissions to execute
     * a query. The result does only contain {@link DataReference}s, where the user has a show
     * permission.
     * 
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @return the result of the query.
     */
    public MetaDataResultList executeMetaDataQuery(Query query, Integer maxResults) {
        return queryService.executeMetaDataQuery(certificate, query, maxResults);
    }

    /**
     * Executes a {@link Query} and returns the result as a {@link MetaDataResultList}. It is first
     * checked, if the user owns the permissions to execute a query. The result does only contain
     * {@link DataReference}s, where the user has a show permission.
     * 
     * @param query
     *            The query to execute
     * @param maxResults
     *            Maximal number of DataReference to search for. Zero means no limit
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to execute the query. If
     *            <code>null</code> the query will be executed on the local platform.
     * @return the result of the query.
     */
    public MetaDataResultList executeMetaDataQuery(Query query, Integer maxResults, PlatformIdentifier platform) {
        return queryService.executeMetaDataQuery(certificate, query, maxResults, platform);
    }

    /**
     * Retrieves a {@link DataReference} for a given guid from the Catalog.
     * 
     * @param dataReferenceGuid
     *            The guid of the {@link DataReference} to return.
     * @param platform
     *            The {@link PlatformIdentifier} of the platform to query. If <code>null</code> the
     *            reference will be gotten from the local platform.
     * @return the found {@link DataReference} as a clone.
     * @throws AuthorizationException
     *             Thrown if user does not own the SHOW permission for the {@link DataReference}.
     */
    public DataReference getReference(UUID dataReferenceGuid, PlatformIdentifier platform) throws AuthorizationException {
        return queryService.getReference(certificate, dataReferenceGuid, platform);
    }

    /**
     * Retrieves a {@link DataReference} for a given guid from the Catalog.
     * 
     * @param dataReferenceGuid
     *            The guid of the {@link DataReference} to return.
     * @return the found {@link DataReference} as a clone.
     * @throws AuthorizationException
     *             Thrown if user does not own the SHOW permission for the {@link DataReference}.
     */
    public DataReference getReference(UUID dataReferenceGuid) throws AuthorizationException {
        return queryService.getReference(certificate, dataReferenceGuid);
    }
}
