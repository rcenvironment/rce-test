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

import junit.framework.TestCase;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;

/**
 * Unit test for the {@link SOAPCommunicationRequest}.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
public class SOAPCommunicationRequestTest extends TestCase {

    private ServiceCallRequest communicationRequest;

    private SOAPCommunicationRequest request;
    
    @Override
    protected void setUp() throws Exception {
        request = new SOAPCommunicationRequest();
        communicationRequest = new ServiceCallRequest(
            PlatformIdentifierFactory.fromHostAndNumber(SOAPTestConstants.LOCALHOST, SOAPTestConstants.INSTANCE),
            PlatformIdentifierFactory.fromHostAndNumber(SOAPTestConstants.LOCALHOST, SOAPTestConstants.INSTANCE),
            SOAPTestConstants.BUNDLE_SYMBOLIC_NAME,
            SOAPTestConstants.SERVICE,
            SOAPTestConstants.METHOD,
            new ArrayList<Serializable>());   
    }

    /**
     * Test getCommunicationRequest for Sanity.
     * 
     * @throws Exception Thrown on error.
     */
    public void testGetServiceCallRequestForSanity() throws Exception {
        request = new SOAPCommunicationRequest(communicationRequest);
        ServiceCallRequest finalServiceCallRequest = request.getServiceCallRequest();
        assertEquals(ServiceCallRequest.class, finalServiceCallRequest.getClass());
        assertEquals(communicationRequest.getParameterList(), finalServiceCallRequest.getParameterList());
        assertEquals(communicationRequest.getService(), finalServiceCallRequest.getService());
        assertEquals(communicationRequest.getServiceMethod(), finalServiceCallRequest.getServiceMethod());
        assertEquals(communicationRequest.getServiceProperties(), finalServiceCallRequest.getServiceProperties());
        assertEquals(communicationRequest.getCallingPlatform(), finalServiceCallRequest.getCallingPlatform());
        assertEquals(communicationRequest.getRequestedPlatform(), finalServiceCallRequest.getRequestedPlatform());
    }
}
