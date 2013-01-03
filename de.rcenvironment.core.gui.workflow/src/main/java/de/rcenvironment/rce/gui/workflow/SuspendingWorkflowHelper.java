/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.view.properties.InputModel;
import de.rcenvironment.rce.notification.SimpleNotificationService;


/**
 * Utility class for suspending workflows.
 * 
 * @author Doreen Seider
 */
public final class SuspendingWorkflowHelper {
    
    private SuspendingWorkflowHelper() { }

    /**
     * Suspends a workflow. Thereby, it gets all current pending inputs of the workflow and pushes
     * it to the input view for editing support.
     * @param workflowInformation of the workflow to suspend
     */
    public static void suspendWorkflow(WorkflowInformation workflowInformation) {
        new SimpleNotificationService().subscribe(WorkflowConstants.STATE_NOTIFICATION_ID + workflowInformation.getIdentifier(),
            new WorkflowPausedListener(workflowInformation),
            workflowInformation.getControllerPlatform());
        SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        swr.pauseWorkflow(workflowInformation);
    }
    
    /**
     * Resumes a workflow. Thereby, it checks in advance if inputs were modified. If so, the inputs
     * are pushed to the workflow before resuming it.
     * @param workflowInformation of the workflow to resume
     */
    public static void resumeWorkflow(WorkflowInformation workflowInformation) {
        SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        InputModel model = InputModel.getInstance();
        for (ComponentInstanceDescriptor cid : swr.getComponentInstanceDescriptors(workflowInformation)) {
            if (model.hasBeenModified(workflowInformation.getIdentifier(), cid.getIdentifier())) {
                String workflowId = workflowInformation.getIdentifier();
                String componentId = cid.getIdentifier();
                swr.setInputs(workflowInformation, componentId, model.getRawInputs(workflowId, componentId));
            }
        }
        swr.resumeWorkflow(workflowInformation);
    }
    
}
