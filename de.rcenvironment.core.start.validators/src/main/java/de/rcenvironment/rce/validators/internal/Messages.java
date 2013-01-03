/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.validators.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 * 
 * @author Christian Weiss
 */
public class Messages extends NLS {

    /** Constant. */
    public static String directoryNoConfigurationService;

    /** Constant. */
    public static String directoryRceFolderDoesNotExist;

    /** Constant. */
    public static String directoryRceFolderNotReadWriteAble;
    
    /** Constant. */
    public static String directoryRceFolderPathTooLong;
    /** Constant. */
    public static String permGenSizeTooLow;
    
    
    private static final String BUNDLE_NAME = Messages.class.getPackage()
            .getName() + ".messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
