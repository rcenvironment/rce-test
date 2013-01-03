/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Test cases for {@link ServiceCallHandler}.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class ServiceCallHandlerDummy implements ServiceCallHandler {
    @Override
    public ServiceCallResult handle(ServiceCallRequest serviceCallRequestHandleIn) throws CommunicationException {
        if (!serviceCallRequestHandleIn.getRequestedPlatform().equals(SOAPTestConstants.COMM_OBJECT.getRequestedPlatform())) {
            throw new CommunicationException("handle Method from Service Call Handler failed.");
        }
        return new ServiceCallResult(SOAPTestConstants.RETURN_VALUE);
    }
}
