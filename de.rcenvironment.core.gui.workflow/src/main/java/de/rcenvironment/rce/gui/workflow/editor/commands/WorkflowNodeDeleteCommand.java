/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;

import de.rcenvironment.rce.component.workflow.Connection;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * Command to delete a WorkflowNode.
 *
 * @author Heinrich Wendel
 */
public class WorkflowNodeDeleteCommand extends Command {
    
    /** The parent. **/
    private WorkflowDescription model;
    
    /** The child. **/
    private WorkflowNode node;
    
    /** Connections of this node for redo. */
    private List<Connection> connections = new ArrayList<Connection>();
    
    /**
     * Constructor.
     * 
     * @param model The parent.
     * @param node The child.
     */
    public WorkflowNodeDeleteCommand(WorkflowDescription model, WorkflowNode node) {
        this.model = model;
        this.node = node;
    }
    
    @Override
    public void execute() {
        redo();
    }
    
    @Override
    public void redo() {
        model.removeWorkflowNode(node);
        
        for (Connection c: model.getConnections()) {
            if (c.getTarget().equals(node) || c.getSource().equals(node)) {
                connections.add(c);
            }
        }
        
        for (Connection c: connections) {
            model.removeConnection(c);
        }
    }
    
    @Override
    public void undo() {
        model.addWorkflowNode(node);

        for (Connection c: connections) {
            model.addConnection(c);
        }
    }
    
}
