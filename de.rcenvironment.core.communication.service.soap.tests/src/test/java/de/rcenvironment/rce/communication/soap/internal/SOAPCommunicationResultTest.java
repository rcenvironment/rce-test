/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import de.rcenvironment.rce.communication.service.ServiceCallResult;
import junit.framework.TestCase;

/**
 * Unit test for the {@link SOAPCommunicationResult}.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
public class SOAPCommunicationResultTest extends TestCase {

    private ServiceCallResult communicationResult;
    
    private SOAPCommunicationResult result;

    @Override
    protected void setUp() throws Exception {
        result = new SOAPCommunicationResult();
        communicationResult = new ServiceCallResult(SOAPTestConstants.RETURN_VALUE);   
    }

    /**
     * Test getCommunicationResult for Sanity.
     * 
     * @throws Exception Thrown on error.
     */
    public void testGetServiceCallRequestForSanity() throws Exception {
        result = new SOAPCommunicationResult(communicationResult);
        ServiceCallResult finalServiceCallResult = result.getServiceCallResult();
        assertEquals(ServiceCallResult.class, finalServiceCallResult.getClass());
        assertEquals(SOAPTestConstants.RETURN_VALUE, finalServiceCallResult.getReturnValue());
    }
}
