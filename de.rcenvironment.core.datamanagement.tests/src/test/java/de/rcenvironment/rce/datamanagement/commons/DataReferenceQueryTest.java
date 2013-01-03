/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link DataReferenceQuery}.
 *
 * @author Juergen Klein
 */
public class DataReferenceQueryTest {

    private DataReferenceQuery dataReferenceQuery;

    private UUID uuid;

    /** Set up. */
    @Before
    public void setUp() {
        uuid = UUID.randomUUID();
        
    }

    /** Test. */
    @Test
    public void tests() {
        dataReferenceQuery = new DataReferenceQuery(uuid);
        assertEquals(CatalogConstants.GUID + "='" + uuid.toString() + "'", dataReferenceQuery.getQuery());
        assertEquals(CatalogConstants.GUID + "='" + uuid.toString() + "'", dataReferenceQuery.toString());
    }

}
