/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor.handlers;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.gui.workflow.editor.commands.ConnectionCreateCommand;


/**
 * Opens the connection editor.
 *
 * @author Doreen Seider
 */
public class OpenConnectionEditorHandler extends AbstractWorkflowNodeEditHandler {

    @Override
    void edit() {
        commandStack.execute(new ConnectionCreateCommand((WorkflowDescription) viewer.getContents().getModel(), null));
    }

}
