/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.commons;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.gui.commons.EditorsHelper;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.DataManagementService;

/**
 * Utilities for data management tasks in the RCP workbench environment.
 * 
 * @author Robert Mischke
 */
public class DataManagementWorkbenchUtils {

    // injected
    /**
     * Service.
     */
    public static DataManagementService dataManagementService;

    private static final Log LOGGER = LogFactory.getLog(DataManagementWorkbenchUtils.class);

    /**
     * Only used for OSGi DS.
     */
    @Deprecated
    public DataManagementWorkbenchUtils() {}

    // FIXME code review; purpose in this utility class?
    /**
     * Javadoc.
     * 
     * @param reference :
     * @param filename : 
     * @param certificate : 
     * @throws AuthorizationException : 
     * @throws IOException Exception
     */
    public static void saveReferenceToFile(final String reference,
            final String filename, final User certificate) throws AuthorizationException, IOException {
        final File file = new File(filename);
        dataManagementService.copyReferenceToLocalFile(certificate, reference,
                file);
    }

    /**
     * Tries to open a data management reference in a read-only workbench text editor.
     * 
     * @param reference the data management reference
     * @param filename the filename to use for the given data
     * @param certificate the proxy certificate to resolve the reference
     */
    public static void tryOpenDataReferenceInReadonlyEditor(final String reference, final String filename,
            final User certificate) {
        try {
            // acquire local temporary file with the associated filename
            final File tempFile = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(filename);
            // open = copy to local temporary file + open in editor
            final Job openJob = new Job("Opening data reference") {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        // copy data reference content to local temporary file
                        dataManagementService.copyReferenceToLocalFile(certificate, reference, tempFile);
                    } catch (AuthorizationException e) {
                        LOGGER.error("Failed to copy datamanagement reference to local file.", e);
                    } catch (IOException e) {
                        LOGGER.error("Failed to copy datamanagement reference to local file.", e);
                    }
                    // best-effort try to make the file read-only; the actual outcome is ignored
                    tempFile.setWritable(false);
                    // open in editor
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            try {
                                EditorsHelper.openExternalFileInEditor(tempFile);
                            } catch (final PartInitException e) {
                                LOGGER.error("Failed to open datamanagement reference copied to local file in an editor.", e);
                            }
                        }
                    });
                    return Status.OK_STATUS;
                }

            };
            openJob.setUser(true);
            openJob.schedule();
        } catch (IOException e) {
            // acquiring a temporary file should always work fine, so this exception should not
            // happen
            throw new RuntimeException(e);
        }
    }
    
    /**
     * DS injection bind method.
     * 
     * @param newDataManagementService the dataManagementService to set
     */
    protected void bindDataManagementService(DataManagementService newDataManagementService) {
        DataManagementWorkbenchUtils.dataManagementService = newDataManagementService;
    }

    /**
     * DS injection unbind method.
     * 
     * @param newDataManagementService the dataManagementService to unset
     */
    protected void unbindDataManagementService(DataManagementService newDataManagementService) {
        DataManagementWorkbenchUtils.dataManagementService = null;
    }

}
