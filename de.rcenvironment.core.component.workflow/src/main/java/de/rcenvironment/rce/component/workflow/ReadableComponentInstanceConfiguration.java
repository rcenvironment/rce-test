/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Provides read-write access to the configuration-time setup of component instances.
 * 
 * @author Robert Mischke
 */
public interface ReadableComponentInstanceConfiguration extends ChannelEventSource {

    /**
     * @return all existing configuration map ids.
     */
    List<String> getPropertyMapIds();
    
    /**
     * @return id of the current property map.
     */
    String getPropertyMapId();
    
    /**
     * Returns the value of the given property belonging to default configuration (id:
     * {@link de.rcenvironment.rce.component.ComponentDescription#DEFAULT_CONFIG_ID}).
     * 
     * @param key The key of the property.
     * @return The value of the property.
     */
    Serializable getProperty(String key);
    
    /**
     * Checks if a given property with the given key exists in default configuration (id:
     * {@link de.rcenvironment.rce.component.ComponentDescription#DEFAULT_CONFIG_ID}).
     * 
     * @param key The key of the property.
     * @return <code>true</code> if a property with the given id exists, else <code> false</code>.
     */
    boolean propertyExists(String key);

    /**
     * Checks whether the given name could be used for a new dynamic input.
     * 
     * @param name The name of an input to validate.
     * @return true, if this name could be used for a new dynamic input
     */
    boolean validateInputName(String name);

    /**
     * Checks whether a given fully qualified type name is valid as an input type.
     * 
     * @param type The fully qualified type name.
     * @return true, if the given type is valid
     */
    boolean validateInputType(String type);

    /**
     * @param name The name of the input.
     * 
     * @return the fully qualified type name for the given input.
     */
    String getInputType(String name);

    /**
     * @return all registered dynamic inputs.
     */
    Map<String, Class<? extends Serializable>> getDynamicInputDefinitions();

    /**
     * @param inputName the name of the affected
     *        {@link de.rcenvironment.rce.component.endpoint.Input}.
     * @return a map containing meta data keys and its values.
     */
    Map<String, Serializable> getInputMetaData(String inputName);

    /**
     * Checks whether the given name could be used for a new dynamic output.
     * 
     * @param name The name of an output to validate.
     * @return true, if this name could be used for a new dynamic output
     */
    boolean validateOutputName(String name);

    /**
     * Checks whether a given fully qualified type name is valid as an output type.
     * 
     * @param type The fully qualified type name.
     * @return true, if the given type is valid
     */
    boolean validateOutputType(String type);

    /**
     * @return all registered dynamic outputs.
     */
    Map<String, Class<? extends Serializable>> getDynamicOutputDefinitions();

    /**
     * @param name The name of the output.
     * 
     * @return the fully qualified type name for the given output.
     */
    String getOutputType(String name);

    /**
     * @param outputName the name of the affected
     *        {@link de.rcenvironment.rce.component.endpoint.Input}.
     * @return a map containing meta data keys and its values.
     */
    Map<String, Serializable> getOutputMetaData(String outputName);

    /**
     * Adds the given {@link PropertyChangeListener}.
     * 
     * @param listener the {@link PropertyChangeListener}
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes the given {@link PropertyChangeListener}.
     * 
     * @param listener the {@link PropertyChangeListener}
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
