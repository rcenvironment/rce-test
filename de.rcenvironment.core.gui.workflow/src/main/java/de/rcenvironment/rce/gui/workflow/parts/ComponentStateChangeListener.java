/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.parts;

import java.io.Serializable;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * {@link NotificationSubscriber} for {@link ComponentState} changes. 
 *
 * @author Christian Weiss
 */
public class ComponentStateChangeListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = -5025502558454267143L;

    private transient ReadonlyWorkflowNodePart part;
    
    public ComponentStateChangeListener(ReadonlyWorkflowNodePart newPart) {
        part = newPart;
    }
    
    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    @Override
    public void notify(Notification notification) {
        if (notification.getHeader().getNotificationIdentifier().contains(ComponentConstants.STATE_NOTIFICATION_ID_PREFIX)) {
            part.handleStateNotification(notification);            
        } else if (notification.getHeader().getNotificationIdentifier().contains(ComponentConstants.NO_OF_RUNS_NOTIFICATION_ID_PREFIX)) {
            part.handleNoOfRunsNotification(notification);
        }
    }
}
