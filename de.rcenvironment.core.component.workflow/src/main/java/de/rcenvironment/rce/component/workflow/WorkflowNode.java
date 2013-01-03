/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import de.rcenvironment.rce.component.ChangeSupport;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;

/**
 * A node within a {@link WorkflowDescription}.
 * 
 * @author Roland Gude
 * @author Heinrich Wendel
 * @author Robert Mischke
 * @author Christian Weiss
 */
public class WorkflowNode extends ChangeSupport implements Serializable, ComponentInstanceConfiguration, Comparable<WorkflowNode> {

    /**
     * Prefix for {@link java.beans.PropertyChangeEvent}s concerning properties.
     */
    public static final String PROPERTIES_PREFIX = "properties.";

    /**
     * {@link Pattern} to match property names of {@link java.beans.PropertyChangeEvent}s concerning
     * properties.
     */
    public static final Pattern PROPERTIES_PATTERN = Pattern.compile("^properties\\.(.*)$");

    /**
     * Property that is fired when the location changes.
     */
    public static final String LOCATION_PROP = "WorkflowNode.Location";

    /**
     * Property that is fired when the name changes.
     */
    public static final String NAME_PROP = "WorkflowNode.Name";

    /**
     * Property that is fired when the configuration changes.
     */
    public static final String CONFIGURATION_PROP = "WorkflowNode.Configuration";

    /**
     * Property that is fired when the configuration changes.
     */
    public static final String DYN_ENDPOINTS_PROP = "WorkflowNode.DynamicEndpoints";

    /**
     * Property that is fired when the configuration changes.
     */
    public static final String META_DATA_PROP = "WorkflowNode.MetaData";


    private static final long serialVersionUID = -7495156467094187194L;

    /**
     * The {@link ComponentDescription} of the {@link de.rcenvironment.rce.component.Component}
     * represented by this {@link WorkflowNode}.
     */
    private ComponentDescription compDesc;

    /**
     * Unique identifier of this node.
     */
    private String identifier;

    /**
     * Visual name of the node.
     */
    private String name;

    /**
     * X position of the location in a graphical editor.
     */
    private int x;

    /**
     * Y position of the location in a graphical editor.
     */
    private int y;

    // TODO check! seems to be GUI stuff under the hood. (caused NotSerializableException, e.g. for
    // de.rcenvironment.rce.components.gui.optimizer.properties.OptimizerWorkflowNodeValidator)
    private transient List<ChannelListener> channelListeners = new LinkedList<ChannelListener>();

    /**
     * Constructor.
     * 
     * @param componentDescription The {@link ComponentDescription} of the
     *        {@link de.rcenvironment.rce.component.Component} represented by this
     *        {@link WorkflowNode}.
     * @param componentConfiguration The configuration of this
     *        {@link de.rcenvironment.rce.component.Component} represented by this
     *        {@link WorkflowNode}.
     */
    public WorkflowNode(ComponentDescription componentDescription) {
        compDesc = componentDescription;
        identifier = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return compDesc.getIdentifier();
    }

    public ComponentDescription getComponentDescription() {
        return compDesc;
    }

    /**
     * Setter.
     * 
     * @param cd ComponentDescription to set.
     */
    public void setComponentDescription(ComponentDescription cd) {
        this.compDesc = cd;
    }

    /**
     * @return the X location of the node.
     */
    public int getX() {
        return x;
    }

    /**
     * @return the Y location of the node.
     */
    public int getY() {
        return y;
    }

