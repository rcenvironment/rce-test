/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 * 
 * @author Christian Weiss
 */
public class Messages extends NLS {

    /** Constant. */
    public static String executableSectionTitle;

    /** Constant. */
    public static String directoryLabel;

    /** Constant. */
    public static String directoryImportButtonLabel;

    /** Constant. */
    public static String directoryExportButtonLabel;

    /** Constant. */
    public static String separateExecutionDirectoriesLabel;

    /** Constant. */
    public static String initInvocationSectionTitle;

    /** Constant. */
    public static String runInvocationSectionTitle;

    /** Constant. */
    public static String initCommandLabel;

    /** Constant. */
    public static String doInitCommandLabel;

    /** Constant. */
    public static String commandLabel;

    /** Constant. */
    public static String variablesLabel;

    /** Constant. */
    public static String variablesInsertButtonLabel;

    /** Constant. */
    public static String directoryChooserDialogTitle;

    /** Constant. */
    public static String variablesInputPattern;

    /** Constant. */
    public static String errorEmptyValue;

    /** Constant. */
    public static String errorMustBeSet;

    /** Constant. */
    public static String executableDirectoryErrorEmptyValue;

    /** Constant. */
    public static String executableDirectoryContentSetLabel;

    /** Constant. */
    public static String executableDirectoryContentNotSetLabel;

    /** Constant. */
    public static String invalidDirectory;

    /** Constant. */
    public static String executableDirectoryNotValid;

    /** Constant. */
    public static String initCommandErrorEmptyValue;

    /** Constant. */
    public static String runCommandErrorEmptyValue;

    /** Constant. */
    public static String executableDirectoryContentNotSet;

    /** Constant. */
    public static String mappingSectionTitle;

    /** Constant. */
    public static String doInitCommandNotSetValue;
    
    /** Constant. */
    public static String direction;
    
    /** Constant. */
    public static String name;
    
    /** Constant. */
    public static String path;

    /** Constant. */
    public static String invalidDir;

    /** Constant. */
    public static String importDir;

    /** Constant. */
    public static String loadFiles;

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * Binds the message to the given parameters.
     * 
     * @param message the message
     * @param bindings the bindings
     * @return the bound message
     */
    public static String bind2(final String message, final Object... bindings) {
        return NLS.bind(message, bindings);
    }

}
