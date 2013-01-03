/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.commons;

/**
 * Constants shared by GUI and Non-GUI implementations.
 * 
 * @author Markus Kunde
 */
public final class ParametricStudyComponentConstants {

    /** Name of the component as it is defined declaratively in OSGi component. */
    public static final String COMPONENT_NAME = "Parametric Study";

    /** Internal identifier of the Parametric Study component. */
    public static final String COMPONENT_ID = "de.rcenvironment.rce.components.parametricstudy.ParametricStudyComponent_" + COMPONENT_NAME;

    /** Suffix used for publishing Parametric Study notifications. */
    public static final String NOTIFICATION_SUFFIX = ":rce.component.parametricstudy";
    
    /** Suffix used for configuration value name. */
    public static final String CV_FROMVALUE = "FromValue";
    
    /** Suffix used for configuration value name. */
    public static final String CV_TOVALUE = "ToValue";
    
    /** Suffix used for configuration value name. */
    public static final String CV_STEPSIZE = "StepSize";
    

    private ParametricStudyComponentConstants() {}

}
