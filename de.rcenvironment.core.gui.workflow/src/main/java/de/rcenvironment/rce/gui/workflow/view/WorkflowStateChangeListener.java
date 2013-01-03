/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view;

import java.io.Serializable;

import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Class that listens for changes concerning
 * {@link de.rcenvironment.rce.component.workflow.WorkflowInformation}s and updates the
 * {@link WorkflowListView}.
 * 
 * @author Doreen Seider
 */
public class WorkflowStateChangeListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void notify(Notification notification) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {            
                ReadOnlyWorkflowRunEditor view = ReadOnlyWorkflowRunEditor.getInstance();
                if (view != null) {
                    view.updateTitle();
                }
            }
        });
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

}
