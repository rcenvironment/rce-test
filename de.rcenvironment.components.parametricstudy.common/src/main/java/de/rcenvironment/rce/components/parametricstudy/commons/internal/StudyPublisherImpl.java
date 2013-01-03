/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.parametricstudy.commons.internal;

import de.rcenvironment.rce.components.parametricstudy.commons.Study;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyDataset;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyPublisher;
import de.rcenvironment.rce.notification.NotificationService;


/**
 * Implementation of {@link StudyPublisher}.
 * @author Christian Weiss
 */
public final class StudyPublisherImpl implements StudyPublisher {

    private static final long serialVersionUID = 6027553291193203997L;

    private final Study study;

    private final String notificationId;
    
    private NotificationService notificationService;

    public StudyPublisherImpl(final Study study, NotificationService notificationService) {
        this.study = study;
        this.notificationService = notificationService;
        notificationId = ParametricStudyUtils.createDataIdentifier(study);
        setBufferSize(BUFFER_SIZE);
    }
    
    @Override
    public Study getStudy() {
        return study;
    }
    
    @Override
    public void setBufferSize(final int bufferSize) {
        notificationService.setBufferSize(notificationId, bufferSize);
    }

    @Override
    public void add(final StudyDataset dataset) {
        notificationService.send(notificationId, dataset);
    }

}

