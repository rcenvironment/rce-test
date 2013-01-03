/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Class providing easy access to all {@link MetaDataService}s in the distributed system.
 *
 * @author Doreen Seider
 */
public class SimpleMetaDataService {

    private static DistributedMetaDataService metaDataService = ServiceUtils.createNullService(DistributedMetaDataService.class);

    private User certificate;

    /**
     * Only used by OSGi component instantiation.
     */
    @Deprecated
    public SimpleMetaDataService() {
        
    }
    
    public SimpleMetaDataService(User certificate) {
        this.certificate = certificate;
    }
    
    protected void bindDistributedMetaDataService(DistributedMetaDataService newMetaDataService) {
        metaDataService = newMetaDataService;
    }

    protected void unbindDistributedMetaDataService(DistributedMetaDataService newQueryService) {
        metaDataService = ServiceUtils.createNullService(DistributedMetaDataService.class);
    }
    
    /**
     * Updates a given {@link MetaDataSet} of a {@link DataReference}.
     * 
     * @param dataReference The {@link DataReference} for which the MetaDataSet has to be updated.
     * @param revisionNumber The {@link Revision} number of the object to update the {@link MetaDataSet} for.
     * @param metaDataSet The new {@link MetaDataSet}.
     * @param includeRevisionIndependent <code>true</code> if all revisions shall be updated,
     *        <code>false</code> otherwise.
     * @throws AuthorizationException If the user has no update permission.
     */
    public void updateMetaDataSet(DataReference dataReference, Integer revisionNumber, MetaDataSet metaDataSet,
            boolean includeRevisionIndependent) throws AuthorizationException {
        metaDataService.updateMetaDataSet(certificate, dataReference, revisionNumber, metaDataSet, includeRevisionIndependent);
    }

    /**
     * Returns the MetaDataSet for a Revision in a {@link DataReference}.
     * 
     * @param dataReference The {@link DataReference} to get the {@link MetaDataSet} for.
     * @param revisionNumber The {@link Revision} number to get the {@link MetaDataSet} for. If argument is '0', the meta
     *        data of the HEADREVISION will be returned.
     * @return The {@link MetaDataSet} of the {@link Revision}.
     * @throws AuthorizationException If the user has no get permission.
     */
    public MetaDataSet getMetaDataSet(DataReference dataReference, int revisionNumber) 
        throws AuthorizationException {
        return metaDataService.getMetaDataSet(certificate, dataReference, revisionNumber);
    }
}
