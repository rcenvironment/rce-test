/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;


/**
 * Test cases for {@link ComponentUtils}.
 *
 * @author Doreen Seider
 */
public class ComponentUtilsTest {

    private static final String COLON = ":";
    private static final String COUNTER = "counter";
    private static final String NAME = "first:lastname";
    private static final String ESCAPED_NAME = "first\\:lastname";
    
    private static final String CONFIG5 = "config5";
    private static final String CONFIG4 = "config4";
    private static final String CONFIG3 = "config3";
    private static final String CONFIG2 = "config:2";
    private static final String CONFIG1 = "config1";
    private static final String CONFIG7 = "config7";
    
    private List<ComponentDescription> descriptions;
    
    private ComponentDescription cd1;
    private ComponentDescription cd2;
    private ComponentDescription cd3;
    
    private final String compId1 = "cId1";
    private final String compId3 = "cId3";
    
    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");
    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");
    
    /** Setup. */
    @SuppressWarnings("serial")
    @Before
    public void setUp() {
        
        ComponentDescription clonedCd1 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(clonedCd1.getIdentifier()).andReturn(compId1).anyTimes();
        EasyMock.replay(clonedCd1);
        cd1 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd1.getIdentifier()).andReturn(compId1).anyTimes();
        EasyMock.expect(cd1.getPlatform()).andReturn(pi1).anyTimes();
        EasyMock.expect(cd1.clone()).andReturn(clonedCd1).anyTimes();
        EasyMock.replay(cd1);
        cd2 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd2.getIdentifier()).andReturn(compId1).anyTimes();
        EasyMock.expect(cd2.getPlatform()).andReturn(pi2).anyTimes();
        EasyMock.replay(cd2);
        ComponentDescription clonedCd3 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(clonedCd3.getIdentifier()).andReturn(compId3).anyTimes();
        EasyMock.replay(clonedCd3);
        cd3 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd3.getIdentifier()).andReturn(compId3).anyTimes();
        EasyMock.expect(cd3.getPlatform()).andReturn(pi2).anyTimes();
        EasyMock.expect(cd3.clone()).andReturn(clonedCd3).anyTimes();
        EasyMock.replay(cd3);
        
        
        descriptions = new ArrayList<ComponentDescription>() {

            {
                add(cd1);
                add(cd2);
                add(cd3);
            }
        };
    }
    /** Test. */
    @Test
    public void testConvertProperty() {
        String[] value1 = { ESCAPED_NAME + COLON + String.class.getName(), COUNTER + COLON + Integer.class.getName() };
        Map<String, Class<? extends Serializable>> map =
            ComponentUtils.parsePropertyForConfigTypes(value1);
        assertEquals(2, map.size());
        assertTrue(map.containsKey(NAME));
        assertTrue(map.containsKey(COUNTER));

        // empty string
        String[] value2 = {};
        map = ComponentUtils.parsePropertyForConfigTypes(value2);
        assertEquals(0, map.size());

        // null
        map = ComponentUtils.parsePropertyForConfigTypes(null);
        assertNotNull(map);
        assertEquals(0, map.size());

        // invalid string
        String[] value3 = { NAME + COLON + String.class.getName(), COUNTER + COLON + Integer.class.getName() + "hust" };
        try {
            ComponentUtils.parsePropertyForConfigTypes(value3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        String[] value4 = { NAME + String.class.getName(), COUNTER + COLON + Integer.class.getName() };
        try {
            ComponentUtils.parsePropertyForConfigTypes(value4);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    

    /** Test. */
    @Test
    public void testDivideProperties(){
        ComponentUtils.divideProperty(new String[]{
            "prop1:java.lang.Double" + ComponentConstants.METADATA_SEPARATOR + "usage" 
                + ComponentConstants.METADATA_VALUE_SEPARATOR + "required",
            "prop2:java.lang.Double" + ComponentConstants.METADATA_SEPARATOR,
            "prop3:java.lang.Double" + ComponentConstants.METADATA_SEPARATOR + "age" + ComponentConstants.METADATA_VALUE_SEPARATOR + "req",
            "prop4:java.lang.Double" + ComponentConstants.METADATA_SEPARATOR + "asdfaa"
                    
        });
    }
    
    /** Test. */
    @Test
    public void testConvertConfigurationValues() {
        
        Map<String, String> config = new HashMap<String, String>();
        config.put(CONFIG1, "1");
        config.put(CONFIG2, "1.0");
        config.put(CONFIG3, "defaultString");
        config.put(CONFIG4, "true");
        config.put(CONFIG5, "3");
        config.put(CONFIG7, "3");
        
        String[] property = new String[] { "config1:java.lang.Integer", "config\\:2:java.lang.Double",
            "config3:java.lang.String", "config4:java.lang.Boolean", "config5:java.lang.Long",
            "config6:java.lang.String", "config7:java.lang.Short" };
        Map<String, Class<? extends Serializable>> configDef = ComponentUtils.parsePropertyForConfigTypes(property);
        
        
        Map<String, Serializable> defaultConfig = ComponentUtils.convertConfigurationValues(configDef, config);
        Object config1 = defaultConfig.get(CONFIG1);
        assertTrue(config1 instanceof Integer);
        assertEquals(1, config1);
        Object config2 = defaultConfig.get(CONFIG2);
        assertTrue(config2 instanceof Double);
        assertEquals(1.0, config2);
        Object config3 = defaultConfig.get(CONFIG3);
        assertTrue(config3 instanceof String);
        assertEquals("defaultString", config3);
        Object config4 = defaultConfig.get(CONFIG4);
        assertTrue(config4 instanceof Boolean);
        assertEquals(true, config4);
        Object config5 = defaultConfig.get(CONFIG5);
        assertTrue(config5 instanceof Long);
        assertEquals(new Long(3), config5);
        Object config6 = defaultConfig.get("config6");
        assertNull(config6);
        
        configDef.remove(ComponentConstants.CONFIGURATION_DEF_KEY);
        config.clear();
        config.put(CONFIG1, "integer");
        config.put(CONFIG2, "double");
        config.put(CONFIG4, "unknown");
        config.put(CONFIG5, "long");
        
        configDef = ComponentUtils.parsePropertyForConfigTypes(property);
        
        
        defaultConfig = ComponentUtils.convertConfigurationValues(configDef, config);
        assertNull(defaultConfig.get(CONFIG1));
        assertNull(defaultConfig.get(CONFIG2));
        assertNull(defaultConfig.get(CONFIG3));
        assertFalse((Boolean) defaultConfig.get(CONFIG4));
        assertNull(defaultConfig.get(CONFIG5));
        assertNull(defaultConfig.get("config6"));
        
        configDef.remove(ComponentConstants.CONFIGURATION_DEF_KEY);
    }
    
    /** Test. */
    @Test
    public void testParseConfiguration() {
        String[] property = new String[] { "config1:java.lang.Integer:1", "config\\:2:java.lang.Double",
            "config3:java.lang.String:aut:sch", "config4:java.lang.Boolean:true", "config5:java.lang.Long",
            "config6:java.lang.String", "config7:java.lang.Short" };
        Map<String, String> parsedConfig = ComponentUtils.parsePropertyForConfigValues(property);
        
        assertEquals(7, parsedConfig.size());
        assertEquals("1", parsedConfig.get(CONFIG1));
        assertEquals("aut:sch", parsedConfig.get(CONFIG3));
        assertEquals("true", parsedConfig.get(CONFIG4));
        assertNull(parsedConfig.get(CONFIG2));
    }
    
    /** Test. */
    @Test
    public void testGetPlatformsForComponent() {
        List<PlatformIdentifier> pis = ComponentUtils.getPlatformsForComponent(descriptions, cd1.getIdentifier());
        assertEquals(2, pis.size());
        assertEquals(pi1, pis.get(0));
        assertEquals(pi2, pis.get(1));
    }

    /** Test. */
    @Test
    public void testHasComponent() {
        assertTrue(ComponentUtils.hasComponent(descriptions, cd1.getIdentifier(), pi1));
        assertFalse(ComponentUtils.hasComponent(descriptions, cd3.getIdentifier(), pi1));
    }
    
    /** Test. */
    @Test
    public void testEliminateDuplicates() {
        List<ComponentDescription> descs = ComponentUtils.eliminateDuplicates(descriptions);
        assertEquals(2, descs.size());
        assertEquals(compId1, descs.get(0).getIdentifier());
        assertEquals(compId3, descs.get(1).getIdentifier());
    }
 
    /** Test. */
    @Test
    public void testGetPlaceholderComponentDescription() {
        ComponentDescription cd = ComponentUtils.getPlaceholderComponentDescription(NAME);
        assertEquals(NAME, cd.getName());
    }
}
