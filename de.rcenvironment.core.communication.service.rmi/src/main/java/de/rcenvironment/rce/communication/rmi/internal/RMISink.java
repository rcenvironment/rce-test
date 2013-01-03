/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Interface for the RMI Sink.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public interface RMISink extends Remote {

    /**
     * Method that executes a {@link ServiceCallRequest} from a remote RCE instance on the local
     * system.
     * 
     * @param serviceCallRequest The {@link ServiceCallRequest} holding information what to do.
     * @return The {@link ServiceCallResult} holding the result of the operation.
     * @throws RemoteException Thrown if the method call fails.
     */
    ServiceCallResult call(ServiceCallRequest serviceCallRequest) throws RemoteException;

}
