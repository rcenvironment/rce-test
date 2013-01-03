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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.component.DistributedComponentRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Test cases for {@link WorkflowRegistryImpl}.
 * 
 * @author Doreen Seider
 * @author Jens Ruehmkorf
 */
public class WorkflowRegistryImplTest {
    
    private static final String BUNDLE_SYMBOLIC_NAME = "de.rce.comp.id";
    
    private static final String ID = "paddel";
    
    private WorkflowRegistryImpl registry;

    private User user;

    private User anotherUser;
    
    private User invalidCert;

    private WorkflowDescription workflowDesc;
    
    private Map<String, Object> config = new HashMap<String, Object>();

    /** Set up. */
    @Before
    public void setUp() {
        Bundle bundle = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.replay(bundle);
        
        BundleContext context = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(context.getBundle()).andReturn(bundle).anyTimes();
        EasyMock.replay(context);

        registry = new WorkflowRegistryImpl();
        
        registry.bindCommunicationService(EasyMock.createNiceMock(CommunicationService.class));
        registry.bindDistributedComponentRegistry(EasyMock.createNiceMock(DistributedComponentRegistry.class));
        registry.bindDistributedNotificationService(EasyMock.createNiceMock(DistributedNotificationService.class));
        registry.activate(context);
        
        user = EasyMock.createNiceMock(User.class);
        EasyMock.expect(user.isValid()).andReturn(true).anyTimes();
        EasyMock.expect(user.same(user)).andReturn(true).anyTimes();
        EasyMock.replay(user);
        anotherUser = EasyMock.createNiceMock(User.class);
        EasyMock.expect(anotherUser.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(anotherUser);
        invalidCert = EasyMock.createNiceMock(User.class);
        
        workflowDesc = EasyMock.createNiceMock(WorkflowDescription.class);
        EasyMock.expect(workflowDesc.getIdentifier()).andReturn(ID).anyTimes();
        EasyMock.expect(workflowDesc.clone(user)).andReturn(workflowDesc).anyTimes();
        EasyMock.expect(workflowDesc.clone(anotherUser)).andReturn(workflowDesc).anyTimes();
        EasyMock.replay(workflowDesc);
    }

    /** Test. */
    @Test
    public void testCreateInstance() {

        WorkflowInformation wi = registry.createWorkflowInstance(user, workflowDesc, "a", config);
        
        assertNotNull(wi);
        assertNotNull(wi.getIdentifier());
        assertEquals(workflowDesc.getIdentifier(), wi.getWorkflowDescription().getIdentifier());

        // invalid certificate
        try {
            registry.createWorkflowInstance(invalidCert, wi.getWorkflowDescription(), "g", config);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /** Test. */
    @Test
    public void testDisposeInstance() {

        WorkflowInformation wi = registry.createWorkflowInstance(user, workflowDesc, "h", config);
        String id = wi.getIdentifier();
        
        // invalid certificate
        try {
            registry.disposeWorkflowInstance(invalidCert, id);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        
        // NoRights
        try {
            wi = registry.createWorkflowInstance(user, workflowDesc, "j", config);
            registry.disposeWorkflowInstance(anotherUser, wi.getIdentifier());
            fail();
        } catch (AuthorizationException e) {
            assertTrue(true);
        }

        registry.disposeWorkflowInstance(user, "bla24");
    }

    /** Test. */
    @Test
    public void testGetInstanceInformations() {

        Collection<WorkflowInformation> informations = registry.getWorkflowInformations(user);
        int sizeBefore = informations.size();
        WorkflowInformation wi1 = registry.createWorkflowInstance(user, workflowDesc, "k", config);
        WorkflowInformation wi2 = registry.createWorkflowInstance(anotherUser, workflowDesc, "l", config);
        informations = registry.getWorkflowInformations(user);

        assertEquals(sizeBefore + 1, informations.size());
        assertTrue(informations.contains(wi1));
        assertFalse(informations.contains(wi2));
        
        // invalid certificate
        try {
            registry.getWorkflowInformations(invalidCert);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    
    /** Test. */
    @Test
    public void testGetInstanceInformation() {
        // invalid certificate
        try {
            registry.createWorkflowInstance(invalidCert, workflowDesc, "m", config);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        
        WorkflowInformation wi = registry.createWorkflowInstance(user, workflowDesc, "n", config);
        assertEquals(wi, registry.getWorkflowInformation(user, wi.getIdentifier()));
        
        // no rights
        try {
            registry.getWorkflowInformation(anotherUser, wi.getIdentifier());
            fail();
        } catch (AuthorizationException e) {
            assertTrue(true);
        }
    }

    /** Test. */
    @Test
    public void testIsCreator() {
        WorkflowInformation wi = registry.createWorkflowInstance(user, workflowDesc, "o", config);

        assertTrue(registry.isCreator(wi.getIdentifier(), user));
        assertFalse(registry.isCreator(wi.getIdentifier(), anotherUser));

        // invalid certificate
        try {
            registry.isCreator(wi.getIdentifier(), invalidCert);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}
