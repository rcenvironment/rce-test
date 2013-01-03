/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Subscriber for all console notifications in the overall system.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public abstract class SubscriptionEventProcessor extends DefaultNotificationSubscriber {

    private static final int UPDATE_TIMER_TICK_INTERVAL = 500;

    private static final long serialVersionUID = 3619909997095130853L;

    protected transient List<Notification> notificationsToProcess = new LinkedList<Notification>();

    private transient Map<String, Long> lastMissedNotifications = new HashMap<String, Long>();

    private transient Map<String, Boolean> catchingUpWithMissedNotifications = new HashMap<String, Boolean>();

    private transient Map<String, Deque<Notification>> queuedNotifications = new HashMap<String, Deque<Notification>>();

    private transient SerializableTimer processNotificationTimer;

    public SubscriptionEventProcessor() {
        // set up the batch processing timer
        processNotificationTimer = new SerializableTimer(true);
        processNotificationTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                processNotifications();
            }
        }, UPDATE_TIMER_TICK_INTERVAL, UPDATE_TIMER_TICK_INTERVAL);
    }

    @Override
    public void notify(Notification notification) {
        String notifId = notification.getHeader().getNotificationIdentifier();
        if (catchingUpWithMissedNotifications.containsKey(notifId)
            && catchingUpWithMissedNotifications.get(notifId)
            && lastMissedNotifications.get(notifId) == NotificationService.NO_MISSED) {
            queuedNotifications.get(notifId).add(notification);
        } else if (catchingUpWithMissedNotifications.containsKey(notifId)
            && catchingUpWithMissedNotifications.get(notifId)
            && notification.getHeader().getNumber() > lastMissedNotifications.get(notifId)) {
            queuedNotifications.get(notifId).add(notification);
        } else {
            handleIncomingNotification(notification);
            if (catchingUpWithMissedNotifications.containsKey(notifId)
                && catchingUpWithMissedNotifications.get(notifId)
                && notification.getHeader().getNumber() == lastMissedNotifications.get(notifId)) {

                while (!queuedNotifications.get(notifId).isEmpty()) {
                    notify(queuedNotifications.get(notifId).getFirst());
                }
                catchingUpWithMissedNotifications.put(notifId, false);
            }
        }
    }

    private void handleIncomingNotification(Notification notification) {
        synchronized (notificationsToProcess) {
            // enqueue received row for batch processing
            notificationsToProcess.add(notification);
        }
    }

    /**
     * Process all collected {@link ConsoleRow} updates and perform a single GUI update to improve
     * performance.
     */
    protected abstract void processNotifications();

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    /**
     * Registers a notification type for handling past notifications of that type.
     * @param notifId identifier of the notification.
     * @param lastMissedNumber number of last missed notification.
     */
    public void addNotificationToHandle(String notifId, Long lastMissedNumber) {
        queuedNotifications.put(notifId, new LinkedList<Notification>());
        lastMissedNotifications.put(notifId, lastMissedNumber);
        catchingUpWithMissedNotifications.put(notifId, true);
    }
    
    /**
     * Flushes all pending notifications.
     */
    public void flush() {
        processNotifications();
    }

    /**
     * Serializable wrapper class for {@link Timer}.
     * 
     * @author Doreen Seider
     */
    public class SerializableTimer extends Timer implements Serializable {

        private static final long serialVersionUID = 1574565492128021092L;

        public SerializableTimer(boolean flag) {
            super(flag);
        }
    }
}
