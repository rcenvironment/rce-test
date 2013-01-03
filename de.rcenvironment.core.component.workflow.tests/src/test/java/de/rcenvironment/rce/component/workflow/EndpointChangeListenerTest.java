/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.endpoint.EndpointChange;


/**
 * Test cases for {@link EndpointChangeListener}.
 *
 * @author Doreen Seider
 */
public class EndpointChangeListenerTest {
    
    private static final String NAME = "name";
    
    private WorkflowDescription wd;
    
    /** Set up. */
    @Before
    public void setUp() {
        
        ComponentDescription cd = EasyMock.createNiceMock(ComponentDescription.class);
        
        WorkflowNode node = EasyMock.createNiceMock(WorkflowNode.class);
        EasyMock.expect(node.getComponentDescription()).andReturn(cd).anyTimes();
        EasyMock.replay(node);
        
        final Connection c = EasyMock.createNiceMock(Connection.class);
        EasyMock.expect(c.getSource()).andReturn(node).anyTimes();
        EasyMock.expect(c.getTarget()).andReturn(node).anyTimes();
        EasyMock.replay(c);
        @SuppressWarnings("serial")
        List<Connection> connections = new ArrayList<Connection>() {
            {
                add(c);
            }
        };
        
        wd = EasyMock.createNiceMock(WorkflowDescription.class);
        EasyMock.expect(wd.getConnections()).andReturn(connections).anyTimes();
        EasyMock.replay(wd);
    }

    /** Test. */
    @Test
    public void testPropertyChange() {
        
        ComponentDescription cd = EasyMock.createNiceMock(ComponentDescription.class);
        
        Object newValue = new EndpointChange(EndpointChange.Type.Remove, EndpointNature.Output, NAME,
            String.class.getCanonicalName(), NAME, String.class.getCanonicalName(), cd);
        
        PropertyChangeEvent propChangeEve = EasyMock.createNiceMock(PropertyChangeEvent.class);
        EasyMock.expect(propChangeEve.getNewValue()).andReturn(newValue).anyTimes();
        EasyMock.replay(propChangeEve);

        EndpointChangeListener listener = new EndpointChangeListener(wd);
        listener.propertyChange(propChangeEve);
        
        newValue = new EndpointChange(EndpointChange.Type.Change, EndpointNature.Output, NAME,
            String.class.getCanonicalName(), NAME, Integer.class.getCanonicalName(), cd);
        
        propChangeEve = EasyMock.createNiceMock(PropertyChangeEvent.class);
        EasyMock.expect(propChangeEve.getNewValue()).andReturn(newValue).anyTimes();
        EasyMock.replay(propChangeEve);
        
        listener.propertyChange(propChangeEve);
    }
    
}
