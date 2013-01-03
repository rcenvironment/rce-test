/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor.handlers;

import java.util.List;

import org.eclipse.gef.ui.actions.Clipboard;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.commands.WorkflowNodeRenameCommand;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowNodePart;


/**
 * Handles paste part of copy/cut & paste.
 *
 * @author Doreen Seider
 */
public class WorkflowNodeRenameHandler extends AbstractWorkflowNodeEditHandler {
    
    @Override
    void edit() {
        @SuppressWarnings("rawtypes")
        List selection = viewer.getSelectedEditParts();
        WorkflowNodePart part = (WorkflowNodePart) selection.get(0);
        Clipboard.getDefault().setContents(part);
        WorkflowNode wn = (WorkflowNode) part.getModel();
        String oldName = wn.getName();
        commandStack.execute(new WorkflowNodeRenameCommand(wn, (WorkflowDescription) viewer.getContents().getModel()));
        if (wn.getName().equals(oldName)) {
            commandStack.undo();
        }
    }

}
