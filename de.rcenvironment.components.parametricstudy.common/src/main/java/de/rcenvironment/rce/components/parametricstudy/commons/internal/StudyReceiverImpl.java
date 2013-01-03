/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.parametricstudy.commons.internal;

import java.util.List;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.components.parametricstudy.commons.Study;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyPublisher;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyReceiver;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Implementation of {@link StudyReceiver}.
 * @author Christian Weiss.
 */
public final class StudyReceiverImpl implements StudyReceiver {

    private static final long serialVersionUID = -6079120096252508794L;

    private static final int MINUS_ONE = -1;

    private final Study study;

    private final PlatformIdentifier platform;

    private NotificationSubscriber notificationSubscriber;
    
    private DistributedNotificationService notificationService;

    public StudyReceiverImpl(final Study study, final PlatformIdentifier platform,
        DistributedNotificationService notificationService) {
        this.study = study;
        this.platform = platform;
        this.notificationService = notificationService;
    }
    
    @Override
    public Study getStudy() {
        return study;
    }

    @Override
    public void setNotificationSubscriber(final NotificationSubscriber notificationSubscriber) {
        this.notificationSubscriber = notificationSubscriber;
    }

    @Override
    public void initialize() {
        final String notificationId = ParametricStudyUtils.createDataIdentifier(study);
        Long missedNumber = notificationService.subscribe(
                notificationId, notificationSubscriber, platform).get(
                notificationId);
        // process missed notifications
        if (missedNumber > MINUS_ONE) {
            if (missedNumber > StudyPublisher.BUFFER_SIZE - 1) {
                missedNumber = new Long(StudyPublisher.BUFFER_SIZE - 1);
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

