/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.notification.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.BatchAggregator;
import de.rcenvironment.commons.BatchAggregator.BatchProcessor;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationPublisher;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Implementation of the {@link NotificationService}.
 * 
 * @author Andre Nurzenski
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class NotificationServiceImpl implements NotificationService {

    /**
     * Helper class to hold local information about subscribers. This includes a set of the
     * subscribed topics, and a {@link BatchAggregator} to group messages to this subscriber.
     * 
     * @author Robert Mischke
     */
    private static final class LocalSubscriberMetaData {

        private final Set<NotificationTopic> subscribedTopics;

        private final BatchAggregator<Notification> batchAggregator;

        /**
         * @param batchAggregator the aggregator instance to use for this subscriber
         */
        public LocalSubscriberMetaData(BatchAggregator<Notification> batchAggregator) {
            this.batchAggregator = batchAggregator;
            this.subscribedTopics = new HashSet<NotificationTopic>();
        }

        /**
         * Adds a {@link NotificationTopic} that this subscriber has registered for. Used via
         * {@link #getSubscribedTopics()} to unsubscribe from all topics if necessary.
         * 
         * @param topic the already-subscribed topic
         */
        public void addSubscribedTopic(NotificationTopic topic) {
            synchronized (subscribedTopics) {
                subscribedTopics.add(topic);
            }
        }

        /**
         * Adds a {@link NotificationTopic} that this subscriber is no longer registered for.
         * 
         * @param topic the topic to disconnect from this subscriber
         */
        public boolean removeSubscribedTopic(NotificationTopic topic) {
            synchronized (subscribedTopics) {
                return subscribedTopics.remove(topic);
            }
        }

        public Collection<NotificationTopic> getSubscribedTopics() {
            synchronized (subscribedTopics) {
                // copy to immutable collection to prevent concurrent modifications
                return new ArrayList<NotificationTopic>(subscribedTopics);
            }
        }

        public BatchAggregator<Notification> getBatchAggregator() {
            return batchAggregator;
        }

    }

    /**
     * A {@link BatchProcessor} implementation that sends out batches of {@link Notification}s to a
     * single {@link NotificationSubscriber}.
     * 
     * @author Robert Mischke
     */
    private final class NotificationBatchSender implements BatchAggregator.BatchProcessor<Notification> {

        private NotificationSubscriber subscriber;

        /**
         * @param subscriber the subscriber to send received batches to
         */
        public NotificationBatchSender(NotificationSubscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void processBatch(List<Notification> batch) {
            sendNotificationsToSubscriber(subscriber, batch);
        }
    }

    // the maximum number of notifications to aggregate to a single batch
    // NOTE: arbitrary value; adjust when useful/necessary
    private static final int MAX_NOTIFICATION_BATCH_SIZE = 50;

    // the maximum time a notification may be delayed by batch aggregation
    // NOTE: arbitrary value; adjust when useful/necessary
    private static final long MAX_NOTIFICATION_LATENCY = 200;

    private static final Log LOGGER = LogFactory.getLog(NotificationServiceImpl.class);

    /** Local topics. */
    private Map<String, NotificationTopic> topics = new ConcurrentHashMap<String, NotificationTopic>();

    /** Registered publishers. */
    private Map<String, List<de.rcenvironment.rce.notification.NotificationPublisher>> publishers = Collections
        .synchronizedMap(new HashMap<String, List<de.rcenvironment.rce.notification.NotificationPublisher>>());

    /** Current number of all notifications. */
    private Map<String, Long> currentNumbers = new ConcurrentHashMap<String, Long>();

    /** Buffer sizes of all notifications. */
    private Map<String, Integer> bufferSizes = Collections.synchronizedMap(new HashMap<String, Integer>());

    /** Stored notifications. */
    private Map<String, SortedMap<NotificationHeader, Notification>> allNotifications =
        Collections.synchronizedMap(new HashMap<String, SortedMap<NotificationHeader, Notification>>());

    private WeakHashMap<NotificationSubscriber, LocalSubscriberMetaData> subscriberMap =
        new WeakHashMap<NotificationSubscriber, NotificationServiceImpl.LocalSubscriberMetaData>();

    private PlatformService platformService;

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    @Override
    public void setBufferSize(String notificationId, int bufferSize) {
        if (bufferSize != 0) {
            bufferSizes.put(notificationId, new Integer(bufferSize));
            // synchronize explicitly to avoid potential "lost update" problem
            synchronized (allNotifications) {
                if (!allNotifications.containsKey(notificationId)) {
                    allNotifications.put(notificationId, new TreeMap<NotificationHeader, Notification>());
                }
            }
        }
    }

    @Override
    public void addPublisher(String notificationId, NotificationPublisher publisher) {

        if (publishers.get(notificationId) == null) {
            List<de.rcenvironment.rce.notification.NotificationPublisher> pubs =
                new ArrayList<de.rcenvironment.rce.notification.NotificationPublisher>();
            pubs.add(publisher);
            publishers.put(notificationId, pubs);
        } else {
            publishers.get(notificationId).add(publisher);
        }
    }

    @Override
    public void removePublisher(String notificationId) {

        NotificationTopic topic = getNotificationTopic(notificationId);

        if (topic != null) {
            // send a notification about removing the publisher to all subscribers
            send(notificationId, NotificationService.PUBLISHER_DEREGISTERED);

            topics.remove(topic.getName());
            currentNumbers.remove(notificationId);
            bufferSizes.remove(notificationId);
            allNotifications.remove(notificationId);
            publishers.remove(notificationId);
        }
    }

    @Override
    public synchronized <T extends Serializable> void send(String notificationId, T notificationBody) {

        NotificationTopic topic = getNotificationTopic(notificationId);
        if (topic == null) {
            topic = registerNotificationTopic(notificationId);
        }

        Long currentEdition = currentNumbers.get(notificationId);
        Notification notification = new Notification(notificationId, currentEdition.longValue() + 1,
            platformService.getPlatformIdentifier(), notificationBody);

        SortedMap<NotificationHeader, Notification> notifications = allNotifications.get(notificationId);
        if (notifications != null) {
            Integer bufferSize = bufferSizes.get(notificationId);

            synchronized (notifications) {
                if (bufferSize > 0 && notifications.size() >= bufferSize) {
                    if (notifications.remove(notifications.firstKey()) != null) {
                        notifications.put(notification.getHeader(), notification);
                    }
                } else {
                    notifications.put(notification.getHeader(), notification);
                }
            }
        }

        for (NotificationTopic matchingTopic : getMatchingNotificationTopics(notificationId)) {
            for (NotificationSubscriber subscriber : matchingTopic.getSubscribers()) {
                sendNotificationToSubscriber(notification, subscriber);
            }
        }

        // TODO review: is this guaranteed to be consistent with asynchronous sending? -- misc_ro
        currentNumbers.put(notificationId, notification.getHeader().getNumber());
    }

    private void sendNotificationToSubscriber(Notification notification, NotificationSubscriber subscriber) {
        getLocalSubscriberMetaData(subscriber).getBatchAggregator().enqueue(notification);
    }

    @Override
    @AllowRemoteAccess
    public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber) {

        Map<String, Long> lastNumbers = new HashMap<String, Long>();

        NotificationTopic topic = getNotificationTopic(notificationId);
        if (topic == null) {
            topic = registerNotificationTopic(notificationId);
        }

        topic.add(subscriber);
        getLocalSubscriberMetaData(subscriber).addSubscribedTopic(topic);

        synchronized (currentNumbers) {
            for (String tmpId : currentNumbers.keySet()) {
                if (tmpId.matches(notificationId)) {
                    lastNumbers.put(tmpId, currentNumbers.get(tmpId));
                }
            }
        }

        return lastNumbers;
    }

    @Override
    @AllowRemoteAccess
    public void unsubscribe(String notificationId, NotificationSubscriber subscriber) {

        NotificationTopic topic = getNotificationTopic(notificationId);

        if (topic != null) {
            topic.remove(subscriber);
            getLocalSubscriberMetaData(subscriber).removeSubscribedTopic(topic);
        }
    }

    @Override
    public Notification getNotification(NotificationHeader header) {

        Notification notification = null;
        Map<NotificationHeader, Notification> notifications = allNotifications.get(header.getNotificationIdentifier());
        if (notifications != null) {
            notification = notifications.get(header);
        }
        return notification;
    }

    @Override
    public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationId) {

        Map<String, SortedSet<NotificationHeader>> allHeaders = new HashMap<String, SortedSet<NotificationHeader>>();

        // note: access to iterators of synchronized maps must be synchronized explicitly
        synchronized (allNotifications) {
            // TODO iterating over map entries would probably be more efficient
            for (String tmpId : allNotifications.keySet()) {
                if (tmpId.matches(notificationId)) {
                    Map<NotificationHeader, Notification> notifications = allNotifications.get(tmpId);
                    SortedSet<NotificationHeader> headers = new TreeSet<NotificationHeader>(notifications.keySet());
                    allHeaders.put(tmpId, headers);
                }
            }
        }

        return allHeaders;
    }

    @Override
    @AllowRemoteAccess
    public Map<String, List<Notification>> getNotifications(String notificationId) {

        Map<String, List<Notification>> allNotificationsToGet = new HashMap<String, List<Notification>>();

        // note: access to iterators of synchronized maps must be synchronized explicitly
        synchronized (allNotifications) {
            // TODO iterating over map entries would probably be more efficient
            for (String tmpId : allNotifications.keySet()) {
                if (tmpId.matches(notificationId)) {
                    Map<NotificationHeader, Notification> notifications = allNotifications.get(tmpId);
                    List<Notification> notificationsToGet = new ArrayList<Notification>(notifications.values());
                    allNotificationsToGet.put(tmpId, notificationsToGet);
                }
            }
        }

        return allNotificationsToGet;
    }

    @Override
    public void trigger(String notificationId) {

        if (publishers.get(notificationId) != null) {
            for (NotificationPublisher publisher : publishers.get(notificationId)) {
                publisher.trigger(notificationId);
            }
        }
    }

    /**
     * Sends a single {@link Notification} to a {@link NotificationSubscriber}.
     * 
     * @param subscriber the subscriber to send the notification to
     * @param matchingTopic the matching topic that caused the subscriber to receive this
     *        notification
     * @param notifications the notifications, ie the actual content
     */
    private void sendNotificationsToSubscriber(NotificationSubscriber subscriber, List<Notification> notifications) {
        try {
            subscriber.processNotifications(notifications);
        } catch (RuntimeException e) {
            LOGGER.warn("Removing event listener of type " + subscriber.getInterface().getSimpleName()
                + " because contacting it caused an error", e);
            for (NotificationTopic topic : getLocalSubscriberMetaData(subscriber).getSubscribedTopics()) {
                unsubscribe(topic.getName(), subscriber);
                LOGGER.debug("Unsubscribed local subscriber for interface " + subscriber.getInterface() + " from topic " + topic.getName()
                    + " after notification failure");
            }
        }
    }

    private NotificationTopic registerNotificationTopic(String notificationId) {

        NotificationTopic topic = new NotificationTopic(notificationId);
        topics.put(topic.getName(), topic);
        currentNumbers.put(notificationId, new Long(NO_MISSED));
        return topic;
    }

    private NotificationTopic getNotificationTopic(String notificationId) {

        synchronized (topics) {
            NotificationTopic topic = null;
            for (String topicName : topics.keySet()) {
                if (topicName.equals(notificationId)) {
                    topic = topics.get(topicName);
                    break;
                }
            }
            return topic;
        }

    }

    private LocalSubscriberMetaData getLocalSubscriberMetaData(NotificationSubscriber subscriber) {
        synchronized (subscriberMap) {
            LocalSubscriberMetaData metaData = subscriberMap.get(subscriber);
            if (metaData == null) {
                final BatchAggregator.BatchProcessor<Notification> batchProcessor = new NotificationBatchSender(subscriber);
                final BatchAggregator<Notification> batchAggregator =
                    new BatchAggregator<Notification>(MAX_NOTIFICATION_BATCH_SIZE, MAX_NOTIFICATION_LATENCY,
                        batchProcessor);
                metaData = new LocalSubscriberMetaData(batchAggregator);
                subscriberMap.put(subscriber, metaData);
            }
            return metaData;
        }
    }

    private Set<NotificationTopic> getMatchingNotificationTopics(String currentNotificationId) {

        Set<NotificationTopic> matchingTopics = new HashSet<NotificationTopic>();
        synchronized (topics) {
            for (NotificationTopic topic : topics.values()) {
                if (topic.getNotificationIdFilter().matcher(currentNotificationId).matches()) {
                    matchingTopics.add(topic);
                }
            }
        }
        return matchingTopics;
    }

}
