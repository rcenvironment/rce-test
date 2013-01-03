/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.internal.WorkflowImpl;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Responsible for recognizing finish state of this {@link Workflow}'s {@link Component}.
 * 
 * @author Doreen Seider
 */
public class ComponentFinishListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 1693105741819705197L;

    private transient WorkflowImpl workflow;

    private transient User user;

    private transient DistributedNotificationService notificationService;

    private transient Set<String> finishedComponents = new HashSet<String>();

    public ComponentFinishListener(WorkflowImpl newWorkflow, User newCert,
        DistributedNotificationService newNotificationService) {
        workflow = newWorkflow;
        user = newCert;
        notificationService = newNotificationService;
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    @Override
    public synchronized void notify(Notification notification) {
        finishedComponents.add((String) notification.getBody());

        // finish workflow if all components are finished
        if (finishedComponents.size() == workflow.getComponentInstanceDescriptors().size()) {

            // internal consistency check
            if (workflow.getState(user) == WorkflowState.FINISHED) {
                LogFactory.getLog(getClass()).warn("All components sent FINISHED, but workflow is already FINISHED?!");
                return;
            }

            workflow.setState(WorkflowState.FINISHED);
            workflow.finished(user);
            for (ComponentInstanceDescriptor compInfo : workflow.getComponentInstanceDescriptors()) {
                notificationService.unsubscribe(ComponentConstants.FINISHED_STATE_NOTIFICATION_ID_PREFIX,
                    this, compInfo.getPlatform());
            }
        }
    }
}
