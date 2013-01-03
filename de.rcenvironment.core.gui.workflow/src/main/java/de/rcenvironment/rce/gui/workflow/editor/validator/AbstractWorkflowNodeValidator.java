/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.validator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.component.workflow.ChannelListener;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.component.workflow.WorkflowNodeUtil;

/**
 * Abstract base implementation of a {@link WorkflowNodeValidator}.
 * 
 * @author Christian Weiss
 */
public abstract class AbstractWorkflowNodeValidator implements WorkflowNodeValidator {

    private WorkflowNode workflowNode;

    private WorkflowNodeChangeListener changeListener;

    private final List<WorkflowNodeValidityChangeListener> changeListeners = new LinkedList<WorkflowNodeValidityChangeListener>();

    private final List<WorkflowNodeValidityStateListener> stateListeners = new LinkedList<WorkflowNodeValidityStateListener>();

    private final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();

    private Boolean valid;

    @Override
    public final void setWorkflowNode(final WorkflowNode workflowNode) {
        if (this.workflowNode != null) {
            throw new IllegalStateException("WorkflowNode already set.");
        }
        this.workflowNode = workflowNode;
        initializeModelBinding();
        revalidate();
    }

    private void initializeModelBinding() {
        final WorkflowNodeChangeListener listener = createWorkflowNodeChangeListener();
        workflowNode.addPropertyChangeListener(listener);
        workflowNode.addChannelListener(listener);
        afterInitializingModelBinding();
    }

    protected void afterInitializingModelBinding() {
        // do nothing
    }

    protected WorkflowNode getWorkflowNode() {
        return workflowNode;
    }

    protected WorkflowNodeChangeListener getWorkflowNodeChangeListener() {
        return changeListener;
    }

    protected WorkflowNodeChangeListener createWorkflowNodeChangeListener() {
        return new DefaultWorkflowNodeChangeListener();
    }

    private void revalidate() {
        if (workflowNode == null) {
            throw new IllegalStateException("WorkflowNode not set.");
        }
        messages.clear();
        final Collection<WorkflowNodeValidationMessage> validateMessages = validate();
        if (validateMessages != null) {
            messages.addAll(validateMessages);
        }
    }

    @Override
    public final Collection<WorkflowNodeValidationMessage> getMessages() {
        if (workflowNode == null) {
            throw new IllegalStateException("WorkflowNode not set.");
        }
        return messages;
    }

    /**
     * Returns all {@link WorkflowNodeValidationMessage} that hint for failures or errors in the
     * configuration. If no failures/errors were found an empty collection or 'null' should be
     * returned.
     * 
     * @return 'null' or an empty collection, if no failures/errors were found, otherwise all
     *         {@link WorkflowNodeValidationMessage} that explain the failures/errors.
     */
    protected abstract Collection<WorkflowNodeValidationMessage> validate();

    @Override
    public boolean isValid() {
        if (valid == null) {
            refreshValid();
        }
        return valid;
    }

    /**
     * Returns the current metadata for the given input channel.
     * @param channelName : name of the channel to get the metadata from
     * @return : map with all metadata for the given channel
     */
    public Map<String, Serializable> getInputMetaData(String channelName) {
        return workflowNode.getInputMetaData(channelName);
    }

    /**
     * Returns the current metadata for the given output channel.
     * @param channelName : name of the channel to get the metadata from
     * @return : map with all metadata for the given channel
     */
    public Map<String, Serializable> getOutputMetaData(String channelName) {
        return workflowNode.getOutputMetaData(channelName);
    }

    protected void setValid(final boolean valid) {
        if (this.valid == null || this.valid != valid) {
            this.valid = valid;
            final WorkflowNodeValidityChangeEvent event = new WorkflowNodeValidityChangeEvent(getWorkflowNode(), valid);
            fireWorkflowNodeValidityChangeEvent(event);
        }
        final WorkflowNodeValidityStateEvent event = new WorkflowNodeValidityStateEvent(getWorkflowNode(), valid);
        fireWorkflowNodeValidityStateEvent(event);
    }

    protected void refreshValid() {
        final List<WorkflowNodeValidationMessage> oldMessages = new ArrayList<WorkflowNodeValidationMessage>(messages);
        revalidate();
        if (valid == null || !messages.equals(oldMessages)) {
            final boolean newValid = messages == null || messages.isEmpty();
            setValid(newValid);
        }
    }

