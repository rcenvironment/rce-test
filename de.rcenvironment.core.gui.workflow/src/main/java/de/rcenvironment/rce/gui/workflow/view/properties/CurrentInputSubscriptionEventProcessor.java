/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.gui.workflow.SubscriptionEventProcessor;
import de.rcenvironment.rce.notification.Notification;

/**
 * Subscriber for all input notifications in the overall system.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class CurrentInputSubscriptionEventProcessor extends SubscriptionEventProcessor {

    private static final long serialVersionUID = 2685613452747737482L;

    private final transient InputModel inputModel;

    public CurrentInputSubscriptionEventProcessor(InputModel consoleModel) {
        super();
        this.inputModel = consoleModel;
    }

    /**
     * Process all collected {@link Input} or {@link InputIndex} updates and perform a single GUI update to improve
     * performance.
     */
    @Override
    protected synchronized void processNotifications() {
        List<Notification> readOnlyList = null;
        synchronized (notificationsToProcess) {
            if (!notificationsToProcess.isEmpty()) {
                // create read-only copy of pending notifications list
                readOnlyList = new ArrayList<Notification>(notificationsToProcess);
                notificationsToProcess.clear();
            }
        }
        
        // process the list outside the synchronization block
        List<Input> inputs = new ArrayList<Input>();
        if (readOnlyList != null) {
            for (Notification notification : readOnlyList) {
                Serializable body = notification.getBody();
                if (body instanceof Input) {
                    if (notification.getHeader().getNotificationIdentifier()
                            .startsWith(ComponentConstants.CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID)) {
                        inputModel.setCurrentInput((Input) body);
                    } else {
                        inputs.add((Input) body);
                    }
                }
            }
            inputModel.addInputs(inputs);
        }
    }
}
