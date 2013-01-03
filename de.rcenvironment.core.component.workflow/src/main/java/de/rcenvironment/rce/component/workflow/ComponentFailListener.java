/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.workflow.internal.WorkflowImpl;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * Responsible for recognizing failed state of this {@link Workflow}'s {@link Component}.
 * 
 * @author Doreen Seider
 */
public class ComponentFailListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 1693105741819705197L;
    
    private static final Log LOGGER = LogFactory.getLog(ComponentFailListener.class);
    
    private transient WorkflowImpl workflow;
    
    private transient User user;
    
    public ComponentFailListener(WorkflowImpl newWorkflow, User newUser) {
        workflow = newWorkflow;
        user = newUser;
    }
    
    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }
    
    @Override
    public synchronized void notify(Notification notification) {
        WorkflowState wfState = workflow.getState(user);
        if (wfState == WorkflowState.READY
            || wfState == WorkflowState.RUNNING
            || wfState == WorkflowState.PAUSED) {
            LOGGER.error("At least one component run failed. Workflow will be canceled.");
            workflow.cancel(user);
        }
        workflow.setState(WorkflowState.FAILED);
    }
    
}

