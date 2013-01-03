/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.gui.python;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.PartInitException;

import de.rcenvironment.commons.FileEncodingUtils;
import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.gui.commons.EditorsHelper;

/**
 * Opens file for scripting and load content of configuration if there is already some script stuff.
 * 
 * @author Doreen Seider
 */
public abstract class AbstractEditScriptRunnable implements Runnable {

    private static final Log LOGGER = LogFactory.getLog(AbstractEditScriptRunnable.class);
    
    @Override
    public void run() {
        try {
            final File tempFile = TempFileUtils.getDefaultInstance()
                .createTempFileWithFixedFilename("script.py");
            // if script content already exist, load it to temp file
            if (getScript() != null) {
                FileEncodingUtils.saveUnicodeStringToFile(getScript(), tempFile);
            }

            EditorsHelper.openExternalFileInEditor(tempFile, new Runnable[] {
                new Runnable() {

                    public void run() {
                        try {
                            // save new tempFile in component's configuration
                            setScript(FileEncodingUtils.loadUnicodeStringFromFile(tempFile));
                        } catch (final IOException e) {
                            LOGGER.error(e);
                        }
                    }
                }
            });
        } catch (final IOException e) {
            LOGGER.error(e);
        } catch (final PartInitException e) {
            LOGGER.error(e);
        }
    }
    
    protected abstract void setScript(String script);
    
    protected abstract String getScript();

}
