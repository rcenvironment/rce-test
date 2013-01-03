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
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * QueryService provides methods to query the catalog for {@link DataReference}s and the
 * {@link MetaData}.
 * 
 * @author Juergen Klein
 */
public interface QueryService {

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
     * @return the result of the query.
     */
    Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults);

    /**
     * Retrieves a {@link DataReference} for a given guid from the Catalog.
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
     * @return the result of the query.
     */
    MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults);

}
