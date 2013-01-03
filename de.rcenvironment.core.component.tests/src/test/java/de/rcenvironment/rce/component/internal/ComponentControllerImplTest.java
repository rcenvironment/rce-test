/*
 * Copyright (C) 2006-2011 DLR, Germany
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.OutputDescriptor;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;
import de.rcenvironment.rce.notification.DistributedNotificationService;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationHeader;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Test cases for {@link ComponentControllerImpl}.
 * 
 * @author Doreen Seider
 */
public class ComponentControllerImplTest {

    private ComponentControllerImpl controller;

    /** Set up. */
    @Before
    public void setUp() {
        controller = new ComponentControllerImpl();
        controller.setConfigurationService(new DummyConfigurationService());
        controller.setDistributedNotificationService(new DummyNotificationService());
    }

    /** Test. */
    @Test
    public void testLifeCycle() {

        assertNull(controller.getState());

        User user = EasyMock.createNiceMock(User.class);

        String controllerId = "papa schlumpf";

        String compClazz = DummyComponent.class.getName();

        String compName = "schlumpfine";

        ComponentDescription compDesc = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(compDesc.getPlatform()).andReturn(PlatformIdentifierFactory.fromHostAndNumberString("schlumpf:7")).anyTimes();
        Map<String, Class<? extends Serializable>> outputsDef = new HashMap<String, Class<? extends Serializable>>();
        outputsDef.put("lang", Long.class);
        EasyMock.expect(compDesc.getOutputDefinitions()).andReturn(outputsDef).anyTimes();
        Map<String, Class<? extends Serializable>> inputsDef = new HashMap<String, Class<? extends Serializable>>();
        inputsDef.put("doppelt", Double.class);
        EasyMock.expect(compDesc.getInputDefinitions()).andReturn(inputsDef).anyTimes();
        EasyMock.replay(compDesc);

        ComponentContext compCtx = EasyMock.createNiceMock(ComponentContext.class);
        EasyMock.expect(compCtx.getName()).andReturn(MockComponentStuffFactory.WORKFLOW_NAME).anyTimes();
        EasyMock.replay(compCtx);

        try {
            controller.initialize(user, controllerId, compClazz, compName, compDesc, compCtx, true);
        } catch (ComponentException e) {
            fail();
        }

        try {
            controller.initialize(user, controllerId, compClazz + "so nich", compName, compDesc, compCtx, true);
            fail();
        } catch (ComponentException e) {
            assertTrue(true);
        }

        assertEquals(ComponentState.INSTANTIATED, controller.getState());

        Map<OutputDescriptor, String> endpoints = new HashMap<OutputDescriptor, String>();
        ComponentInstanceDescriptor compInstDesc = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);
        EasyMock.expect(compInstDesc.getIdentifier()).andReturn("gargamel");
        EasyMock.replay(compInstDesc);
        String inputName = "schlaubi";
        OutputDescriptor outputDesc = new OutputDescriptor(compInstDesc, "schlumpf");
        endpoints.put(outputDesc, inputName);
        controller.prepare(user, endpoints);
        controller.waitForLifecyclePhaseFinished();
        controller.start(user);
        controller.waitForLifecyclePhaseFinished();
        controller.pause(user);
        controller.waitForLifecyclePhaseFinished();
        controller.resume(user);
        controller.waitForLifecyclePhaseFinished();

        final Double value = 42.0;
        Input input = EasyMock.createNiceMock(Input.class);
        EasyMock.expect(input.getValue()).andReturn(value).anyTimes();
        EasyMock.expect(input.getName()).andReturn(inputName).anyTimes();
        EasyMock.replay(input);

        controller.newInput(input);
        controller.newInput(input);

        controller.cancel(user);
        controller.waitForLifecyclePhaseFinished();
        controller.dispose(user);
        controller.waitForLifecyclePhaseFinished();

        for (int i = 0; i < 8; i++) {
            controller.waitForLifecyclePhaseFinished();
        }
        
        BlockingQueue<Input> inputs = new LinkedBlockingQueue<Input>();
        controller.setInputs(inputs);
        assertEquals(inputs, controller.getInputs());

    }

    /**
     * Dummy implementation of {@link DistributedNotificationService}.
     * @author Doreen Seider
     */
    public class DummyNotificationService implements DistributedNotificationService {

        @Override
        public void setBufferSize(String notificationIdentifier, int bufferSize) {}

        @Override
        public void removePublisher(String notificationIdentifier) {}

        @Override
        public <T extends Serializable> void send(String notificationId, T notificationBody) {}

        @Override
        public Map<String, Long> subscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publisherPlatform) {
            return null;
        }

        @Override
        public void unsubscribe(String notificationId, NotificationSubscriber subscriber, PlatformIdentifier publishPlatform) {}

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
        public void trigger(String notificationId, PlatformIdentifier publishPlatform) {}

    }

    /**
     * Dummy implementation of {@link ConfigurationService}.
     * @author Doreen Seider
     */
    public class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        private File tempDir;

        public DummyConfigurationService() {
            try {
                tempDir = TempFileUtils.getDefaultInstance().createManagedTempDir();
            } catch (IOException e) {
                Assert.fail(e.toString());
            }
        }

        @Override
        public String getPlatformTempDir() {
            return tempDir.getAbsolutePath();
        }
    }

}
