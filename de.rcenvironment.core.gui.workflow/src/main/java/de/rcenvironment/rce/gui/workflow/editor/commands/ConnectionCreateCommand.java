/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.commands;

import org.eclipse.gef.commands.Command;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController;

/**
 * Command that opens the connection dialog.
 * 
 * @author Heinrich Wendel
 */
public class ConnectionCreateCommand extends Command {

    /** The workflow those nodes belong to. */
    private WorkflowDescription model;
    
    /** The source node. **/
    private WorkflowNode sourceNode;

    /** The target node. **/
    private WorkflowNode targetNode;

    /**
     * Constructor.
     * 
     * @param model The workflow those nodes belong to.
     * @param sourceNode The source node.
     */
    public ConnectionCreateCommand(WorkflowDescription model, WorkflowNode sourceNode) {
        this.model = model;
        this.sourceNode = sourceNode;
    }

    @Override
    public void execute() {
        new ConnectionDialogController(model, sourceNode, targetNode).open();
    }

    /**
     * Sets the target node.
     * @param target The target node.
     */
    public void setTarget(WorkflowNode target) {
        this.targetNode = target;
    }

}
