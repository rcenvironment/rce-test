/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import javax.xml.soap.SOAPException;

import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.jetty.JettyService;

/**
 * The Jetty dummy server.
 * 
 * @author Tobias Menden
 */
public class JettyServiceImplDummy implements JettyService{
    
    @Override
    public void deployWebService(Object webService, String address) {
    }
    
    @Override
    public void undeployWebService(String address) { 
    }

    @Override
    public Object createWebServiceClient(Class<?> webServiceInterface, String address) {
        if (address == null || webServiceInterface == null) {
            return null;
        }
        return new CommunicationWebService() {
            @Override
            public SOAPCommunicationResult call(SOAPCommunicationRequest request)
                throws SOAPException {
                ServiceCallRequest serviceRequest = request.getServiceCallRequest();
                if (serviceRequest.getServiceMethod().equals(SOAPTestConstants.UNKNOWN_METHOD)) {
                    throw new SOAPException("Exception Thrown cause a uknown method is requested.");
                }
                if (serviceRequest.getServiceMethod().equals(SOAPTestConstants.SERIALIZATION_EXCEPTION_METHOD)) {
                    throw new SOAPException("Exception Thrown cause a IO or ClssNotFound Error raise due serialization.");
                }
                return new SOAPCommunicationResult(new ServiceCallResult(serviceRequest.getServiceMethod()));
            }
        };
    }
}
