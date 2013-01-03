/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.converger.gui;

import org.eclipse.osgi.util.NLS;


/**
 * Supports language specific messages.
 * 
 * @author Sascha Zur
 */
public class Messages extends NLS {

    /** Constant. */
    public static String absoluteConvergenceMessage;

   
    /** Constant. */
    public static String relativeConvergenceMessage;

    
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
    
    /** Constant. */
    public static String startValue;

    /** Constant.  */
    public static String addInput;
    /** Constant.  */

    public static String editInput;

    /** Constant.  */
    public static String hasStartValue;

    /** Constant.  */
    public static String none;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.components.converger.gui.messages";

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
