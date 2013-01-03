/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rcenvironment.commons.StringUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.endpoint.DynamicEndpointManager;
import de.rcenvironment.rce.component.endpoint.EndpointChange;
import de.rcenvironment.rce.component.endpoint.EndpointMetaDataManager;
import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Class holding information about an installed {@link Component}.
 * 
 * @author Roland Gude
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 * @author Robert Mischke
 * @author Christian Weiss
 * @author Sascha Zur
 */
public class ComponentDescription extends ChangeSupport implements Serializable, Cloneable, Comparable<ComponentDescription> {

    /** Property that is fired when an endpoint was changed. */
    public static final String ENDPOINT_CHANGED_PROP = "ComponentDescription.EndpointChanged";

    /** Key of default configuration map. */
    public static final String DEFAULT_CONFIG_ID = "de.rcenvironment.rce.component.configuration.default";


    /**
     * The direction of an endpoint.
     * @author Christian Weiss
     */
    public static enum EndpointNature {
        /** Inputs. */
        Input,
        /** Outputs. */
        Output;
    }

    private static final long serialVersionUID = -7551319972711119245L;

    private final DeclarativeComponentDescription declarativeCD;

    private PlatformIdentifier platform;

    private DynamicEndpointManager inputManager;

    private DynamicEndpointManager outputManager;

    private EndpointMetaDataManager inputMetaDataManager;

    private EndpointMetaDataManager outputMetaDataManager;

    private Map<String, Map<String, Serializable>> configuration;

    private Map<String, Map<String, String>> placeholderAttributes;

    private Map<String, Serializable> placeholderMap;

    private String currentConfigMapId = DEFAULT_CONFIG_ID;

    public ComponentDescription(DeclarativeComponentDescription newDeclarativeCD) {
        declarativeCD = newDeclarativeCD;
        configuration = new HashMap<String, Map<String, Serializable>>();
        configuration.put(DEFAULT_CONFIG_ID, new HashMap<String, Serializable>(declarativeCD.getDefaultConfiguration()));
        inputManager = new DynamicEndpointManager(declarativeCD.getInputDefinitions());
        outputManager = new DynamicEndpointManager(declarativeCD.getOutputDefinitions());
        inputMetaDataManager = new EndpointMetaDataManager(declarativeCD.getInputMetaDefs());
        outputMetaDataManager = new EndpointMetaDataManager(declarativeCD.getOutputMetaDefs());
        setPlaceholderAttributes(declarativeCD.getPlaceholderAttributesDefs());
        placeholderMap = new HashMap<String, Serializable>();
    }

    public String getIdentifier() {
        return declarativeCD.getIdentifier();
    }

    public String getClassName() {
        return declarativeCD.getClassName();
    }

    public String getName() {
        return declarativeCD.getName();
    }

    public String getGroup() {
        return declarativeCD.getGroup();
    }

    public String getVersion() {
        return declarativeCD.getVersion();
    }

    public byte[] getIcon16() {
        return declarativeCD.getIcon16();
    }

    public byte[] getIcon32() {
        return declarativeCD.getIcon32();
    }

    public Map<String, Class<? extends Serializable>> getConfigurationDefinitions() {
        return declarativeCD.getConfigurationDefinitions();
    }

    public Map<String, Serializable> getDefaultConfiguration() {
        return declarativeCD.getDefaultConfiguration();
    }

