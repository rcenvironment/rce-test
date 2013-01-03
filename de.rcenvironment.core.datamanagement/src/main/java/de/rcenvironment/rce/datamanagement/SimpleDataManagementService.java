/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Class providing easy access to the base implementation of DataManagementService.
 * Delegates everything to {@link DataManagementService} with the default implementation of {@link DataManagementServiceImpl}.
 *
 * @author Arne Bachmann
 */
public class SimpleDataManagementService {

    /**
     * The bound service.
     */
    private static DataManagementService dataManagementService = ServiceUtils.createNullService(DataManagementService.class);

    /**
     * Certificate or other authentication.
     */
    private User user;


    /**
     * Only used by OSGi component instantiation.
     */
    @Deprecated
    public SimpleDataManagementService() {
    }
    
    public SimpleDataManagementService(User user) {
        this.user = user;
    }
    
    
    protected void bindDataManagementService(DataManagementService newDataService) {
        dataManagementService = newDataService;
    }

    protected void unbindDataManagementService(DataManagementService newDataService) {
        dataManagementService = ServiceUtils.createNullService(DataManagementService.class);
    }


    /**
     * Creates a new data management entry with the contents of the given file and returns a new and
     * unique String id for it. For this new data management entry, the common managed metadata
     * values are set automatically; additional metadata can be provided in an optional
     * {@link MetaDataSet}. See [MetaDataKeys.Managed] for a list of the managed entries.
     * 
     * Note that the name of the local file is not automatically added to the metadata of the new
     * entry. If this is desired, create an appropriate entry in the {@link MetaDataSet} passed to
     * this method.
     * 
     * @param file
     *            the local file
     * @param additionalMetaData
     *            additional metadata key/value pairs to add to the automatically generated
     *            metadata; can be null if not required
     * @param platformIdentifier
     *            the identifier of the platform to create the data management entry on; if null,
     *            the local platform is used
     * @return the unique String reference to the created data management entry; its internal format
     *         is implementation-dependent
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no create permission (copied from
     *             {@link FileDataService})
     */
    public String createReferenceFromLocalFile(File file, MetaDataSet additionalMetaData, PlatformIdentifier platformIdentifier) throws
            IOException, AuthorizationException {
        return dataManagementService.createReferenceFromLocalFile(user, file, additionalMetaData, platformIdentifier);
    }

    /**
     * Writes the data referenced by the given string id to a local file.
     * 
     * @param reference
     *            the String id referencing a data management entry, as created, for example, by
     *            {@link #createReferenceFromLocalFile(User, File, MetaDataSet, PlatformIdentifier)}
     *            ; its internal format is implementation-dependent
     * @param targetFile
     *            the local file to copy the referenced data to
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no read permission (copied from
     *             {@link FileDataService})
     */
    @Deprecated
    public void copyReferenceToLocalFile(String reference, File targetFile) throws IOException, AuthorizationException {
        dataManagementService.copyReferenceToLocalFile(user, reference, targetFile);
    }

    /**
     * Writes the data referenced by the given string id stored on a given platform to a local file.
     * 
     * @param reference
     *            the String id referencing a data management entry, as created, for example, by
     *            {@link #createReferenceFromLocalFile(User, File, MetaDataSet, PlatformIdentifier)}
     *            ; its internal format is implementation-dependent
     * @param targetFile
     *            the local file to copy the referenced data to
     * @param platformIdentifier
     *            platform where the data is stored
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no read permission (copied from
     *             {@link FileDataService})
     */
    public void copyReferenceToLocalFile(String reference, File targetFile, PlatformIdentifier platformIdentifier) throws
            IOException, AuthorizationException {
        dataManagementService.copyReferenceToLocalFile(user, reference, targetFile, platformIdentifier);
    }
    
    /**
     * Writes the data referenced by the given string id stored on a given platform to a local file.
     * 
     * @param reference
     *            the String id referencing a data management entry, as created, for example, by
     *            {@link #createReferenceFromLocalFile(User, File, MetaDataSet, PlatformIdentifier)}
     *            ; its internal format is implementation-dependent
     * @param targetFile
     *            the local file to copy the referenced data to
     * @param platforms
     *            platform where the data is queried from
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no read permission (copied from
     *             {@link FileDataService})
     */
    public void copyReferenceToLocalFile(String reference, File targetFile, Collection<PlatformIdentifier> platforms)
        throws IOException, AuthorizationException {
        dataManagementService.copyReferenceToLocalFile(user, reference, targetFile, platforms);
    }
    
}
