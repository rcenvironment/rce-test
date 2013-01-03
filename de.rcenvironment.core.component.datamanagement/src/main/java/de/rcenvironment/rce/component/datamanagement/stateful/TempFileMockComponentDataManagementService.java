/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A mock {@link StatefulComponentDataManagementService} that creates local temporary files and uses
 * their absolute paths as references. Useful for testing wrappers without a full data management
 * backend.
 * 
 * @author Robert Mischke
 * 
 */
public class TempFileMockComponentDataManagementService implements StatefulComponentDataManagementService {

    private static Log log = LogFactory.getLog(TempFileMockComponentDataManagementService.class);

    @Override
    @Deprecated
    public void copyReferenceToLocalFile(String reference, File targetFile) throws IOException {
        // false = do not preserve timestamp
        FileUtils.copyFile(new File(reference), targetFile, false);
    }

    // method does not make sense in this context, because this method implementation is only
    // intended to work locally
    @Override
    public void copyReferenceToLocalFile(String reference, File targetFile, Collection<PlatformIdentifier> platforms) throws IOException {
        // false = do not preserve timestamp
        FileUtils.copyFile(new File(reference), targetFile, false);
    }

    // method does not make sense in this context, because this method implementation is only
    // intended to work locally
    @Override
    public void copyReferenceToLocalFile(String reference, File targetFile, PlatformIdentifier platform) throws IOException {
        // false = do not preserve timestamp
        FileUtils.copyFile(new File(reference), targetFile, false);
    }

    @Override
    public String retrieveStringFromReference(String reference, Collection<PlatformIdentifier> platforms) throws IOException {
        return FileUtils.readFileToString(new File(reference));
    }

    @Override
    public String createTaggedReferenceFromLocalFile(File file, String filename) throws IOException {
        // copy the file to a temporary file to take an immutable snapshot
        File tmpFile = File.createTempFile("datamgmt." + file.getName() + ".", ".tmp");
        // tmpFile.deleteOnExit();
        // false = do not preserve timestamp
        FileUtils.copyFile(file, tmpFile, false);
        String reference = tmpFile.getAbsolutePath();
        if (log.isTraceEnabled()) {
            log.trace("Created reference " + reference + " for local file " + file.getAbsolutePath());
        }

        return reference;
    }

    @Override
    public String createTaggedReferenceFromString(String object) throws IOException {
        // copy the file to a temporary file to take an immutable snapshot
        File tmpFile = File.createTempFile("datamgmt.", ".tmp");
        FileUtils.writeStringToFile(tmpFile, object);
        String reference = tmpFile.getAbsolutePath();
        if (log.isTraceEnabled()) {
            log.trace("Created reference " + reference + " for String");
        }

        return reference;
    }

    @Override
    public void addHistoryDataPoint(Serializable historyData, String userInfoText) throws IOException {
        // not supported, calls are silently ignored
    }
}
