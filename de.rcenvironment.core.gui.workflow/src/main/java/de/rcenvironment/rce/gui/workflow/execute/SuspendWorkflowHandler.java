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

import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.SuspendingWorkflowHelper;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowStateModel;

/**
 * 
 * Cancels workflows.
 * 
 * @author Christian Weiss
 */
public class SuspendWorkflowHandler extends AbstractWorkflowHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        for (final WorkflowInformation workflowInformation : getWorkflowInformations()) {
            if (workflowInformation != null) {
                SuspendingWorkflowHelper.suspendWorkflow(workflowInformation);
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        boolean result = false;
        for (final WorkflowInformation workflowInformation : getWorkflowInformations()) {
            final WorkflowState workflowState = WorkflowStateModel.getInstance().getState(workflowInformation.getIdentifier());
            if (workflowState == WorkflowState.PREPARING
                || workflowState == WorkflowState.READY
                || workflowState == WorkflowState.RUNNING
                || workflowState == WorkflowState.RESUMING) {
                result = true;
                break;
            }
        }
        return result;
    }

}
