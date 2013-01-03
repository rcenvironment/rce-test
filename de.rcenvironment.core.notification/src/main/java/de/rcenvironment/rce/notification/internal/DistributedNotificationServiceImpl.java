/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.notification.internal;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationService;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Implementation of {@link DistributedNotificationService}.
 * 
 * @author Doreen Seider
 * 
 */
// FIXME clarify behavior on failure: return null, empty collections or throw exceptions? -- misc_ro
// (see related Mantis issue #6542)
public class DistributedNotificationServiceImpl implements DistributedNotificationService {

    private static final Log LOGGER = LogFactory.getLog(DistributedNotificationServiceImpl.class);

    private static NotificationService notificationService;

    private static CommunicationService nullCommunicationService = ServiceUtils.createNullService(CommunicationService.class);

    private static CommunicationService communicationService = nullCommunicationService;

    private static BundleContext context;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindNotificationService(NotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    @Override
    public void setBufferSize(String notificationId, int bufferSize) {
        notificationService.setBufferSize(notificationId, bufferSize);
    }

    @Override
    public void removePublisher(String notificationId) {
        notificationService.removePublisher(notificationId);
    }

    @Override
    public <T extends Serializable> void send(String notificationId, T notificationBody) {
        notificationService.send(notificationId, notificationBody);
    }

    @Override
    public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber,
        PlatformIdentifier publishPlatform) {
        try {
            Pattern.compile(notificationId);
        } catch (RuntimeException e) {
            LOGGER.error("Notification Id is not a valid RegExp: " + notificationId, e);
            throw e;
        }
        try {
            return ((NotificationService) communicationService.getService(NotificationService.class, publishPlatform, context))
                .subscribe(notificationId, subscriber);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to subscribe to remote publisher @{0}: ", publishPlatform), e);
            return new HashMap<String, Long>();
        }
    }

    @Override
    public void unsubscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publishPlatform) {
        try {
            ((NotificationService) communicationService.getService(NotificationService.class, publishPlatform, context))
                .unsubscribe(notificationId, subscriber);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to unsubscribe from remote publisher @{0}: ", publishPlatform), e);
        }
    }

    @Override
    public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationId, PlatformIdentifier publishPlatform) {
        try {
            return ((NotificationService) communicationService.getService(NotificationService.class, publishPlatform, context))
                .getNotificationHeaders(notificationId);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to get remote notification headers @{0}: ", publishPlatform), e);
            return null;
        }
    }

    @Override
    public Map<String, List<Notification>> getNotifications(String notificationId, PlatformIdentifier publishPlatform) {
        try {
            return ((NotificationService) communicationService.getService(NotificationService.class, publishPlatform, context))
                .getNotifications(notificationId);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to get remote notifications @{0}: ", publishPlatform), e);
            return null;
        }
    }

    @Override
    public Notification getNotification(NotificationHeader header) {
        try {
            return ((NotificationService) communicationService
                 .getService(NotificationService.class, header.getPublishPlatform(), context)).getNotification(header);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to get remote notification @{0}: ", header.getPublishPlatform()), e);
            return null;
        }
    }

    @Override
    public void trigger(String notificationId, PlatformIdentifier publishPlatform) {
        try {
            ((NotificationService) communicationService
                 .getService(NotificationService.class, publishPlatform, context)).trigger(notificationId);
        } catch (RuntimeException e) {
            LOGGER.error(MessageFormat.format("Failed to trigger remote publisher @{0}: ", publishPlatform), e);
        }
    }
}
