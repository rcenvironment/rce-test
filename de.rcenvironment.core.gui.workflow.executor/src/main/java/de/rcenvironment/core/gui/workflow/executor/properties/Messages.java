/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Doreen Seider
 */
public class Messages extends NLS {

    /** Constant. */
    public static String configureHost;
    
    /** Constant. */
    public static String configureUploadFiles;
    
    /** Constant. */
    public static String uploadFiles;
    
    /** Constant. */
    public static String hostLabel;

    /** Constant. */
    public static String portLabel;
    
    /** Constant. */
    public static String sandboxRootLabel;
    
    /** Constant. */
    public static String errorMissing;
    
    /** Constant. */
    public static String warningFileListEmpty;
    
    /** Constant. */
    public static String addFromFileSystem;
    
    /** Constant. */
    public static String addFromProject;

    /** Constant. */
    public static String remove;
    
    /** Constant. */
    public static String configureScript;
    
    /** Constant. */
    public static String dialogTitleNotSupported;
    
    /** Constant. */
    public static String dialogMessageNotSupported;
    
    /** Constant. */
    public static String configureDownloadFiles;
    
    /** Constant. */
    public static String downloadFiles;
    
    /** Constant. */
    public static String toRceDataManagement;
    
    /** Constant. */
    public static String toFileSystem;
    
    /** Constant. */
    public static String threeDots;
    
    
    private static final String BUNDLE_NAME = "de.rcenvironment.core.gui.workflow.executor.properties.messages"; //$NON-NLS-1$
    
    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
