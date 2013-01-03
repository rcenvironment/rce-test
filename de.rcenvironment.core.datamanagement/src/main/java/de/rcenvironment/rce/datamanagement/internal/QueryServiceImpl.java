/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.util.Collection;
import java.util.UUID;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;


/**
 * Implementation of {@link QueryService}.
 *
 * @author Juergen Klein
 */
public class QueryServiceImpl implements QueryService {

    private static final String PASSED_PROXY_CERTIFICATE_IS_NOT_VALID = "Passed ProxyCertificate is not valid.";
    
    @Override
    public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
        Assertions.isTrue(proxyCertificate.isValid(), PASSED_PROXY_CERTIFICATE_IS_NOT_VALID);

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        return catalogBackend.executeQuery(query, maxResults);
    }

    @Override
    @AllowRemoteAccess
    public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
        Assertions.isTrue(proxyCertificate.isValid(), PASSED_PROXY_CERTIFICATE_IS_NOT_VALID);

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        return catalogBackend.getReference(dataReferenceGuid);
    }

    @Override
    @AllowRemoteAccess
    public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
        Assertions.isTrue(proxyCertificate.isValid(), PASSED_PROXY_CERTIFICATE_IS_NOT_VALID);

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        return catalogBackend.executeMetaDataQuery(query, maxResults);
    }

}
