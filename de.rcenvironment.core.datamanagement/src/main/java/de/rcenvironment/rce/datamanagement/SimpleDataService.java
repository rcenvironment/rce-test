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
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.DataReference;

/**
 * Class providing easy access to all {@link DataService}s in the distributed system.
 * 
 * @author Doreen Seider
 */
abstract class SimpleDataService {

    protected static DistributedDataService dataService = ServiceUtils.createNullService(DistributedDataService.class);
    
    protected User user;
    
    /**
     * Deletes a whole local or remote {@link DataReference} with all {@link Revision}s.
     * 
     * @param dataReference
     *            DataReference that shall be deleted.
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    public void deleteReference(DataReference dataReference) throws AuthorizationException {
        dataService.deleteReference(user, dataReference);
    }

    /**
     * Deletes the {@link Revision} with the given revision number from a local {@link DataReference}.
     * 
     * Caution: the latest revision (HEAD) cannot be removed.
     * 
     * Removing old revision should be handled with care as this "changes history". It should only
     * be used to free storage.
     * 
     * @param dataReference
     *            DataReference from which a revision shall be deleted.
     * @param revisionNumber
     *            RevisionNumber that shall be deleted.
     * @return dataReference without the deleted revision
     * @throws AuthorizationException
     *             If the user has no delete permission.
     */
    public DataReference deleteRevision(DataReference dataReference, Integer revisionNumber) throws AuthorizationException {
        return dataService.deleteRevision(user, dataReference, revisionNumber);
    }

    /**
     * Branches a local or remote {@link DataReference} at the given revision number to a local or remote
     * data repository.
     * 
     * @param sourceDataReference
     *            DataReference that shall be the source (parent) of the branch.
     * @param sourceRevision
     *            RevisionNumber that shall be the source (parent) of the branch.
     * @param repositoryPlatform
     *            {@link PlatformIdentifier} of the branch target platform.
     * @return new dataReference with the sourceDataReference as parent.
     * @throws AuthorizationException
     *             If the user has no create or no read permission.
     */
    public DataReference branch(DataReference sourceDataReference, Integer sourceRevision, PlatformIdentifier repositoryPlatform)
        throws AuthorizationException {
        return dataService.branch(user, sourceDataReference, sourceRevision, repositoryPlatform);
    }

}
