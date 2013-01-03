/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.service.internal.ServiceCallHandlerImplTest;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;

/**
 * 
 * Dummy service call request sender to simulate endless loop.
 * 
 * @author Doreen Seider
 */
public class ServiceCallSenderDummy implements ServiceCallSender {

    @Override
    public ServiceCallResult send(ServiceCallRequest serviceCallRequest) throws CommunicationException {
        return ServiceCallHandlerImplTest.getCallHandler().handle(serviceCallRequest);
    }

    @Override
    public void initialize(NetworkContact contact) throws CommunicationException {}

}
