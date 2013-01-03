/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Provides information about the context the component is part of.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public interface ComponentContext {

    /**
     * @return the identifier representing the context.
     */
    String getIdentifier();

    /**
     * @return the name of the context.
     */
    String getName();

    /**
     * @return the {@link PlatformIdentifier} of the platform the controller is running.
     */
    PlatformIdentifier getControllerPlatform();

    /**
     * Specifies the default platform to store data management entries and history data on. Whether
     * overriding this setting will be allowed by the various data management methods is still an
     * open design issue (see https://www.sistec.dlr.de/mantis/view.php?id=5982).
     * 
     * @return the {@link PlatformIdentifier} of the default platform to create new data management
     *         entries on
     */
    PlatformIdentifier getDefaultStoragePlatform();

    /**
     * @return the {@link PlatformIdentifier} of the platforms the components are running.
     */
    Set<PlatformIdentifier> getInvolvedPlatforms();

}
