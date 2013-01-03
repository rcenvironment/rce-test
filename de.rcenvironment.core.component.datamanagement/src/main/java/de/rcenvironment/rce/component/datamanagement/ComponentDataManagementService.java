/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentInstanceInformation;

/**
 * TODO see {@link StatefulComponentDataManagementService} for javadoc until the API is settled.
 * 
 * @author Robert Mischke
 * @author Sascha Zur
 */
public interface ComponentDataManagementService {

    /**
     * A constant used in several methods as a "magic" filename parameter to denote that an
     * automatically derived filename should be used. See method javadocs for details.
     */
    String SAME_FILENAME = "*ATTACH_AUTOMATIC_FILENAME*";

    /**
     * TODO see {@link StatefulComponentDataManagementService} for javadoc until the API is settled.
     * 
     * @param instanceInformation the {@link ComponentInstanceInformation} to read metadata and the
     *        proxy certificate from
     * @param user the logged in user
     * @param file the local file
     * @param filename either a custom filename to attach to the reference, or the constant
     *        {@link ComponentDataManagementService#SAME_FILENAME} to use the filename of the local
     *        file, or "null" to attach no filename
     * @return the created reference
     * @throws IOException on a local I/O or data management error
     */
    String createTaggedReferenceFromLocalFile(ComponentInstanceInformation instanceInformation, User user, File file, String filename)
        throws IOException;
    
    /**
     * 
     * @param object the object
     * @param instanceInformation the {@link ComponentInstanceInformation} to read metadata and the
     *        proxy certificate from
     * @param user the logged in user
     * @return the created reference
     * @throws IOException on a local I/O or data management error
     */
    String createTaggedReferenceFromString(ComponentInstanceInformation instanceInformation, User user, String object) throws IOException;
    /**
     * TODO see {@link StatefulComponentDataManagementService} for javadoc until the API is settled.
     * 
     * @param user the logged in user
     * @param reference the reference
     * @param targetFile the local file to write to
     * @throws IOException on a local I/O or data management error
     */
    @Deprecated
    void copyReferenceToLocalFile(User user, String reference, File targetFile) throws IOException;

    /**
     * Copies the data "body" identified by a data management reference to a local file.
     * 
     * @param user the logged in user
     * @param reference the reference
     * @param targetFile the local file to write to
     * @param platforms The platforms to try to fetch data from
     * @throws IOException on a local I/O or data management error
     */
    void copyReferenceToLocalFile(User user, String reference, File targetFile, Collection<PlatformIdentifier> platforms) 
        throws IOException;
    /**
     * Retrieved the String "body" identified by a data management reference.
     *
     * @param user the logged in user
     * @param reference the reference
     * @param platforms The platforms to try to fetch data from
     * @return the retrieved String 
     * @throws IOException on a local I/O or data management error
     */
    String retrieveStringFromReference(User user, String reference, Collection<PlatformIdentifier> platforms) throws IOException;
    /**
     * TODO see {@link StatefulComponentDataManagementService} for javadoc until the API is settled.
     * 
     * @param instanceInformation the {@link ComponentInstanceInformation} to read metadata and the
     *        proxy certificate from
     * @param user the logged in user
     * @param historyData the {@link Serializable} object that represents the history entry; is
     *        decoded by an appropriate subtree builder (see
     *        de.rcenvironment.rce.gui.datamanagement.browser.spi package for details)
     * @param userInfoText a user description for this history entry; used as title for GUI entries
     *        representing this history entry
     * @throws IOException on a data management error
     */
    void addHistoryDataPoint(ComponentInstanceInformation instanceInformation, User user, Serializable historyData, String userInfoText)
        throws IOException;

    /**
     * Copies the data "body" identified by a data management reference to a local file.
     * 
     * @param user the logged in user
     * @param reference the reference
     * @param targetFile the local file to write to
     * @param platform platform the data is stored
     * @throws IOException on a local I/O or data management error
     */
    void copyReferenceToLocalFile(User user, String reference, File targetFile, PlatformIdentifier platform) throws IOException;
    

}
