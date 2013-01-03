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
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.view.properties.InputModel;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.SimpleNotificationService;


/**
 * Listens for specified workflow getting in paused state.
 *
 * @author Doreen Seider
 */
public class WorkflowPausedListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = -6095141356825812640L;

    private transient WorkflowInformation workflowInformation;
    
    public WorkflowPausedListener(WorkflowInformation workflowInformation) {
        this.workflowInformation = workflowInformation;
    }
    
    @Override
    public void notify(Notification notification) {
        if (notification.getBody() instanceof String && ((String) notification.getBody()).equals(WorkflowState.PAUSED.name())) {
            SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
            InputModel model = InputModel.getInstance();
            model.getEventProcessor().flush();
            for (ComponentInstanceDescriptor cid : swr.getComponentInstanceDescriptors(workflowInformation)) {
                String workflowId = workflowInformation.getIdentifier();
                String componentId = cid.getIdentifier();
                model.setCurrentInputNumbers(workflowId, componentId, swr.getCurrentInputNumbers(workflowInformation, componentId));
                model.setRawInputs(workflowId, componentId, swr.getInputs(workflowInformation, componentId));
            }
            new SimpleNotificationService().unsubscribe(WorkflowConstants.STATE_NOTIFICATION_ID + workflowInformation.getIdentifier(),
                this,
                workflowInformation.getControllerPlatform());
        }        
    }

    @Override
    public Class<?> getInterface() {
        return NotificationSubscriber.class;
    }

}
