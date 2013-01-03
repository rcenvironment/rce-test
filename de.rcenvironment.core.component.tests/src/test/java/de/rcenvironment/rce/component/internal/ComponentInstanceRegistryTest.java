/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentInstance;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;

/**
 * Test cases for {@link ComponentInstanceRegistry}.
 * 
 * @author Doreen Seider
 */
public class ComponentInstanceRegistryTest {

    private ComponentInstanceRegistry localRegistry = new ComponentInstanceRegistry();

    private User user;
    
    private ComponentInstance compInstance;
    
    private ComponentInstanceDescriptor compInstDesc;
    
    /** set up. */
    @Before
    public void setUp() {
        user = EasyMock.createNiceMock(User.class);        
        compInstDesc = MockComponentStuffFactory.createComponentInstanceDescriptor();

        compInstance = new ComponentInstance() {
            
            @Override
            public Object getInstance() {
                return null;
            }
            
            @Override
            public void dispose() {
            }
        };
    }
    
    /** tear down. */
    @After
    public void tearDown() {
        localRegistry.removeComponentInstance(MockComponentStuffFactory.COMPONENT_INSTANCE_IDENTIFIER);
    }
    
    /** Test. */
    @Test
    public void testAddComponentInstance() {
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        localRegistry.removeComponentInstance(compInstDesc.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testGetComponentInstance() {
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        assertTrue(localRegistry.getComponentInstance(MockComponentStuffFactory.COMPONENT_INSTANCE_IDENTIFIER).equals(compInstance));
        localRegistry.removeComponentInstance(compInstDesc.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testGetComponentInstanceDescriptor() {
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        assertEquals(compInstDesc, localRegistry.getComponentInstanceDescriptor(MockComponentStuffFactory.COMPONENT_INSTANCE_IDENTIFIER));
        localRegistry.removeComponentInstance(compInstDesc.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testGetAllComponentInstanceDescriptors() {
        assertEquals(0, localRegistry.getAllComponentInstanceDescriptors().size());
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        assertEquals(1, localRegistry.getAllComponentInstanceDescriptors().size());
        assertTrue(localRegistry.getAllComponentInstanceDescriptors().contains(compInstDesc));
    }
    
    /** Test. */
    @Test
    public void testRemoveComponentInstance() {
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        localRegistry.removeComponentInstance(MockComponentStuffFactory.COMPONENT_INSTANCE_IDENTIFIER);
    }
    
    /** Test. */
    @Test
    public void testIsCreator() {
        localRegistry.addCompControllerInstance(compInstance, compInstDesc, user);
        assertTrue(localRegistry.isCreator(compInstDesc.getIdentifier(), user));

        User notEqualCert = EasyMock.createNiceMock(User.class);

        assertFalse(localRegistry.isCreator(compInstDesc.getIdentifier(), notEqualCert));
        assertFalse(localRegistry.isCreator("unknown id", user));
        
    }
}
