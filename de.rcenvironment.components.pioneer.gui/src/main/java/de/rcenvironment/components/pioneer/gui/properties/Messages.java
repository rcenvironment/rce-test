/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.gui.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 * 
 * @author Christian Weiss
 */
public class Messages extends NLS {

    /** Constant. */
    public static String messageSectionTitle;

    /** Constant. */
    public static String messageLabel;

    /** Constant. */
    public static String textLabelMessage;

    /** Constant. */
    public static String iterationsLabel;

    /** Constant. */
    public static String waitButtonLabelCheck;

    /** Constant. */
    public static String waitButtonLabelToggle;

    /** Constant. */
    public static String executeInitialButtonLabel;

    /** Constant. */
    public static String resetButtonLabel;

    /** Constant. */
    public static String overviewActiveLabelText;

    /** Constant. */
    public static String overviewPassiveLabelText;

    /** Constant. */
    public static String errorEmptyMessageRelative;

    /** Constant. */
    public static String errorEmptyMessageAbsolute;

    /** Constant. */
    public static String errorZeroIterationsRelative;

    /** Constant. */
    public static String errorZeroIterationsAbsolute;

    /** Constant. */
    public static String errorOpertionModeNotSetRelative;

    /** Constant. */
    public static String errorOpertionModeNotSetAbsolute;

    /** Constant. */
    public static String activeSectionTitle;

    /** Constant. */
    public static String passiveSectionTitle;

    /** Constant. */
    public static String overviewSectionTitle;

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
