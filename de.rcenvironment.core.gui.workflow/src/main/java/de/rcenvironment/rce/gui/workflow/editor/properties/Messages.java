/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

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
    public static String itemSelected;

    /**
     * Constant.
     */
    public static String noItemSelected;

    /**
     * Constant.
     */
    public static String property;
    
    /** Constant. */
    public static String defaultConfigMap;
    
    /** Constant.  */
    public static String selectTitle;
    
    /** Constant.  */
    public static String manageTitle;
    
    /** Constant.  */
    public static String newProfile;
    
    /** Constant.  */
    public static String inheritedFrom;
    
    /** Constant.  */
    public static String add;
    
    /** Constant.  */
    public static String edit;
    
    /** Constant.  */
    public static String remove;
    
    /** Constant.  */
    public static String name;
    
    /** Constant.  */
    public static String dataType;
    
    /** Constant.  */
    public static String additionalInputs;
    
    /** Constant.  */
    public static String additionalOutputs;

    /** Constant.  */
    public static String dataUse;
    /** Constant.  */
    public static String dataUseRequired;
    /** Constant.  */
    public static String dataUseInit;
    /** Constant.  */
    public static String dataUseOptional;

    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.editor.properties.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
