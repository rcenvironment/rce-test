/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * A data management API that provides more higher-level operations than the existing interfaces
 * (like {@link FileDataService} or {@link MetaDataService}). Another difference is that, by
 * default, UUIDs are passed as Strings instead of java UUID objects. This relaxes the assumptions
 * about what is used as a data reference, which simplifies mocking scenarios (for example, by using
 * path representations of temporary files as data references).
 * 
 * @author Robert Mischke
 */
public interface DataManagementService {

    /**
     * Convenience variant of
     * {@link #createReferenceFromLocalFile(User, File, MetaDataSet, PlatformIdentifier)} that
     * always creates the new data management entry on the current platform.
     * 
     * @deprecated In the new distributed data management concept, assuming local storage is
     *             unreliable and should not be used anymore. As this stateless service cannot
     *             determine the proper storage without knowing about the context, clients need to
     *             provide the appropriate platform identifier. If local storage is actually
     *             desired, call the 4-parameter variant of this method with "null" for the
     *             {@link PlatformIdentifier}. -- RM
     * 
     * @param user
     *            user representation
     * @param file
     *            the local file
     * @param additionalMetaData
     *            additional metadata key/value pairs to add to the automatically generated
     *            metadata; can be null if not required
     * @return the unique String reference to the created data management entry; its internal format
     *         is implementation-dependent
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no create permission (copied from
     *             {@link FileDataService})
     */
    @Deprecated
    String createReferenceFromLocalFile(User user, File file, MetaDataSet additionalMetaData) throws IOException, AuthorizationException;

    // TODO adapt Checkstyle to not complain if a javadoc @link is set to MetaDataKeys.Managed

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
     * @param user
     *            user representation
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
    String createReferenceFromLocalFile(User user, File file, MetaDataSet additionalMetaData, // CheckStyle
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException;

    /**
     * Creates a new data management entry with the utf-8 byte array form of the given String and
     * returns a new and unique String id for it. For this new data management entry, the common
     * managed metadata values are set automatically; additional metadata can be provided in an
     * optional {@link MetaDataSet}. See [MetaDataKeys.Managed] for a list of the managed entries.
     * 
     * @param user
     *            user representation
     * @param object
     *            the object to serialize
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
    String createReferenceFromString(User user, String object, MetaDataSet additionalMetaData, // CheckStyle
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException;

    /**
     * Writes the data referenced by the given string id to a local file.
     * 
     * @param user
     *            user representation
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
    void copyReferenceToLocalFile(User user, String reference, File targetFile) throws IOException, AuthorizationException;

    /**
     * Writes the data referenced by the given string id stored on a given platform to a local file.
     * 
     * @param user
     *            user representation
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
    void copyReferenceToLocalFile(User user, String reference, File targetFile, // CheckStyle
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException;

    /**
     * Writes the data referenced by the given string id stored on a given platform to a local file.
     * 
     * @param user
     *            user representation
     * @param reference
     *            the String id referencing a data management entry, as created, for example, by
     *            {@link #createReferenceFromLocalFile(User, File, MetaDataSet, PlatformIdentifier)}
     *            ; its internal format is implementation-dependent
     * @param targetFile
     *            the local file to copy the referenced data to
     * @param platforms
     *            platforms where the data is queried
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no read permission (copied from
     *             {@link FileDataService})
     */
    void copyReferenceToLocalFile(User user, String reference, File targetFile, // CheckStyle
            Collection<PlatformIdentifier> platforms) throws IOException, AuthorizationException;

    /**
     * Retrieves the String referenced by the given string id.
     * 
     * @param user
     *            user representation
     * @param reference
     *            the String id referencing a data management entry, as created, for example, by
     *            {@link #createReferenceFromString(User, String, MetaDataSet, PlatformIdentifier)};
     *            its internal format is implementation-dependent
     * @param platforms
     *            platforms where the data is queried
     * @return the retrieved String
     * @throws IOException
     *             on I/O errors in the data management, or related to the given file
     * @throws AuthorizationException
     *             if the user or the extension has no read permission (copied from
     *             {@link FileDataService})
     */
    String retrieveStringFromReference(User user, String reference, // CheckStyle
            Collection<PlatformIdentifier> platforms) throws IOException, AuthorizationException;

}
