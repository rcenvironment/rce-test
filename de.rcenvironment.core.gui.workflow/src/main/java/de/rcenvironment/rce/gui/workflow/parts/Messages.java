/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Doreen Seider
 */
public class Messages extends NLS {
    
    /** Constant. */
    public static String runs;
        
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.parts.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
