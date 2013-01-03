/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * Command that creates a new WorkflowNode.
 *
 * @author Heinrich Wendel
 */
public class WorkflowNodeCreateCommand extends Command {

    private static final String MINUS = "-";

    /** The parent WorkflowDescription. */
    private final WorkflowDescription model;
    
    /** The new WorkflowNode. */
    private WorkflowNode node;
    
    /** The constraints. */
    private Rectangle constraint;
    
    /**
     * Constructor.
     * 
     * @param node The new WorkflowNode.
     * @param model The parent WorkflowDescription.
     * @param constraint The constraints for the new node.
     */
    public WorkflowNodeCreateCommand(WorkflowNode node, WorkflowDescription model, Rectangle constraint) {
        this.node = node;
        this.model = model;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        node.setName(getName(node.getComponentDescription().getName()));
        redo();
    }
    
    @Override
    public void redo() {
        node.setLocation(constraint.getLocation().x, constraint.getLocation().y);
        model.addWorkflowNode(node);
    }

    @Override
    public void undo() {
        model.removeWorkflowNode(node);
    }
    
    /** Helper methods which returns the next unused default name of a component. */
    private String getName(String name)  {
        
        int count = 0;
        for (WorkflowNode n: model.getWorkflowNodes()) {
            if (n.getName().equals(name)) {
                if (name.contains(MINUS)) {
                    try {
                        count = Integer.valueOf(name.substring(name.lastIndexOf(MINUS) + 1, name.length()));
                    } catch (NumberFormatException e) {
                        count = 0;
                    }
                    name = name.substring(0, name.lastIndexOf(MINUS));
                }
                count++;
                return getName(name + MINUS + count);
            }
        }
        
        return name;
    }
}
