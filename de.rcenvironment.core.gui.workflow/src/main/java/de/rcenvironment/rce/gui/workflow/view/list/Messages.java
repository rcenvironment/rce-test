/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.list;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Tobias Menden
 */
public class Messages extends NLS {

    /** Constant. */
    public static String additionalInformation;

    /** Constant. */
    public static String additionalInformationColon;

    /** Constant. */
    public static String pause;
    
    /** Constant. */
    public static String resume;
    
    /** Constant. */
    public static String cancel;

    /** Constant. */
    public static String dispose;

    /** Constant. */
    public static String name;

    /** Constant. */
    public static String platform;

    /** Constant. */
    public static String refresh;

    /** Constant. */
    public static String status;

    /** Constant. */
    public static String time;

    /** Constant. */
    public static String user;

    /** Constant. */
    public static String localPlatform;
    
    /** Constant. */
    public static String executing;
    
    /** Constant. */
    public static String workflows;
    
    /** Constant. */
    public static String fetchingWorkflows;
    
    /** Constant. */
    public static String pausingWorkflow;
    
    /** Constant. */
    public static String cancelingWorkflow;
    
    /** Constant. */
    public static String resumingWorkflow;
    
    /** Constant. */
    public static String disposingWorkflow;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.gui.workflow.view.list.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
