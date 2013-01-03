/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.python.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Doreen Seider
 */
public class Messages extends NLS {

    /** Constant. */
    public static String edit;
    
    /** Constant. */
    public static String select;

    /** Constant. */
    public static String scriptSectionTitle;
    
    /** Constant. */
    public static String fromFileSystem;
    
    /** Constant. */
    public static String load;
        
    /** Constant. */
    public static String py;
    
    /** Constant. */
    public static String loadTitle;
    
    /** Constant. */
    public static String loadMessage;
    
    /** Constant. */
    public static String python;
    
    /** Constant. */
    public static String fromProject;
    /** Constant. */

    public static String noInput;
    /** Constant. */

    public static String noInstallation;
    /** Constant. */
    public static String noScript;
   
        
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.components.gui.python.properties.messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
