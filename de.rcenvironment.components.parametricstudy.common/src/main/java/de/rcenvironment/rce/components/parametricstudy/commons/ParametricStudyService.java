/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.commons;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Service used to announce and receive values used for parameter study purposes.
 * @author Christian Weiss
 */
public interface ParametricStudyService {

    /**
     * Creates a {@link StudyPublisher}.
     * 
     * @param identifier the unique identifier
     * @param title the title
     * @param structure the structure definition of the values
     * @return the created {@link StudyPublisher}.
     */
    StudyPublisher createPublisher(final String identifier, final String title, final StudyStructure structure);

    /**
     * Create a {@link StudyReceiver}.
     * 
     * @param identifier the unique identifier
     * @param platform the platform to receive values from
     * @return the created {@link StudyReceiver}
     */
    StudyReceiver createReceiver(final String identifier, final PlatformIdentifier platform);

}