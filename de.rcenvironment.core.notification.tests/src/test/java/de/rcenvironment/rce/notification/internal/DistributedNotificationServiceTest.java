/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.notification.internal;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationPublisher;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.NotificationTestConstants;

/**
 * Test cases for {@link DistributedNotificationServiceImpl}.
 * 
 * @author Doreen Seider
 */
public class DistributedNotificationServiceTest {

    private DistributedNotificationServiceImpl notificationService;

    private Notification remoteNotification = new Notification("identifier", 0, NotificationTestConstants.REMOTEHOST, new String());

    private Notification anotherRemoteNotification = new Notification("id", 0, NotificationTestConstants.REMOTEHOST, new String());

    private Map<String, List<Notification>> notifications = new HashMap<String, List<Notification>>();
    
    private Map<String, SortedSet<NotificationHeader>> headers = new HashMap<String, SortedSet<NotificationHeader>>();
    
    private Map<String, List<Notification>> remoteNotifications = new HashMap<String, List<Notification>>();;

    private Map<String, SortedSet<NotificationHeader>> remoteHeaders = new HashMap<String, SortedSet<NotificationHeader>>();
    
    private BundleContext context = EasyMock.createNiceMock(BundleContext.class);

    /** Inject the notification service before the test methods run. */
    @Before
    public void initialize() {
        notificationService = new DistributedNotificationServiceImpl();
        notificationService.bindNotificationService(new DummyLocalNotificationService());
        notificationService.bindCommunicationService(new DummyCommunicationService());
        notificationService.activate(context);
    }
    
    /** Test. */
    @Test
    public void testRemovePublisher() {
        try {
            notificationService.removePublisher(NotificationTestConstants.NOTIFICATION_ID);
        } catch (RuntimeException e) {
            assertEquals("deregistered", e.getMessage());
        }
    }

    /** Test. */
    @Test
    public void testSetBufferSize() {

        try {
            notificationService.setBufferSize(NotificationTestConstants.NOTIFICATION_ID, 0);
        } catch (RuntimeException e) {
            assertEquals("registered with buffer", e.getMessage());
        }
    }

    /** Test. */
    @Test
    public void testGetNotification() {
        assertEquals(NotificationTestConstants.NOTIFICATION,
            notificationService.getNotification(NotificationTestConstants.NOTIFICATION_HEADER));
        
        assertEquals(remoteNotification,
            notificationService.getNotification(remoteNotification.getHeader()));
        
        notificationService.getNotification(anotherRemoteNotification.getHeader());
    }

    /** Test. */
    @Test
    public void testGetNotificationHeaders() {
        assertEquals(headers, notificationService.getNotificationHeaders(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.LOCALHOST));
        
        assertEquals(remoteHeaders,
            notificationService.getNotificationHeaders(remoteNotification.getHeader().getNotificationIdentifier(),
                NotificationTestConstants.REMOTEHOST));
        
        notificationService.getNotificationHeaders(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.REMOTEHOST);
    }

    /** Test. */
    @Test
    public void testGetNotifications() {
        assertEquals(notifications, notificationService.getNotifications(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.LOCALHOST));
        
        assertEquals(remoteNotifications,
            notificationService.getNotifications(remoteNotification.getHeader().getNotificationIdentifier(),
                NotificationTestConstants.REMOTEHOST));
        
        notificationService.getNotifications(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.REMOTEHOST);
    }

    /** Test. */
    @Test
    public void testSend() {
        try {
            notificationService.send(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.NOTIFICATION.getBody());
        } catch (RuntimeException e) {
            assertEquals("sent", e.getMessage());
        }
    }

    /** Test. */
    @Test
    public void testSubscribe() {
        try {
            notificationService.subscribe(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.NOTIFICATION_SUBSCRIBER,
                NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("subscribed", e.getMessage());            
        }
    }

    /** Test. */
    @Test
    public void testUnsubscribe() {
        try {
            notificationService.unsubscribe(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.NOTIFICATION_SUBSCRIBER,
                NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("unsubscribed", e.getMessage());
        }
    }
    
    /** Test. */
    @Test
    public void testTrigger() {
        try {
            notificationService.trigger(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("triggered", e.getMessage());
        }
        
        notificationService.trigger(remoteNotification.getHeader().getNotificationIdentifier(), NotificationTestConstants.REMOTEHOST);
        
        notificationService.trigger(NotificationTestConstants.NOTIFICATION_ID, NotificationTestConstants.REMOTEHOST);
    }

    /**
     * Test {@link NotificationService}.
     * @author Doreen Seider
     */
    class DummyLocalNotificationService implements NotificationService {

        @Override
        public void removePublisher(String notificationIdentifier) {
            throw new RuntimeException("deregistered");
        }

        @Override
        public Notification getNotification(NotificationHeader header) {
            if (header.equals(NotificationTestConstants.NOTIFICATION_HEADER)) {
                return NotificationTestConstants.NOTIFICATION;
            } else {
                return null;
            }
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationIdentifier) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                return headers;
            } else {
                return null;
            }
        }

        @Override
        public Map<String, List<Notification>> getNotifications(String notificationIdentifier) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                return notifications;
            } else {
                return null;
            }
        }

