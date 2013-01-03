/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;

/**
 * Test cases for {@link SimpleNotificationService}.
 * 
 * @author Doreen Seider
 */
public class SimpleNotificationServiceTest {

    private SimpleNotificationService notificationService;

    private Map<String, List<Notification>> notifications = new HashMap<String, List<Notification>>();;

    private Map<String, SortedSet<NotificationHeader>> headers = new HashMap<String, SortedSet<NotificationHeader>>();

    private PlatformIdentifier localPi = PlatformIdentifierFactory.fromHostAndNumberString("pitti:1");

    private PlatformIdentifier remotePi = PlatformIdentifierFactory.fromHostAndNumberString("platsch:1");

    private Map<String, Long> remoteMissedNumber = new HashMap<String, Long>();

    private Map<String, Long> missedNumbers = new HashMap<String, Long>();

    private final String notifId = "notificationId";

    private final String anotherNotifId = "anotherNotificationId";

    private NotificationSubscriber testSubscriber;

    /** Set up. */
    @Before
    public void initialize() {
        notificationService = new SimpleNotificationService();
        remoteMissedNumber.put(anotherNotifId, new Long(7));
        missedNumbers.put(notifId, new Long(8));
        notificationService.bindNotificationService(new DummyNotificationService());
        notificationService.bindDistributedNotificationService(new DummyDistributedNotificationService());
        notificationService.bindPlatformService(new DummyPlatformService());
        notificationService.bindCommunicationService(new DummyCommunicationService());
    }

    /** Test. */
    @Test
    public void testSubscribeToAllPlatforms() {
        testSubscriber = new DefaultNotificationSubscriber() {

            private static final long serialVersionUID = 1L;

            @Override
            public Class<? extends Serializable> getInterface() {
                return null;
            }

            @Override
            public void notify(Notification notification) {

            }
        };
        Map<PlatformIdentifier, Map<String, Long>> missed = notificationService.subscribeToAllPlatforms(notifId, testSubscriber);

        assertEquals(2, missed.size());
        assertTrue(missed.containsKey(localPi));
        assertTrue(missed.get(localPi).containsKey(notifId));
        assertEquals(new Long(8), missed.get(localPi).get(notifId));

        assertTrue(missed.containsKey(remotePi));
        assertTrue(missed.get(remotePi).containsKey(anotherNotifId));
        assertEquals(new Long(7), missed.get(remotePi).get(anotherNotifId));

    }

