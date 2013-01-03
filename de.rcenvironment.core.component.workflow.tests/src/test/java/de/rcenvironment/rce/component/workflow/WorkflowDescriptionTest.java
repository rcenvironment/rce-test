/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import org.easymock.EasyMock;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentDescription;


/**
 * Test cases for {@link WorkflowDescription}.
 *
 * @author Heinrich Wendel
 */
public class WorkflowDescriptionTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    private ComponentDescription cd = EasyMock.createNiceMock(ComponentDescription.class);
    
    private String id = "neNummer";

    /** Test. */
    @Test
    public void testWorkflowDescription() {
        WorkflowDescription desc = new WorkflowDescription("test");
        assertEquals("test", desc.getIdentifier());
    }

    /** Test. */
    @Test
    public void testGetName() {
        WorkflowDescription desc = new WorkflowDescription(id);
        desc.setName("test2");
        assertEquals("test2", desc.getName());
    }
    
    /** Test. */
    @Test
    public void testGetWorkflowVersion() {
        WorkflowDescription desc = new WorkflowDescription(id);
        final int version = 7;
        desc.setWorkflowVersion(version);
        assertEquals(version, desc.getWorkflowVersion());
    }

    /** Test. */
    @Test
    public void testGetAdditionalInformation() {
        WorkflowDescription desc = new WorkflowDescription(id);
        String value = "test ... " + RANDOM.nextLong();
        String expectedValue = new String(value);
        desc.setAdditionalInformation(value);
        assertEquals(expectedValue, desc.getAdditionalInformation());
    }

    /** Test. */
    @Test
    public void testGetTargetPlatform() {
        WorkflowDescription desc = new WorkflowDescription(id);
        PlatformIdentifier tp = PlatformIdentifierFactory.fromHostAndNumberString("test:1");
        desc.setTargetPlatform(tp);
        assertEquals(tp, desc.getTargetPlatform());
    }

    /** Test. */
    @Test
    public void testGetWorkflowNodes() {
        WorkflowDescription desc = new WorkflowDescription(id);
        WorkflowNodeChangeListener l1 = new WorkflowNodeChangeListener(WorkflowDescription.NODES_CHANGED_PROP);
        desc.addPropertyChangeListener(l1);
        
        assertEquals(0, desc.getWorkflowNodes().size());
        WorkflowNode node = new WorkflowNode(cd);
        desc.addWorkflowNode(node);
        assertTrue(l1.getFired());
        assertEquals(1, desc.getWorkflowNodes().size());
        assertTrue(desc.getWorkflowNodes().contains(node));

        desc.removePropertyChangeListener(l1);
        WorkflowNodeChangeListener l2 = new WorkflowNodeChangeListener(WorkflowDescription.NODES_CHANGED_PROP);
        desc.addPropertyChangeListener(l2);
        
        desc.removeWorkflowNode(node);
        assertTrue(l2.getFired());
        assertEquals(0, desc.getWorkflowNodes().size());
    }

    /** Test. */
    @Test
    public void testGetWorkflowNode() {
        WorkflowDescription desc = new WorkflowDescription(id);
        WorkflowNode node = new WorkflowNode(cd);
        try {
            desc.getWorkflowNode(node.getIdentifier());
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        desc.addWorkflowNode(node);
        assertEquals(node, desc.getWorkflowNode(node.getIdentifier()));
    }

    /** Test. */
    @Test
    public void testGetConnections() {
        WorkflowDescription desc = new WorkflowDescription(id);

        WorkflowNodeChangeListener l1 = new WorkflowNodeChangeListener(WorkflowDescription.CONNECTIONS_CHANGED_PROP);
             
        WorkflowNode node1 = new WorkflowNode(cd);
        WorkflowNode node2 = new WorkflowNode(cd);
        Connection connection = new Connection(node1, "output", node2, "target");
        desc.addWorkflowNode(node1);
        desc.addWorkflowNode(node2);

        desc.addPropertyChangeListener(l1);
        assertEquals(0, desc.getConnections().size());
        desc.addConnection(connection);
        assertEquals(1, desc.getConnections().size());
        assertTrue(desc.getConnections().contains(connection));
        assertTrue(l1.getFired());
        
        desc.removePropertyChangeListener(l1);
        
        WorkflowNodeChangeListener l2 = new WorkflowNodeChangeListener(WorkflowDescription.CONNECTIONS_CHANGED_PROP);
        desc.addPropertyChangeListener(l2);
        desc.removeConnection(connection);
        assertEquals(0, desc.getConnections().size());
        assertTrue(l2.getFired());
    }
    
    /** Test. */
    @Test
    public void testClone() {
        WorkflowDescription desc = new WorkflowDescription(id);
        desc.clone(EasyMock.createNiceMock(User.class));
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
         * 
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
