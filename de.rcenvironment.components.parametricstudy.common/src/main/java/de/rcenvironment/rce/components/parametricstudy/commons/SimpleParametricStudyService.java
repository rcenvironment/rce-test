/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.commons;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Simple version of the {@link ParametricStudyService}.
 * @author Christian Weiss
 */
public class SimpleParametricStudyService implements ParametricStudyService {

    private static ParametricStudyService parametricStudyService = ServiceUtils.createNullService(ParametricStudyService.class);

    protected void bindParametricStudyService(final ParametricStudyService newParametricStudyService) {
        SimpleParametricStudyService.parametricStudyService = newParametricStudyService;
    }
    
    protected void unbindParametricStudyService(final ParametricStudyService oldParametricStudyService) {
        SimpleParametricStudyService.parametricStudyService = ServiceUtils.createNullService(ParametricStudyService.class);
    }

    @Override
    public StudyPublisher createPublisher(final String identifier,
            final String title, final StudyStructure structure) {
        return parametricStudyService.createPublisher(identifier, title, structure);
    }

    @Override
    public StudyReceiver createReceiver(final String identifier,
            final PlatformIdentifier platform) {
        return parametricStudyService.createReceiver(identifier, platform);
    }

}
