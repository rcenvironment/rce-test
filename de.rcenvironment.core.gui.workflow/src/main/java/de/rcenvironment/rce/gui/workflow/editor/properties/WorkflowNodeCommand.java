/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import org.eclipse.gef.commands.CommandStack;

import de.rcenvironment.rce.component.workflow.ComponentInstanceConfiguration;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * A command requesting access to {@link WorkflowNode} data.
 *
 * @author Christian Weiss
 */
public abstract class WorkflowNodeCommand {
    
    private String label;
    
    private CommandStack commandStack;
    
    private WorkflowNode workflowNode;

    private ComponentInstanceConfiguration compontentInstanceConfiguration;
    
    protected void setLabel(final String label) {
        this.label = label;
    }
    
    /**
     * Returns the label.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    final void setCommandStack(final CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    final void setWorkflowNode(final WorkflowNode workflowNode) {
        this.workflowNode = workflowNode;
    }
    
    protected WorkflowNode getWorkflowNode() {
        return workflowNode;
    }
    
    protected ComponentInstanceConfiguration getComponentInstanceConfiguration() {
        if (commandStack == null || workflowNode == null) {
            throw new IllegalStateException("Property input not set");
        }
        // returning a new ComponentPropertySource results in weird behaviour of Undo/Redo
        // if (compontentInstanceConfiguration == null) {
        // compontentInstanceConfiguration = new ComponentPropertySource(commandStack,
        // workflowNode);
        // }
        // return compontentInstanceConfiguration;
        return workflowNode;
    }
    
    /**
     * Performs initialization tasks BEFORE pushing the command on the stack.
     *
     */
    public abstract void initialize();
    
    /**
     * Returns, whether the command can be executed.
     * 
     * @return true, if the command can be executed
     */
    public abstract boolean canExecute();
    
    /**
     * Returns, whether the command can be undone.
     * 
     * @return true, if the command can be undone.
     */
    public abstract boolean canUndo();
    
    /**
     * Executes the command.
     *
     */
    public abstract void execute();
    
    /**
     * Re-executes the command.
     *
     */
    public void redo() {
        execute();
    }

    /**
     * Undoes the changes performed during {@link #execute())}.
     *
     */
    public abstract void undo();

    /**
     * An executor capable of handling {@link WorkflowNodeCommand}s.
     *
     * @author Christian Weiss
     */
    public interface Executor {
        
        /**
         * Executes the given {@link WorkflowNodeCommand}.
         * 
         * @param command the {@link WorkflowNodeCommand} to execute.
         */
        void execute(WorkflowNodeCommand command);
        
    }

}
