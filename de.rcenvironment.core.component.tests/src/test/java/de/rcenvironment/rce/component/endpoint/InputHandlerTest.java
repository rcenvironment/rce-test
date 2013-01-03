/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.internal.ComponentControllerImpl;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * Test cases for {@link InputHandler}.
 *
 * @author Doreen Seider
 */
public class InputHandlerTest {

    private InputHandler inputHandler;
    
    private Serializable value;
    
    private String workflowId;
    
    private String compId;
    
    /** Set up. */
    @Before
    public void setUp() {
        ComponentControllerImpl compController = EasyMock.createNiceMock(ComponentControllerImpl.class);
        final String inputName = "";
        Class<? extends Serializable> inputType = Double.class;
        value = new Double(0);
        workflowId = "aha";
        compId = "nee";
        inputHandler = new InputHandler(compController, inputName, inputType, workflowId, compId);
    }
    
    /** Test. */
    @Test
    public void testNotify() {
        Notification notification = new Notification("flexibel", 8, PlatformIdentifierFactory.fromHostAndNumberString("hmmm:1"), value);
        inputHandler.notify(notification);
    }
    
    /** Test. */
    @Test
    public void testGetInterface() {
        assertEquals(NotificationSubscriber.class, inputHandler.getInterface());
    }
}
