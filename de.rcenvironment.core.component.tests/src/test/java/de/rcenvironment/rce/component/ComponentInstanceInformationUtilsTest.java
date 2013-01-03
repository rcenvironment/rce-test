/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import de.rcenvironment.rce.component.endpoint.Output;

/**
 * Test cases for {@link ComponentInstanceInformationUtils}.
 * 
 * @author Doreen Seider
 */
public class ComponentInstanceInformationUtilsTest {

    private static final String ANOTHERKEY = "anotherkey";

    private static final String KEY = "key";

    private Set<Output> outputs = new HashSet<Output>();

    private boolean inputsConnected = false;

    private ComponentDescription compDescription;

    private Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();

    private Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();

    private Map<String, Map<String, Serializable>> inputMetaDefs = new HashMap<String, Map<String, Serializable>>();

    private Map<String, Map<String, Serializable>> outputMetaDefs = new HashMap<String, Map<String, Serializable>>();

    private Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();

    private Map<String, Serializable> defaultConfigs = new HashMap<String, Serializable>();

    /**
     * Cleans up member variables.
     */
    @After
    public void cleanUp() {
        outputs = new HashSet<Output>();
        inputsConnected = false;
        inputDefs = new HashMap<String, Class<? extends Serializable>>();
        outputDefs = new HashMap<String, Class<? extends Serializable>>();
        configDefs = new HashMap<String, Class<? extends Serializable>>();
        defaultConfigs = new HashMap<String, Serializable>();
    }

    private ComponentInstanceInformation createComponentInstanceInformation() {

        DeclarativeComponentDescription declCompDescription = new DeclarativeComponentDescription(
            "watschel_platsch", null, null, null, inputDefs, outputDefs,
            inputMetaDefs, outputMetaDefs, configDefs, null, defaultConfigs,
            null, null);
        compDescription = new ComponentDescription(declCompDescription);

        return new ComponentInstanceInformation(null, null, null,
            compDescription, null, null, inputsConnected, outputs);
    }

    /** Test. */
    @Test
    public void testHasInputs() {
        ComponentInstanceInformation instInformation = createComponentInstanceInformation();
        assertFalse(ComponentInstanceInformationUtils
            .hasInputs(instInformation));
        inputDefs.put(KEY, String.class);
        instInformation = createComponentInstanceInformation();
        assertTrue(ComponentInstanceInformationUtils.hasInputs(instInformation));

        inputDefs.put(ANOTHERKEY, Integer.class);
        instInformation = createComponentInstanceInformation();
        assertTrue(ComponentInstanceInformationUtils.hasInputs(instInformation));

        inputDefs.clear();
        instInformation = createComponentInstanceInformation();
        assertFalse(ComponentInstanceInformationUtils
            .hasInputs(instInformation));
    }

    /** Test. */
    @Test
    public void testGetInputs() {
        ComponentInstanceInformation instInformation = createComponentInstanceInformation();
        assertEquals(
            0,
            ComponentInstanceInformationUtils.getInputs(String.class,
                instInformation).size());

        inputDefs.put(KEY, String.class);
        instInformation = createComponentInstanceInformation();
        assertEquals(1, ComponentInstanceInformationUtils.getInputs(String.class, instInformation).size());
        assertEquals(0, ComponentInstanceInformationUtils.getInputs(Integer.class, instInformation).size());

        inputDefs.put(ANOTHERKEY, String.class);
        inputDefs.put("yetanotherkey", Integer.class);
        instInformation = createComponentInstanceInformation();
        assertEquals(2, ComponentInstanceInformationUtils.getInputs(String.class, instInformation).size());
        assertEquals(1, ComponentInstanceInformationUtils.getInputs(Integer.class, instInformation).size());
        assertEquals(0, ComponentInstanceInformationUtils.getInputs(Long.class, instInformation).size());
    }

    /** Test. */
    @Test
    public void testGetOutputs() {
        ComponentInstanceInformation instInformation = createComponentInstanceInformation();
        assertEquals(0, ComponentInstanceInformationUtils.getOutputs(String.class, instInformation).size());

        outputDefs.put(KEY, Long.class);
        instInformation = createComponentInstanceInformation();
        assertEquals(1, ComponentInstanceInformationUtils.getOutputs(Long.class, instInformation).size());
        assertEquals(0, ComponentInstanceInformationUtils.getOutputs(Integer.class, instInformation).size());

        outputDefs.put(ANOTHERKEY, Long.class);
        outputDefs.put("yetanotherkey", String.class);
        instInformation = createComponentInstanceInformation();
        assertEquals(2, ComponentInstanceInformationUtils.getOutputs(Long.class, instInformation).size());
        assertEquals(1, ComponentInstanceInformationUtils.getOutputs(String.class, instInformation).size());
        assertEquals(0,  ComponentInstanceInformationUtils.getOutputs(Integer.class, instInformation).size());
    }

    /** Test. */
    @Test
    public void testGetConfigurationValue() {
        ComponentInstanceInformation instInformation = createComponentInstanceInformation();
        assertEquals(0, ComponentInstanceInformationUtils.getOutputs(String.class, instInformation).size());

        configDefs.put(KEY, Long.class);
        defaultConfigs.put(KEY, new Long(9));

        configDefs.put(ANOTHERKEY, Long.class);

        instInformation = createComponentInstanceInformation();
        assertTrue(ComponentInstanceInformationUtils.getConfigurationValue(
            KEY, Long.class, instInformation).equals(new Long(9)));
        assertTrue(ComponentInstanceInformationUtils.getConfigurationValue(
            KEY, Long.class, new Long(7), instInformation).equals(new Long(9)));
        assertNull(ComponentInstanceInformationUtils.getConfigurationValue(
            ANOTHERKEY, Long.class, instInformation));
        assertTrue(ComponentInstanceInformationUtils.getConfigurationValue(
            ANOTHERKEY, Long.class, new Long(7), instInformation).equals(new Long(7)));
        assertNull(ComponentInstanceInformationUtils.getConfigurationValue(
            "unknownkey", Long.class, instInformation));

        try {
            assertNull(ComponentInstanceInformationUtils.getConfigurationValue(
                KEY, String.class, instInformation));
            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }
}
