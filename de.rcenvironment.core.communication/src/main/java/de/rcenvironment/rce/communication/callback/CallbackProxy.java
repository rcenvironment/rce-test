/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Interface which needs to be implemented by proxy object which are transfered to another platform
 * in order to enable callbacks.
 * 
 * @author Doreen Seider
 */
public interface CallbackProxy {

    /**
     * Returns the identifier of the object.
     * 
     * @return The identifier of the object.
     */
    String getObjectIdentifier();

    /**
     * Returns the {@link PlatformIdentifier} of the home platform, i.e. the platform to call back.
     * 
     * @return The {@link PlatformIdentifier} of the home platform..
     */
    PlatformIdentifier getHomePlatform();

}
