/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.commons.StringUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Test cases for {@link ComponentDescription}.
 * 
 * @author Doreen Seider
 */
public class ComponentDescriptionTest extends TestCase {

    private static final String JAVA_LANG_INTEGER = "java.lang.Integer";

    private static final String JAVA_LANG_DOUBLE = "java.lang.Double";

    private static final String JAVA_LANG_STRING = "java.lang.String";

    private final String declOutputName = "knorke";

    private final String declInputName = "allet";

    // declarative component description stuff
    private DeclarativeComponentDescription declarativeCD;
    
    private final String identifier = "super.dolle.component_Aufi";

    private final String name = "name";
    
    private final String group = "group";

    private final String version = "version";

    private final Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();

    private final Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();

    private final Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();

    private final Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();

    private final byte[] icon16 = new byte[10];

    private final byte[] icon32 = new byte[0];
    
    private PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("mensch:3");
    
    /** Set up. */
    @Before
    public void setUp() {
        inputDefs.put(declInputName, String.class);
        outputDefs.put(declOutputName, Integer.class);
        
        declarativeCD = new DeclarativeComponentDescription(identifier, name, group, version,
            inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
    }
    
    /** Test. */
    @Test
    public void testConstuctorAndDelegateStuff() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        assertEquals(declarativeCD.getIdentifier(), cd.getIdentifier());
        assertEquals(declarativeCD.getClassName(), cd.getClassName());
        assertEquals(declarativeCD.getName(), cd.getName());
        assertEquals(declarativeCD.getGroup(), cd.getGroup());
        assertEquals(declarativeCD.getVersion(), cd.getVersion());
        assertEquals(declarativeCD.getIcon16(), cd.getIcon16());
        assertEquals(declarativeCD.getIcon32(), cd.getIcon32());
        assertEquals(declarativeCD.getName(), cd.getName());
        assertEquals(declarativeCD.getConfigurationDefinitions().size(), cd.getConfigurationDefinitions().size());
        assertEquals(declarativeCD.getDefaultConfiguration().size(), cd.getDefaultConfiguration().size());
        assertEquals(declarativeCD.getDefaultConfiguration().size(), cd.getConfiguration().size());
    }
    
    
    /** Test. */
    @Test
    public void testPlatformIdentifierStuff() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        cd.setPlatform(pi);
        assertEquals(pi, cd.getPlatform());
    }
    
    /** Test. */
    @Test
    public void testEndpointAddRemoveChange() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
       
        int inputCount = cd.getInputDefinitions().size();
        int dynInputCount = cd.getDynamicInputDefinitions().size();
        
        int outputCount = cd.getOutputDefinitions().size();
        int dynOutputCount = cd.getDynamicOutputDefinitions().size();
        
        final String endpointName = "marco";
        final String changedEndpointName = "baldo";

        // add endpoint
        // input
        cd.addInput(endpointName, JAVA_LANG_STRING);
        inputCount++;
        dynInputCount++;
        assertEquals(inputCount, cd.getInputDefinitions().size());
        assertEquals(dynInputCount, cd.getDynamicInputDefinitions().size());
        //output
        cd.addOutput(endpointName, JAVA_LANG_STRING);
        outputCount++;
        dynOutputCount++;
        assertEquals(outputCount, cd.getOutputDefinitions().size());
        assertEquals(dynOutputCount, cd.getDynamicOutputDefinitions().size());
        
        // change endpoint
        // input
        cd.changeInput(endpointName, changedEndpointName, JAVA_LANG_DOUBLE);
        assertEquals(inputCount, cd.getInputDefinitions().size());
        assertEquals(dynInputCount, cd.getDynamicInputDefinitions().size());
        // output
        cd.changeOutput(endpointName, changedEndpointName, JAVA_LANG_DOUBLE);
        assertEquals(outputCount, cd.getOutputDefinitions().size());
        assertEquals(dynOutputCount, cd.getDynamicOutputDefinitions().size());

        // nonsense removals
        // input
        cd.removeInput("12");
        cd.removeInput(endpointName);
        assertEquals(inputCount, cd.getInputDefinitions().size());
        assertEquals(dynInputCount, cd.getDynamicInputDefinitions().size());
        // output
        cd.removeOutput("2010");
        cd.removeOutput(endpointName);
        assertEquals(outputCount, cd.getOutputDefinitions().size());
        assertEquals(dynOutputCount, cd.getDynamicOutputDefinitions().size());
        
        // remove endpoint
        // input
        cd.removeInput(changedEndpointName);
        inputCount--;
        dynInputCount--;
        assertEquals(inputCount, cd.getInputDefinitions().size());
        assertEquals(dynInputCount, cd.getDynamicInputDefinitions().size());
        // output
        cd.removeOutput(changedEndpointName);
        outputCount--;
        dynOutputCount--;
        assertEquals(outputCount, cd.getOutputDefinitions().size());
        assertEquals(dynOutputCount, cd.getDynamicOutputDefinitions().size());
    }

    /** Test. */
    @Test
    public void testEndpointStuff() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);

        final String inputName = "rein";
        final String changedInputName = "rinn";
        final String outputName = "raus";
        
        cd.addInput(inputName, JAVA_LANG_STRING);
        cd.addOutput(outputName, JAVA_LANG_INTEGER);
        
        // validate
        assertFalse(cd.validateInputName(declInputName));
        assertFalse(cd.validateInputName(inputName));
        assertTrue(cd.validateInputName(changedInputName));
        assertFalse(cd.validateOutputName(declOutputName));
        assertFalse(cd.validateOutputName(outputName));
        assertTrue(cd.validateInputType(JAVA_LANG_STRING));
        assertTrue(cd.validateOutputType(JAVA_LANG_STRING));
        assertFalse(cd.validateInputType("java.lang.Object"));
        assertFalse(cd.validateOutputType("java.lang.Object"));
        assertTrue(cd.validateInputType("java.io.Serializable"));
        assertTrue(cd.validateOutputType("java.io.Serializable"));
        // change input
        cd.changeInput(inputName, changedInputName, JAVA_LANG_DOUBLE);
        // validate input
        assertFalse(cd.validateInputName(declInputName));
        assertTrue(cd.validateInputName(inputName));
        assertFalse(cd.validateInputName(changedInputName));
    }
    
    /** Test. */
    @Test
    public void testMetaDataStuff() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        
        final String inputName = "rein";
        final String inputMetaDataKey = "schluesssel";
        final String inputMetaDataValue = "schloss";
        cd.setInputMetaData(inputName, inputMetaDataKey, inputMetaDataValue);
        
        Map<String, Serializable> metaData = cd.getInputMetaData(inputName);
        assertEquals(1, metaData.size());
        assertEquals(inputMetaDataValue, metaData.get(inputMetaDataKey));
        
        metaData = cd.getInputMetaData("verloren");
        assertEquals(0, metaData.size());
        
        final String outputName = "kreditkarte";
        final String outputMetaDataKey = "abgebrochen";
        final String outputMetaDataValue = "schlosser";
        cd.setOutputMetaData(outputName, outputMetaDataKey, outputMetaDataValue);
        
        metaData = cd.getOutputMetaData(outputName);
        assertEquals(1, metaData.size());
        assertEquals(outputMetaDataValue, metaData.get(outputMetaDataKey));
        
        metaData = cd.getOutputMetaData("offen");
        assertEquals(0, metaData.size());
    }
    
    /** Test. */
    @Test
    public void testConfigurationStuff() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        
        assertEquals(1, cd.getConfigurationIds().size());
        assertTrue(cd.getConfigurationIds().contains(ComponentDescription.DEFAULT_CONFIG_ID));
        
        String mapId = "dupa";
        cd.addConfiguration(mapId, ComponentDescription.DEFAULT_CONFIG_ID);
        
        assertEquals(2, cd.getConfigurationIds().size());
        assertTrue(cd.getConfigurationIds().contains(ComponentDescription.DEFAULT_CONFIG_ID));
        assertTrue(cd.getConfigurationIds().contains(mapId));
        
        assertEquals(0, cd.getConfiguration().size());
        cd.setConfigurationId(mapId);
        assertEquals(0, cd.getConfiguration().size());
        assertEquals(mapId, cd.getConfigurationId());
        
        String key = "eieiei";
        Map<String, Serializable> config = new HashMap<String, Serializable>();
        config.put(key, "wow");
        
        String mapId2 = "supa";
        cd.addConfiguration(mapId2, config);
        
        assertEquals(0, cd.getConfiguration().size());
        assertEquals(1, cd.getConfiguration(mapId2).size());
        cd.setConfigurationId(mapId2);
        assertEquals(1, cd.getConfiguration().size());
        
        assertEquals(3, cd.getConfigurationIds().size());
        cd.removeConfiguration(mapId);
        assertEquals(1, cd.getConfiguration().size());
        assertEquals(2, cd.getConfigurationIds().size());
        assertTrue(cd.getConfigurationIds().contains(ComponentDescription.DEFAULT_CONFIG_ID));
        assertTrue(cd.getConfigurationIds().contains(mapId2));
        
        cd.removeConfiguration(mapId2);
        assertEquals(0, cd.getConfiguration().size());
        
        String mapId3 = "schnupa";
        cd.addConfiguration(mapId3, (Map<String, Serializable>) null);
        assertEquals(0, cd.getConfiguration(mapId3).size());
        
        String mapId4 = "prima";
        cd.addConfiguration(mapId4, (String) null);
        assertEquals(0, cd.getConfiguration(mapId4).size());
    }

    /** Test. */
    @Test
    public void testToString() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        cd.setPlatform(pi);
        assertEquals(pi.toString() + StringUtils.SEPARATOR + identifier, cd.toString());
    }
    
    /** Test. */
    @Test
    public void testCompareTo() {
        
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        
        DeclarativeComponentDescription declarativeCDLower = new DeclarativeComponentDescription(identifier, "a" + name, group, version,
            inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        ComponentDescription cdLower = new ComponentDescription(declarativeCDLower);
        assertEquals(0, cd.compareTo(cd));
        assertTrue(cd.compareTo(cdLower) > 0);
        assertTrue(cdLower.compareTo(cd) < 0);
    }

    /** Test. */
    @Test
    public void testClone() {
        ComponentDescription cd = new ComponentDescription(declarativeCD);
        assertFalse(cd.clone().equals(cd));
    }
}
