/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for  {@link DynamicEndpointManager}.
 *
 * @author Doreen Seider
 */
public class DynamicEndpointManagerTest {

    private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
    
    private DynamicEndpointManager manager;
    
    private final String entryKey1 = "snoopy";
    private final String entryKey2 = "woodstock";
    private final String entryKey3 = "charles";
    private final String entryKey4 = "linus";
    
    /** Set up. */
    @Before
    public void setUp() {
        Map<String, Class<? extends Serializable>> declarativeEntries = new HashMap<String, Class<? extends Serializable>>();
        declarativeEntries.put(entryKey1, String.class);
        declarativeEntries.put(entryKey2, Integer.class);
        manager = new DynamicEndpointManager(declarativeEntries);
    }
    
    /** Test. */
    @Test
    public void testValidateNewName() {
        assertFalse(manager.validateNewName(entryKey1));
        assertFalse(manager.validateNewName(""));
        assertTrue(manager.validateNewName("wissen:machtAh"));
        assertTrue(manager.validateNewName(entryKey3));
        manager.addEndpoint(entryKey3, "java.lang.Boolean");
        assertFalse(manager.validateNewName(entryKey3));
    }
    
    /** Test. */
    @Test
    public void testValidateTypeName() {
        assertTrue(manager.validateTypeName("java.lang.String"));
        assertFalse(manager.validateTypeName("jav.lang.String"));
        assertFalse(manager.validateTypeName("java.lang.Strig"));
    }
    
    /** Test. */
    @Test
    public void testAddRemoveEndpoint() {
        
        try {
            manager.addEndpoint(entryKey1, JAVA_LANG_INTEGER);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.addEndpoint(entryKey3, "ava.lang.Integer");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.addEndpoint(entryKey3, this.getClass().getCanonicalName());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
        try {
            manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        manager.removeEndpoint(entryKey3);
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
    }
    
    /** Test. */
    @Test
    public void testChangeEndpoint() {
        
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
        int endpointCount = manager.getEndpointDefinitions().size();
        assertTrue(manager.getEndpointNames().contains(entryKey3));

        try {
            manager.changeEndpoint(entryKey3, entryKey1, JAVA_LANG_INTEGER);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        
        try {
            manager.changeEndpoint(entryKey3, entryKey4, "jva.lang.Integer");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        
        manager.changeEndpoint(entryKey3, entryKey4, JAVA_LANG_INTEGER);
        assertEquals(endpointCount, manager.getEndpointDefinitions().size());
        assertTrue(manager.getEndpointNames().contains(entryKey4));
        assertFalse(manager.getEndpointNames().contains(entryKey3));
    }
    
    /** Test. */
    @Test
    public void testGetEndpointType() {
        assertNull(manager.getEndpointType(entryKey2));
        assertNull(manager.getEndpointType(entryKey3));
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
        assertEquals(JAVA_LANG_INTEGER, manager.getEndpointType(entryKey3));
    }
    
    /** Test. */
    @Test
    public void testGetEndpointNames() {
        assertEquals(0, manager.getEndpointNames().size());
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
        List<String> names = manager.getEndpointNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(entryKey3));
    }
    
    /** Test. */
    @Test
    public void testGetEndpointDefinitions() {
        assertEquals(0, manager.getEndpointDefinitions().size());
        manager.addEndpoint(entryKey3, JAVA_LANG_INTEGER);
        Map<String, Class<? extends Serializable>> defs = manager.getEndpointDefinitions();
        assertEquals(1, defs.size());
        assertEquals(Integer.class, defs.get(entryKey3));
    }

}
