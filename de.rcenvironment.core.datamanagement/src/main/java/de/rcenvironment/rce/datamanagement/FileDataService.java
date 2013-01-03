/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.io.InputStream;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Interface for the RCE user data system for file support.
 * 
 * @author Sandra Schroedter
 * @author Juergen Klein
 * 
 */
public interface FileDataService extends DataService {

    /**
     * Returns the InputStream of the given revision of dataReference.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference which contains the needed revision.
     * @param revisionNumber
     *            RevisionNumber which contains the wanted IntputStream.
     * @return InputStream of the given revision
     * @throws AuthorizationException
     *             If the user or the extension has no read permission.
     */
    InputStream getStreamFromDataReference(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException;

    /**
     * Creates a new DataReference from the given inputStream on the Platform targetDataManagement.
     * The new dataReference will contain the given MetaData and reserved MetaData, that the
     * DataInterface adds automatically.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param inputStream
     *            InputStream that shall be saved.
     * @param metaDataSet
     *            MetaDataSet that shall be saved.
     * @return DataReference for the given InputStream and MetaData.
     * @throws AuthorizationException
     *             If the user or the extension has no create permission.
     */
    DataReference newReferenceFromStream(User proxyCertificate, InputStream inputStream, MetaDataSet metaDataSet)
        throws AuthorizationException;

    /**
     * Creates a new Revision in the given DataReference from the given inputStream.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference to which the new Revision shall be added.
     * @param inputStream
     *            InputStream that shall be added.
     * @param metaDataSet
     *            MetaDataSet that shall be added.
     * @return DataReference containing the new Revision.
     * @throws AuthorizationException
     *             If the user or the extension has no create permission.
     */
    DataReference newRevisionFromStream(User proxyCertificate, DataReference dataReference, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException;

}
