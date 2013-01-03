/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;

/**
 * 
 * Implementation of {@link ServiceCallSenderFactory}.
 * Creates RMI {@link ServiceCallSender} objects.
 *
 * @author Doreen Seider
 */
public class RMIServiceCallSenderFactory implements ServiceCallSenderFactory {

    @Override
    public ServiceCallSender createServiceCallSender(NetworkContact contact) throws CommunicationException {
        Assertions.isDefined(contact, "The parameter \"contact\" must not be null.");
        RMIServiceCallSender requestSender = new RMIServiceCallSender();
        requestSender.initialize(contact);
        return requestSender;
    }

}
