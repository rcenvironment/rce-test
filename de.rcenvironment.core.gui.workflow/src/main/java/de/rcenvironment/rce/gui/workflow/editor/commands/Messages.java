/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.commands;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Tobias Menden
 */
public class Messages extends NLS {

    /** Constant. */
    public static String enterName;

    /** Constant. */
    public static String name;

    /** Constant. */
    public static String renameError;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.editor.commands.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
