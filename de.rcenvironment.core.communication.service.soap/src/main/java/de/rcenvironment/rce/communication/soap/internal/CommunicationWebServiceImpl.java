/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Implementation of {@link CommunicationWebService}.
 * 
 * @author Tobias Menden
 */
@WebService(endpointInterface = "de.rcenvironment.rce.communication.soap.internal.CommunicationWebService",
    serviceName = "SOAPCommunication")
public class CommunicationWebServiceImpl implements CommunicationWebService {

    private ServiceCallHandler serviceCallHandler;

    @Override
    public SOAPCommunicationResult call(SOAPCommunicationRequest request) throws SOAPException {
        
        ServiceCallRequest serviceCallRequest = request.getServiceCallRequest();

        try {
            ServiceCallResult result = serviceCallHandler.handle(serviceCallRequest);
            return new SOAPCommunicationResult(result);
        } catch (CommunicationException e) {
            String call = serviceCallRequest.getService() + ":"
                + serviceCallRequest.getServiceMethod() + "@"
                + serviceCallRequest.getRequestedPlatform();
            throw new SOAPException("An error occured when a remote platform requested this instance: " + call, e);
        }
    }

    public void setServiceCallHandler(ServiceCallHandler newServiceCallHandler) {
        serviceCallHandler = newServiceCallHandler;
    }
}
