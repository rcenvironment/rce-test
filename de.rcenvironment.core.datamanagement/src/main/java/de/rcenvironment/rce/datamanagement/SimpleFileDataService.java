/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.io.InputStream;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Class providing easy access to all {@link FileDataService}s in the distributed system.
 * 
 * @author Doreen Seider
 */
public class SimpleFileDataService extends SimpleDataService {

    private static DistributedFileDataService fileDataService = ServiceUtils.createNullService(DistributedFileDataService.class);

    /** Only used by OSGi component instantiation. */
    @Deprecated
    public SimpleFileDataService() {
    }
    
    public SimpleFileDataService(User aUser) {
        user = aUser;
    }
    
    protected void bindDistributedFileDataService(DistributedFileDataService newFileDataService) {
        fileDataService = newFileDataService;
        dataService = newFileDataService;
    }

    protected void unbindDistributedFileDataService(DistributedFileDataService oldFileDataService) {
        fileDataService = ServiceUtils.createNullService(DistributedFileDataService.class);
        dataService = ServiceUtils.createNullService(DistributedFileDataService.class);
    }
    
    /**
     * Returns the {@link InputStream} of the given revision of the given {@link DataReference}.
     * 
     * @param dataReference
     *            {@link DataReference} which contains the needed revision.
     * @param revisionNumber
     *            Revision number which contains the desired {@link InputStream}.
     * @return {@link InputStream} of the given revision.
     * @throws AuthorizationException
     *             If the user has no read permission.
     */
    public InputStream getStreamFromDataReference(DataReference dataReference, Integer revisionNumber) throws AuthorizationException {
        return fileDataService.getStreamFromDataReference(user, dataReference, revisionNumber);
    }

    /**
     * Creates a new {@link DataReference} from the given {@link InputStream} on the platform
     * represented by the given {@link PlatformIdentifier}. The new {@link DataReference} will
     * contain the given {@link MetaData} and reserved {@link MetaData} which will be set
     * automatically.
     * 
     * @param inputStream
     *            InputStream that shall be saved.
     * @param metaDataSet
     *            MetaDataSet that shall be saved.
     * @param platform
     *            {@link PlatformIdentifier} of the platform to store the reference. If
     *            <code>null</code> the new reference will be created on the local platform.
     * @return DataReference for the given InputStream and MetaData.
     * @throws AuthorizationException
     *             If the user or the extension has no create permission.
     */
    public DataReference newReferenceFromStream(InputStream inputStream, MetaDataSet metaDataSet, PlatformIdentifier platform)
        throws AuthorizationException {
        return fileDataService.newReferenceFromStream(user, inputStream, metaDataSet, platform);
    }

    /**
     * Creates a new Revision in the given {@link DataReference} from the given {@link InputStream}.
     * 
     * @param dataReference
     *            {@link DataReference} to which the new Revision shall be added.
     * @param inputStream
     *            {@link InputStream} that shall be added.
     * @param metaDataSet
     *            {@link MetaDataSet} that shall be added.
     * @return {@link DataReference} containing the new Revision.
     * @throws AuthorizationException
     *             If the user has no create permission.
     */
    public DataReference newRevisionFromStream(DataReference dataReference, InputStream inputStream, MetaDataSet metaDataSet)
        throws AuthorizationException {
        return fileDataService.newRevisionFromStream(user, dataReference, inputStream, metaDataSet);
    }
}
