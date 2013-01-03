/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Service that provides easy access to all catalogs by calling remote {@link MetaDataService}s in
 * the distributed system.
 * 
 * @author Doreen Seider
 */
public interface DistributedMetaDataService {

    /**
     * Updates a given {@link MetaDataSet} of a {@link DataReference}.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            The {@link DataReference} for which the MetaDataSet has to be updated.
     * @param revisionNumber
     *            The {@link Revision} number of the object to update the {@link MetaDataSet} for.
     * @param metaDataSet
     *            The new {@link MetaDataSet}.
     * @param includeRevisionIndependent
     *            <code>true</code> if all revisions shall be updated, <code>false</code> otherwise.
     * @throws AuthorizationException
     *             If the user has no update permission.
     */
    void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber, MetaDataSet metaDataSet,
            boolean includeRevisionIndependent) throws AuthorizationException;

    /**
     * Returns the MetaDataSet for a Revision in a {@link DataReference}.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            The {@link DataReference} to get the {@link MetaDataSet} for.
     * @param revisionNumber
     *            The {@link Revision} number to get the {@link MetaDataSet} for. If argument is
     *            '0', the meta data of the HEADREVISION will be returned.
     * @return The {@link MetaDataSet} of the {@link Revision}.
     * @throws AuthorizationException
     *             If the user has no get permission.
     */
    MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
        throws AuthorizationException;
}
