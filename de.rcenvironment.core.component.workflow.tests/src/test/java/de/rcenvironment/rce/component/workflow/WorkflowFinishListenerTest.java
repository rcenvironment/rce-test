/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.internal.WorkflowImpl;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * Test cases for {@link ComponentFinishListener}.
 *
 * @author Doreen Seider
 */
public class WorkflowFinishListenerTest {

    private WorkflowImpl workflow;
    
    private User cert;
    
    private DistributedNotificationService service;
    
    /** Set up. */
    @Before
    public void setUp() {
        workflow = EasyMock.createNiceMock(WorkflowImpl.class);
        @SuppressWarnings("serial")
        Set<ComponentInstanceDescriptor> descs = new HashSet<ComponentInstanceDescriptor>() {
            {
                add(EasyMock.createNiceMock(ComponentInstanceDescriptor.class));
            }
        };
        EasyMock.expect(workflow.getComponentInstanceDescriptors()).andReturn(descs).anyTimes();
        EasyMock.replay(workflow);
        
        cert = EasyMock.createNiceMock(User.class);
        
        service = EasyMock.createNiceMock(DistributedNotificationService.class);
    }
    
    /** Test. */
    @Test
    public void testNotify() {
        
        ComponentFinishListener listener = new ComponentFinishListener(workflow, cert, service);
        
        Notification notification = new Notification("abc", 0, PlatformIdentifierFactory.fromHostAndNumberString("heinz:7"), "xyz");
        listener.notify(notification);
    }
    
    /** Test. */
    @Test
    public void testFetInterface() {
        
        ComponentFinishListener listener = new ComponentFinishListener(workflow, cert, service);
        assertEquals(NotificationSubscriber.class, listener.getInterface());
    }
}
