/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.component.workflow.ChannelListener;
import de.rcenvironment.rce.component.workflow.ComponentInstanceConfiguration;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * Class that maps properties of a component onto the IPropertySource interface.
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
public class ComponentPropertySource implements IPropertySource2, ComponentInstanceConfiguration {

    /** The WorkflowNode to operate on, holding the ComponentDescription. */
    private final WorkflowNode node;

    /** List of all properties of the component. */
    private final Map<String, Class<? extends Serializable>> properties;

    /** The command stack. */
    private final CommandStack cs;

    private final Map<String, Serializable> defaultValues;

    private final List<ChannelListener> channelListeners = new LinkedList<ChannelListener>();

    public ComponentPropertySource(CommandStack stack, WorkflowNode node) {
        this.node = node;

        properties = node.getComponentDescription().getConfigurationDefinitions();
        defaultValues = node.getComponentDescription().getDefaultConfiguration();
        cs = stack;
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {

        List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();

        for (Entry<String, Class<? extends Serializable>> entry : properties.entrySet()) {
            descriptors.add(new TextPropertyDescriptor(
                entry.getKey(), entry.getKey() + " (" + entry.getValue().getSimpleName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return descriptors.toArray(new IPropertyDescriptor[] {});
    }

    @Override
    public boolean isPropertyResettable(final Object key) {
        return false;
    }

    @Override
    public Object getPropertyValue(Object key) {
        Object value = node.getProperty(key.toString());
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        return value.toString();
    }

    @Override
    public boolean isPropertySet(Object key) {
        return properties.containsKey(key);
    }

    @Override
    public void resetPropertyValue(Object key) {
        setProperty((String) key, defaultValues.get(key));
    }

    @Override
    public void setPropertyValue(Object key, Object value) {
        if (value == null || ((String) value).isEmpty()) {
            setProperty((String) key, (String) null);
            return;
        }

        Class<? extends Serializable> type = properties.get(key);
        
        // attention: this type check is done in de.rcenvironment.rce.component.ComponentUtils
        // as well. if a supported data type is added or removed, so it is needed to do this in the
        // other class as well

        if (type == Integer.class) {
            try {
                setProperty((String) key, Integer.parseInt((String) value));
            } catch (NumberFormatException e) {
                final int i = 42;
            }

        } else if (type == Double.class) {
            try {
                setProperty((String) key, Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                final int i = 42;
            }

        } else if (type == Long.class) {
            try {
                setProperty((String) key, Long.parseLong((String) value));
            } catch (NumberFormatException e) {
                final int i = 42;
            }

        } else if (type == String.class) {
            setProperty((String) key, (String) value);

        } else if (type == Boolean.class) {
            try {
                setProperty((String) key, Boolean.parseBoolean((String) value));
            } catch (NumberFormatException e) {
                final int i = 42;
            }
        }
    }

    @Override
    public List<String> getPropertyMapIds() {
        return node.getPropertyMapIds();
    }
    
    @Override
    public void setPropertyMapId(String propertyMapId) {
        node.setPropertyMapId(propertyMapId);
    }

    @Override
    public String getPropertyMapId() {
        return node.getPropertyMapId();
    }
    
    @Override
    public void addPropertyMap(String newPropertyMapId, String clonePropertyMapId) {
        node.addPropertyMap(newPropertyMapId, clonePropertyMapId);
    }

    @Override
    public void removePropertyMap(String propertyMapId) {
        node.removePropertyMap(propertyMapId);
    }

    @Override
    public Serializable getProperty(String key) {
        return node.getProperty(key);
    }

    @Override
    public <T extends Serializable> void setProperty(String key, T value) {
        if ((value == null && getProperty(key) != null)
            || (value != null && !value.equals(getProperty(key)))) {
            SetValueCommand setCommand = new SetValueCommand(Messages.property, node, key, value);
            cs.execute(setCommand);
        }
    }
    
    @Override
    public boolean propertyExists(String key) {
        return isPropertySet(key);
    }

    /**
     * Command to change a property value.
     * 
     * @author Heinrich Wendel
     */
    class SetValueCommand extends Command {

        private final WorkflowNode target;

        private final String propertyName;

        private final Serializable propertyValue;

        private Serializable undoValue;

        public SetValueCommand(String label, WorkflowNode node, String id, Serializable value) {
            super(label);
            target = node;
            propertyName = id;
            propertyValue = value;
        }

        @Override
        public void execute() {
            undoValue = target.getProperty(propertyName);
            target.setProperty(propertyName, propertyValue);
        }

        @Override
        public void undo() {
            target.setProperty(propertyName, undoValue);
        }

    }

    @Override
    public void addInput(String name, String type) throws IllegalArgumentException {
        node.addInput(name, type);
        final ChannelEvent event = ChannelEvent.createAddEvent(this, EndpointNature.Input, name);
        fireChannelEvent(event);
    }

    @Override
    public void removeInput(String name) {
        node.removeInput(name);
        final ChannelEvent event = ChannelEvent.createRemoveEvent(this, EndpointNature.Input, name);
        fireChannelEvent(event);
    }

    @Override
    public Map<String, Class<? extends Serializable>> getDynamicInputDefinitions() {
        return node.getDynamicInputDefinitions();
    }

    @Override
    public void changeInput(String name, String newName, String newType) {
        final String oldType = node.getInputType(name);
        node.changeInput(name, newName, newType);
        final ChannelEvent event = ChannelEvent.createChangeEvent(this, EndpointNature.Input, name, newName, oldType, newType);
        fireChannelEvent(event);
    }

    @Override
    public boolean validateInputName(String name) {
        return node.validateInputName(name);
    }

    @Override
    public boolean validateInputType(String type) {
        return node.validateInputType(type);
    }

    @Override
    public String getInputType(String name) {
        return node.getInputType(name);
    }

    @Override
    public Map<String, Serializable> getInputMetaData(String inputName) {
        return node.getInputMetaData(inputName);
    }
    
    @Override
    public void setInputMetaData(String inputName, String metaDataKey, Serializable metaDataValue) {
        final Serializable oldValue = node.getInputMetaData(inputName).get(metaDataKey);
        node.setInputMetaData(inputName, metaDataKey, metaDataValue);
        final ChannelEvent event =
            ChannelEvent.createPropertyChangeEvent(this, EndpointNature.Input, inputName, metaDataKey, oldValue, metaDataValue);
        fireChannelEvent(event);
    }
    
    @Override
    public void addOutput(String name, String type) throws IllegalArgumentException {
        node.addOutput(name, type);
        final ChannelEvent event = ChannelEvent.createAddEvent(this, EndpointNature.Output, name);
        fireChannelEvent(event);
    }

    @Override
    public void removeOutput(String name) {
        node.removeOutput(name);
        final ChannelEvent event = ChannelEvent.createRemoveEvent(this, EndpointNature.Output, name);
        fireChannelEvent(event);
    }

    @Override
    public Map<String, Class<? extends Serializable>> getDynamicOutputDefinitions() {
        return node.getDynamicOutputDefinitions();
    }

    @Override
    public void changeOutput(String name, String newName, String newType) {
        final String oldType = node.getOutputType(name);
        node.changeOutput(name, newName, newType);
        final ChannelEvent event = ChannelEvent.createChangeEvent(this, EndpointNature.Output, name, newName, oldType, newType);
        fireChannelEvent(event);
    }

    @Override
    public boolean validateOutputName(String name) {
        return node.validateOutputName(name);
    }

    @Override
    public boolean validateOutputType(String type) {
        return node.validateOutputType(type);
    }

    @Override
    public String getOutputType(String name) {
        return node.getOutputType(name);
    }

    @Override
    public Map<String, Serializable> getOutputMetaData(String outputName) {
        return node.getOutputMetaData(outputName);
    }
    
    @Override
    public void setOutputMetaData(String outputName, String metaDataKey, Serializable metaDataValue) {
        final Serializable oldValue = node.getOutputMetaData(outputName).get(metaDataKey);
        node.setOutputMetaData(outputName, metaDataKey, metaDataValue);
        final ChannelEvent event =
            ChannelEvent.createPropertyChangeEvent(this, EndpointNature.Output, outputName, metaDataKey, oldValue, metaDataValue);
        fireChannelEvent(event);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        node.addPropertyChangeListener(listener);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        node.removePropertyChangeListener(listener);
    }

    @Override
    public void addChannelListener(final ChannelListener listener) {
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

}
