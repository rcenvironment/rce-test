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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;

/**
 * Test cases for {@link WorkflowInformationImpl}.
 * 
 * @author Doreen Seider
 */
public class WorkflowInformationImplTest {
    
    private WorkflowInformationImpl information;
    
    private final String id = "id";
    
    private final String userID = "userID";
    
    private final String name = "name";
    
    private User pc;
    
    private final String addInfo = "geilomat, ey";
    
    private WorkflowDescription wd;
    
    private ComponentDescription cd;
    
    private PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("elephant:7");

    /** Set up. */
    @Before
    public void setUp() {
        createComponentDescriptionMock();
        createTestWorkflowDescription();
        pc = EasyMock.createNiceMock(User.class);
        EasyMock.expect(pc.getUserId()).andReturn(userID).anyTimes();
        EasyMock.replay(pc);
        information = new WorkflowInformationImpl(id, name, wd, pc);
    }

    /** Test. */
    @Test
    public void testConstructor() {
        new WorkflowInformationImpl(id, null, wd, EasyMock.createNiceMock(User.class));
    }
    
    /** Test. */
    @Test
    public void testGetIdentifier() {
        assertEquals(id, information.getIdentifier());
    }
    
    /** Test. */
    @Test
    public void testGetName() {
        assertEquals(name, information.getName());
    }

    /** Test. */
    @Test
    public void testGetUser() {
        assertEquals(userID, information.getUser());
    }
    
    /** Test. */
    @Test
    public void testGetWorkflowDescription() {
        assertEquals(wd, information.getWorkflowDescription());
    }
    
    /**
     * Test.
     * @throws Exception if an error occurred.
     */
    @Test
    public void testGetInstantiationTime() throws Exception {
        Date actualTime = new Date();
        final int oneThousand = 1000;
        Thread.sleep(oneThousand);
        information = new WorkflowInformationImpl(id, name, wd, pc);
        Date time = information.getInstantiationTime();
        Thread.sleep(oneThousand);
        assertTrue(time.after(actualTime));
        assertTrue(time.before(new Date()));
    }
    
    /** Test. */
    @Test
    public void testAdditionalInformation() {
        assertEquals(addInfo, information.getAdditionalInformation());
    }
    
    /** Test. */
    @Test
    public void testComponentInstanceDescriptor() {
        String compId = "id";
        String nodeName = "kreativBin";
        ComponentInstanceDescriptor cid1 = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(cid1.getComponentIdentifier()).andReturn(id).anyTimes();
        EasyMock.expect(cid1.getName()).andReturn(nodeName).anyTimes();
        EasyMock.replay(cid1);
        
        ComponentInstanceDescriptor cid2 = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(cid2.getComponentIdentifier()).andReturn(nodeName).anyTimes();
        EasyMock.expect(cid2.getName()).andReturn(id).anyTimes();
        EasyMock.replay(cid2);
        
        Set<ComponentInstanceDescriptor> cids = new HashSet<ComponentInstanceDescriptor>();
        cids.add(cid1);
        cids.add(cid2);
        
        information.setComponentInstanceDescriptors(cids);
        
        assertEquals(cid1, information.getComponentInstanceDescriptor(nodeName, compId));
        assertEquals(cid2, information.getComponentInstanceDescriptor(compId, nodeName));
        assertEquals(null, information.getComponentInstanceDescriptor(nodeName, ""));
        assertEquals(null, information.getComponentInstanceDescriptor(compId, ""));
        assertEquals(null, information.getComponentInstanceDescriptor("", nodeName));
        assertEquals(null, information.getComponentInstanceDescriptor("", compId));
        assertEquals(null, information.getComponentInstanceDescriptor("", ""));
    }
    
    /** Test. */
    @Test
    public void testGetControllerPlatform() {
        assertEquals(pi, information.getControllerPlatform());
    }
    
    /** Test. */
    @Test
    public void testgetInvolvedPlatforms() {
        Set<PlatformIdentifier> pis = information.getInvolvedPlatforms();
        
        assertEquals(1, pis.size());
        assertTrue(pis.contains(pi));
    }
    
    private void createTestWorkflowDescription() {
        wd = new WorkflowDescription("wd.id");
        
        WorkflowNode source = new WorkflowNode(cd);
        WorkflowNode target = new WorkflowNode(cd);
        wd.addWorkflowNode(source);
        wd.addWorkflowNode(target);
        wd.setTargetPlatform(pi);
        wd.setAdditionalInformation(addInfo);
        wd.addConnection(new Connection(source, "local.output", 
            target, "local.input"));
        
    }
    
    private void createComponentDescriptionMock() {
        cd = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(cd.getPlatform()).andReturn(pi).anyTimes();
        EasyMock.replay(cd);
    }

}