    /** Test if the methods can be invoked if the notification service is injected. */
    @Test
    public void callNotificationServiceMethods() {
        try {
            notificationService.removePublisher(NotificationTestConstants.NOTIFICATION_ID);
        } catch (RuntimeException e) {
            assertEquals("deregistered", e.getMessage());
        }
        assertEquals(NotificationTestConstants.NOTIFICATION,
            notificationService.getNotification(NotificationTestConstants.NOTIFICATION_HEADER));
        assertEquals(notifications,
            notificationService.getNotifications(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.REMOTEHOST));
        assertEquals(headers,
            notificationService.getNotificationHeaders(NotificationTestConstants.NOTIFICATION_ID,
                NotificationTestConstants.REMOTEHOST));
        try {
            notificationService.setBufferSize(NotificationTestConstants.NOTIFICATION_ID, 0);
        } catch (RuntimeException e) {
            assertEquals("registered with buffer", e.getMessage());
        }

        try {
            notificationService.send(NotificationTestConstants.NOTIFICATION_ID, null);
        } catch (RuntimeException e) {
            assertEquals("sent", e.getMessage());
        }
        try {
            notificationService.subscribe(NotificationTestConstants.NOTIFICATION_ID, null, NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("subscribed", e.getMessage());
        }

        try {
            notificationService.unsubscribe(NotificationTestConstants.NOTIFICATION_ID, null, NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("unsubscribed", e.getMessage());
        }

        try {
            notificationService.addPublisher(NotificationTestConstants.NOTIFICATION_ID, new NotificationPublisher() {

                @Override
                public void trigger(String notificationIdentifier) {}
            });
        } catch (RuntimeException e) {
            assertEquals("added", e.getMessage());
        }

        try {
            notificationService.trigger(NotificationTestConstants.NOTIFICATION_ID, NotificationTestConstants.LOCALHOST);
        } catch (RuntimeException e) {
            assertEquals("triggered", e.getMessage());
        }

    }

    /** Test if the methods can not be invoked if the notification service is not injected. */
    @Test(expected = IllegalStateException.class)
    public void callWithoutNotificationService() {
        notificationService.unbindDistributedNotificationService(new DummyDistributedNotificationService());
        notificationService.removePublisher(NotificationTestConstants.NOTIFICATION_ID);
    }

    /**
     * Test {@link DistributedNotificationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedNotificationService implements DistributedNotificationService {

        @Override
        public void removePublisher(String notificationIdentifier) {
            throw new RuntimeException("deregistered");
        }

        @Override
        public Notification getNotification(NotificationHeader header) {
            return NotificationTestConstants.NOTIFICATION;
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationId,
            PlatformIdentifier publishPlatform) {
            return headers;
        }

        @Override
        public Map<String, List<Notification>> getNotifications(String notificationId, PlatformIdentifier publisherPlatform) {
            return notifications;
        }

        @Override
        public void setBufferSize(String notificationIdentifier, int buffer) {
            throw new RuntimeException("registered with buffer");

        }

        @Override
        public void send(String notificationIdentifier, Serializable notificationBody) {
            throw new RuntimeException("sent");
        }

        @Override
        public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber,
            PlatformIdentifier publishPlatform) {
            if (notificationId.equals(notifId) && publishPlatform != null && publishPlatform.equals(remotePi)) {
                return remoteMissedNumber;
            } else if (notificationId.equals(notifId) && (publishPlatform == null || publishPlatform.equals(localPi))) {
                return missedNumbers;
            }
            throw new RuntimeException("subscribed");
        }

        @Override
        public void unsubscribe(String notificationIdentifier, NotificationSubscriber subscriber, PlatformIdentifier publisherPlatform) {
            throw new RuntimeException("unsubscribed");
        }

        @Override
        public void trigger(String notificationIdentifier, PlatformIdentifier publisherPlatform) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)
                && publisherPlatform.equals(NotificationTestConstants.LOCALHOST)) {
                throw new RuntimeException("triggered");
            }
        }
    }

    /**
     * Test {@link NotificationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyNotificationService implements NotificationService {

        @Override
        public void addPublisher(String notificationIdentifier, NotificationPublisher notificationPublisher) {
            if (notificationIdentifier.equals(NotificationTestConstants.NOTIFICATION_ID)) {
                throw new RuntimeException("added");
            }
        }

        @Override
        public Notification getNotification(NotificationHeader header) {
            return null;
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationIdentifier) {
            return null;
        }

        @Override
        public Map<String, List<Notification>> getNotifications(String notificationIdentifier) {
            return null;
        }

        @Override
        public void removePublisher(String notificationIdentifier) {}

        @Override
        public void send(String notificationIdentifier, Serializable notificationBody) {}

        @Override
        public void setBufferSize(String notificationIdentifier, int bufferSize) {}

        @Override
        public Map<String, Long> subscribe(String notificationIdentifier, NotificationSubscriber subscriber) {
            Map<String, Long> numbers = new HashMap<String, Long>();
            numbers.put(notificationIdentifier, new Long(5));
            return numbers;
        }

        @Override
        public void trigger(String notificationIdentifier) {}

        @Override
        public void unsubscribe(String notificationIdentifier, NotificationSubscriber subscriber) {}

    }

    /**
     * Test implementation for {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return localPi;
        }

        @Override
        public Set<PlatformIdentifier> getRemotePlatforms() {
            Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
            pis.add(remotePi);
            return pis;
        }

    }

    /**
     * Test implementation for {@link CommunicationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

        @Override
        public Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
            if (!forceRefresh) {
                Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
                pis.add(remotePi);
                return pis;
            }
            return null;
        }

    }

}
