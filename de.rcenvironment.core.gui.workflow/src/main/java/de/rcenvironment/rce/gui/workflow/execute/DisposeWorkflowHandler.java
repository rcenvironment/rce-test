/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowStateModel;

/**
 * 
 * Disposes of workflows.
 * 
 * @author Christian Weiss
 */
public class DisposeWorkflowHandler extends AbstractWorkflowHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final SimpleWorkflowRegistry workflowRegistry = getWorkflowRegistry();
        for (final WorkflowInformation workflowInformation : getWorkflowInformations()) {
            if (workflowInformation != null) {
                workflowRegistry.disposeWorkflow(workflowInformation);
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        boolean result = false;
        for (final WorkflowInformation workflowInformation : getWorkflowInformations()) {
            final WorkflowState workflowState = WorkflowStateModel.getInstance().getState(workflowInformation.getIdentifier());
            if (workflowState == WorkflowState.FINISHED
                || workflowState == WorkflowState.CANCELED
                || workflowState == WorkflowState.FAILED) {
                result = true;
                break;
            }
        }
        return result;
    }

}
