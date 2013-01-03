/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.component.ComponentDescription;

/**
 * Test cases for {@link Connection}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class ConnectionTest {

    private ComponentDescription cd = EasyMock.createNiceMock(ComponentDescription.class);
    
    private WorkflowNode node1 = new WorkflowNode(cd);

    private WorkflowNode node2 = new WorkflowNode(cd);
    
    private WorkflowNode node3 = new WorkflowNode(cd);

    private String output = "output";

    private String input = "input";

    private Connection connection;

    private Connection anotherConnection;

    /** Test. */
    @Before
    public void setUp() {
        connection = new Connection(node1, output, node2, input);
        anotherConnection = new Connection(node1, output, node3, input);
    }
    
    /** Test. */
    @Test
    public void testConnection() {
        assertSame(connection.getSource(), node1);
        assertSame(connection.getTarget(), node2);
        assertSame(connection.getOutput(), output);
        assertSame(connection.getInput(), input);
    }
    
    /** Test. */
    @Test
    public void testEquals() {
        
        assertTrue(connection.equals(connection));
        assertFalse(connection.equals(anotherConnection));
        assertFalse(anotherConnection.equals(connection));
        assertTrue(anotherConnection.equals(anotherConnection));
    }
    
    /** Test. */
    @Test
    public void testHashCode() {
        assertEquals(connection.hashCode(), connection.hashCode());
        assertFalse(connection.hashCode() == anotherConnection.hashCode());
    }

}
