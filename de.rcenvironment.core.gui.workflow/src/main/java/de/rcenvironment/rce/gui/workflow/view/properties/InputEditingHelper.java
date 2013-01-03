/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.properties;

import java.io.Serializable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.Endpoint;
import de.rcenvironment.rce.gui.workflow.SuspendingWorkflowHelper;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowStateModel;


/**
 * Provides helper methods used if editing inputs.
 *
 * @author Doreen Seider
 */
public final class InputEditingHelper {

    private InputEditingHelper() {}
    
    /**
     * Handles an editing request.
     * @param shell shell used to open dialogs
     * @param workflowInformation information about current workflow
     */
    public static void handleEditingRequest(Shell shell, WorkflowInformation workflowInformation) {
        final WorkflowState state = WorkflowStateModel.getInstance().getState(workflowInformation.getIdentifier());
        if (state == WorkflowState.RUNNING) {
            InputEditingSupport.enableEdit(false);
            if (MessageDialog.openQuestion(shell, Messages.confirmPauseTitle, Messages.confirmPauseMessage)) {
                SuspendingWorkflowHelper.suspendWorkflow(workflowInformation);
            }                    
        } else if (state == WorkflowState.PAUSED) {
            InputEditingSupport.enableEdit(true);
            ComponentInputSection.getInstance().cancelRefreshTimer();
        } else {
            InputEditingSupport.enableEdit(false);
            MessageDialog.openInformation(shell, Messages.editingInformationTitle , Messages.editingInformationMessage);
        }
    }
    
    /**
     * @param workflowInformation of affected workflow
     * @param endpoint of affected input
     * @return input value belonging to given endpoint
     */
    public static String getCurrentInputValue(WorkflowInformation workflowInformation, Endpoint endpoint) {
        return getCurrentInputValueFromEndpoint(workflowInformation.getIdentifier(), 
            getComponentIdentifier(workflowInformation, endpoint),
            endpoint);
    }
    
    /**
     * @param workflowInformation of affected workflow
     * @param endpoint of affected input
     * @return input value belonging to given endpoint
     */
    public static String getNextInputValue(WorkflowInformation workflowInformation, Endpoint endpoint) {
        return getNextInputValueFromEndpoint(workflowInformation.getIdentifier(), 
            getComponentIdentifier(workflowInformation, endpoint),
            endpoint);
    }
    
    private static String getComponentIdentifier(WorkflowInformation workflowInformation, Endpoint endpoint) {
        WorkflowNode workflowNode = endpoint.getWorkflowNode();
        return workflowInformation.getComponentInstanceDescriptor(workflowNode.getName(),
            workflowNode.getComponentDescription().getIdentifier()).getIdentifier();
    }
    
    /**
     * 
     * @param workflowId identifier of affected workflow
     * @param componentId identefier of affected component
     * @param endpoint of affected input
     * @return current input value belonging to given endpoint
     */
    public static String getCurrentInputValueFromEndpoint(String workflowId, String componentId, Endpoint endpoint) {
        Input input = InputModel.getInstance().getCurrentInput(workflowId, componentId, endpoint.getName());
        return getValueFromInput(input);
    }
    
    /**
     * @param workflowId identifier of affected workflow
     * @param componentId identefier of affected component
     * @param endpoint of affected input
     * @return next input value belonging to given endpoint
     */
    public static String getNextInputValueFromEndpoint(String workflowId, String componentId, Endpoint endpoint) {
        Input input = InputModel.getInstance().getNextInput(workflowId, componentId, endpoint.getName());
        return getValueFromInput(input);
    }
    
    private static String getValueFromInput(Input input) {
        String inputValue;
        if (input != null && input.getValue() != null) {
            Serializable rawInputValue = input.getValue();
            // user proprietary getName(), because toString() of DataManagementFileReference
            // has too many dependencies to change behavior
            if (rawInputValue instanceof DataManagementFileReference) {
                inputValue = ((DataManagementFileReference) rawInputValue).getName();
            } else {
                inputValue = rawInputValue.toString();                        
            }
        } else {
            inputValue = "N/A";
        }
        return inputValue;
    }
    
    /**
     * @param workflowInformation of affected workflow
     * @param endpoint of affected input
     * @return next input
     */
    public static Input getNextInput(WorkflowInformation workflowInformation, Endpoint endpoint) {
        WorkflowNode workflowNode = endpoint.getWorkflowNode();
        String componentId = workflowInformation.getComponentInstanceDescriptor(workflowNode.getName(),
            workflowNode.getComponentDescription().getIdentifier()).getIdentifier();
        return InputModel.getInstance().getNextInput(workflowInformation.getIdentifier(), componentId, endpoint.getName());
    }
}
