/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.notification;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;

/**
 * Default implementation of {@link NotificationSubscriber}. Use this as a base class for
 * {@link NotificationSubscriber}s, unless there is a specific reason to implement the interface
 * directly.
 * 
 * @author Robert Mischke
 */
public abstract class DefaultNotificationSubscriber implements NotificationSubscriber {

    private static final long serialVersionUID = -3772887574186333020L;

    @Override
    @AllowRemoteAccess
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

}
