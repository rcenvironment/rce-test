/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.view;

import org.eclipse.osgi.util.NLS;


/**
 * Supports language specific messages.
 *
 * @author Markus Kunde
 */
public class Messages extends NLS {

    /** Constant. */
    public static String inputChannelName;
    
    /** Constant. */
    public static String outputChannelName;
    
    /** Constant. */
    public static String inputChannelNameType;
    
    /** Constant. */
    public static String outputChannelNameType;
    
    /** Constant. */
    public static String directionColumnName;

    /** Constant. */
    public static String channelColumnName;
    
    /** Constant. */
    public static String valueColumnName;
    
    /** Constant. */
    public static String valueColumnIteration;
    
    /** Constant. */
    public static String doubleClickHeader;
    
    /** Constant. */
    public static String doubleClickShellName;
    
    /** Constant. */
    public static String newFileTitle;
    
    /** Constant. */
    public static String newFileNewLabel;
    
    /** Constant. */
    public static String newFileFileLabel;
    
    /** Constant. */
    public static String newFilePageName;
    
    /** Constant. */
    public static String newFileDescriptionPart1;
    
    /** Constant. */
    public static String newFileDescriptionPart2;
    
    /** Constant. */
    public static String viewName;
    
    /** Constant. */
    public static String copyToClipboard;
    
    /** Constant. */
    public static String exportExcel;
    
    /** Constant. */
    public static String actionDone;
    
    /** Constant. */
    public static String actionError;
    
    /** Constant. */
    public static String actionButton;
    
    /** Constant. */
    public static String loadInBackground;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.components.excel.gui.view.messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
