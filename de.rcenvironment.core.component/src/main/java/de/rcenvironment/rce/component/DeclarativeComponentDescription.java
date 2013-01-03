/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.util.Map;

/**
 * Class holding {@link Component} information described declaratively.
 * 
 * @author Doreen Seider
 */
public final class DeclarativeComponentDescription implements Serializable, Comparable<DeclarativeComponentDescription> {
    
    private static final long serialVersionUID = -7551319972711119245L;

    private final String identifier;
    
    private final String className;

    private final String name;
    
    private final String group;

    private final String version;

    private final Map<String, Class<? extends Serializable>> inputDefs;

    private final Map<String, Class<? extends Serializable>> outputDefs;
    
    private final Map<String, Map<String, Serializable>> inputMetaDefs;

    private final Map<String, Map<String, Serializable>> outputMetaDefs;
    
    private final Map<String, Class<? extends Serializable>> configDefs;

    private final Map<String, Serializable> defaultConfig;

    private final byte[] icon16;

    private final byte[] icon32;

    private final Map<String, Map<String, String>> placeholderAttributesDefs;

  
    public DeclarativeComponentDescription(String newIdentifier, String newName, String newGroup, String newVersion,
        Map<String, Class<? extends Serializable>> newInputDefs, Map<String, Class<? extends Serializable>> newOutputDefs, 
        Map<String, Map<String, Serializable>> newInputMetaDefs,
        Map<String, Map<String, Serializable>> newOutputMetaDefs,
        Map<String, Class<? extends Serializable>> newConfigDefs, Map<String, 
        Map<String, String>> newPlaceholderAttributesDefs, Map<String, Serializable> newDefaultConfig,
        byte[] newIcon16, byte[] newIcon32) {

        identifier = newIdentifier;
        className = identifier.substring(0, identifier.indexOf(ComponentConstants.COMPONENT_ID_SEPARATOR));
        name = newName;
        if (newGroup == null) {
            group = ComponentConstants.COMPONENT_GROUP_UNKNOWN;
        } else {
            group = newGroup;            
        }
        version = newVersion;
        inputDefs = newInputDefs;
        outputDefs = newOutputDefs;
        inputMetaDefs = newInputMetaDefs;
        outputMetaDefs = newOutputMetaDefs;
        configDefs = newConfigDefs;
        defaultConfig = newDefaultConfig;
        icon16 = newIcon16;
        icon32 = newIcon32;
        placeholderAttributesDefs = newPlaceholderAttributesDefs;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getName() {
        return name;
    }
    
    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public byte[] getIcon16() {
        return icon16;
    }

    public byte[] getIcon32() {
        return icon32;
    }

    public Map<String, Class<? extends Serializable>> getInputDefinitions() {
        return inputDefs;
    }

    public Map<String, Class<? extends Serializable>> getOutputDefinitions() {
        return outputDefs;
    }

    public Map<String, Map<String, Serializable>> getInputMetaDefs() {
        return inputMetaDefs;
    }

    public Map<String, Map<String, Serializable>> getOutputMetaDefs() {
        return outputMetaDefs;
    }

    public Map<String, Class<? extends Serializable>> getConfigurationDefinitions() {
        return configDefs;
    }

    public Map<String, Serializable> getDefaultConfiguration() {
        return defaultConfig;
    }

    @Override
    public int compareTo(DeclarativeComponentDescription o) {
        return name.compareTo(o.getName());
    }

  
    public Map<String, Map<String, String>> getPlaceholderAttributesDefs() {
        return placeholderAttributesDefs;
    }
}
