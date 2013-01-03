/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.configuration;

import de.rcenvironment.commons.ServiceUtils;


/**
 * Simple version of the {@link ConfigurationService}.
 *
 * @author Doreen Seider
 */
public class SimpleConfigurationService {
    
    private static ConfigurationService configurationService = ServiceUtils.createNullService(ConfigurationService.class);

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }
        
    /**
     * @see de.rcenvironment.rce.configuration.ConfigurationService#getPlatformHome()
     * 
     * @return path to platform's home
     */
    public String getPlatformHome() {
        return configurationService.getPlatformHome();
    }

}
