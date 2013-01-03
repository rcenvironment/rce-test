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

import de.rcenvironment.rce.gui.workflow.parts.WorkflowNodePart;


/**
 * Handle copy part of copy&paste.
 *
 * @author Doreen Seider
 */
public class WorkflowNodeCopyHandler extends AbstractWorkflowNodeEditHandler {

    @Override
    void edit() {
        @SuppressWarnings("rawtypes")
        List selection = viewer.getSelectedEditParts();
        Clipboard.getDefault().setContents((WorkflowNodePart) selection.get(0));
    }

}
