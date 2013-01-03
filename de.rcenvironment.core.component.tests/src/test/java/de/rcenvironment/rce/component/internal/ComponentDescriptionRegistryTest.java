/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;

/**
 * Test cases for {@link ComponentDescriptionRegistry}.
 * 
 * @author Doreen Seider
 */
public class ComponentDescriptionRegistryTest {

    private ComponentDescriptionRegistry localRegistry = new ComponentDescriptionRegistry();

    private ComponentFactory componentFactory;

    private ComponentDescription componentDescription;

    /** Set up. */
    @Before
    public void setUp() {
        
        componentFactory = new ComponentFactory() {

            @SuppressWarnings("rawtypes")
            @Override
            public ComponentInstance newInstance(Dictionary arg0) {
                return null;
            }
        };
        
        componentDescription = MockComponentStuffFactory.createComponentDescription();
    }

    /** Tear down. */
    @After
    public void tearDown() {
        
        componentFactory = new ComponentFactory() {

            @SuppressWarnings("rawtypes")
            @Override
            public ComponentInstance newInstance(Dictionary arg0) {
                return null;
            }
        };

        localRegistry.removeAllComponents();
    }

    /** Test. */
    @Test
    public void testAddComponent() {
        localRegistry.addComponent(componentDescription, componentFactory);
    }

    /** Test. */
    @Test
    public void testGetComponentDescription() {
        localRegistry.addComponent(componentDescription, componentFactory);
        ComponentDescription desc = localRegistry.getComponentDescription(MockComponentStuffFactory.COMPONENT_IDENTIFIER);
        assertNotSame(componentDescription, desc);
        assertTrue(componentDescription.getIdentifier().equals(desc.getIdentifier()));
    }

    /** Test. */
    @Test
    public void testGetComponentFactory() {
        localRegistry.addComponent(componentDescription, componentFactory);
        assertTrue(localRegistry.getComponentFactory(MockComponentStuffFactory.COMPONENT_IDENTIFIER).equals(componentFactory));
    }

    /** Test. */
    @Test
    public void testGetComponents() {

        ComponentDescription cd1 = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd1.getIdentifier()).andReturn("id1");
        EasyMock.replay(cd1);

        localRegistry.addComponent(componentDescription, componentFactory);
        localRegistry.addComponent(cd1, componentFactory);

        assertEquals(2, localRegistry.getComponentDescriptions().size());

        localRegistry.removeAllComponents();
        assertEquals(0, localRegistry.getComponentDescriptions().size());
    }

    /** Test. */
    @Test
    public void testRemoveComponent() {
        localRegistry.addComponent(componentDescription, componentFactory);
        assertEquals(componentDescription.getIdentifier(), localRegistry.removeComponent(componentFactory));
        assertNull(localRegistry.getComponentDescription(MockComponentStuffFactory.COMPONENT_IDENTIFIER));
    }

}
