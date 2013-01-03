/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test cases for {@link DerbyCatalogBackendConfiguration}.
 *
 * @author Juergen Klein
 * @author Tobias Menden
 */
public class DerbyCatalogBackendConfigurationTest {

    private DerbyCatalogBackendConfiguration catalogConfig;
    private String databaseURL = "";

    /** Test. */
    @Test
    public void test() {
        catalogConfig = new DerbyCatalogBackendConfiguration();
        assertTrue(catalogConfig.getDatabaseURL().isEmpty());
        catalogConfig.setDatabaseUrl(databaseURL);
        assertEquals(databaseURL, catalogConfig.getDatabaseURL());
    }
}
