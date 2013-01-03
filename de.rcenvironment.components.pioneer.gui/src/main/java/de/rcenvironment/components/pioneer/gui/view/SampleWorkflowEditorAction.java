/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.pioneer.gui.view;

import org.eclipse.gef.commands.Command;

import de.rcenvironment.rce.gui.workflow.editor.WorkflowEditorAction;

/**
 * Sample for {@link WorkflowEditorAction}.
 * @author Christian Weiss
 */
public class SampleWorkflowEditorAction extends WorkflowEditorAction {

    @Override
    public void run() {
        commandStack.execute(new EditMessageCommand("pong"));
    }
    
    /**
     * Command to edit the underlying Python script.
     * @author Doreen Seider
     */
    private class EditMessageCommand extends Command {
        
        private static final String MESSAGE = "message";
        private String newMessage;
        private String oldMessage;
        
        protected EditMessageCommand(String newMessage) {
            this.newMessage = newMessage;
            oldMessage = (String) workflowNode.getProperty(MESSAGE);
        }
        
        @Override
        public void execute() {
            workflowNode.setProperty(MESSAGE, newMessage);
        }
        
        public void undo() {
            workflowNode.setProperty(MESSAGE, oldMessage);
        }
        
        public void redo() {
            execute();
        }
    }

}
