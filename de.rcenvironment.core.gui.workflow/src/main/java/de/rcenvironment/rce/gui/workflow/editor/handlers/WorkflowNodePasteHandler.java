/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor.handlers;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ui.actions.Clipboard;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.commands.WorkflowNodeCreateCommand;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowNodePart;


/**
 * Renames a workflow node.
 *
 * @author Doreen Seider
 */
public class WorkflowNodePasteHandler extends AbstractWorkflowNodeEditHandler {
    
    @Override
    void edit() {
        final int offset = 20;
        
        Object content = Clipboard.getDefault().getContents();
        if (content instanceof WorkflowNodePart) {
            WorkflowNodePart part = (WorkflowNodePart) content;
            WorkflowNode wn = (WorkflowNode) part.getModel();
            commandStack.execute(new WorkflowNodeCreateCommand(new WorkflowNode(wn.getComponentDescription().clone()),
                (WorkflowDescription) viewer.getContents().getModel(),
                new Rectangle(wn.getX() + offset , wn.getY() + offset, 0, 0)));            
        }
    }

}
