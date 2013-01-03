/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration;

/**
 * Listener interface to be implemented by components that want to get informed about errors
 * occurring in a {@link ConfigurationService}.
 * 
 * @author Christian Weiss
 */
public interface ConfigurationServiceMessageEventListener {

    /**
     * Handles a {@link ConfigurationServiceMessageEvent}.
     * 
     * @param error the error to handle
     */
    void handleConfigurationServiceError(ConfigurationServiceMessageEvent error);

}
