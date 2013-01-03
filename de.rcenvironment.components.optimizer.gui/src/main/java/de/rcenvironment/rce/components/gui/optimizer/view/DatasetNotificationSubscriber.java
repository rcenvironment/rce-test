/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.gui.optimizer.view;

import java.io.Serializable;


import de.rcenvironment.rce.components.optimizer.commons.OptimizerResultSet;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
/**
 * Used to subscribe to {@link Dataset}s.
 * @author Christian Weiss
 */
public class DatasetNotificationSubscriber extends DefaultNotificationSubscriber {

    private static final long serialVersionUID = 7984538979387371048L;

    private final transient OptimizerDatastore datastore;

    public DatasetNotificationSubscriber(final OptimizerDatastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public Class<? extends Serializable> getInterface() {
        return NotificationSubscriber.class;
    }

    @Override
    public void notify(Notification notification) {
        final OptimizerResultSet dataset = (OptimizerResultSet) notification.getBody();
        datastore.addDataset(dataset);
    }
    
}