        @Override
        public void setBufferSize(String notificationIdentifier, int buffer) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID) && buffer == 0) {
                throw new RuntimeException("registered with buffer");
            }
        }

        @Override
        public void send(String notificationIdentifier, Serializable notificationBody) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)
                && notificationBody.equals(NotificationTestConstants.NOTIFICATION.getBody())) {
                throw new RuntimeException("sent");
            }
        }

        @Override
        public Map<String, Long> subscribe(String notificationIdentifier, NotificationSubscriber subscriber) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)
                && subscriber.equals(NotificationTestConstants.NOTIFICATION_SUBSCRIBER)) {
                throw new RuntimeException("subscribed");
            }
            return null;
        }

        @Override
        public void unsubscribe(String notificationIdentifier, NotificationSubscriber subscriber) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)
                && subscriber.equals(NotificationTestConstants.NOTIFICATION_SUBSCRIBER)) {
                throw new RuntimeException("unsubscribed");
            }
        }

        @Override
        public void addPublisher(String notificationIdentifier, NotificationPublisher notificationPublisher) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                throw new RuntimeException("added");
            }
        }

        @Override
        public void trigger(String notificationIdentifier) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                throw new RuntimeException("triggered");
            }
        }

    }

    /**
     * Test {@link NotificationService} implementation.
     * @author Doreen Seider
     */
    class DummyRemoteNotificationService implements NotificationService {

        @Override
        public void removePublisher(String notificationIdentifier) {}

        @Override
        public Notification getNotification(NotificationHeader header) {
            if (header.equals(remoteNotification.getHeader())) {
                return remoteNotification;
            } else if (header.equals(anotherRemoteNotification.getHeader())) {
                throw new UndeclaredThrowableException(new RuntimeException());
            } else {
                return null;
            }
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationIdentifier) {
            if (notificationIdentifier.equals(remoteNotification.getHeader().getNotificationIdentifier())) {
                return remoteHeaders;
            } else if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                throw new UndeclaredThrowableException(new RuntimeException());
            } else {
                return null;
            }
        }

        @Override
        public Map<String, List<Notification>> getNotifications(String notificationIdentifier) {
            if (notificationIdentifier.equals(remoteNotification.getHeader().getNotificationIdentifier())) {
                return remoteNotifications;
            } else if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                throw new UndeclaredThrowableException(new RuntimeException());
            } else {
                return null;
            }
        }

        @Override
        public void setBufferSize(String notificationIdentifier, int buffer) {}

        @Override
        public void send(String notificationIdentifier, Serializable notificationBody) {}

        @Override
        public Map<String, Long> subscribe(String notificationIdentifier, NotificationSubscriber subscriber) {
            Map<String, Long> numbers = new HashMap<String, Long>();
            numbers.put(notificationIdentifier, new Long(5));
            return numbers;
        }

        @Override
        public void unsubscribe(String notificationIdentifier, NotificationSubscriber subscriber) {}

        @Override
        public void addPublisher(String notificationIdentifier, NotificationPublisher notificationPublisher) {}

        @Override
        public void trigger(String notificationIdentifier) {
            if (!notificationIdentifier.equals(remoteNotification.getHeader().getNotificationIdentifier())) {
                throw new UndeclaredThrowableException(new RuntimeException());
            }
        }
    }

    /**
     * Test {@link CommunicationService} implementation.
     * @author Doreen Seider
     */
    class DummyCommunicationService extends MockCommunicationService {
        
        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            
            if (iface == NotificationService.class
                && platformIdentifier.equals(NotificationTestConstants.LOCALHOST)
                && context == bundleContext) {
                return new DummyLocalNotificationService();
            } else if (iface == NotificationService.class
                && platformIdentifier.equals(NotificationTestConstants.REMOTEHOST)
                && context == bundleContext) {
                return new DummyRemoteNotificationService();
            } else {
                return null;
            }
        }

    }

}
