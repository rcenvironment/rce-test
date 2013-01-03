/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.component.Component;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentController;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentRegistry;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.testutils.MockDistributedComponentRegistry;
import de.rcenvironment.rce.component.workflow.Connection;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowInformationImpl;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Test cases for the {@link WorkflowImpl}.
 * 
 * @author Roland Gude
 * @author Doreen Seider
 */
public class WorkflowImplTest extends TestCase {
    
    private static final String INPUT_NAME = "myInput";

    private static final String OUTPUT_NAME = "myOutput";

    private static final String JAVA_LANG_STRING = "java.lang.String";
    
    private static final String WD_ID = "janz";

    private static final String COMP_ID = "jenau";
        
    private static final String ANOTHER_COMP_ID = "so";

    private static final String COMP_INST_ID = "nich";

    private static final String ANOTHER_COMP_INST_ID = "anders";

    private User user;
    
    private WorkflowImpl workflow;
    
    private ComponentDescription compDesc;
    
    private ComponentDescription anotherCompDesc;
    
    private ComponentInstanceDescriptor compInstDesc;
    
    private ComponentInstanceDescriptor anotherCcompInstDesc;
    
    private PlatformIdentifier platform = PlatformIdentifierFactory.fromHostAndNumberString("rumpel:1");
    
    private PlatformIdentifier anotherPlatform = PlatformIdentifierFactory.fromHostAndNumberString("pumpel:1");

