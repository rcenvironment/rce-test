/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import java.io.IOException;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import junit.framework.TestCase;

/**
 * 
 * Unit test for the {@link SOAPServiceCallSender}.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */ 
public class SOAPServiceCallSenderTest extends TestCase {
    
    private static final String HTTP = "http://";

    private static final String EXCEPTION_NOT_THROWN = "Exception not thrown";

    private SOAPServiceCallSender soapServiceCallSender = null;
    
    private NetworkContact contact = new NetworkContact(
        SOAPTestConstants.LOCALHOST_IP, SOAPTestConstants.BUNDLE_SYMBOLIC_NAME, SOAPTestConstants.PORT);

    private SOAPServiceCallSenderFactory senderFactory;
    
    @Override
    protected void setUp() throws Exception {
        soapServiceCallSender = new SOAPServiceCallSender();
        senderFactory = new SOAPServiceCallSenderFactory();
        senderFactory.bindJettyService(new JettyServiceImplDummy());
        senderFactory.bindConfigurationService(SOAPMockFactory.getInstance().getConfigurationService(false));
        senderFactory.bindPlatformService(SOAPMockFactory.getInstance().getPlatformService());
        senderFactory.activate(SOAPMockFactory.getInstance().getBundleContextMock());
    }

    @Override
    protected void tearDown() throws Exception {
        senderFactory.deactivate();
        senderFactory = null;
    }

    /**
     * Test method
     * 'de.rcenvironment.rce.communication.SOAP.SOAPCommunicator.testSetContact(CommunicationContact)'
     * for success.
     * @throws IOException Thrown if test failed.
     * @throws CommunicationException Thrown if test failed.
     */
    public void testSetContactForSuccess() throws IOException, CommunicationException {
        soapServiceCallSender.setJettyService(new JettyServiceImplDummy());
        soapServiceCallSender.setProtocol(HTTP);
        soapServiceCallSender.initialize(contact);
    }
    
    /**
     * Test method
     * 'de.rcenvironment.rce.communication.SOAP.SOAPCommunicator.sSend(CommunicationRequest,
     * boolean)' for success.
     * @throws IOException Thrown if test failed.
     * @throws CommunicationException Thrown if test failed.
     * 
     */ 
    public void testSendForSuccess() throws IOException, CommunicationException {
        soapServiceCallSender.setJettyService(new JettyServiceImplDummy());
        soapServiceCallSender.setProtocol(HTTP);
        soapServiceCallSender.initialize(contact);
        soapServiceCallSender.send(SOAPTestConstants.REQUEST);
    }

    /**
     * Test method
     * 'de.rcenvironment.rce.communication.SOAP.SOAPCommunicator.send(CommunicationRequest,
     * boolean)' for sanity.
     * @throws CommunicationException Thrown if test failed.
     * 
     */ 
    public void testSendForSanity() throws CommunicationException {
        soapServiceCallSender.setJettyService(new JettyServiceImplDummy());
        soapServiceCallSender.setProtocol(HTTP);
        soapServiceCallSender.initialize(contact);
        ServiceCallResult commResult = soapServiceCallSender.send(SOAPTestConstants.REQUEST);
        assertEquals(SOAPTestConstants.METHOD, (String) commResult.getReturnValue());
    }

    /**
     * Test method
     * 'de.rcenvironment.rce.communication.SOAP.SOAPCommunicator.send(CommunicationRequest,
     * boolean)' for failure.
     * 
     * @throws Exception Thrown if test failed.
     */
    public void testSendForFailure() throws Exception {
        soapServiceCallSender.setJettyService(new JettyServiceImplDummy());
        soapServiceCallSender.setProtocol(HTTP);
        soapServiceCallSender.initialize(contact);

        // UnknownMethod Request
        try {
            soapServiceCallSender.send(SOAPTestConstants.UNKNOWN_METHOD_REQUEST);
            fail(EXCEPTION_NOT_THROWN);
        } catch (CommunicationException e) {
            assertTrue(true);
        }
        
        // SOAPException Request
        try {
            soapServiceCallSender.send(SOAPTestConstants.SERIALIZATION_EXCEPTION_METHOD_REQUEST);
            fail(EXCEPTION_NOT_THROWN);
        } catch (CommunicationException e) {
            assertTrue(true);
        }
    }
}

