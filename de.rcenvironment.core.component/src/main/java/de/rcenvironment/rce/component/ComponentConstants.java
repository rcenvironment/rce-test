/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

/**
 * Class holding component constants.
 * 
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 * @author Heinrich Wendel
 */
public final class ComponentConstants {

    /**
     * Key to identify a created component controller instance at the service registry.
     */
    public static final String COMP_INSTANCE_ID_KEY = "rce.component.controller.instance";
    
    /**
     * Separator used in component controller instance key.
     */
    public static final String COMPONENT_ID_SEPARATOR = "_";
    
    /**
     * Key for full qualified name of the component implementing class.
     */
    public static final String COMPONENT_CLASS_KEY = "rce.component.class";
    
    /** Key for component's name. */
    public static final String COMPONENT_NAME_KEY = "rce.component.name";
    
    /**  Key for component's group. */
    public static final String COMPONENT_NAME_GROUP = "rce.component.group";

    /**
     * Key used within the properties of the component to define its version.
     */
    public static final String VERSION_DEF_KEY = "rce.component.version";
    
    /**
     * Key that specifies the location of the 16x16px icon for the component to show in the GUI.
     */
    public static final String ICON_16_KEY = "rce.component.icon-16";

    /**
     * Key that specifies the location of the 32x32px icon for the component to show in the GUI.
     */
    public static final String ICON_32_KEY = "rce.component.icon-32";
    
    /**
     * Key used within the properties of the component to define inputs as a comma separated list.
     */
    public static final String INPUTS_DEF_KEY = "rce.component.inputs";

    /**
     * Key used within the properties of the component to define outputs as a comma separated list.
     */
    public static final String OUTPUTS_DEF_KEY = "rce.component.outputs";

    /**
     * Key used within the properties of the component to define possible configuration keys as a
     * comma separated list.
     */
    public static final String CONFIGURATION_DEF_KEY = "rce.component.configuration";
    /**
     * Key used within the properties of the component to define possible placeholder attributes keys as a
     * comma separated list.
     */
    public static final String PLACEHOLDER_ATTRIBUTES_DEF_KEY = "rce.component.placeholderAttributes";

    /**
     * The entry of the Manifest indicating that the bundle provides at least one integrated
     * {@link Component}.
     */
    public static final String MANIFEST_ENTRY = "RCE-Component";
    
    /**
     * Separator in notification ids.
     */
    public static final String NOTIFICATION_ID_SEPARATOR = ":";
    
    /**
     * Prefix for identifier of output notifications.
     */
    public static final String OUTPUT_NOTIFICATION_ID_PREFIX = "rce.component.output:";
    
    /**
     * Notification identifier for state notifications.
     */
    public static final String STATE_NOTIFICATION_ID_PREFIX = "rce.component.state:";
    
    /**
     * Notification identifier for notifications about number or runs.
     */
    public static final String NO_OF_RUNS_NOTIFICATION_ID_PREFIX = "rce.component.noofruns:";
    
    /**
     * Notification identifier for finshed state notifications.
     */
    public static final String FINISHED_STATE_NOTIFICATION_ID_PREFIX = "rce.component.state.finished:";
    
    /**
     * Notification identifier for falied state notifications.
     */
    public static final String FAILED_STATE_NOTIFICATION_ID_PREFIX = "rce.component.state.failed:";
    
    /**
     * Notification identifier for input values notifications.
     */
    public static final String INPUT_NOTIFICATION_ID = "rce.component.input:";
    
    /**
     * Notification identifier for input values notifications.
     */
    public static final String CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID = "rce.component.input.current:";
    
    /** Substring of identifier of placeholder component used if a given one is not available. */
    public static final String PLACEHOLDER_COMPONENT_IDENTIFIER_CLASS = "de.rcenvironment.rce.component.Placeholder_";
    
    /** Group name for unknown components. */
    public static final String COMPONENT_GROUP_UNKNOWN = "Other";
    
    /** Group name for unknown components. */
    public static final String COMPONENT_GROUP_TEST = "Test";
    
    /** Group name for unknown components. */
    public static final String COMPONENT_VERSION_UNKNOWN = "x.y";
    
    /** usage type 'required': input value must be provided by previous component. */
    public static final String INPUT_USAGE_TYPE_REQUIRED = "required";
    
    /** default usage type . */
    public static final String INPUT_USAGE_TYPE_DEFAULT = INPUT_USAGE_TYPE_REQUIRED;

    /** usage type 'init': input value must be provided at least once by previous component. */
    public static final String INPUT_USAGE_TYPE_INIT = "init";

    /** usage type 'optional': input value can be provided by previous component but doesn't have to. */
    public static final String INPUT_USAGE_TYPE_OPTIONAL = "optional";

    /** usage types for dynamic inputs.*/
    public static final String[] INPUT_USAGE_TYPES = { INPUT_USAGE_TYPE_REQUIRED, INPUT_USAGE_TYPE_INIT, INPUT_USAGE_TYPE_OPTIONAL };

    /** meta data key for defining usage of dynamic inputs.*/
    public static final String METADATAKEY_INPUT_USAGE = "usage";

    /** constant. */
    public static final String METADATA_SEPARATOR = "#";
    /** constant. */
    public static final String METADATA_VALUE_SEPARATOR = "=";
    /** constant. */
    public static final String PLACEHOLDER_ATTRIBUTE_GUINAME = "guiName";
    /** constant. */
    public static final Object PLACEHOLDER_ATTRIBUTE_ISPATH = "isPath";
    /** constant. */
    public static final Object PLACEHOLDER_ATTRIBUTE_PRIORITY = "priority";
    /** Private Constructor. */
    private ComponentConstants() {
    // NOP
    }
}
