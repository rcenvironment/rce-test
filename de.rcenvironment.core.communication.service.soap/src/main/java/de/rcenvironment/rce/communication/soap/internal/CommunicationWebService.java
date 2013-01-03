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

/**
 * The Web service interface for SOAP connections.
 * 
 * @author Tobias Menden
 */
@WebService
public interface CommunicationWebService {

    /**
     * Executes a {@link ServiceCallRequest} from a remote RCE instance on the local
     * system.
     * 
     * @param request The {@link SOAPCommunicationRequest} holding information what to do.
     * @return The {@link SOAPCommunicationResult} holding the result of the operation.
     * @throws SOAPException Thrown if the call fails.
     */
    SOAPCommunicationResult call(SOAPCommunicationRequest request) throws SOAPException;
}
