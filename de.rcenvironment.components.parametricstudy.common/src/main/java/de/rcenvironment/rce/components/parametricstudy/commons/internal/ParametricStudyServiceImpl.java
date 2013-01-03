/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.commons.internal;

import java.io.Serializable;
import java.util.List;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyService;
import de.rcenvironment.rce.components.parametricstudy.commons.Study;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyPublisher;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyReceiver;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyStructure;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationService;

/**
 * Implementation of {@link ParametricStudyService}.
 * @author Christian Weiss
 */
public class ParametricStudyServiceImpl implements ParametricStudyService {

    private NotificationService notificationService;

    private DistributedNotificationService distributedNotificationService;

    protected void bindNotificationService(final NotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void bindDistributedNotificationService(final DistributedNotificationService newDistrNotificationService) {
        distributedNotificationService = newDistrNotificationService;
    }

    @Override
    public StudyPublisher createPublisher(final String identifier,
            final String title, final StudyStructure structure) {
        final Study study = new Study(identifier, title, structure);
        final StudyPublisher studyPublisher = new StudyPublisherImpl(study, notificationService);
        final String notificationId = String.format(ParametricStudyUtils.STRUCTURE_PATTERN, study.getIdentifier());
        notificationService.setBufferSize(notificationId, 1);
        notificationService.send(notificationId, new Serializable[] { study.getStructure(), title});
        return studyPublisher;
    }

    @Override
    public StudyReceiver createReceiver(final String identifier,
            final PlatformIdentifier platform) {
        final String notificationId = String.format(ParametricStudyUtils.STRUCTURE_PATTERN,
                identifier);
        final List<Notification> notifications = distributedNotificationService
                .getNotifications(notificationId, platform).get(notificationId);
        if (notifications.size() > 0) {
            final Notification studyNotification = notifications
                    .get(notifications.size() - 1);
            final Serializable[] notificationContent = (Serializable[]) studyNotification.getBody();
            final StudyStructure structure = (StudyStructure) notificationContent[0];
            final String title = (String) notificationContent[1];
            final Study study = new Study(identifier, title, structure);
            final StudyReceiver studyReceiver = new StudyReceiverImpl(study,
                    platform, distributedNotificationService);
            return studyReceiver;
        }
        return null;
    }

}
