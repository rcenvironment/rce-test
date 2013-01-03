/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.list;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.util.SafeRunnable;

import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Class that listens for changes concerning
 * {@link de.rcenvironment.rce.component.workflow.WorkflowInformation}s and updates the
 * {@link WorkflowListView}.
 * 
 * @author Christian Weiss
 * @author Doreen Seider
 */
public class WorkflowInformationChangeListener extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 1L;

    private static WorkflowInformationChangeListener instance;

    private static final List<Runnable> REACTIONS = new LinkedList<Runnable>();

    public static WorkflowInformationChangeListener getInstance() {
        return instance;
    }

    static {
        instance = new WorkflowInformationChangeListener();
    }

    @Override
    public void notify(Notification notification) {
        String topic = notification.getHeader().getNotificationIdentifier();
        if (topic.startsWith(WorkflowConstants.STATE_NOTIFICATION_ID)) {
            String workflowInformationIdentifier = topic.replace(WorkflowConstants.STATE_NOTIFICATION_ID, "");
            WorkflowState state = WorkflowState.valueOf((String) notification.getBody());
            
            WorkflowStateModel.getInstance().setState(workflowInformationIdentifier, state);
        } 
        
        synchronized (REACTIONS) {
            for (final Runnable runnable : REACTIONS) {
                SafeRunnable safeRunnable = new SafeRunnable() {

                    @Override
                    public void run() throws Exception {
                        runnable.run();
                    }
                };
                SafeRunnable.run(safeRunnable);
            }
        }
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    /**
     * Adds a {@link Runnable} to be executed upon workflow information changes.
     * 
     * @param runnable the reaction implementation
     */
    public static void addReaction(final Runnable runnable) {
        synchronized (REACTIONS) {
            REACTIONS.add(runnable);
        }
    }

    /**
     * 
     * Removes a {@link Runnable} to be executed upon workflow information changes.
     * 
     * @param runnable the reaction implementation
     */
    public static void removeReaction(final Runnable runnable) {
        synchronized (REACTIONS) {
            REACTIONS.remove(runnable);
        }
    }

}
