/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.optimizer.commons;

import java.util.List;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Implementation of {@link OptimizerReceiver}.
 * @author Christian Weiss.
 */
public final class OptimizerReceiverImpl implements OptimizerReceiver {

    private static final long serialVersionUID = -6079120096252508794L;

    private static final int MINUS_ONE = -1;

    private final ResultSet study;

    private final PlatformIdentifier platform;

    private NotificationSubscriber notificationSubscriber;
    
    private DistributedNotificationService notificationService;

    public OptimizerReceiverImpl(final ResultSet study, final PlatformIdentifier platform,
        DistributedNotificationService notificationService) {
        this.study = study;
        this.platform = platform;
        this.notificationService = notificationService;
    }
    
    @Override
    public ResultSet getStudy() {
        return study;
    }

    @Override
    public void setNotificationSubscriber(final NotificationSubscriber notificationSubscriber) {
        this.notificationSubscriber = notificationSubscriber;
    }

    @Override
    public void initialize() {
        final String notificationId = OptimizerUtils.createDataIdentifier(study);
        Long missedNumber = notificationService.subscribe(
                notificationId, notificationSubscriber, platform).get(
                notificationId);
        // process missed notifications
        if (missedNumber > MINUS_ONE) {
            if (missedNumber > OptimizerPublisher.BUFFER_SIZE - 1) {
                missedNumber = new Long(OptimizerPublisher.BUFFER_SIZE - 1);
            }
            final List<Notification> missedNotifications = notificationService
                    .getNotifications(notificationId, platform)
                    .get(notificationId)
                    .subList(0, missedNumber.intValue() + 1);
            for (final Notification missedNotification : missedNotifications) {
                notificationSubscriber.notify(missedNotification);
            }
        }
    }

}

