/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.callback.CallbackProxy;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;

/**
 * Test cases for {@link CallbackServiceImpl}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class CallbackServiceImplTest {

    private CallbackServiceImpl service;

    private String callbackObject = new String("kawumm");

    private final PlatformIdentifier piLocal = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    private final PlatformIdentifier piRemote = PlatformIdentifierFactory.fromHostAndNumberString("remotehost:1");

    /** Set up. */
    @Before
    public void setUp() {
        service = new CallbackServiceImpl();
    }

    /**
     * Test.
     * 
     * @throws CommunicationException if an error occurs.
     **/
    @SuppressWarnings("serial")
    @Test
    public void testRemainingMethods() throws CommunicationException {
        String id = service.addCallbackObject(callbackObject, piRemote);
        assertNotNull(id);
        assertEquals(id, service.addCallbackObject(callbackObject, piRemote));
        assertTrue(id != service.addCallbackObject(new String(), piRemote));

        assertEquals(id, service.getCallbackObjectIdentifier(callbackObject));
        assertNull(service.getCallbackObjectIdentifier(new Object()));

        assertEquals(callbackObject, service.getCallbackObject(id));
        assertNull(service.getCallbackObject("gaehn"));

        service.setTTL(id, new Long(5));
        service.setTTL("kasperle", new Long(2));

        assertEquals(callbackObject.toString(), service.callback(id, "toString", new ArrayList<Serializable>()));

        assertEquals(true, service.callback(id, "equals",
            new ArrayList<Serializable>() {

                {
                    add(callbackObject);
                }
            }));

        assertEquals(false, service.callback(id, "equals",
            new ArrayList<Serializable>() {

                {
                    add(new String());
                }
            }));

        callbackObject = null;
        System.gc();
        assertNull(service.getCallbackObject(id));

        service.activate(EasyMock.createNiceMock(BundleContext.class));
        service.deactivate(EasyMock.createNiceMock(BundleContext.class));
    }

    /**
     * Test.
     * 
     * @throws CommunicationException if an error occurs.
     **/
    @Test(expected = CommunicationException.class)
    public void testForFailure() throws CommunicationException {
        service.callback("id", "toString", new ArrayList<Serializable>());
    }

    /** Test. */
    @Test
    public void testCreateProxy() {
        service.bindPlatformService(new DummyPlatformService());

        String id = UUID.randomUUID().toString();
        Object proxy = service.createCallbackProxy(new DummyObject(), id, piRemote);

        assertTrue(proxy != null);
        assertTrue(proxy instanceof DummyInterface);
        assertTrue(proxy instanceof CallbackProxy);
        assertEquals(piLocal, ((CallbackProxy) proxy).getHomePlatform());
        assertEquals(id, ((CallbackProxy) proxy).getObjectIdentifier());
        assertEquals("method called", ((DummyInterface) proxy).method());
    }

    /** Test. */
    @Test
    public void testCreateProxyForClassWithCustomSuperclass() {
        service.bindPlatformService(new DummyPlatformService());

        String id = UUID.randomUUID().toString();
        Object proxy = service.createCallbackProxy(new DummyLevel2Object(), id, piRemote);

        assertTrue(proxy != null);
        assertTrue(proxy instanceof DummyInterface);
        assertTrue(proxy instanceof CallbackProxy);
        assertEquals(piLocal, ((CallbackProxy) proxy).getHomePlatform());
        assertEquals(id, ((CallbackProxy) proxy).getObjectIdentifier());
        assertEquals("method called", ((DummyInterface) proxy).method());
    }

    /**
     * Test implementation of the {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return piLocal;
        }
    }

}
