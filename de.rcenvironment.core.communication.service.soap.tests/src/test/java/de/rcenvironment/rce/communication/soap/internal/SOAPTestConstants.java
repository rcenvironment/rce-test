/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import java.io.Serializable;
import java.util.ArrayList;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;

/**
 * Test constants for the SOAP communication tests.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Tobias Menden
 */
public final class SOAPTestConstants {

    /**
     * Bundle symbolic name.
     */
    public static final String BUNDLE_SYMBOLIC_NAME = "de.rcenvironment.rce.communication.soap";

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
     * Test SOAP port.
     */
    public static final int PORT = 666;

    /**
     * Test RMI port.
     */
    public static final int BROKEN_PORT = -666;

    /**
     * Test method.
     */
    public static final String METHOD = "getValue";

    /**
     * Test method.
     */
    public static final String UNKNOWN_METHOD = "unknown";

    /**
     * Test method.
     */
    public static final String SERIALIZATION_EXCEPTION_METHOD = "SOAPException";
    
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
     * Test communication request.
     */
    public static final ServiceCallRequest SERIALIZATION_EXCEPTION_METHOD_REQUEST = new ServiceCallRequest(LOCAL_PLATFORM, LOCAL_PLATFORM,
        SERVICE, null, SERIALIZATION_EXCEPTION_METHOD,
        new ArrayList<Serializable>());
    
    /**
     * Default commObj.
    */ 
    public static final ServiceCallRequest COMM_OBJECT = new ServiceCallRequest(
            PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST, 1),
            PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST, 1),
            BUNDLE_SYMBOLIC_NAME,
            "string3",
            "string4",
            null);

    /**
     * Second commObj.
    */ 
    public static final ServiceCallRequest COMM_OBJECT_2 = new ServiceCallRequest(
            PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST, 1),
            PlatformIdentifierFactory.fromHostAndNumber(LOCALHOST, 1),
            BUNDLE_SYMBOLIC_NAME,
            "none",
            "string4",
            null);
        
    /**
     * Private constructor of this utility class.
     */
    private SOAPTestConstants() {
    }
}
