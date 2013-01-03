/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.testutils.MockDistributedComponentRegistry;

/**
 * Test cases for {@link SimpleComponentRegistry}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class SimpleComponentRegistryTest {

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");
    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");
    
    private ComponentDescription cd1;
    private ComponentDescription cd2;
    private ComponentDescription cd3;
    
    private final String compId1 = "cId1";
    private final String compId3 = "cId3";

    private ComponentInstanceDescriptor ci1;
    private ComponentInstanceDescriptor ci2;
    private ComponentInstanceDescriptor ci3;
    
    private ComponentContext ctx;
    
    private SimpleComponentRegistry registry;

    /** Setup. */
    @Before
    public void setUp() {
        
        User user = EasyMock.createNiceMock(User.class);
        ctx = EasyMock.createNiceMock(ComponentContext.class);
        
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
        
        ci1 = new ComponentInstanceDescriptor("ci1", "n1", pi1, "w1", "cid1", "ei", new HashSet<PlatformIdentifier>());
        ci2 = new ComponentInstanceDescriptor("ci2", "n2", pi2, "w2", "cid2", "au", new HashSet<PlatformIdentifier>());
        ci3 = new ComponentInstanceDescriptor("ci3", "n3", pi2, "w3", "cid2", "aeu", new HashSet<PlatformIdentifier>());
        
        registry = new SimpleComponentRegistry(user);
        registry.bindDistributedComponentRegistry(new DummyDistributedComponentRegistry());
    }

    /** Test. */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetAllComponentDescriptions() {
        List<ComponentDescription> descs = registry.getAllComponentDescriptions();
        assertEquals(2, descs.size());
        assertTrue(descs.contains(cd1));
        assertTrue(descs.contains(cd3));
        
        descs = registry.getAllComponentDescriptions(false);
        assertEquals(2, descs.size());
        assertTrue(descs.contains(cd1));
        assertTrue(descs.contains(cd3));
        
        descs = registry.getAllComponentDescriptions(true);
        assertEquals(3, descs.size());
        assertTrue(descs.contains(cd1));
        assertTrue(descs.contains(cd2));
        assertTrue(descs.contains(cd3));

    }

    /** Test. */
    @Test
    public void testGetComponentDescription() {
        ComponentDescription desc = registry.getComponentDescription(cd1.getIdentifier());
        assertEquals(desc.getIdentifier(), cd1.getIdentifier());
        assertNotSame(desc, cd1);
        assertNull(registry.getComponentDescription("123"));
    }

    /**
     * Test.
     * @throws ComponentException if an error occurs.
     **/
    @Test
    public void testCreateComponentInstance() throws ComponentException {
        ComponentInstanceDescriptor ciDesc = registry.createComponentInstance(cd1, "namen sind schall und rauch", ctx, true, pi1);
        assertEquals(ciDesc, ci1);
        assertNull(registry.createComponentInstance(cd3, "kleider machen leute", ctx, true, pi1));
    }

    /** Test. */
    @Test
    public void testDisposeComponentInstance() {
        registry.disposeComponentInstance(cd1.getIdentifier(), pi1);
    }

    /** Test. */
    @Test
    public void testGetComponentInformation() {
        ComponentInstanceDescriptor ciDesc = registry.getComponentInstanceDescriptor(ci1.getIdentifier(), pi1);
        assertEquals(ci1, ciDesc);
        assertNull(registry.getComponentInstanceDescriptor(ci3.getIdentifier(), pi1));
    }

    /** Test. */
    @Test
    public void testUnbindedService() {
        registry.unbindDistributedComponentRegistry(new DummyDistributedComponentRegistry());
        try {
            registry.disposeComponentInstance(cd1.getIdentifier(), pi1);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    /**
     * Dummy implementation for the {@link DistributedComponentRegistry}.
     *
     * @author Heinrich Wendel
     */
    private class DummyDistributedComponentRegistry extends MockDistributedComponentRegistry {

        private boolean called = false;
        
        @Override
        public ComponentInstanceDescriptor createComponentInstance(User proxyCertificate, ComponentDescription description,
            String name, ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException {
            
            ComponentInstanceDescriptor ci = null;
            if (description.equals(cd1) && platformId.equals(pi1)) {
                ci = ci1;
            } else if (description.equals(cd2) && platformId.equals(pi2)) {
                ci = ci2;
            } else if (description.equals(cd3) && platformId.equals(pi2)) {
                ci = ci3;
            }
            return ci;
        }

        @SuppressWarnings("serial")
        @Override
        public List<ComponentDescription> getAllComponentDescriptions(User certificate, boolean forceRefresh) {
            List<ComponentDescription> freshDescs = new ArrayList<ComponentDescription>() {

                {
                    add(cd1);
                    add(cd2);
                    add(cd3);
                }
            };
            
            List<ComponentDescription> cachedDescs = new ArrayList<ComponentDescription>() {

                {
                    add(cd1);
                    add(cd3);
                }
            };
            
            if (!called) {
                called = true;
                return cachedDescs;
            } else {
                if (forceRefresh) {
                    return freshDescs;
                } else {
                    return cachedDescs;
                }
            }

        }

        @Override
        public ComponentInstanceDescriptor getComponentInstanceDescriptor(
            User certificate, String instanceId, PlatformIdentifier platformId) {
            
            ComponentInstanceDescriptor ci = null;
            if (instanceId.equals(ci1.getIdentifier()) && platformId.equals(pi1)) {
                ci = ci1;
            } else if (instanceId.equals(ci2.getIdentifier()) && platformId.equals(pi2)) {
                ci = ci2;
            } else if (instanceId.equals(ci3.getIdentifier()) && platformId.equals(pi2)) {
                ci = ci3;
            }
            return ci;
        }

    }

}
