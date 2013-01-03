/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.properties;

import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Listens to disposed workflows and removes it {@link Input}s from {@link InputModel}.
 * 
 * @author Doreen Seider
 */
public class WorkflowDisposeListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 4881329118422751676L;

    private transient InputModel inputModel;
    
    public WorkflowDisposeListener() {
        inputModel = InputModel.getInstance();
    }
    
    @Override
    public void notify(Notification notification) {
        if (notification.getBody() instanceof String) {
            inputModel.removeInputs((String) notification.getBody());            
        }
    }

    @Override
    public Class<?> getInterface() {
        return NotificationSubscriber.class;
    }

}
