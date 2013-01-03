/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.components.excel.commons.ChannelValue;
import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.SimpleNotificationService;


/**
 * ModelProvider of channels.
 *
 * @author Markus Kunde
 */
public class ModelProvider extends Observable implements NotificationSubscriber {

    private static final long serialVersionUID = 611752605431248651L;
    
    private List<ChannelValue> channelValues = null;
    
    private boolean areNotificationsMissed = true;
    
    private Long lastMissedNotification = null;
    
    private Deque<Notification> queuedNotifications = new LinkedList<Notification>();
    
    private boolean isSubscribed = false;
    
    
    
    /**
     * Constructor.
     * 
     */
    public ModelProvider() {
        channelValues = new ArrayList<ChannelValue>();
    }
    
    /**
     * Subscribing model to notifications at all platforms.
     * 
     * @param componentIdentifier identifier of specific component
     */
    public void subscribeToAllPlatForms(final String componentIdentifier) {
        if (!isSubscribed) {
            SimpleNotificationService sns = new SimpleNotificationService();
            Map<PlatformIdentifier, Map<String, Long>> lastMissedNumbers =
                      sns.subscribeToAllPlatforms(componentIdentifier + ExcelComponentConstants.NOTIFICATION_SUFFIX, this);
            retrieveMissedNotifications(sns, lastMissedNumbers);
            isSubscribed = true;
        }
    }
    
    /** 
     * Returns all ChannelValues.
     * 
     * @return list of ChannelValues
     */
    public List<ChannelValue> getChannelValues() {
        return channelValues;
    }  
    
    /**
     * Method to add channelvalues to model.
     * 
     * @param channelVals list of channelvalues to add to model
     */
    public void addNewChannelValues(final List<ChannelValue> channelVals) {
        channelValues.addAll(channelVals);
        setChanged();
        notifyObservers();
    }
    
    /**
     * Method to add channelvalues to model.
     * 
     * @param cval channelvalue to add to model
     */
    public void addNewChannelValue(final ChannelValue cval) {
        channelValues.add(cval);
        setChanged();
        notifyObservers();
    }

    // NOTE: copied from DefaultNotificationSubscriber; rework to subclass instead?
    @Override
    public void processNotifications(List<Notification> notifications) {
        // catch all RTEs here so only transport errors can reach the remote caller
        try {
            for (Notification notification : notifications) {
                notify(notification);
            }
        } catch (RuntimeException e) {
            // Note: acquiring the logger dynamically as it will be used very rarely
            LogFactory.getLog(getClass()).error("Error in notification handler", e);
        }
    }

    @Override
    public void notify(Notification notification) {
        if (areNotificationsMissed && lastMissedNotification == NotificationService.NO_MISSED) {
            queuedNotifications.add(notification);
        } else if (areNotificationsMissed && notification.getHeader().getNumber() > lastMissedNotification) {
            queuedNotifications.add(notification);
        } else {
            final Object body = notification.getBody();
            if (body instanceof ChannelValue) {
                ChannelValue val = (ChannelValue) notification.getBody();
                channelValues.add(val);
                setChanged();
                notifyObservers();
            }
            if (areNotificationsMissed && notification.getHeader().getNumber() == lastMissedNotification) {

                while (!queuedNotifications.isEmpty()) {
                    notify(queuedNotifications.getFirst());
                }
                areNotificationsMissed = false;
            }
        }
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }
    
    public void setLastMissedNotification(Long number) {
        lastMissedNotification = number;
    }

    public void setNotificationsMissed(boolean missed) {
        areNotificationsMissed = missed;
    }
    
    private void retrieveMissedNotifications(SimpleNotificationService sns,
        Map<PlatformIdentifier, Map<String, Long>> lastMissedNumbers) {
        
        for (PlatformIdentifier pi : lastMissedNumbers.keySet()) {
            for (String notifId : lastMissedNumbers.get(pi).keySet()) {
                Long lastMissedNumber = lastMissedNumbers.get(pi).get(notifId);
                if (lastMissedNumber == NotificationService.NO_MISSED) {
                    setNotificationsMissed(false);
                    setLastMissedNotification(lastMissedNumber);
                } else {
                    setNotificationsMissed(true);
                    setLastMissedNotification(lastMissedNumber);
                }
                for (List<Notification> notifications : sns.getNotifications(notifId, pi).values()) {
                    Iterator<Notification> notificationIterator = notifications.iterator();
                    while (notificationIterator.hasNext()) {
                        notify(notificationIterator.next());
                    }
                }
            }
        }

    }
}
