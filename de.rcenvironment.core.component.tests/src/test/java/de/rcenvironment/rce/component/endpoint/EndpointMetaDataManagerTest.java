/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Test cases for {@link EndpointMetaDataManager}.
 *
 * @author Doreen Seider
 */
public class EndpointMetaDataManagerTest {

    /** Test. */
    @Test
    public void test() {
        final String epName = "gestiefelter Kater";
        EndpointMetaDataManager manager = new EndpointMetaDataManager(null);
        
        assertEquals(0, manager.getEndpointMetaData(epName).size());
        final String mdKey1 = "sieben";
        final String mdValue1 = "7";
        
        final String mdKey2 = "acht";
        final Integer mdValue2 = 8;
        
        manager.setEndpointMetaData(epName, mdKey1, mdValue1);
        assertEquals(1, manager.getEndpointMetaData(epName).size());
        
        manager.setEndpointMetaData(epName, mdKey2, mdValue2);
        assertEquals(2, manager.getEndpointMetaData(epName).size());
        
        assertEquals(0, manager.getEndpointMetaData(epName + "unknown").size());
    }
}
