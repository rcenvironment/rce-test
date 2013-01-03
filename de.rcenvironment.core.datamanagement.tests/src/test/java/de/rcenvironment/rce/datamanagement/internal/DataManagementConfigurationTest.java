/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Test cases for {@link DataManagementConfiguration}.
 *
 * @author Doreen Seider
 */
public class DataManagementConfigurationTest {

    private String catalogBackend = "de.rcenvironment.rce.datamanagement.backend.catalog.derby";
    private String fileDataBackend = "de.rcenvironment.rce.datamanagement.backend.data.efs";
    
    private DataManagementConfiguration dmConfig = new DataManagementConfiguration();
    
    /** Test. */
    @Test
    public void test() {
        assertEquals(catalogBackend, dmConfig.getCatalogBackend());
        assertEquals(fileDataBackend, dmConfig.getFileDataBackend());
        
        dmConfig.setCatalogBackend(fileDataBackend);
        assertEquals(fileDataBackend, dmConfig.getCatalogBackend());
        dmConfig.setFileDataBackend(catalogBackend);
        assertEquals(catalogBackend, dmConfig.getFileDataBackend());
    }
}