    @Override
    public void setUp() throws Exception {
        
        user = EasyMock.createNiceMock(User.class);
        EasyMock.expect(user.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(user);
        createComponentDescriptionMock();
        createAnotherComponentDescriptionMock();
        
        compInstDesc = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(compInstDesc.getPlatform()).andReturn(platform).anyTimes();
        EasyMock.expect(compInstDesc.getIdentifier()).andReturn(COMP_INST_ID).anyTimes();
        EasyMock.replay(compInstDesc);
        anotherCcompInstDesc = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(anotherCcompInstDesc.getPlatform()).andReturn(anotherPlatform);
        EasyMock.expect(anotherCcompInstDesc.getIdentifier()).andReturn(ANOTHER_COMP_INST_ID).anyTimes();
        EasyMock.replay(anotherCcompInstDesc);
        
        WorkflowDescription wd = new WorkflowDescription(WD_ID);
        WorkflowNode nodeOne = new WorkflowNode(compDesc);
        WorkflowNode nodeTwo = new WorkflowNode(anotherCompDesc);
        wd.addWorkflowNode(nodeOne);
        wd.addWorkflowNode(nodeTwo);
        Connection cOne = new Connection(nodeOne, OUTPUT_NAME, nodeTwo, INPUT_NAME);
        Connection cTwo = new Connection(nodeTwo, OUTPUT_NAME, nodeOne, INPUT_NAME);
        wd.addConnection(cOne);
        wd.addConnection(cTwo);

        WorkflowInformation wi = new WorkflowInformationImpl(WD_ID, "peng", wd,
            EasyMock.createNiceMock(User.class));
        workflow = new WorkflowImpl(wi, user);
        workflow.setBundleContext(EasyMock.createNiceMock(BundleContext.class));
        workflow.setCommunicationService(new DummyCommuniationService());
        workflow.setDistributedNotificationService(new DummyDistributedNotificationService());
        workflow.setDistributedComponentRegistry(new DummyDistributedComponentRegistry());
        workflow.setState(WorkflowState.READY);
    }

    /**
     * Test the lifecycle of the workflow. 
     * @throws AuthorizationException if an error occur.
     **/
    
    public void test() throws AuthorizationException {
        assertEquals(WorkflowState.READY, workflow.getState(user));
        workflow.start(user);
        workflow.pause(user);
        workflow.resume(user);
        workflow.cancel(user);
        workflow.dispose(user);
        
        workflow.getStateOfComponent(user, COMP_INST_ID);
    }
    
    @SuppressWarnings("unchecked")
    private void createComponentDescriptionMock() {
        compDesc = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(compDesc.getIdentifier()).andReturn(COMP_ID).anyTimes();
        EasyMock.expect(compDesc.getPlatform()).andReturn(platform).anyTimes();
        
        Map<String, Class<? extends Serializable>> inputs = new HashMap<String, Class<? extends Serializable>>();
        try {
            inputs.put(INPUT_NAME, (Class<? extends Serializable>) getClass().getClassLoader().loadClass(JAVA_LANG_STRING));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EasyMock.expect(compDesc.getInputDefinitions()).andReturn(inputs).anyTimes();
        
        Map<String, Class<? extends Serializable>> outputs = new HashMap<String, Class<? extends Serializable>>();
        try {
            outputs.put(OUTPUT_NAME, (Class<? extends Serializable>) getClass().getClassLoader().loadClass(JAVA_LANG_STRING));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EasyMock.expect(compDesc.getOutputDefinitions()).andReturn(outputs).anyTimes();
        
        Map<String, Serializable> config = new HashMap<String, Serializable>();
        config.put("value", "key");
        EasyMock.expect(compDesc.getConfiguration()).andReturn(config).anyTimes();

        EasyMock.replay(compDesc);
    }
    
    @SuppressWarnings("unchecked")
    private void createAnotherComponentDescriptionMock() {
        anotherCompDesc = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(anotherCompDesc.getIdentifier()).andReturn(ANOTHER_COMP_ID).anyTimes();
        EasyMock.expect(anotherCompDesc.getPlatform()).andReturn(platform).anyTimes();
        
        Map<String, Class<? extends Serializable>> inputs = new HashMap<String, Class<? extends Serializable>>();
        try {
            inputs.put(INPUT_NAME, (Class<? extends Serializable>) getClass().getClassLoader().loadClass(JAVA_LANG_STRING));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EasyMock.expect(anotherCompDesc.getInputDefinitions()).andReturn(inputs).anyTimes();
        
        Map<String, Class<? extends Serializable>> outputs = new HashMap<String, Class<? extends Serializable>>();
        try {
            outputs.put(OUTPUT_NAME, (Class<? extends Serializable>) getClass().getClassLoader().loadClass(JAVA_LANG_STRING));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EasyMock.expect(anotherCompDesc.getOutputDefinitions()).andReturn(outputs).anyTimes();
        
        Map<String, Serializable> config = new HashMap<String, Serializable>();
        config.put("key", "value");
        EasyMock.expect(anotherCompDesc.getConfiguration()).andReturn(config).anyTimes();
        
        EasyMock.replay(anotherCompDesc);
    }
    
    /**
     * Test {@link CommunicationService} implementation.
     *
     * @author Doreen Seider
     */
    private class DummyCommuniationService extends MockCommunicationService {

        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            return getService(iface, null, platformIdentifier, bundleContext);
        }

        @Override
        public Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier,
            BundleContext bundleContext) throws IllegalStateException {
            Object service = null;
            if (iface == ComponentRegistry.class) {
                service = EasyMock.createNiceMock(ComponentRegistry.class);
            } else if (iface == Component.class) {
                service = EasyMock.createNiceMock(Component.class);
            } else if (iface == ComponentController.class) {
                ComponentController compController = EasyMock.createNiceMock(ComponentController.class);
                EasyMock.expect(compController.getState()).andReturn(ComponentState.DISPOSED);
                EasyMock.replay(compController);
                service = compController;
            }
            return service;
        }

    }
    
    /**
     * Test {@link DistributedNotificationService} implementation.
     *
     * @author Doreen Seider
     */
    private class DummyDistributedNotificationService implements DistributedNotificationService {

        @Override
        public void setBufferSize(String notificationIdentifier, int bufferSize) {
        }

        @Override
        public void removePublisher(String notificationIdentifier) {

        }

        @Override
        public <T extends Serializable> void send(String notificationId, T notificationBody) {
        }

        @Override
        public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publisherPlatform) {
            return null;
        }

        @Override
        public void unsubscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publishPlatform) {
        }

        @Override
        public Map<String, SortedSet<NotificationHeader>> getNotificationHeaders(String notificationId,
            PlatformIdentifier publishPlatform) {
            return null;
        }


        @Override
        public Map<String, List<Notification>> getNotifications(String notificationId, PlatformIdentifier publishPlatform) {
            return null;
        }

        @Override
        public Notification getNotification(NotificationHeader header) {
            return null;
        }

        @Override
        public void trigger(String notificationId, PlatformIdentifier publishPlatform) {
        }
        
    }
    
    /**
     * Test {@link DistributedComponentRegistry} implementation.
     *
     * @author Doreen Seider
     */
    private class DummyDistributedComponentRegistry extends MockDistributedComponentRegistry {

        @Override
        public ComponentInstanceDescriptor createComponentInstance(User proxyCertificate, ComponentDescription description,
            String name, ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException {
            if (proxyCertificate == user && description == compDesc) {
                return compInstDesc;
            } else if (proxyCertificate == user && description == anotherCompDesc) {
                return anotherCcompInstDesc;
            } else {
                throw new ComponentException("So nich");
            }
        }
        
    }

}
