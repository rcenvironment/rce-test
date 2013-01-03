/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.component.ComponentDescription;

/**
 * Test cases for {@link WorkflowNode}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class WorkflowNodeTest {

    private static final String PROP_MAP_ID = "aha";

    private final String compId = "tappTapp";
    
    private ComponentDescription compDesc;
    
    private final String configKey = "fellfarbe";

    private final Serializable configValue = "braun";
    
    private final String dynEndpointName = "schweineohr";

    private final Class<? extends Serializable> dynEndpointType = Long.class;
    
    private final String endpointName = "herzen";

    /** Set up. */
    @Before
    public void setUp() {
        compDesc = EasyMock.createNiceMock(ComponentDescription.class);
        Map<String, Serializable> config = new HashMap<String, Serializable>();
        config.put(configKey, configValue);
        EasyMock.expect(compDesc.getConfiguration()).andReturn(config).anyTimes();
        Map<String, Class<? extends Serializable>> dynEndpointsDef = new HashMap<String, Class<? extends Serializable>>();
        dynEndpointsDef.put(dynEndpointName, dynEndpointType);
        EasyMock.expect(compDesc.getDynamicInputDefinitions()).andReturn(dynEndpointsDef).anyTimes();
        EasyMock.expect(compDesc.getDynamicOutputDefinitions()).andReturn(dynEndpointsDef).anyTimes();
        Map<String, Serializable> metaData = new HashMap<String, Serializable>();
        EasyMock.expect(compDesc.getInputMetaData(endpointName)).andReturn(metaData).anyTimes();
        EasyMock.expect(compDesc.getOutputMetaData(endpointName)).andReturn(metaData).anyTimes();
        EasyMock.expect(compDesc.validateInputName(dynEndpointName)).andReturn(false).anyTimes();
        EasyMock.expect(compDesc.validateOutputName(dynEndpointName)).andReturn(false).anyTimes();
        EasyMock.expect(compDesc.validateInputType(dynEndpointType.getCanonicalName())).andReturn(true).anyTimes();
        EasyMock.expect(compDesc.validateOutputType(dynEndpointType.getCanonicalName())).andReturn(true).anyTimes();
        EasyMock.expect(compDesc.getInputType(dynEndpointName)).andReturn(dynEndpointType.getClass().getCanonicalName()).anyTimes();
        EasyMock.expect(compDesc.getOutputType(dynEndpointName)).andReturn(dynEndpointType.getClass().getCanonicalName()).anyTimes();
        List<String> confMapIds = new ArrayList<String>();
        confMapIds.add(PROP_MAP_ID);
        confMapIds.add(ComponentDescription.DEFAULT_CONFIG_ID);
        EasyMock.expect(compDesc.getConfigurationIds()).andReturn(confMapIds).anyTimes();
        EasyMock.expect(compDesc.getConfigurationId()).andReturn(ComponentDescription.DEFAULT_CONFIG_ID).anyTimes();
        EasyMock.replay(compDesc);
    }
    
    /** Test. */
    @Test
    public void testWorkflowNode() {
        WorkflowNode node = new WorkflowNode(compDesc);
        
        assertSame(compDesc, node.getComponentDescription());
    }

    /** Test. */
    @Test
    public void testToString() {
        ComponentDescription desc = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(desc.getIdentifier()).andReturn(compId).anyTimes();
        EasyMock.replay(desc);
        WorkflowNode node = new WorkflowNode(desc);
        
        assertEquals(desc.getIdentifier(), node.toString());
    }

    /** Test. */
    @Test
    public void testSetLocation() {
        WorkflowNode node = new WorkflowNode(compDesc);
        WorkflowNodeChangeListener l3 = new WorkflowNodeChangeListener(WorkflowNode.LOCATION_PROP);
        node.addPropertyChangeListener(l3);
        
        node.setLocation(2, 3);
        assertEquals(2, node.getX());
        assertEquals(3, node.getY());

        assertEquals(true, l3.fired);
        node.removePropertyChangeListener(l3);
    }

    /** Test. */
    @Test
    public void testGetComponentDescription() {
        WorkflowNode node = new WorkflowNode(compDesc);

        ComponentDescription desc = EasyMock.createNiceMock(ComponentDescription.class);
        node.setComponentDescription(desc);
        assertEquals(desc, node.getComponentDescription());
    }

    /** Test. */
    @Test
    public void testGetName() {
        WorkflowNode node = new WorkflowNode(compDesc);
        node.setName("test");
        assertEquals("test", node.getName());
    }
    
    /** Test. */
    @Test
    public void testGetIdentifier() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertNotNull(node.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testSetIdentifier() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String nodeId = "fellfratze";
        node.setIdentifier(nodeId);
        assertEquals(nodeId, node.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testHashCode() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String nodeId = "kuschelfresse";
        node.setIdentifier(nodeId);
        assertEquals(nodeId.hashCode(), node.hashCode());
    }
    
    /** Test. */
    @Test
    public void testEquals() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String nodeId = "jacko";
        node.setIdentifier(nodeId);
        
        WorkflowNode anotherEqualNode = new WorkflowNode(compDesc);
        anotherEqualNode.setIdentifier(nodeId);
        
        WorkflowNode anotherUnequalNode = new WorkflowNode(compDesc);
        String anotherNodeId = "zwerghusky";
        anotherUnequalNode.setIdentifier(anotherNodeId);
        
        assertFalse(node.equals(nodeId));
        assertTrue(node.equals(node));
        assertTrue(node.equals(anotherEqualNode));
        assertFalse(node.equals(anotherUnequalNode));
    }
    
    /** Test. */
    @Test
    public void testGetProperty() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertEquals(configValue, node.getProperty(configKey));
    }
    
    /** Test. */
    @Test
    public void testSetProperty() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String key = "augen";
        Serializable value = "blau";
        node.setProperty(key, value);
        
        assertEquals(value, node.getProperty(key));
        
        node.setProperty(key, null);
        assertFalse(node.propertyExists(key));
    }
    
    /** Test. */
    @Test
    public void testPropertyExists() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertFalse(node.propertyExists("fluegel"));
        assertTrue(node.propertyExists(configKey));
    }
    
    /** Test. */
    @Test
    public void testPropertyStuff() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertEquals(ComponentDescription.DEFAULT_CONFIG_ID, node.getPropertyMapId());
        assertEquals(2, node.getPropertyMapIds().size());
        node.addPropertyMap("new one", null);
        node.removePropertyMap(PROP_MAP_ID);
        node.removePropertyMap(PROP_MAP_ID);
    }
    
    /** Test. */
    @Test
    public void testInputStuff() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String inputName = "ochsenziemer";
        assertFalse(node.validateInputName(dynEndpointName));
        assertTrue(node.validateInputType(dynEndpointType.getCanonicalName()));
        node.addInput(inputName, Double.class.getCanonicalName());
        assertEquals(dynEndpointType.getClass().getCanonicalName(), node.getInputType(dynEndpointName));
        node.removeInput(inputName);
        node.changeInput(inputName, "pansen", Double.class.getCanonicalName());
        assertTrue(node.getDynamicInputDefinitions().containsKey(dynEndpointName));
    }
    
    /** Test. */
    @Test
    public void testOutputStuff() {
        WorkflowNode node = new WorkflowNode(compDesc);
        String outputName = "ochsenziemer";
        assertFalse(node.validateOutputName(dynEndpointName));
        assertTrue(node.validateOutputType(dynEndpointType.getCanonicalName()));
        node.addOutput(outputName, Double.class.getCanonicalName());
        assertEquals(dynEndpointType.getClass().getCanonicalName(), node.getOutputType(dynEndpointName));
        node.changeOutput(outputName, "pansen", Double.class.getCanonicalName());
        node.removeOutput(outputName);
        assertTrue(node.getDynamicOutputDefinitions().containsKey(dynEndpointName));
    }
    
    /** Test. */
    @Test
    public void testInputMetaDataStuff() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertNotNull(node.getInputMetaData(endpointName));
        node.setInputMetaData(endpointName, configKey, configValue);
    }
    
    /** Test. */
    @Test
    public void testOutputMetaDataStuff() {
        WorkflowNode node = new WorkflowNode(compDesc);
        assertNotNull(node.getOutputMetaData(endpointName));
        node.setOutputMetaData(endpointName, configKey, configValue);
    }
    
    /**
     * Dummy implementation of {@link PropertyChangeListener}.
     *
     * @author Heinrich Wendel
     */
    class WorkflowNodeChangeListener implements PropertyChangeListener {

        /** Fired or not? */
        private boolean fired = false;
        
        /** Event to check. */
        private String event;

        /**
         * Constructor.
         * @param event Name of the property to listen for.
         */
        public WorkflowNodeChangeListener(String event) {
            this.event = event;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assertEquals(event, evt.getPropertyName());
            fired = true;
        }
        
        public boolean getFired() {
            return fired;
        }
    };
}