    @Override
    public void addWorkflowNodeValidityChangeListener(final WorkflowNodeValidityChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void removeWorkflowNodeValidityChangeListener(final WorkflowNodeValidityChangeListener listener) {
        changeListeners.remove(listener);
    }

    protected void fireWorkflowNodeValidityChangeEvent(final WorkflowNodeValidityChangeEvent event) {
        for (final WorkflowNodeValidityChangeListener listener : changeListeners) {
            listener.handleWorkflowNodeValidityChangeEvent(event);
        }
    }

    @Override
    public void addWorkflowNodeValidityStateListener(WorkflowNodeValidityStateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public void removeWorkflowNodeValidityStateListener(WorkflowNodeValidityStateListener listener) {
        stateListeners.remove(listener);
    }

    protected void fireWorkflowNodeValidityStateEvent(final WorkflowNodeValidityStateEvent event) {
        for (final WorkflowNodeValidityStateListener listener : stateListeners) {
            listener.handleWorkflowNodeValidityStateEvent(event);
        }
    }

    protected boolean hasInputs() {
        return WorkflowNodeUtil.hasInputs(workflowNode);
    }

    protected boolean hasOutputs() {
        return WorkflowNodeUtil.hasOutputs(workflowNode);
    }

    protected boolean hasInputs(final Class<? extends Serializable> type) {
        return WorkflowNodeUtil.hasInputs(workflowNode, type);
    }

    protected boolean hasOutputs(final Class<? extends Serializable> type) {
        return WorkflowNodeUtil.hasOutputs(workflowNode, type);
    }

    protected Map<String, Class<? extends Serializable>> getInputs() {
        return WorkflowNodeUtil.getInputs(workflowNode);
    }

    protected <T extends Serializable> Map<String, Class<? extends T>> getInputs(final Class<T> type) {
        return WorkflowNodeUtil.getInputs(workflowNode, type);
    }

    protected Map<String, Class<? extends Serializable>> getOutputs() {
        return WorkflowNodeUtil.getOutputs(workflowNode);
    }

    protected <T extends Serializable> Map<String, Class<? extends T>> getOutputs(final Class<T> type) {
        return WorkflowNodeUtil.getOutputs(workflowNode, type);
    }

    protected boolean hasProperty(final String key) {
        return WorkflowNodeUtil.hasProperty(workflowNode, key);
    }

    protected Class<? extends Serializable> getPropertyType(final String key) {
        return WorkflowNodeUtil.getPropertyType(workflowNode, key);
    }

    protected boolean isPropertySet(final String key) {
        return WorkflowNodeUtil.isPropertySet(workflowNode, key);
    }

    protected Serializable getProperty(final String key) {
        return WorkflowNodeUtil.getProperty(workflowNode, key);
    }

    protected <T extends Serializable> T getProperty(final String key, final Class<T> clazz) {
        return WorkflowNodeUtil.getProperty(workflowNode, key, clazz);
    }

    protected <T extends Serializable> T getProperty(final String key, final Class<T> clazz, final T defaultValue) {
        return WorkflowNodeUtil.getProperty(workflowNode, key, clazz, defaultValue);
    }

    /**
     * Interface to be used to synchronize with changes in the {@link WorkflowNode}.
     * 
     * @author Christian Weiss
     */
    protected interface WorkflowNodeChangeListener extends PropertyChangeListener, ChannelListener {

    }

    /**
     * Default implementation of {@link WorkflowNodeChangeListener}.
     * 
     * @author Christian Weiss
     */
    protected class DefaultWorkflowNodeChangeListener implements WorkflowNodeChangeListener, Serializable {

        private static final long serialVersionUID = 8472168965932179663L;

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            final String propertyNameString = event.getPropertyName();
            final Matcher propertiesPatternMatcher = WorkflowNode.PROPERTIES_PATTERN.matcher(propertyNameString);
            if (propertiesPatternMatcher.matches()) {
                refresh();
            }
        }

        @Override
        public void handleChannelEvent(final ChannelEvent event) {
            refresh();
        }

        protected void refresh() {
            AbstractWorkflowNodeValidator.this.refreshValid();
        }

    }

}