    /**
     * @param newX The new X location.
     * @param newY The new Y location.
     */
    public void setLocation(int newX, int newY) {
        x = newX;
        y = newY;
        firePropertyChange(LOCATION_PROP);
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
        firePropertyChange(NAME_PROP);
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WorkflowNode) {
            return identifier.equals(((WorkflowNode) other).identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public List<String> getPropertyMapIds() {
        return compDesc.getConfigurationIds();
    }

    @Override
    public void setPropertyMapId(String propertyMapId) {
        String oldMapId = compDesc.getConfigurationId();
        compDesc.setConfigurationId(propertyMapId);
        firePropertyChange(CONFIGURATION_PROP, oldMapId, propertyMapId);
    }

    @Override
    public String getPropertyMapId() {
        return compDesc.getConfigurationId();
    }

    @Override
    public void addPropertyMap(String newPropertyMapId, String clonePropertyMapId) {
        compDesc.addConfiguration(newPropertyMapId, clonePropertyMapId);
        firePropertyChange(CONFIGURATION_PROP, null, newPropertyMapId);
    }

    @Override
    public void removePropertyMap(String propertyMapId) {
        compDesc.removeConfiguration(propertyMapId);
        firePropertyChange(CONFIGURATION_PROP, propertyMapId, null);
    }

    @Override
    public Serializable getProperty(String key) {
        return compDesc.getConfiguration().get(key);
    }

    @Override
    public <T extends Serializable> void setProperty(String key, T value) {
        final Object oldValue = compDesc.getConfiguration().get(key);
        if (oldValue == value || (value != null && value.equals(oldValue))) {
            return;
        }
        if (value == null) {
            compDesc.getConfiguration().remove(key);
        } else {
            compDesc.getConfiguration().put(key, value);
        }
        firePropertyChange(CONFIGURATION_PROP, oldValue, value);
        firePropertyChange(PROPERTIES_PREFIX + key, oldValue, value);
    }

    @Override
    public boolean propertyExists(String key) {
        return compDesc.getConfiguration().containsKey(key);
    }

    // needed for the persistence handler to set the identifier given by file
    protected void setIdentifier(String newIdentifier) {
        identifier = newIdentifier;
    }

    @Override
    public void addInput(String inputName, String type) throws IllegalArgumentException {
        compDesc.addInput(inputName, type);
        final ChannelEvent event = ChannelEvent.createAddEvent(this, EndpointNature.Input, inputName);
        fireChannelEvent(event);
    }

    @Override
    public void removeInput(String inputName) {
        compDesc.removeInput(inputName);
        final ChannelEvent event = ChannelEvent.createRemoveEvent(this, EndpointNature.Input, inputName);
        fireChannelEvent(event);
    }

    @Override
    public String getInputType(String inputName) {
        return compDesc.getInputType(inputName);
    }

    @Override
    public Map<String, Class<? extends Serializable>> getDynamicInputDefinitions() {
        return Collections.unmodifiableMap(compDesc.getDynamicInputDefinitions());
    }

    @Override
    public void changeInput(String inputName, String newName, String newType) {
        final String oldType = compDesc.getInputType(inputName);
        compDesc.changeInput(inputName, newName, newType);
        final ChannelEvent event = ChannelEvent.createChangeEvent(this, EndpointNature.Input, inputName, newName, oldType, newType);
        fireChannelEvent(event);
    }

    @Override
    public boolean validateInputName(String inputName) {
        return compDesc.validateInputName(inputName);
    }

    @Override
    public boolean validateInputType(String type) {
        return compDesc.validateInputType(type);
    }

    @Override
    public Map<String, Serializable> getInputMetaData(String inputName) {
        return Collections.unmodifiableMap(compDesc.getInputMetaData(inputName));
    }

    @Override
    public void setInputMetaData(String inputName, String metaDataKey, Serializable metaDataValue) {
        final Serializable oldValue = compDesc.getInputMetaData(inputName).get(metaDataKey);
        compDesc.setInputMetaData(inputName, metaDataKey, metaDataValue);
        firePropertyChange(META_DATA_PROP);
        final ChannelEvent event =
            ChannelEvent.createPropertyChangeEvent(this, EndpointNature.Input, inputName, metaDataKey, oldValue, metaDataValue);
        fireChannelEvent(event);
    }

    @Override
    public void addOutput(String outputName, String type) throws IllegalArgumentException {
        compDesc.addOutput(outputName, type);
        final ChannelEvent event = ChannelEvent.createAddEvent(this, EndpointNature.Output, outputName);
        fireChannelEvent(event);
    }

    @Override
    public void removeOutput(String outputName) {
        compDesc.removeOutput(outputName);
        final ChannelEvent event = ChannelEvent.createRemoveEvent(this, EndpointNature.Output, outputName);
        fireChannelEvent(event);
    }

    @Override
    public String getOutputType(String outputName) {
        return compDesc.getOutputType(outputName);
    }

    @Override
    public Map<String, Class<? extends Serializable>> getDynamicOutputDefinitions() {
        return Collections.unmodifiableMap(compDesc.getDynamicOutputDefinitions());
    }

    @Override
    public void changeOutput(String outputName, String newName, String newType) {
        final String oldType = compDesc.getOutputType(outputName);
        compDesc.changeOutput(outputName, newName, newType);
        final ChannelEvent event = ChannelEvent.createChangeEvent(this, EndpointNature.Output, outputName, newName, oldType, newType);
        fireChannelEvent(event);
    }

    @Override
    public boolean validateOutputName(String outputName) {
        return compDesc.validateOutputName(outputName);
    }

    @Override
    public boolean validateOutputType(String type) {
        return compDesc.validateOutputType(type);
    }

    @Override
    public Map<String, Serializable> getOutputMetaData(String outputName) {
        return Collections.unmodifiableMap(compDesc.getOutputMetaData(outputName));
    }

    @Override
    public void setOutputMetaData(String outputName, String metaDataKey, Serializable metaDataValue) {
        final Serializable oldValue = compDesc.getOutputMetaData(outputName).get(metaDataKey);
        compDesc.setOutputMetaData(outputName, metaDataKey, metaDataValue);
        firePropertyChange(META_DATA_PROP);
        final ChannelEvent event =
            ChannelEvent.createPropertyChangeEvent(this, EndpointNature.Output, outputName, metaDataKey, oldValue, metaDataValue);
        fireChannelEvent(event);
    }

    @Override
    public void addChannelListener(final ChannelListener listener) {
        if (channelListeners == null){
            channelListeners = new LinkedList<ChannelListener>();
        }
        channelListeners.add(listener);
    }

    @Override
    public void removeChannelListener(final ChannelListener listener) {
        channelListeners.remove(listener);
    }

    private void fireChannelEvent(final ChannelEvent event) {
        for (final ChannelListener listener : channelListeners) {
            listener.handleChannelEvent(event);
        }
    }
    @Override
    public int compareTo(WorkflowNode o) {
        return getName().compareTo(o.getName());
    }



}
