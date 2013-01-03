/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.parametricstudy.gui.properties;

import org.eclipse.osgi.util.NLS;


/**
 * Supports language specific messages.
 * 
 * @author Sascha Zur
 */
public class Messages extends NLS {

    /** Constant. */
    public static String fromMsg;

    /** Constant. */
    public static String toMsg;

    /** Constant. */
    public static String stepSizeMsg;

    /** Constant. */
    public static String inStepMsg;

    /** Constant. */
    public static String stepsMsg;

    /** Constant. */
    public static String noInput;

    /** Constant. */
    public static String noValue;
    
    /** Constant. */
    public static String rangeMsg;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.components.parametricstudy.gui.properties.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
