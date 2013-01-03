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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Test cases for {@link SimpleWorkflowRegistry}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class SimpleWorkflowRegistryTest {
    
    private final User user = EasyMock.createNiceMock(User.class);

    private final PlatformIdentifier pi1 = PlatformIdentifierFactory.fromHostAndNumberString("localhost:0");
    private final PlatformIdentifier pi2 = PlatformIdentifierFactory.fromHostAndNumberString("remoteHost:0");

    private final WorkflowDescription wd1 = new WorkflowDescription("wd1");
    private final WorkflowDescription wd2 = new WorkflowDescription("wd2");
    private final WorkflowDescription wd3 = new WorkflowDescription("wd3");

    private final WorkflowInformationImpl wi1 = new WorkflowInformationImpl("wi1", "n1", wd1, user);
    private final WorkflowInformationImpl wi2 = new WorkflowInformationImpl("wi2", "n2", wd2, user);
    private final WorkflowInformationImpl wi3 = new WorkflowInformationImpl("wi3", "n3", wd3, user);

    private ComponentInstanceDescriptor cid;
    private final String cidName = "wow";
    private final String cidId = "toll";
    
    private final Map<String, Object> config = new HashMap<String, Object>();
    
    private SimpleWorkflowRegistry registry;

    private LinkedBlockingQueue<Input> queue;
    

    /** Set up. */
    @Before
    public void setUp() {
        registry = new SimpleWorkflowRegistry(user);
        registry.bindDistributedWorkflowRegistry(new DummyDistributedWorkflowRegistry());
        
        cid = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(cid.getName()).andReturn(cidName).anyTimes();
        EasyMock.expect(cid.getComponentIdentifier()).andReturn(cidId).anyTimes();
        EasyMock.replay(cid);
    }

    /** Test. */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetAllWorkflowInformations() {
        Set<WorkflowInformation> informations = registry.getAllWorkflowInformations();
        assertEquals(3, informations.size());
        
        informations = registry.getAllWorkflowInformations(false);
        assertEquals(3, informations.size());
        
        informations = registry.getAllWorkflowInformations(true);
        assertEquals(2, informations.size());
    }

    /** Test. */
    @Test
    public void testGetWorkflowInformation() {
        WorkflowInformation wi = registry.getWorkflowInformation(wi1.getIdentifier(), false);
        assertEquals(wi, wi1);
        wi = registry.getWorkflowInformation("abc", false);
        assertNull(wi);
    }
    
    /** Test. */
    @Test
    public void testCreateWorkflowInstance() {
        wd1.setTargetPlatform(pi1);
        WorkflowInformation information = registry.createWorkflowInstance(wd1, wi1.getName(), config);
        assertEquals(information, wi1);
        wd3.setTargetPlatform(pi1);
        assertNull(registry.createWorkflowInstance(wd3, wi3.getName(), config));
    }

    /** Test. */
    @Test
    public void testDisposeWorkflowInstance() {
        registry.disposeWorkflowInstance(wi1);
    }
    
    /** Test. */
    @Test
    public void testStartWorkflow() {
        registry.startWorkflow(wi1);
    }
    
    /** Test. */
    @Test
    public void testPauseWorkflow() {
        registry.pauseWorkflow(wi1);
    }
    
    /** Test. */
    @Test
    public void testResumeWorkflow() {
        registry.resumeWorkflow(wi1);
    }
    
    /** Test. */
    @Test
    public void testCancelWorkflow() {
        registry.cancelWorkflow(wi1);
    }
    
    /** Test. */
    @Test
    public void testDisposeWorkflow() {
        registry.disposeWorkflow(wi1);
    }

    /** Test. */
    @Test
    public void testCancelWorkflows() {
        registry.cancelActiveWorkflows();
    }

    /** Test. */
    @Test
    public void testDisposeWorkflows() {
        registry.disposeWorkflows();
    }
    
    /** Test. */
    @Test
    public void testHasActiveWorkflows() {
        assertTrue(registry.hasActiveWorkflows());
        registry.disposeWorkflow(wi1);
        assertFalse(registry.hasActiveWorkflows());
    }
    
    /** Test. */
    @Test
    public void testGetStateOfWorkflow() {
        assertEquals(WorkflowState.RUNNING, registry.getStateOfWorkflow(wi1));
        try {
            registry.getStateOfWorkflow(wi2);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
        assertEquals(WorkflowState.CANCELED, registry.getStateOfWorkflow(wi3));
    }
    
    /** Test. */
    @Test
    public void testGetComponentDescriptors() {
        assertNotNull(registry.getComponentInstanceDescriptors(wi1));
        assertEquals(0, registry.getComponentInstanceDescriptors(wi2).size());
    }
    
    /** Test. */
    @Test
    public void testGetComponentDescriptor() {
        WorkflowNode node = new WorkflowNode(EasyMock.createNiceMock(ComponentDescription.class));
        ComponentDescription cd = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd.getIdentifier()).andReturn(cidId).anyTimes();
        EasyMock.replay(cd);
        node.setComponentDescription(cd);
        node.setName(cidName);
        wi1.getWorkflowDescription().addWorkflowNode(node);
        assertNotNull(registry.getComponentInstanceDescriptor(node, wi1));
    }
    
    /** Test. */
    @Test
    public void testInputs() {
        queue = new LinkedBlockingQueue<Input>();
        registry.setInputs(wi1, cidId, queue);
        assertNotNull(registry.getInputs(wi1, cidId));
    }
    
    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testIfRequiredServiceIsNotAvailable() {
        registry.unbindDistributedWorkflowRegistry(null);
        registry.disposeWorkflow(wi1);
    }

    /**
     * Dummy implementation for the {@link DistributedWorkflowRegistry}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedWorkflowRegistry implements DistributedWorkflowRegistry {

        private boolean getAllWorkflowInforationsCalled = false;
        
        @SuppressWarnings("serial")
        private Set<WorkflowInformation> cachedWis = new HashSet<WorkflowInformation>() {
            {
                add(wi1);
                add(wi2);
                add(wi3);
            }
        };
        
        @SuppressWarnings("serial")
        private Set<WorkflowInformation> freshWis = new HashSet<WorkflowInformation>() {
            {
                add(wi1);
                add(wi3);
            }
        };
            
        @Override
        public WorkflowInformation createWorkflowInstance(User aUser, WorkflowDescription workflowDescription,
            String name, Map<String, Object> configuration, PlatformIdentifier platformIdentifier) {

            WorkflowInformation wi = null;
            if (workflowDescription.getIdentifier().equals(wd1.getIdentifier()) && platformIdentifier.equals(pi1)) {
                wi = wi1;
            } else if (workflowDescription.getIdentifier().equals(wd2.getIdentifier()) && platformIdentifier.equals(pi2)) {
                wi = wi2;
            }
            return wi;
        }

        @Override
        public void disposeWorkflowInstance(User aUser, String identifier, PlatformIdentifier platformIdentifier) {
            Set<WorkflowInformation> newWis = new HashSet<WorkflowInformation>();
            
            for (WorkflowInformation wi : cachedWis) {
                if (!wi.getIdentifier().equals(identifier)) {
                    newWis.add(wi);
                }
            }
            cachedWis = newWis;
        }
        
        @Override
        public Set<WorkflowInformation> getAllWorkflowInformations(User aUser, boolean forceRefresh) {
            if (!getAllWorkflowInforationsCalled) {
                getAllWorkflowInforationsCalled = true;
                return cachedWis;
            }
            if (forceRefresh) {
                return freshWis;                
            } else {
                return cachedWis;
            }
        }

        @Override
        public Workflow getWorkflow(WorkflowInformation workflowInformation) {
            Workflow w = null;
            if (workflowInformation.equals(wi1)) {
                w = EasyMock.createNiceMock(Workflow.class);
                EasyMock.expect(w.getState(user)).andReturn(WorkflowState.RUNNING).anyTimes();
                Set<ComponentInstanceDescriptor> cids = new TreeSet<ComponentInstanceDescriptor>();
                cids.add(cid);
                EasyMock.expect(w.getComponentInstanceDescriptors(user)).andReturn(cids).anyTimes();
                EasyMock.expect(w.getInputs(user, cidId)).andReturn(queue).anyTimes();
                EasyMock.replay(w);
                
            } else if (workflowInformation.equals(wi2)) {
                w = EasyMock.createNiceMock(Workflow.class);
                EasyMock.expect(w.getState(user)).andThrow(new UndeclaredThrowableException(new RuntimeException())).anyTimes();
                EasyMock.expect(w.getComponentInstanceDescriptors(user))
                    .andThrow(new UndeclaredThrowableException(new RuntimeException())).anyTimes();
                EasyMock.replay(w);
            } else if (workflowInformation.equals(wi3)) {
                w = EasyMock.createNiceMock(Workflow.class);
                EasyMock.expect(w.getState(user)).andReturn(WorkflowState.CANCELED).anyTimes();
                EasyMock.replay(w);
            }
            return w;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<WorkflowInformation> getWorkflowInformations(User aUser) {
            return new HashSet<WorkflowInformation>(cachedWis) {
                {
                    remove(wi2);
                }
            };
        }

    }

}
