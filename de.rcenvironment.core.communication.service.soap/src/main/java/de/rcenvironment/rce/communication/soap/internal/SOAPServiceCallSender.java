/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.SOAPException;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.jetty.JettyService;

/**
 * The SOAP implementation of {@link ServiceCallSender}.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
public class SOAPServiceCallSender implements ServiceCallSender {

    private static final String ERROR_COMMUNICATION_FAILED = "Communication via SOAP to {0}:{1} failed.";

    private static Map<NetworkContact, CommunicationWebService> clients
        = new HashMap<NetworkContact, CommunicationWebService>();
    
    private JettyService jettyService;
    
    private String protocol;
    
    private CommunicationWebService client;
    
    private NetworkContact communicationContact;
  
    @Override
    public void initialize(NetworkContact contact) {
        communicationContact = contact;
        if (clients.containsKey(communicationContact)) {
            client = clients.get(communicationContact);
        } else {
            String address = protocol + contact.getHost()  + ":" + communicationContact.getPort() + "/SOAPCommunication";
            client = (CommunicationWebService) jettyService.createWebServiceClient(CommunicationWebService.class, address);
            clients.put(communicationContact, client);
        }
    }

    @Override
    public ServiceCallResult send(ServiceCallRequest request) throws CommunicationException {
        try {
            SOAPCommunicationRequest soapRequest = new SOAPCommunicationRequest(request);
            return client.call(soapRequest).getServiceCallResult();
        } catch (SOAPException e) {
            String call = request.getService() + ":"
                + request.getServiceMethod() + "@"
                + request.getRequestedPlatform();
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED,
                    communicationContact.getHost() + "; call=" + call, String.valueOf(communicationContact.getPort())), e);
        } catch (RuntimeException e) {
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED,
                    communicationContact.getHost(), String.valueOf(communicationContact.getPort())), e);
        }
    }
    
    public void setJettyService(JettyService service) {
        jettyService = service;
    }
    
    public void setProtocol(String value) {
        protocol = value;
    }
}
