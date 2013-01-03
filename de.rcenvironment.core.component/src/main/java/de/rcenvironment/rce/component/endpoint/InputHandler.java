/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;

import de.rcenvironment.rce.component.ComponentController;
import de.rcenvironment.rce.component.internal.ComponentControllerImpl;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Waits for {@link Input}s and is responsible for sorting and announcing new {@link Input}s to
 * corresponding {@link ComponentController}.
 * 
 * @author Doreen Seider
 */
public class InputHandler extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = -3278696240489595361L;
    
    private transient ComponentController componentController;

    private transient String inputName;

    private transient Class<? extends Serializable> inputType;
    
    private transient String workflowIdentifier;
    
    private transient String componentIdentifier;
    
    private transient int counter;
    
    public InputHandler(ComponentController newComponentContoller,
        String newInputName, Class<? extends Serializable> newInputType,
        String newWorkflowIdentifier, String newComponentIdentifier) {
        componentController = newComponentContoller;
        inputName = newInputName;
        inputType = newInputType;
        workflowIdentifier = newWorkflowIdentifier;
        componentIdentifier = newComponentIdentifier;
        counter = 0;
    }
    
    @Override
    public synchronized void notify(Notification notification) {
        Input input = new Input(inputName, inputType, notification.getBody(),
            workflowIdentifier, componentIdentifier, counter++);
        ((ComponentControllerImpl) componentController).newInput(input);
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }
    
}
