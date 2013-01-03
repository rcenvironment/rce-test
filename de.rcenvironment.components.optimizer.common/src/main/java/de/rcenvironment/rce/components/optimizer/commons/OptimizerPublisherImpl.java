/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.optimizer.commons;

import de.rcenvironment.rce.notification.NotificationService;


/**
 * Implementation of {@link StudyPublisher}.
 * @author Christian Weiss
 */
public final class OptimizerPublisherImpl implements OptimizerPublisher {

    private static final long serialVersionUID = 6027553291193203997L;

    private final ResultSet study;

    private final String notificationId;
    
    private NotificationService notificationService;

    public OptimizerPublisherImpl(final ResultSet study, NotificationService notificationService) {
        this.study = study;
        this.notificationService = notificationService;
        notificationId = OptimizerUtils.createDataIdentifier(study);
        setBufferSize(BUFFER_SIZE);
    }
    
    @Override
    public ResultSet getStudy() {
        return study;
    }
    
    @Override
    public void setBufferSize(final int bufferSize) {
        notificationService.setBufferSize(notificationId, bufferSize);
    }

    @Override
    public void add(final OptimizerResultSet dataset) {
        notificationService.send(notificationId, dataset);
    }

}

