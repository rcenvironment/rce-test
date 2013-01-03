/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import javax.xml.soap.SOAPException;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Unittest for the implmentation of <code>SOAPCommunicationImpl</code>.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
public class CommunicationWebServiceImplTest extends TestCase {

    private static final String MOCK = "mock";
        
    private CommunicationWebServiceImpl soapCommunicationImpl;
    
    private String returnValue = "Hallo Welt";
    
    private ServiceCallResult obj;

    @Override
    protected void setUp() throws Exception {
        soapCommunicationImpl = new CommunicationWebServiceImpl();
        soapCommunicationImpl.setServiceCallHandler(new ServiceCallHandlerDummy());
    }

    @Override
    protected void tearDown() throws Exception {
        soapCommunicationImpl = null;
    }
    
    /**
     * Test call for Sanity.
     * 
     * @throws Exception Thrown on error.
     */
    public void testCallforSanity() throws Exception {
        
        obj = soapCommunicationImpl.call(new SOAPCommunicationRequest(
            SOAPTestConstants.COMM_OBJECT)).getServiceCallResult();
        assertEquals(returnValue, (String) obj.getReturnValue());

        obj = soapCommunicationImpl.call(new SOAPCommunicationRequest(SOAPTestConstants.COMM_OBJECT)).getServiceCallResult();
        assertEquals(String.class, obj.getReturnValue().getClass());
    }
    
    /**
     * Test call for Failure.
     * 
     * @throws Exception Thrown on error.
     */
    public void testCallforFailure() throws Exception {

        ServiceCallRequest myBrokenCommRequest = getCommunicationRequest("de.rcenvironment.rce.communication.NotExists",
            PlatformIdentifierFactory.fromHostAndNumber(SOAPTestConstants.LOCALHOST_IP, SOAPTestConstants.INSTANCE));
        try {
            obj = soapCommunicationImpl.call(new SOAPCommunicationRequest(myBrokenCommRequest)).getServiceCallResult();
        } catch (SOAPException e) {
            assertTrue(true);
        }
        
        myBrokenCommRequest = getCommunicationRequest(SOAPTestConstants.BUNDLE_SYMBOLIC_NAME,
                PlatformIdentifierFactory.fromHostAndNumber(SOAPTestConstants.RETURN_VALUE, SOAPTestConstants.INSTANCE));
        try {
            obj = soapCommunicationImpl.call(new SOAPCommunicationRequest(myBrokenCommRequest)).getServiceCallResult();
            fail();
        } catch (SOAPException e) {
            assertTrue(true);
        }
        
        SOAPCommunicationRequest soapCommunicationRequest = EasyMock.createNiceMock(
            SOAPCommunicationRequest.class);
        EasyMock.expect(soapCommunicationRequest.getServiceCallRequest()).andThrow(
            new SOAPException("Symbol for the I/O and ClassNotFoundException in SOAPCommunicationRequest")).anyTimes();
        EasyMock.replay(soapCommunicationRequest);
        try {
            obj = soapCommunicationImpl.call(soapCommunicationRequest).getServiceCallResult();
            fail();
        } catch (SOAPException e) {
            assertTrue(true);
        }
        
        
    }
    
    private ServiceCallRequest getCommunicationRequest(String serviceMethod, PlatformIdentifier requestedPlatform) {
        ServiceCallRequest newRequest = new ServiceCallRequest(
                requestedPlatform,
                SOAPTestConstants.LOCAL_PLATFORM,
                serviceMethod,
                MOCK, MOCK, null);
        return newRequest;
    }
}

