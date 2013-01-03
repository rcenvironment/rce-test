/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import de.rcenvironment.rce.component.workflow.Workflow;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowInformationImpl;
import de.rcenvironment.rce.component.workflow.WorkflowRegistry;


/**
 * Test case for {@link DistributedWorkflowRegistryImpl}.
 *
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class DistributedWorkflowRegistryTest {

    private final User pc1 = EasyMock.createNiceMock(User.class);
    private final User pc2 = EasyMock.createNiceMock(User.class);
    private final User pc3 = EasyMock.createNiceMock(User.class);
    
    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");
    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");
    private final PlatformIdentifier pi3 = PlatformIdentifierFactory.fromHostAndNumberString("notReachable:0");
    
    private final WorkflowDescription wd1 = new WorkflowDescription("wid1");
    private final WorkflowDescription wd2 = new WorkflowDescription("wid1");
    private final WorkflowDescription wd3 = new WorkflowDescription("wid2");

    private final WorkflowInformation wi1 = new WorkflowInformationImpl("wi1", "n1", wd1, pc1);
    private final WorkflowInformation wi2 = new WorkflowInformationImpl("wi2", "n2", wd2, pc2);
    private final WorkflowInformation wi3 = new WorkflowInformationImpl("wi3", "n3", wd3, pc3);
    
    private DistributedWorkflowRegistryImpl registry;
    private User user = EasyMock.createNiceMock(User.class);
    private Map<String, Object> config = new HashMap<String, Object>();
    
    /** Setup. */
    @Before
    public void setUp() {
        registry = new DistributedWorkflowRegistryImpl();
        registry.bindCommunicationService(new DummyCommunicationService());
        registry.bindWorkflowRegistry(new DummyWorkflowRegistryLocal());
        registry.activate(EasyMock.createNiceMock(BundleContext.class));
        wd1.setTargetPlatform(pi1);
        wd2.setTargetPlatform(pi2);
        wd3.setTargetPlatform(pi2);
    }

    /** Test. */
    @Test
    public void testGetAllWorkflowInformations() {
        Collection<WorkflowInformation> descriptions = registry.getAllWorkflowInformations(user, true);
        assertEquals(3, descriptions.size());
        assertTrue(descriptions.contains(wi1));
        assertTrue(descriptions.contains(wi2));
        assertTrue(descriptions.contains(wi3));
        
        descriptions = registry.getAllWorkflowInformations(user, false);
        assertEquals(3, descriptions.size());
        assertTrue(descriptions.contains(wi1));
        assertTrue(descriptions.contains(wi2));
        assertTrue(descriptions.contains(wi3));
        
        descriptions = registry.getAllWorkflowInformations(user, true);
        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains(wi1));
        assertTrue(descriptions.contains(wi3));
    }
    
    /** Test. */
    @Test
    public void testGetWorkflowInformations() {
        Collection<WorkflowInformation> informations = registry.getWorkflowInformations(user);
        assertEquals(1, informations.size());
        assertTrue(informations.contains(wi1));
        assertFalse(informations.contains(wi2));
        assertFalse(informations.contains(wi3));
    }
    
    /** Test. */
    @Test
    public void testCreateWorkflowInstance() {
        assertEquals(wi1, registry.createWorkflowInstance(user, wd1, wi1.getName(), config, wd1.getTargetPlatform()));
        assertEquals(wi2, registry.createWorkflowInstance(user, wd2, wi2.getName(), config, wd2.getTargetPlatform()));
        try {
            registry.createWorkflowInstance(user, wd2, wi2.getName(), config, pi3);
            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    /** Test. */
    @Test
    public void testDisposeWorkflowInstance() {
        try {
            registry.disposeWorkflowInstance(user, wi1.getIdentifier(), wd1.getTargetPlatform());
            fail();
        } catch (RuntimeException e) {
            assertEquals("PI1", e.getMessage());
        }
        try {
            registry.disposeWorkflowInstance(user, wi2.getIdentifier(), wd2.getTargetPlatform());
            fail();
        } catch (RuntimeException e) {
            assertEquals("PI2", e.getMessage());
        }
        registry.disposeWorkflowInstance(user, wi2.getIdentifier(), pi3);            

    }
    
    /** Test. */
    @Test
    public void testGetWorkflow() {     
        assertTrue(registry.getWorkflow(wi1) != null);
        assertNull(registry.getWorkflow(wi2));
    }
    
    /**
     * Test {@link CommunicationService} implementation.
     * @author Heinrich Wendel
     */
    private class DummyCommunicationService extends MockCommunicationService {
        
        private DummyWorkflowRegistryLocal registryLocal = new DummyWorkflowRegistryLocal();
        
        private DummyWorkflowRegistryRemote registryRemote = new DummyWorkflowRegistryRemote();
        
        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            Object service = null;
            if (platformIdentifier.equals(pi1) && iface == WorkflowRegistry.class) {
                service = registryLocal;
            } else if (platformIdentifier.equals(pi2) && iface == WorkflowRegistry.class) {
                service = registryRemote;
            } else if (platformIdentifier.equals(pi3) && iface == WorkflowRegistry.class) {
                service = createNullService(WorkflowRegistry.class);
            }
            return service;
        }

        @Override
        public Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier,
            BundleContext bundleContext) throws IllegalStateException {
            if (properties.get(WorkflowConstants.WORKFLOW_INSTANCE_ID_KEY).equals(wi1.getIdentifier())) {
                return EasyMock.createNiceMock(Workflow.class);
            } else {
                return null;                
            }
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
                return new HashSet<PlatformIdentifier>() { { add(pi1); add(pi2); } };
            }
            return null;
        }

    }

    /**
     * Test local {@link WorkflowRegistry}.
     * @author Heinrich Wendel
     */
    private class DummyWorkflowRegistryLocal implements WorkflowRegistry {

        @Override
        public WorkflowInformation createWorkflowInstance(User aUser, WorkflowDescription description, String name,
            Map<String, Object> configuration)
            throws AuthorizationException {
            if (description.equals(wd1) && aUser == user) {
                return wi1;
            } else {
                return null;                
            }
        }

        @Override
        public void disposeWorkflowInstance(User aUser, String instanceIdentifier) throws AuthorizationException {
            if (instanceIdentifier.equals(wi1.getIdentifier()) && aUser == user) {
                throw new RuntimeException("PI1");
            }
        }

        @Override
        public WorkflowInformation getWorkflowInformation(User aUser, String instanceIdentifier)
            throws AuthorizationException {
            if (instanceIdentifier.equals(wi1.getIdentifier()) && aUser == user) {
                return wi1;
            } else {
                return null;
            }
        }

        @SuppressWarnings("serial")
        @Override
        public Set<WorkflowInformation> getWorkflowInformations(User aUser) {
            if (aUser == user) {
                return new HashSet<WorkflowInformation>() { { add(wi1); } };
            } else {
                return null;                
            }
        }

        @Override
        public boolean isCreator(String instanceIdentifier, User proxyCertificate) throws AuthorizationException {
            return false;
        }

    }

    /**
     * Test remote {@link WorkflowRegistry}.
     * @author Heinrich Wendel
     */
    private class DummyWorkflowRegistryRemote implements WorkflowRegistry {

        private boolean getWorkflowInformationsCalled = false;
        
        @Override
        public WorkflowInformation createWorkflowInstance(User aUser, WorkflowDescription description, String name,
            Map<String, Object> configuration)
            throws AuthorizationException {
            if (description.equals(wd2) && aUser == user) {
                return wi2;
            } else {
                return null;                
            }
        }

        @Override
        public void disposeWorkflowInstance(User aUser, String instanceIdentifier) throws AuthorizationException {
            if (instanceIdentifier.equals(wi2.getIdentifier()) && aUser == user) {
                throw new RuntimeException("PI2");
            }
        }

        @Override
        public WorkflowInformation getWorkflowInformation(User aUser, String instanceIdentifier)
            throws AuthorizationException {
            if (instanceIdentifier.equals(wi2.getIdentifier()) && aUser == user) {
                return wi2;
            } else {
                return null;
            }
        }

        @SuppressWarnings("serial")
        @Override
        public Set<WorkflowInformation> getWorkflowInformations(User aUser) {
            if (!getWorkflowInformationsCalled) {
                getWorkflowInformationsCalled = true;
                return new HashSet<WorkflowInformation>() { { add(wi2); add(wi3); } };
            } else {
                return new HashSet<WorkflowInformation>() { { add(wi3); } };
            }
        }

        @Override
        public boolean isCreator(String instanceIdentifier, User aUser) throws AuthorizationException {
            return false;
        }

    }

}
