/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import org.eclipse.osgi.util.NLS;

/**
 * Supports language specific messages.
 *
 * @author Sascha Zur
 */
public class Messages extends NLS {

    /** Constant. */
    public static String algorithm;
  
    /** Constant. */
    public static String loadTitle;
    
    /** Constant. */
    public static String dataTabText;

    /** Constant. */
    public static String dataTabToolTipText;

    /** Constant. */
    public static String chartTabText;

    /** Constant. */
    public static String chartTabToolTipText;

    /** Constant. */
    public static String configurationTreeDimensionsLabel;

    /** Constant. */
    public static String configurationTreeMeasuresLabel;

    /** Constant. */
    public static String copyToClipboardLabel;

    /** Constant. */
    public static String propertyLabel;

    /** Constant. */
    public static String valueLabel;

    /** Constant. */
    public static String trueLabel;

    /** Constant. */
    public static String falseLabel;

    /** Constant. */
    public static String removeTraceActionLabel;

    /** Constant. */
    public static String addTraceButtonLabel;
    /** Constant. */
    public static String saveData;
    /** Constant. */
    public static String targetFunction;
    /** Constant. */
    public static String constraints;
    /** Constant. */
    public static String designVariables;
    
    /** Constant. */
    public static String algorithmProperties;
    /** Constant. */

    public static String add;
    /** Constant. */

    public static String edit;
    /** Constant. */

    public static String remove;
    /** Constant. */

    public static String addVariable;

    /** Constant. */
    public static String weight;

    /** Constant. */
    public static String goal;

    /** Constant. */
    public static String lowerBound;

    /** Constant. */
    public static String upperBound;

    /** Constant. */
    public static String startValue;

    /** Constant. */
    public static String name;

    /** Constant. */
    public static String dataType;
    /** Constant. */
    public static String dakotaPathOptional;
    /** Constant. */
    public static String excelExport;
    
    private static final String BUNDLE_NAME = "de.rcenvironment.rce.components.gui.optimizer.properties.messages"; //$NON-NLS-1$


  

    
    
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
