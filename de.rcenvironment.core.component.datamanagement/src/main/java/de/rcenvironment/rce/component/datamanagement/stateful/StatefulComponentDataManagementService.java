/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement.stateful;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A stateful version of {@link ComponentDataManagementService}. Implementations will usually store
 * a relevant {@link ComponentInstanceInformation} internally.
 * 
 * This interface allows clients to use implementations like
 * {@link SimpleComponentDataManagementService} and also pass them as parameters, while still
 * allowing replacement with mock objects for test runs or integration tests.
 * 
 * @author Robert Mischke
 */
public interface StatefulComponentDataManagementService {

    /**
     * Creates a reference from a local file and automatically sets component-related metadata.
     * 
     * The following {@link MetaDataKeys} are automatically filled in:
     * <ul>
     * <li>COMPONENT_CONTEXT_UUID</li>
     * <li>COMPONENT_CONTEXT_NAME</li>
     * <li>COMPONENT_UUID</li>
     * <li>COMPONENT_NAME</li>
     * <li>FILENAME (if the "filename" parameter is not null; see below)</li>
     * </ul>
     * 
     * TODO add parameter for automatic/custom/empty associated filename?
     * 
     * TODO add revision parameter?
     * 
     * TODO add parameter to add custom metadata?
     * 
     * @param file the local file
     * @param filename either a custom filename to attach to the reference, or the constant
     *        {@link ComponentDataManagementService#SAME_FILENAME} to use the filename of the local
     *        file, or "null" to attach no filename
     * @return the created reference
     * @throws IOException on a local I/O or data management error
     * 
     */
    String createTaggedReferenceFromLocalFile(File file, String filename) throws IOException;
    
    /**
     * 
     * @param object the object
     * @return the created reference
     * @throws IOException on a local I/O or data management error
     */
    String createTaggedReferenceFromString(String object) throws IOException;

    /**
     * Copies the data "body" identified by a data management reference to a local file.
     * 
     * @param reference the reference
     * @param targetFile the local file to write to
     * @throws IOException on a local I/O or data management error
     */
    @Deprecated
    void copyReferenceToLocalFile(String reference, File targetFile) throws IOException;

    /**
     * Copies the data "body" identified by a data management reference to a local file.
     * 
     * @param reference the reference
     * @param targetFile the local file to write to
     * @param platforms The platforms to try to fetch data from
     * @throws IOException on a local I/O or data management error
     */
    void copyReferenceToLocalFile(String reference, File targetFile, Collection<PlatformIdentifier> platforms) throws IOException;

    /**
     * Copies the data "body" identified by a data management reference to a local file.
     * 
     * @param reference the reference
     * @param targetFile the local file to write to
     * @param platform platform the data is stored
     * @throws IOException on a local I/O or data management error
     */
    void copyReferenceToLocalFile(String reference, File targetFile, PlatformIdentifier platform) throws IOException;
    
    /**
     * Retrieved the String "body" identified by a data management reference.
     * 
     * @param reference the reference
     * @param platforms The platforms to try to fetch data from
     * @return the retrieved String 
     * @throws IOException on a local I/O or data management error
     */
    String retrieveStringFromReference(String reference, Collection<PlatformIdentifier> platforms) throws IOException;

    /**
     * Creates a "history" point in the data management with appropriate metadata entries.
     * 
     * @param historyData the {@link Serializable} object that represents the history entry; is
     *        decoded by an appropriate subtree builder (see
     *        de.rcenvironment.rce.gui.datamanagement.browser.spi package for details)
     * @param userInfoText a user description for this history entry; used as title for GUI entries
     *        representing this history entry
     * @throws IOException on a data management error
     */
    void addHistoryDataPoint(Serializable historyData, String userInfoText) throws IOException;

}
