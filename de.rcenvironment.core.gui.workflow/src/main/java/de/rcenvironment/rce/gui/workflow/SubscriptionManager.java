/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * Handles the connection to the subscription service and the retrieval of "missed" notifications.
 * 
 * @author Robert Mischke, based on code by Doreen Seider
 */
public class SubscriptionManager {

    private SubscriptionEventProcessor eventProcessor;
    
    private Set<PlatformIdentifier> platformsSubscribedTo = null;

    /**
     * Default constructor.
     * 
     * @param model the {@link WorkflowStateModel} to apply received events on
     */
    public SubscriptionManager(SubscriptionEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Subscribes to the relevant notification id and catches up with previous updates.
     * @param notificationIds identifiers of notifications to subscribe
     * Note: This method should not be called more than once. For refresh call updateSubscriptions(String[] notificationIds).
     */
    public synchronized void initialize(String[] notificationIds) {
        SimpleNotificationService sns = new SimpleNotificationService();
        for (String notificationId : notificationIds) {
            Map<PlatformIdentifier, Map<String, Long>> lastMissedNumbers = sns.subscribeToAllPlatforms(notificationId, eventProcessor);
            platformsSubscribedTo = lastMissedNumbers.keySet();
            retrieveMissedNotifications(sns, lastMissedNumbers);
        }        
        
    }
    
    /**
     * Subscribes to the relevant notification id and catches up with previous updates. It only
     * considers "new" platforms, which where not known during initialize.
     * @param notificationIds identifiers of notifications to subscribe
     */
    public synchronized void updateSubscriptions(String[] notificationIds) {
        SimpleNotificationService sns = new SimpleNotificationService();
        if (platformsSubscribedTo == null) {
            initialize(notificationIds);
        } else {
            SimpleCommunicationService scs = new SimpleCommunicationService();
            Set<PlatformIdentifier> allPlatforms = scs.getAvailableNodes();
            for (PlatformIdentifier platform : allPlatforms) {
                if (!platformsSubscribedTo.contains(platform)) {
                    for (String notificationId : notificationIds) {
                        Map<PlatformIdentifier, Map<String, Long>> lastMissedNumbers = new HashMap<PlatformIdentifier, Map<String, Long>>();
                        lastMissedNumbers.put(platform, sns.subscribe(notificationId, eventProcessor, platform));
                        retrieveMissedNotifications(sns, lastMissedNumbers);    
                    }
                }
            }
            platformsSubscribedTo = allPlatforms;
        }
    }

    private void retrieveMissedNotifications(SimpleNotificationService sns,
        Map<PlatformIdentifier, Map<String, Long>> lastMissedNumbers) {

        for (PlatformIdentifier pi : lastMissedNumbers.keySet()) {
            for (String notifId : lastMissedNumbers.get(pi).keySet()) {
                Long lastMissedNumber = lastMissedNumbers.get(pi).get(notifId);
                if (lastMissedNumber != NotificationService.NO_MISSED) {
                    eventProcessor.addNotificationToHandle(notifId, lastMissedNumber);
                    for (List<Notification> notifications : sns.getNotifications(notifId, pi).values()) {
                        Iterator<Notification> notificationIterator = notifications.iterator();
                        while (notificationIterator.hasNext()) {
                            eventProcessor.notify(notificationIterator.next());
                        }
                    }
                }
            }
        }

    }

}
