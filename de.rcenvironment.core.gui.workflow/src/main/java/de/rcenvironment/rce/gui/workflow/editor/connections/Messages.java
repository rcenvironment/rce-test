/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.connections;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Tobias Menden
 */
public class Messages extends NLS {

    /**
     * Constant.
     */
    public static String and;

    /**
     * Constant.
     */
    public static String connectionEditor;

    /**
     * Constant.
     */
    public static String connections;

    /**
     * Constant.
     */
    public static String delete;

    /**
     * Constant.
     */
    public static String error;

    /**
     * Constant.
     */
    public static String incompatibleTypes;

    /**
     * Constant.
     */
    public static String source;

    /**
     * Constant.
     */
    public static String target;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.editor.connections.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
