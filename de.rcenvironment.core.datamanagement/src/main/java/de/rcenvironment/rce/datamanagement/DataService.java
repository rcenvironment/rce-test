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

/**
 * Common interface methods of the data services. This interface is not intended to be used by the
 * user directly. Users should use the {@link DataReference} type dependent services instead, e.g.
 * {@link FileDataService} for DataReferenceType.fileObject or {@link XmlDataService} for
 * DataReferenceType.xmlObject.
 * 
 * @author Sandra Schroedter
 * @author Juergen Klein
 * 
 */
public interface DataService {

    /**
     * Deletes a whole local {@link DataReference} with all {@link Revision}s.
     * 
     * @param user
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference that shall be deleted.
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    void deleteReference(User user, DataReference dataReference) throws AuthorizationException;

    /**
     * Deletes the {@link Revision} with the given revision number from a local {@link DataReference}
     * .
     * 
     * Caution: the latest revision (HEAD) cannot be removed.
     * 
     * Removing old revision should be handled with care as this "changes history". It should only
     * be used to free storage.
     * 
     * @param user
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference from which a revision shall be deleted.
     * @param revisionNumber
     *            RevisionNumber that shall be deleted.
     * @return dataReference without the deleted revision
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    DataReference deleteRevision(User user, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException;

    /**
     * Branches a local or remote {@link DataReference} at the given revision number to the local
     * data repository.
     * 
     * @param user
     *            The {@link User} of the user.
     * @param sourceDataReference
     *            DataReference that shall be the source (parent) of the branch.
     * @param sourceRevision
     *            RevisionNumber that shall be the source (parent) of the branch.
     * @return new dataReference with the sourceDataReference as parent.
     * @throws AuthorizationException
     *             If the user has no create or no read permission.
     */
    DataReference branch(User user, DataReference sourceDataReference, Integer sourceRevision)
        throws AuthorizationException;

}
