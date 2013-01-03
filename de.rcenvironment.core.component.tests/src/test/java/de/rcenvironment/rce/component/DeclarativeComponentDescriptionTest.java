/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;


/**
 * Test cases for {@link DeclarativeComponentDescription}.
 *
 * @author Doreen Seider
 */
public class DeclarativeComponentDescriptionTest {

    private final String identifier = String.class + "_id";

    private final String name = "name";
    
    private final String group = "group";

    private final String version = "version";

    private final Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();

    private final Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();
    
    private final Map<String, Map<String, Serializable>> inputMetaDefs = new  HashMap<String, Map<String, Serializable>>();

    private final Map<String, Map<String, Serializable>> outputMetaDefs = new  HashMap<String, Map<String, Serializable>>();
    
    private final Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();

    private final Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();

    private final byte[] icon16 = new byte[10];

    private final byte[] icon32 = new byte[0];
    
    /** Set up. */
    @Before
    public void setUp() {
        inputDefs.put("allet", String.class);
        outputDefs.put("knorke", Integer.class);
    }
    
    /** Test. */
    @Test
    public void testConstuctor() {
        
        DeclarativeComponentDescription cd = new DeclarativeComponentDescription(identifier, name, group, version,
            inputDefs, outputDefs, inputMetaDefs, outputMetaDefs, configDefs, null, defaultConfig, icon16, icon32);
        assertEquals(identifier, cd.getIdentifier());
        assertEquals(name, cd.getName());
        assertEquals(group, cd.getGroup());
        assertEquals(version, cd.getVersion());
        assertEquals(inputDefs, cd.getInputDefinitions());
        assertEquals(outputDefs, cd.getOutputDefinitions());
        assertEquals(configDefs, cd.getConfigurationDefinitions());
        assertEquals(defaultConfig, cd.getDefaultConfiguration());
        assertEquals(icon16, cd.getIcon16());
        assertEquals(icon32, cd.getIcon32());
    }
    
    /** Test. */
    @Test
    public void testCompareTo() {
        
        DeclarativeComponentDescription cd = new DeclarativeComponentDescription(identifier, name, group, version,
            inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        DeclarativeComponentDescription cdLower = new DeclarativeComponentDescription(identifier, "a" + name, group, version,
            inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        
        assertEquals(0, cd.compareTo(cd));
        assertTrue(cd.compareTo(cdLower) > 0);
        assertTrue(cdLower.compareTo(cd) < 0);
    }
}
