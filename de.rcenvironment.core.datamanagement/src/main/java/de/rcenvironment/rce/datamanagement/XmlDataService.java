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
 * This interface describes how to deal with xml data.
 * 
 * @author Heinrich Wendel
 */
public interface XmlDataService extends FileDataService {

    /**
     * Returns the Object of the given revision of dataReference.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference which contains the needed revision.
     * @param revisionNumber
     *            RevisionNumber which contains the wanted IntputStream.
     * @param types
     *            The types of the class to load.
     * @return Object of the given revision
     * @throws AuthorizationException
     *             If the user or the extension has no read permission.
     */
    Object getObjectFromDataReference(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
            Class<?>[] types) throws AuthorizationException;

    /**
     * Creates a new DataReference from the given Object on the Platform targetDataManagement. The
     * new dataReference will contain the given MetaData and reserved MetaData, that the
     * DataInterface adds automatically.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param object
     *            Object that shall be saved.
     * @param metaDataSet
     *            MetaDataSet that shall be saved.
     * @param types
     *            The types of the class to save.
     * @return DataReference for the given InputStream and MetaData.
     * @throws AuthorizationException
     *             If the user or the extension has no create permission.
     */
    DataReference newReferenceFromObject(User proxyCertificate, Object object, MetaDataSet metaDataSet, Class<?>[] types)
        throws AuthorizationException;

    /**
     * Creates a new Revision in the given DataReference from the given Object.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference to which the new Revision shall be added.
     * @param object
     *            Object that shall be added.
     * @param metaDataSet
     *            MetaDataSet that shall be added.
     * @param types
     *            The types of the classes to save.
     * @return DataReference containing the new Revision.
     * @throws AuthorizationException
     *             If the user or the extension has no create permission.
     */
    DataReference newRevisionFromObject(User proxyCertificate, DataReference dataReference, Object object,
            MetaDataSet metaDataSet, Class<?>[] types) throws AuthorizationException;

}
