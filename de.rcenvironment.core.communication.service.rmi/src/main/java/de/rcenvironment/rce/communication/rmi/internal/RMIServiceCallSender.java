/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.MessageFormat;

import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;

/**
 * Class that implements {@link ServiceCallSender} based on RMI.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class RMIServiceCallSender implements ServiceCallSender {

    private static final String ERROR_COMMUNICATION_FAILED = "Communication via RMI to {0}:{1} failed.";

    private NetworkContact communicationContact;
    private RMISink stub;

    @Override
    public void initialize(NetworkContact contact) throws CommunicationException {
        
        if (contact == null){
            throw new IllegalArgumentException();
        }
        
        communicationContact = contact;
       
        try {
            Registry registry = LocateRegistry.getRegistry(communicationContact.getHost(), communicationContact.getPort());
            stub = (RMISink) registry.lookup(RMISinkImpl.RMI_METHOD_NAME);
        } catch (RemoteException e) {
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED, communicationContact.getHost(),
                communicationContact.getPort().toString()), e);
        } catch (NotBoundException e) {
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED, communicationContact.getHost(),
                communicationContact.getPort().toString()), e);
        }
    }

    @Override
    public ServiceCallResult send(ServiceCallRequest serviceCallRequest) throws CommunicationException {
        
        if (serviceCallRequest == null){
            throw new IllegalArgumentException();
        }
        
        try {
            return stub.call(serviceCallRequest);
        } catch (RemoteException e) {
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED,
                communicationContact.getHost(), String.valueOf(communicationContact.getPort())), e);
        } catch (RuntimeException e) {
            throw new CommunicationException(MessageFormat.format(ERROR_COMMUNICATION_FAILED,
                communicationContact.getHost(), String.valueOf(communicationContact.getPort())), e);
        }
    }

}
