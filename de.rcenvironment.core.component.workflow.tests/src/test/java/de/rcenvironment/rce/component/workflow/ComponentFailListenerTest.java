/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.workflow.internal.WorkflowImpl;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * Test cases for {@link ComponentFinishListener}.
 *
 * @author Doreen Seider
 */
public class ComponentFailListenerTest {

    private WorkflowImpl workflow;
    
    private User cert;
    
    /** Set up. */
    @Before
    public void setUp() {
        workflow = EasyMock.createNiceMock(WorkflowImpl.class);    
        EasyMock.replay(workflow);
        cert = EasyMock.createNiceMock(User.class);
    }
    
    /** Test. */
    @Test
    public void testNotify() {
        
        ComponentFailListener listener = new ComponentFailListener(workflow, cert);
        
        Notification notification = new Notification("abc", 0, PlatformIdentifierFactory.fromHostAndNumberString("heinz:7"), "xyz");
        listener.notify(notification);
    }
    
    /** Test. */
    @Test
    public void testFetInterface() {
        
        ComponentFailListener listener = new ComponentFailListener(workflow, cert);
        assertEquals(NotificationSubscriber.class, listener.getInterface());
    }
}