    public PlatformIdentifier getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformIdentifier newPlatformIdentifier) {
        platform = newPlatformIdentifier;
    }

    /**
     * @return all inputs.
     */
    public Map<String, Class<? extends Serializable>> getInputDefinitions() {
        Map<String, Class<? extends Serializable>> result = new HashMap<String, Class<? extends Serializable>>();
        result.putAll(declarativeCD.getInputDefinitions());
        result.putAll(inputManager.getEndpointDefinitions());
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param name The name of the input.
     * @return the fully qualified type name for the given input
     */
    public String getInputType(String name) {
        return inputManager.getEndpointType(name);
    }

    /**
     * @return all registered dynamic inputs.
     */
    public Map<String, Class<? extends Serializable>> getDynamicInputDefinitions() {
        return inputManager.getEndpointDefinitions();
    }

    /**
     * Adds a new dynamic input.
     * 
     * @param name The name of the input.
     * @param type The type (class) of the input.
     * 
     * @throws IllegalArgumentException if the given name collides with an existing input/output.
     */
    public void addInput(String name, String type) throws IllegalArgumentException {
        inputManager.addEndpoint(name, type);

        Object newValue = new EndpointChange(EndpointChange.Type.Add, EndpointNature.Input, name, type, null, null, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Removes a dynamic input.
     * 
     * @param name The name of the endpoint to remove.
     */
    public void removeInput(String name) {
        final String type = getInputType(name);
        inputManager.removeEndpoint(name);

        Object newValue = new EndpointChange(EndpointChange.Type.Remove, EndpointNature.Input, name, type, name, type, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Changes the definition of an existing dynamic input.
     * 
     * @param name The name of the input to change.
     * @param newName The new name of the input.
     * @param newType The new type of the input.
     */
    public void changeInput(String name, String newName, String newType) {
        final String formerType = getInputType(name);
        inputManager.changeEndpoint(name, newName, newType);

        Object newValue = new EndpointChange(EndpointChange.Type.Change, EndpointNature.Input, newName, newType, name, formerType, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Checks whether the given name could be used for a new dynamic input.
     * 
     * @param name The name of an input to validate.
     * @return true, if this name could be used for a new dynamic input
     */
    public boolean validateInputName(String name) {
        if (declarativeCD.getInputDefinitions().keySet().contains(name)) {
            return false;
        }
        return inputManager.validateNewName(name);
    }

    /**
     * Checks whether a given fully qualified type name is valid as an input type.
     * 
     * @param type The fully qualified type name.
     * @return true, if the given type is valid
     */
    public boolean validateInputType(String type) {
        return inputManager.validateTypeName(type);
    }

    /**
     * @param inputName the name of the affected {@link Input}.
     * @return a map containing meta data keys and its values.
     */
    public Map<String, Serializable> getInputMetaData(String inputName) {
        return inputMetaDataManager.getEndpointMetaData(inputName);
    }

    /**
     * @param inputName the name of the affected {@link Input}.
     * @param metaDataKey the meta data key to set.
     * @param metaDataValue the meta data value to set.
     */
    public void setInputMetaData(String inputName, String metaDataKey, Serializable metaDataValue) {
        inputMetaDataManager.setEndpointMetaData(inputName, metaDataKey, metaDataValue);
    }
    /**
     * Removes all Inputchannels.
     *
     */
    public void removeAllInputs(){
        inputManager.removeAllEndpoints();
        /*
         Object newValue = new EndpointChange(EndpointChange.Type.Remove, EndpointNature.Input, name, type, name, type, this);
         firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);

         */
    }
    /**
     * @return all outputs.
     */
    public Map<String, Class<? extends Serializable>> getOutputDefinitions() {
        Map<String, Class<? extends Serializable>> result = new HashMap<String, Class<? extends Serializable>>();
        result.putAll(declarativeCD.getOutputDefinitions());
        result.putAll(outputManager.getEndpointDefinitions());
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param name The name of the output.
     * @return the fully qualified type name for the given output.
     */
    public String getOutputType(String name) {
        return outputManager.getEndpointType(name);
    }

    /**
     * @return all registered dynamic outputs.
     */
    public Map<String, Class<? extends Serializable>> getDynamicOutputDefinitions() {
        return outputManager.getEndpointDefinitions();
    }

    /**
     * Adds a new dynamic output.
     * 
     * @param name The name of the output.
     * @param type The type (class) of the output.
     * 
     * @throws IllegalArgumentException if the given name collides with an existing input/output.
     */
    public void addOutput(String name, String type) throws IllegalArgumentException {
        outputManager.addEndpoint(name, type);

        Object newValue = new EndpointChange(EndpointChange.Type.Add, EndpointNature.Output, name, type, null, null, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Removes a dynamic output.
     * 
     * @param name The name of the output to remove.
     */
    public void removeOutput(String name) {
        final String type = getOutputType(name);
        outputManager.removeEndpoint(name);

        Object newValue = new EndpointChange(EndpointChange.Type.Remove, EndpointNature.Output, name, type, name, type, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Changes the definition of an existing dynamic output.
     * 
     * @param name The name of the output to change.
     * @param newName The new name of the output.
     * @param newType The new type of the output.
     */
    public void changeOutput(String name, String newName, String newType) {
        final String formerType = getOutputType(name);
        outputManager.changeEndpoint(name, newName, newType);

        Object newValue = new EndpointChange(EndpointChange.Type.Change, EndpointNature.Output, newName, newType, name, formerType, this);
        firePropertyChange(ENDPOINT_CHANGED_PROP, newValue);
    }

    /**
     * Checks whether the given name could be used for a new dynamic output.
     * 
     * @param name The name of an output to validate.
     * @return true, if this name could be used for a new dynamic output
     */
    public boolean validateOutputName(String name) {
        if (declarativeCD.getOutputDefinitions().keySet().contains(name)) {
            return false;
        }
        return outputManager.validateNewName(name);
    }

    /**
     * Checks whether a given fully qualified type name is valid as an output type.
     * 
     * @param type The fully qualified type name.
     * @return true, if the given type is valid
     */
    public boolean validateOutputType(String type) {
        return outputManager.validateTypeName(type);
    }

    /**
     * @param outputName the name of the affected {@link Input}.
     * @return a map containing meta data keys and its values.
     */
    public Map<String, Serializable> getOutputMetaData(String outputName) {
        return outputMetaDataManager.getEndpointMetaData(outputName);
    }

    /**
     * @param outputName the name of the affected {@link Input}.
     * @param metaDataKey the meta data key to set.
     * @param metaDataValue the meta data value to set.
     */
    public void setOutputMetaData(String outputName, String metaDataKey, Serializable metaDataValue) {
        outputMetaDataManager.setEndpointMetaData(outputName, metaDataKey, metaDataValue);
    }

    /**
     * @return all existing configuration map ids.
     */
    public List<String> getConfigurationIds() {
        return new ArrayList<String>(configuration.keySet());
    }

    /**
     * Sets the configuration map to use to the given one, if it exists.
     * @param configMapId id of map to use.
     */
    public void setConfigurationId(String configMapId) {
        if (!configuration.containsKey(configMapId)) {
            throw new IllegalArgumentException("given configuration map does not exist");
        }
        this.currentConfigMapId = configMapId;
    }

    public String getConfigurationId() {
        return currentConfigMapId;
    }

    /**
     * Adds a new configuration map with given key. Entries are gathered from existing configuration map
     * given by its id. If no id is given the default values are set.
     * 
     * @param newConfigMapId id of the new configuration map.
     * @param cloneConfigMapId id of the configuration map to use its values from. <code>null</code>
     *        for using default values.
     */
    public void addConfiguration(String newConfigMapId, String cloneConfigMapId) {
        if (cloneConfigMapId != null) {
            configuration.put(newConfigMapId, new HashMap<String, Serializable>(configuration.get(cloneConfigMapId)));
        } else {
            configuration.put(newConfigMapId, new HashMap<String, Serializable>(declarativeCD.getDefaultConfiguration()));
        }
    }

    /**
     * Adds a new configuration map with given key. Entries are gathered from existing configuration map
     * given by its id. If no id is given the default values are set.
     * 
     * @param newConfigMapId id of the new configuration map.
     * @param values initial values. <code>null</code> for using default values.
     */
    public void addConfiguration(String newConfigMapId, Map<String, Serializable> values) {
        if (values != null) {
            configuration.put(newConfigMapId, new HashMap<String, Serializable>(values));
        } else {
            configuration.put(newConfigMapId, new HashMap<String, Serializable>(declarativeCD.getDefaultConfiguration()));
        }
    }

    /**
     * Removes configuration map given by its id.
     * @param configMapId id of map to remove.
     */
    public void removeConfiguration(String configMapId) {
        configuration.remove(configMapId);
        if (configMapId.equals(currentConfigMapId)) {
            currentConfigMapId = DEFAULT_CONFIG_ID;
        }
    }

    public Map<String, Serializable> getConfiguration() {
        return configuration.get(currentConfigMapId);
    }

    /**
     * @param configMapId id of map to return.
     * @return configuration map represented by the given id.
     */
    public Map<String, Serializable> getConfiguration(String configMapId) {
        return configuration.get(configMapId);
    }


    public Map<String, Map<String, String>> getPlaceholderAttributes() {
        return placeholderAttributes;
    }

    public void setPlaceholderAttributes(Map<String, Map<String, String>> placeholderAttributes) {
        this.placeholderAttributes = placeholderAttributes;
    }

    @Override
    public String toString() {
        return platform + StringUtils.SEPARATOR + declarativeCD.getIdentifier();
    }

    @Override
    public ComponentDescription clone() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bin);
            return (ComponentDescription) ois.readObject();
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public int compareTo(ComponentDescription o) {
        return this.getName().compareTo(o.getName());
    }

    public Map<String, Serializable> getPlaceholderMap() {
        return placeholderMap;
    }
    /**
     * Adds the given map to the current placeholderMap.
     * @param mapToAdd : placeholders to add.
     */
    public void addPlaceholderMap(Map<String, Serializable> mapToAdd) {
        if (placeholderMap != null && mapToAdd != null){
            this.placeholderMap.putAll(mapToAdd);
        }
    }



}
