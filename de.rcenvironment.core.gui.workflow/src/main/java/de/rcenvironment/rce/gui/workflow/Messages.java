/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Christian Weiss
 */
public class Messages extends NLS {

    /** Constant. */
    public static String activeWorkflowsTitle;

    /** Constant. */
    public static String activeWorkflowsMessage;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
