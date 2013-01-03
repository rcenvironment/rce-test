/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Christian Weiss
 */
public class Messages extends NLS {

    /** Constant. */
    public static String waitSignalNodeLabel;

    /** Constant. */
    public static String deleteNodeActionContextMenuLabel;

    /** Constant. */
    public static String saveNodeActionContextMenuLabel;

    /** Constant. */
    public static String refreshNodeActionContextMenuLabel;

    /** Constant. */
    public static String refreshAllNodesActionContextMenuLabel;

    /** Constant. */
    public static String collapseAllNodesActionContextMenuLabel;
    
    /** Constant. */
    public static String dataManagementBrowser;

    /** Constant. */
    public static String fetchingData;

    /** Constant. */
    public static String sortUp;

    /** Constant. */
    public static String sortDown;

    /** Constant. */
    public static String sortTimeDesc;

    /** Constant. */
    public static String compareMsg;
    
    /** Constant. */
    public static String sortTime;

    /** Constant. */
    public static String dialogMessageDelete;
    
    /** Constant. */
    public static String jobTitleDelete;
    
    /** Constant. */
    public static String dialogTitleDelete;
    

    private static final String BUNDLE_NAME = Messages.class.getPackage()
            .getName() + ".messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
