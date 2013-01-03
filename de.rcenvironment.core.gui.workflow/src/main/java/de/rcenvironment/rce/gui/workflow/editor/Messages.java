/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor;

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
    public static String components;

    /**
     * Constant.
     */
    public static String connection;

    /**
     * Constant.
     */
    public static String connectionEditor;

    /**
     * Constant.
     */
    public static String createWorkflow;
    
    /**
     * Constant.
     */
    public static String fileWorkflow;
    
    /**
     * Constant.
     */
    public static String newConnection;

    /**
     * Constant.
     */
    public static String newWorkflow;

    /**
     * Constant.
     */
    public static String rename;

    /**
     * Constant.
     */
    public static String tools;
    
    /**
     * Constant.
     */
    public static String copy;

    /**
     * Constant.
     */
    public static String paste;
    
    /** Constant. */
    public static String incompatibleVersionTitle;
    
    /** Constant. */
    public static String incompatibleVersionMessage;
    
    /** Constant. */
    public static String rememberIncompatibleVersionQuestionDecision;
    
    /** Constant. */
    public static String updateIncompatibleVersionAutomatically;
    
    /** Constant. */
    public static String fetchingComponents;

    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.editor.messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
