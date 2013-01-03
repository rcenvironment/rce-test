/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.MetaDataService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Implementation if {@link MetaDataService}.
 * 
 * @author Juergen Klein
 */
public class MetaDataServiceImpl implements MetaDataService {

    private static final String PASSED_PROXY_CERTIFICATE_IS_NOT_VALID = "Passed proxy certificate is not valid.";
    
    @Override
    public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
        throws AuthorizationException {
        Assertions.isTrue(proxyCertificate.isValid(), PASSED_PROXY_CERTIFICATE_IS_NOT_VALID);

        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        MetaDataSet metaDataSet = catalogBackend.getMetaDataSet(dataReference.getIdentifier(), revisionNumber);
        
        return metaDataSet;
    }

    @Override
    public void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
            MetaDataSet metaDataSet, boolean includeRevisionIndependent) throws AuthorizationException {
        Assertions.isTrue(proxyCertificate.isValid(), PASSED_PROXY_CERTIFICATE_IS_NOT_VALID);
        
        CatalogBackend catalogBackend = BackendSupport.getCatalogBackend();
        if (!catalogBackend.isLockedDataReference(dataReference.getIdentifier())) {
            catalogBackend.lockDataReference(dataReference.getIdentifier(), proxyCertificate);
            catalogBackend.storeMetaDataSet(dataReference.getIdentifier(), revisionNumber, metaDataSet, includeRevisionIndependent);
            catalogBackend.releaseLockedDataReference(dataReference.getIdentifier(), proxyCertificate);
        }
    }

}
