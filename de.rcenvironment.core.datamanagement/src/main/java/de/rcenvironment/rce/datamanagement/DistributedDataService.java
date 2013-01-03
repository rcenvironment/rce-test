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
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.DataReference;

/**
 * Service that provides easy access to all data by calling remote {@link DataService}s in the
 * distributed system.
 * 
 * @author Doreen Seider
 */
public interface DistributedDataService {

    /**
     * Deletes a whole local or remote {@link DataReference} with all {@link Revision}s.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference that shall be deleted.
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException;

    /**
     * Deletes the {@link Revision} with the given revisionNumber from a local or remote
     * {@link DataReference}.
     * 
     * Caution: the latest revision (HEAD) cannot be removed.
     * 
     * Removing old revision should be handled with care as this "changes history". It should only
     * be used to free storage.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param dataReference
     *            DataReference from which a revision shall be deleted.
     * @param revisionNumber
     *            RevisionNumber that shall be deleted.
     * @return dataReference without the deleted revision
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
        throws AuthorizationException;

    /**
     * Branches a local or remote {@link DataReference} at the given revisionNumber to the local or
     * to a remote data repository.
     * 
     * @param proxyCertificate
     *            The {@link User} of the user.
     * @param sourceDataReference
     *            DataReference that shall be the source (parent) of the branch.
     * @param sourceRevision
     *            RevisionNumber that shall be the source (parent) of the branch.
     * @param repositoryPlatform
     *            The {@link PlatformIdentifier} of the branch target platform.
     * @return new dataReference with the sourceDataReference as parent.
     * @throws AuthorizationException
     *             If the user has no create or no read permission.
     */
    DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision,
            PlatformIdentifier repositoryPlatform) throws AuthorizationException;
}
