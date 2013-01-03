/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;

/**
 * Test constants for the RMI communication tests.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public final class RMITestConstants {

    /**
     * Bundle symbolic name.
     */
    public static final String BUNDLE_SYMBOLIC_NAME = "de.rcenvironment.rce.communication.rmi";

    /**
     * Test protocol.
     */
    public static final String RMI_PROTOCOL = BUNDLE_SYMBOLIC_NAME;

    /**
     * Test service.
     */
    public static final String SERVICE = "de.rcenvironment.rce.communication.servicecall.internal.MethodCallerTestMethods";

    /**
     * Test name of the host.
     */
    public static final String LOCALHOST = "localhost";

    /**
     * Test IP of the host.
     */
    public static final String LOCALHOST_IP = "127.0.0.1";

    /**
     * Test name of the instance.
     */
    public static final int INSTANCE = 0;

    /**
     * Local platform.
     */
    public static final PlatformIdentifier LOCAL_PLATFORM = PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST_IP, INSTANCE);

    /**
     * Test RMI port range.
     */
    public static final int RMI_PORT_RANGE = 10000;

    /**
     * Test RMI port range.
     */
    public static final int RMI_PORT_RANGE_SECTION = 100;

    /**
     * Default RMI port.
     */
    public static final int DEFAULT_PORT = 1099;

    /**
     * Test method.
     */
    public static final String METHOD = "getValue";

    /**
     * Test method.
     */
    public static final String UNKNOWN_METHOD = "unknown";

    /**
     * Test return value.
     */
    public static final String RETURN_VALUE = "Hallo Welt";

    /**
     * Test communication request.
     */
    public static final ServiceCallRequest REQUEST = new ServiceCallRequest(LOCAL_PLATFORM, LOCAL_PLATFORM, SERVICE, null, METHOD,
        new ArrayList<Serializable>());

    /**
     * Test communication request.
     */
    public static final ServiceCallRequest UNKNOWN_METHOD_REQUEST = new ServiceCallRequest(LOCAL_PLATFORM, LOCAL_PLATFORM,
        SERVICE, null, UNKNOWN_METHOD,
        new ArrayList<Serializable>());

    /**
     * Test RMI port.
     */
    private static final int START_PORT = (new Random().nextInt(RMI_PORT_RANGE) % RMI_PORT_RANGE_SECTION)
            * RMI_PORT_RANGE_SECTION
            + (2 * Short.MAX_VALUE - RMI_PORT_RANGE);

    /**
     * -1.
     */
    private static final int MINUS_ONE = -1;

    private static int portIncrement = MINUS_ONE;
    
    /**
     * 
     * Private constructor of this utility class.
     * 
     */
    private RMITestConstants() {

    }

    /**
     * Test RMI port.
     * 
     * @return new hopefully free port
     */
    public static int guessFreePort() {
        if (portIncrement++ >= RMI_PORT_RANGE_SECTION) {
            portIncrement = MINUS_ONE;
        }
        int port = START_PORT + portIncrement;
        return port;
    }

}
