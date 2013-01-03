/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentRegistry;
import de.rcenvironment.rce.component.DeclarativeComponentDescription;

/**
 * Test cases for {@link DistributedComponentRegistryImpl}.
 * 
 * @author Heinrich Wendel
 */
public class DistributedComponentRegistryImplTest {

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");
    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");
    private final PlatformIdentifier pi3 = PlatformIdentifierFactory.fromHostAndNumberString("notReachable:0");
    
    private ComponentDescription cd1;
    private ComponentDescription cd2;
    private ComponentDescription cd3;
    
    private ComponentInstanceDescriptor ci1;
    private ComponentInstanceDescriptor ci2;
    
    private ComponentContext ctx;
    
    private DistributedComponentRegistryImpl registry;
    private User user = EasyMock.createNiceMock(User.class);
    
    private BundleContext bundleCtx = EasyMock.createNiceMock(BundleContext.class);
    
    /** Set up. */
    @Before
    public void setUp() {
        String name = "Mainzelmann";
        String group = "Mainz";
        String version = "version";
        Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();
        byte[] icon16 = new byte[10];
        byte[] icon32 = new byte[0];
        
        DeclarativeComponentDescription declarativeCD1 = new DeclarativeComponentDescription(String.class.getCanonicalName() + "_cid1",
            name, group, version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        DeclarativeComponentDescription declarativeCD2 = new DeclarativeComponentDescription(String.class.getCanonicalName() + "_cid1",
            name, group, version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        DeclarativeComponentDescription declarativeCD3 = new DeclarativeComponentDescription(Long.class.getCanonicalName() + "_cid2",
            name, group, version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
        
        cd1 = new ComponentDescription(declarativeCD1);
        cd1.setPlatform(pi1);
        cd2 = new ComponentDescription(declarativeCD2);
        cd2.setPlatform(pi2);
        cd3 = new ComponentDescription(declarativeCD3);
        cd3.setPlatform(pi2);
        
            
        ci1 = new ComponentInstanceDescriptor("wurschtel", "n/a",
            PlatformIdentifierFactory.fromHostAndNumberString("knorke:0"), "format c:", "rundes Tool",
            "runder workflow", new HashSet<PlatformIdentifier>());
        ci2 = new ComponentInstanceDescriptor("friggel", "n/a",
            PlatformIdentifierFactory.fromHostAndNumberString("knorke:0"), "format c:", "spitzen Tool",
            "spitzen workflow", new HashSet<PlatformIdentifier>());
        
        ctx = EasyMock.createMock(ComponentContext.class);
        registry = new DistributedComponentRegistryImpl();
        registry.bindCommunicationService(new DummyCommunicationService());
        registry.bindPlatformService(new DummyPlatformService());
        registry.activate(bundleCtx);
    }

    /** Test. */
    @Test
    public void testGetAllComponentDescriptions() {
        Collection<ComponentDescription> descriptions = registry.getAllComponentDescriptions(user, false);
        assertEquals(3, descriptions.size());
        assertTrue(descriptions.contains(cd1));
        assertTrue(descriptions.contains(cd2));
        assertTrue(descriptions.contains(cd3));
        
        descriptions = registry.getAllComponentDescriptions(user, false);
        assertEquals(3, descriptions.size());
        assertTrue(descriptions.contains(cd1));
        assertTrue(descriptions.contains(cd2));
        assertTrue(descriptions.contains(cd3));
        
        descriptions = registry.getAllComponentDescriptions(user, true);
        assertEquals(3, descriptions.size());
        assertTrue(descriptions.contains(cd1));
        assertTrue(descriptions.contains(cd2));
    }

    /**
     * Test.
     * @throws ComponentException if an error occurs.
     */
    @Test
    public void testCreateComponentInstance() throws ComponentException {
        assertEquals(ci1, registry.createComponentInstance(user, cd1, cd1.getName(), 
            ctx, true, cd1.getPlatform()));
        assertEquals(ci2, registry.createComponentInstance(user, cd2, cd2.getName(), 
            ctx, true, cd2.getPlatform()));
        try {
            registry.createComponentInstance(user, cd2, cd3.getName(), ctx, true, pi3);
            fail();
        } catch (ComponentException e) {
            assertTrue(true);
        }
    }

    /** Test. */
    @Test
    public void testDisposeComponentInstance() {
        try {
            registry.disposeComponentInstance(user, ci1.getIdentifier(), cd1.getPlatform());
            fail();
        } catch (RuntimeException e) {
            assertEquals("PI1", e.getMessage());
        }
        try {
            registry.disposeComponentInstance(user, ci2.getIdentifier(), cd2.getPlatform());
            fail();
        } catch (RuntimeException e) {
            assertEquals("PI2", e.getMessage());
        }
        registry.disposeComponentInstance(user, ci2.getIdentifier(), pi3);
    }

    /** Test. */
    @Test
    public void testGetComponentInformation() {
        assertEquals(ci1, registry.getComponentInstanceDescriptor(user, ci1.getIdentifier(), cd1.getPlatform()));
        assertEquals(ci2, registry.getComponentInstanceDescriptor(user, ci2.getIdentifier(), cd2.getPlatform()));
        assertNull(registry.getComponentInstanceDescriptor(user, ci2.getIdentifier(), pi3));
    }

    /** Test. */
    private class DummyCommunicationService extends MockCommunicationService {
        
        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            Object service = null;
            if (bundleContext == bundleCtx) {
                if (platformIdentifier.equals(pi1) && iface == ComponentRegistry.class) {
                    service = new DummyComponentRegistryLocal();
                } else if (platformIdentifier.equals(pi2) && iface == ComponentRegistry.class) {
                    service = new DummyComponentRegistryRemote();
                } else if (platformIdentifier.equals(pi3) && iface == ComponentRegistry.class) {
                    service = createNullService(ComponentRegistry.class);
                }                
            }
            return service;
        }

        @SuppressWarnings("unchecked")
        private <T> T createNullService(final Class<T> iface) {
            return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface },
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
                        throw new UndeclaredThrowableException(new RuntimeException("Service not available"));
                    }
                }
            );
        }

        @SuppressWarnings("serial")
        @Override
        public Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
            if (!forceRefresh) {
                return new HashSet<PlatformIdentifier>() { { add(pi1); add(pi2); add(pi3); } };                
            }
            return null;
        }

    }

    /**
     * Test local {@link ComponentRegistry} implementation.
     * @author Heinrich Wendel
     */
    private class DummyComponentRegistryLocal implements ComponentRegistry {

        @Override
        public ComponentInstanceDescriptor createComponentInstance(User aUser, ComponentDescription description,
            ComponentContext context, String name, Boolean inputsConnected) throws AuthorizationException {
            if (description.equals(cd1) && aUser == user) {
                return ci1;
            }
            return null;
        }

        @Override
        public void disposeComponentInstance(User aUser, String instanceIdentifier) throws AuthorizationException {
            if (instanceIdentifier.equals(ci1.getIdentifier()) && aUser == user) {
                throw new RuntimeException("PI1");
            }
        }

        @Override
        public ComponentDescription getComponentDescription(User aUser, String descriptionIdentifier)
            throws AuthorizationException {
            return null;
        }

        @SuppressWarnings({ "serial", "unchecked", "rawtypes" })
        @Override
        public Set<ComponentDescription> getComponentDescriptions(User aUser) {
            throw new IllegalAccessError();
        }

        @Override
        public ComponentInstanceDescriptor getComponentInstanceDescriptor(User aUser, String instanceIdentifier)
            throws AuthorizationException {
            if (instanceIdentifier.equals(ci1.getIdentifier()) && aUser == user) {
                return ci1;
            } else {
                return null;
            }
        }

        @Override
        public boolean isCreator(String instanceIdentifier, User aUser) throws AuthorizationException {
            return false;
        }

        @Override
        public Set<ComponentDescription> getComponentDescriptions(User aUser, PlatformIdentifier requestingPlatform) {
            if (aUser == user) {
                return new HashSet() { { add(cd1); } };
            }
            return null;
        }

    }

    /**
     * Test remote {@link ComponentRegistry} implementation.
     * @author Heinrich Wendel
     */
    private class DummyComponentRegistryRemote implements ComponentRegistry {

        private boolean getComponentDescriptionsCalled = false;
        @Override
        public ComponentInstanceDescriptor createComponentInstance(User aUser, ComponentDescription description,
            ComponentContext context, String name, Boolean inputConnected) throws ComponentException {
            if (description.equals(cd2) && aUser == user) {
                return ci2;
            }
            return null;
        }

        @Override
        public void disposeComponentInstance(User aUser, String instanceIdentifier) throws AuthorizationException {
            if (instanceIdentifier.equals(ci2.getIdentifier()) && aUser == user) {
                throw new RuntimeException("PI2");
            }
        }

        @Override
        public ComponentDescription getComponentDescription(User aUser, String descriptionIdentifier)
            throws AuthorizationException {
            return null;
        }

        @Override
        @SuppressWarnings({ "serial", "unchecked", "rawtypes" })
        public Set<ComponentDescription> getComponentDescriptions(User aUser) {
            throw new IllegalAccessError();
        }

        @Override
        public ComponentInstanceDescriptor getComponentInstanceDescriptor(User aUser, String instanceIdentifier)
            throws AuthorizationException {
            if (instanceIdentifier.equals(ci2.getIdentifier()) && aUser == user) {
                return ci2;
            } else {
                return null;
            }
        }

        @Override
        public boolean isCreator(String instanceIdentifier, User aUser) throws AuthorizationException {
            return false;
        }

        @Override
        public Set<ComponentDescription> getComponentDescriptions(User aUser, PlatformIdentifier requestingPlatform) {
            if (aUser == user) {
                if (!getComponentDescriptionsCalled) {
                    return new HashSet() { { add(cd2); add(cd3); } };                    
                } else {
                    return new HashSet() { { add(cd2); } };
                }
            }
            return null;
        }

    }
    
    /**
     * Test {@link PlatformService} implementation.
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {
        
        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi1;
        }
    }
}
