/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.spi;

import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * This interface describes the methods that the different service call sender has to implement.
 * 
 * @author Thijs Metsch
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public interface ServiceCallSender {

    /**
     * This methods initializes the connection parameters required for the connection. It must be
     * called before send is called.
     * 
     * @param contact Information that describes how to contact the communication partner.
     * @throws CommunicationException Thrown if the communication failed.
     */
    void initialize(NetworkContact contact) throws CommunicationException;

    /**
     * This method establishes a connection to another RCE platform and calls the method described
     * in the {@link ServiceCallRequest}. It returns the result as {@link ServiceCallResult}.
     * 
     * TODO specify if all implementations are expected to be thread-safe - misc_ro
     * 
     * @param serviceCallRequest The {@link ServiceCallRequest} object describing the remote service
     *        method call.
     * @return The result of the remote service method call call as {@link ServiceCallResult}.
     * @throws CommunicationException Thrown if the communication failed.
     */
    ServiceCallResult send(ServiceCallRequest serviceCallRequest) throws CommunicationException;

}
