/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.easymock.EasyMock;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;

/**
 * Test cases for {@link PersistentWorkflowDescriptionUpdater}.
 *
 * @author Doreen Seider
 */

public final class PersistentWorkflowDescriptionUpdaterTest {
    
    private static final String OPTIMIZER_WF = "/workflows_test/Optimizer.wf";
    
    private static final String SIMPLE_WRAPPER_V236_WF = "/workflows_test/SimpleWrapper_v2.3.6.wf";

    /** Test. */
    @Test
    public void testUpdatePersistentWorkflowDescription() {
        // implement test cases if there is at least two update methods implemented
    }
    
    /** Test. */
    @Test
    public void testIsUpdateNeeded() {
        // implement test cases if there is at least two update methods implemented
    }
    
    /**
     * Test. 
     * @throws ParseException on error
     * @throws IOException on error
     **/
    @Test
    public void testUpdatePersistentWorkflowDescriptionFromVersion0To1() throws IOException, ParseException {
        WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();
        handler.bindDistributedComponentRegistry(new WorkflowDescriptionPersistenceHandlerTest().new DummyDistributedComponentRegistry());
        handler.bindPlatformService(new WorkflowDescriptionPersistenceHandlerTest().new DummyPlatformService());
        
        User user = EasyMock.createNiceMock(User.class);
        
        WorkflowDescription wdOld = handler.readWorkflowDescriptionFromStream(
            PersistentWorkflowDescriptionUpdaterTest.class.getResourceAsStream(SIMPLE_WRAPPER_V236_WF), user);
        
        assertEquals(0, wdOld.getWorkflowVersion());
        
        for (WorkflowNode node : wdOld.getWorkflowNodes()) {
            if (node.getComponentDescription().getIdentifier().equals("de.rcenvironment.rce.components.python.PythonComponent_Python")) {
                assertTrue(!node.getProperty("pythonInstallation").equals("${pathPlaceholder}"));
            }
        }
        
        InputStream tempInputStream = PersistentWorkflowDescriptionUpdater.updatePersistentWorkflowDescriptionFromVersion0To1(
            PersistentWorkflowDescriptionUpdaterTest.class.getResourceAsStream(SIMPLE_WRAPPER_V236_WF), user);
        
        WorkflowDescription wdNew = handler.readWorkflowDescriptionFromStream(tempInputStream, EasyMock.createNiceMock(User.class));
        
        assertEquals(1, wdNew.getWorkflowVersion());
        for (WorkflowNode node : wdNew.getWorkflowNodes()) {
            if (node.getComponentDescription().getIdentifier().equals("de.rcenvironment.rce.components.python.PythonComponent_Python")) {
                assertTrue(node.getProperty("pythonInstallation").equals("${pathPlaceholder}"));
            }
        }
    }
    
    /**
     * Test. 
     * @throws ParseException on error
     * @throws IOException on error
     **/
    @Test
    public void isUpdateNeededFromVersion0To1() throws IOException, ParseException {
        WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();
        handler.bindDistributedComponentRegistry(new WorkflowDescriptionPersistenceHandlerTest().new DummyDistributedComponentRegistry());
        handler.bindPlatformService(new WorkflowDescriptionPersistenceHandlerTest().new DummyPlatformService());
        
        User user = EasyMock.createNiceMock(User.class);
        
        assertTrue(PersistentWorkflowDescriptionUpdater.isUpdateNeededFromVersion0To1(
            PersistentWorkflowDescriptionUpdaterTest.class.getResourceAsStream(SIMPLE_WRAPPER_V236_WF), user));
        
        assertFalse(PersistentWorkflowDescriptionUpdater.isUpdateNeededFromVersion0To1(
            PersistentWorkflowDescriptionUpdaterTest.class.getResourceAsStream(OPTIMIZER_WF), user));
    }
}
