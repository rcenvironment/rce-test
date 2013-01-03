/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.optimizer.commons;

import java.io.Serializable;
import java.util.List;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationService;

/**
 * Implementation of {@link OptimizerResultService}.
 * @author Christian Weiss
 */
public class OptimizerResultServiceImpl implements OptimizerResultService {

    private NotificationService notificationService;

    private DistributedNotificationService distributedNotificationService;

    protected void bindNotificationService(final NotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void bindDistributedNotificationService(final DistributedNotificationService newDistrNotificationService) {
        distributedNotificationService = newDistrNotificationService;
    }

    @Override
    public OptimizerPublisher createPublisher(final String identifier,
            final String title, final ResultStructure structure) {
        final ResultSet study = new ResultSet(identifier, title, structure);
        final OptimizerPublisher studyPublisher = new OptimizerPublisherImpl(study, notificationService);
        final String notificationId = String.format(OptimizerUtils.STRUCTURE_PATTERN, study.getIdentifier());
        notificationService.setBufferSize(notificationId, 1);
        notificationService.send(notificationId, new Serializable[] { study.getStructure(), title});
        return studyPublisher;
    }

    @Override
    public OptimizerReceiver createReceiver(final String identifier,
            final PlatformIdentifier platform) {
        final String notificationId = String.format(OptimizerUtils.STRUCTURE_PATTERN,
                identifier);
        final List<Notification> notifications = distributedNotificationService
                .getNotifications(notificationId, platform).get(notificationId);
        if (notifications.size() > 0) {
            final Notification studyNotification = notifications
                    .get(notifications.size() - 1);
            final Serializable[] notificationContent = (Serializable[]) studyNotification.getBody();
            final ResultStructure structure = (ResultStructure) notificationContent[0];
            final String title = (String) notificationContent[1];
            final ResultSet study = new ResultSet(identifier, title, structure);
            final OptimizerReceiver studyReceiver = new OptimizerReceiverImpl(study,
                    platform, distributedNotificationService);
            return studyReceiver;
        }
        return null;
    }

}
