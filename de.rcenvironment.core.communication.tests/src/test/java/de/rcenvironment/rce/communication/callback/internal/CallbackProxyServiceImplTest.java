/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.callback.CallbackProxy;

/**
 * Test cases for {@link CallbackProxyServiceImplTest}.
 * 
 * @author Doreen Seider
 */
public class CallbackProxyServiceImplTest {

    private CallbackProxyServiceImpl service;

    private final String objectID = "objectID";

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    private CallbackProxy proxy = new CallbackProxy() {

        @Override
        public String getObjectIdentifier() {
            return objectID;
        }

        @Override
        public PlatformIdentifier getHomePlatform() {
            return pi;
        }
    };

    /** Set up. */
    @Before
    public void setUp() {
        service = new CallbackProxyServiceImpl();
    }

    /** Test. */
    @Test
    public void test() {
        service.addCallbackProxy(proxy);

        assertEquals(proxy, service.getCallbackProxy(objectID));
        assertNull(service.getCallbackProxy("rumpelstielzchen"));

        service.setTTL(objectID, new Long(7));
        service.setTTL("mobby", new Long(9));

        service.activate(EasyMock.createNiceMock(BundleContext.class));
        service.deactivate(EasyMock.createNiceMock(BundleContext.class));

        proxy = null;
        System.gc();
        assertNull(service.getCallbackProxy(objectID));
    }
}
