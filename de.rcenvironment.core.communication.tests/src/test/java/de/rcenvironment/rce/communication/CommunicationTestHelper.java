/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;
import de.rcenvironment.rce.communication.internal.CommunicationContactMap;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.internal.MethodCallerTestMethods;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * Test constants for the communication tests.
 * 
 * @author Doreen Seider
 */
public final class CommunicationTestHelper {

    /**
     * Bundle symbolic name.
     */
    public static final String BUNDLE_SYMBOLIC_NAME = "de.rcenvironment.rce.communication";

    /**
     * Test protocol.
     */
    public static final String RMI_PROTOCOL = "de.rcenvironment.rce.communication.rmi";

    /**
     * Test service.
     */
    public static final String SERVICE = MethodCallerTestMethods.class.getCanonicalName();

    /**
     * Test name of the host.
     */
    public static final String LOCALHOST = "localhost";

    /**
     * Test IP of the host.
     */
    public static final String LOCALHOST_IP = "127.0.0.1";

    /**
     * Test IP of the host.
     */
    public static final String REMOTE_HOST_IP = "192.168.0.1";

    /**
     * Test name of the instance.
     */
    public static final int INSTANCE = 0;

    /**
     * PlatformIdentifier.
     */
    public static final PlatformIdentifier LOCAL_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST_IP, INSTANCE);

    /**
     * PlatformIdentifier.
     */
    public static final PlatformIdentifier REMOTE_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber(REMOTE_HOST_IP, INSTANCE);

    /**
     * Test RMI port.
     */
    public static final int RMI_PORT = 1099;

    /**
     * Test method.
     */
    public static final String METHOD = "getValue";

    /**
     * Test return value.
     */
    public static final String RETURN_VALUE = "Hallo Welt";

    /**
     * Test parameter.
     */
    public static final List<? extends Serializable> PARAMETER_LIST = new ArrayList<Serializable>();

    /**
     * Test communication contact.
     */
    public static final NetworkContact SERVICE_CONTACT = new NetworkContact(LOCALHOST_IP, RMI_PROTOCOL, RMI_PORT);

    /**
     * Test communication contact.
     */
    public static final NetworkContact FILE_CONTACT = new NetworkContact(LOCALHOST_IP, BUNDLE_SYMBOLIC_NAME, RMI_PORT);

    /**
     * Test communication contact.
     */
    public static final NetworkContact REMOTE_CONTACT = new NetworkContact(REMOTE_HOST_IP, RMI_PROTOCOL, RMI_PORT);

    /**
     * Test communication request.
     */
    public static final ServiceCallRequest REQUEST = new ServiceCallRequest(LOCAL_PLATFORM, REMOTE_PLATFORM, SERVICE,
        null, METHOD, PARAMETER_LIST);

    /**
     * Test communication request.
     */
    public static final ServiceCallRequest REMOTE_REQUEST = new ServiceCallRequest(REMOTE_PLATFORM, LOCAL_PLATFORM, SERVICE,
        null, METHOD, PARAMETER_LIST);

    /**
     * Test communication request.
     */
    public static final ServiceCallResult RESULT = new ServiceCallResult(RETURN_VALUE);

    /**
     * Test communication request.
     */
    public static final String URI = "file://" + LOCALHOST_IP + ":" + INSTANCE + "/src/test/resources/ping.txt";

    private CommunicationTestHelper() {}

    /**
     * Invoke activate method of {@link CommunicationContactMap}.
     */
    public static void activateCommunicationContactMap() {
        final String bundleSymbName = "symbolic.bundle.name";
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(bundleSymbName).anyTimes();
        EasyMock.replay(bundleMock);

        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.replay(bundleContextMock);

        ConfigurationService configServiceMock = EasyMock.createMock(ConfigurationService.class);
        EasyMock.expect(configServiceMock.getConfiguration(bundleSymbName, CommunicationConfiguration.class))
            .andReturn(new CommunicationConfiguration()).anyTimes();
        EasyMock.replay(configServiceMock);

        @SuppressWarnings("deprecation") CommunicationContactMap map = new CommunicationContactMap();
        map.bindConfigurationService(configServiceMock);
        map.activate(bundleContextMock);

    }

}
