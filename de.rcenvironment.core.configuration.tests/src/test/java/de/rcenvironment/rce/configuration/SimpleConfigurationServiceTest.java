/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.rcenvironment.rce.configuration.internal.JSONConfigurationService;

/**
 * Test cases for {@link SimpleConfigurationService}.
 *
 * @author Doreen Seider
 */
public class SimpleConfigurationServiceTest {

    private static final String PLATFORM_HOME = "path";

    /** Test. */
    @Test
    public void testGetPlatformHome() {
        SimpleConfigurationService simpleService = new SimpleConfigurationService();
        simpleService.bindConfigurationService(new DummyConfigurationService());
        assertEquals(PLATFORM_HOME, simpleService.getPlatformHome());
    }
    
    /**
     * Dummy test {@link ConfigurationService}.
     */
    class DummyConfigurationService extends JSONConfigurationService {

        @Override
        public String getPlatformHome() {
            return PLATFORM_HOME;
        }
    }
}
