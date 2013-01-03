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
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationSubscriber;


/**
 * Test cases for {@link Output}.
 *
 * @author Doreen Seider
 */
public class OutputTest {

    private String outputName = "raus hier";
    
    private Class<? extends Serializable> outputType = Long.class;
    
    private Long outputValue = new Long(5);
    
    private Output output;
    
    /** Set up. */
    @Before
    public void setUp() {
        ComponentInstanceDescriptor compInstanceDesc = MockComponentStuffFactory.createComponentInstanceDescriptor();
        OutputDescriptor outputDesc = new OutputDescriptor(compInstanceDesc, outputName);
        
        DistributedNotificationService notificationService = new DummyNotificationService();
        
        output = new Output(outputDesc, outputType, notificationService);
    }
    
    /** Test. */
    @Test
    public void testGetter() {
        
        assertEquals(outputType, output.getType());
        assertEquals(outputName, output.getName());
        assertEquals(MockComponentStuffFactory.COMPONENT_PLATFORM, output.getPlatform());
        assertEquals(MockComponentStuffFactory.COMPONENT_INSTANCE_IDENTIFIER, output.getComponentInstanceIdentifier());
    }
    
    /** Test. */
    @Test
    public void testWrite() {
        output.write(outputValue);
    }
    
    /**
     * Dummy implementation of {@link DistributedNotificationService}.
     *
     * @author Doreen Seider
     */
    public class DummyNotificationService implements DistributedNotificationService {

        @Override
        public void setBufferSize(String notificationIdentifier, int bufferSize) {
        }

        @Override
        public void removePublisher(String notificationIdentifier) {
        }

        @Override
        public <T extends Serializable> void send(String notificationId, T notificationBody) {
            if (!notificationBody.equals(outputValue)) {
                throw new RuntimeException();
            }
        }

        @Override
        public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publisherPlatform) {
            return null;
        }

        @Override
        public void unsubscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publishPlatform) {
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationId,
            PlatformIdentifier publishPlatform) {
            return null;
        }

        @Override
        public Map<String, List<Notification>> getNotifications(String notificationId, PlatformIdentifier publishPlatform) {
            return null;
        }

        @Override
        public Notification getNotification(NotificationHeader header) {
            return null;
        }

        @Override
        public void trigger(String notificationId, PlatformIdentifier publishPlatform) {
        }
        
    }
}
