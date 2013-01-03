/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Tobias Menden
 */
public class Messages extends NLS {
    
    /** Constant. */
    public static String inputs;
    
    /** Constant. */
    public static String currentInput;
    
    /** Constant. */
    public static String nextInput;
    
    /** Constant. */
    public static String confirmPauseTitle;

    /** Constant. */
    public static String confirmPauseMessage;
    
    /** Constant. */
    public static String editingInformationTitle;

    /** Constant. */
    public static String editingInformationMessage;
    
    /** Constant. */
    public static String notLoggedInTitle;

    /** Constant. */
    public static String notLoggedInMessage;
    
    /** Constant. */
    public static String inputQueue;
    
    /** Constant. */
    public static String scrollLock;
    
    /** Constant. */
    public static String inputEditError;

    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.view.properties.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
