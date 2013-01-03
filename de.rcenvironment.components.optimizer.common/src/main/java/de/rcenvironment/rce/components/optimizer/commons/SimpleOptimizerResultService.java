/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.optimizer.commons;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Simple version of the {@link OptimizerResultService}.
 * @author Christian Weiss
 */
public class SimpleOptimizerResultService implements OptimizerResultService {

    private static OptimizerResultService parametricStudyService = ServiceUtils.createNullService(OptimizerResultService.class);

    protected void bindOptimizerResultService(final OptimizerResultService newParametricStudyService) {
        SimpleOptimizerResultService.parametricStudyService = newParametricStudyService;
    }
    
    protected void unbindOptimizerResultService(final OptimizerResultService oldParametricStudyService) {
        SimpleOptimizerResultService.parametricStudyService = ServiceUtils.createNullService(OptimizerResultService.class);
    }

    @Override
    public OptimizerPublisher createPublisher(final String identifier,
            final String title, final ResultStructure structure) {
        return parametricStudyService.createPublisher(identifier, title, structure);
    }

    @Override
    public OptimizerReceiver createReceiver(final String identifier,
            final PlatformIdentifier platform) {
        return parametricStudyService.createReceiver(identifier, platform);
    }

}
