/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.parametricstudy.gui.view;

import java.io.Serializable;

import de.rcenvironment.rce.components.parametricstudy.commons.StudyDataset;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Used to subscribe to {@link Dataset}s.
 * @author Christian Weiss
 */
public class DatasetNotificationSubscriber extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 7984538979387371048L;

    private final transient StudyDatastore datastore;

    public DatasetNotificationSubscriber(final StudyDatastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    @Override
    public void notify(Notification notification) {
        final StudyDataset dataset = (StudyDataset) notification.getBody();
        datastore.addDataset(dataset);
    }
    
}
