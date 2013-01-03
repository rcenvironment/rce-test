/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.gui.python;

import org.eclipse.gef.commands.Command;

import de.rcenvironment.core.gui.workflow.executor.properties.AbstractEditScriptRunnable;
import de.rcenvironment.rce.components.python.commons.PythonComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowEditorAction;

/**
 * {@link WorkflowEditorAction} used to open or edit the underlying Python script.
 * @author Doreen Seider
 */
public class EditScriptWorkflowEditorAction extends WorkflowEditorAction {

    @Override
    public void run() {
        new EditScriptRunnable().run();
    }
    
    /**
     * Implementation of {@link AbstractEditScriptRunnable}.
     * 
     * @author Doreen Seider
     */
    private class EditScriptRunnable extends AbstractEditScriptRunnable {

        protected void setScript(String script) {
            commandStack.execute(new EditScriptCommand(script));
        }
        
        protected String getScript() {
            return (String) workflowNode.getProperty(PythonComponentConstants.SCRIPT);
        }
    }
    
    /**
     * Command to edit the underlying Python script.
     * @author Doreen Seider
     */
    private class EditScriptCommand extends Command {
        
        private String newScript;
        private String oldScript;
        
        protected EditScriptCommand(String newScript) {
            oldScript = (String) workflowNode.getProperty(PythonComponentConstants.SCRIPT);
            this.newScript = newScript;
        }
        
        @Override
        public void execute() {
            workflowNode.setProperty(PythonComponentConstants.SCRIPT, newScript);
        }
        
        public void undo() {
            workflowNode.setProperty(PythonComponentConstants.SCRIPT, oldScript);
        }
        
        public void redo() {
            execute();
        }
    }

}
