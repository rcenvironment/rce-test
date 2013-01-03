/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * This class provides a dummy implementation of the RMI sink.
 * 
 * @author Heinrich Wendel
 */
public class RMISinkDummy extends UnicastRemoteObject implements RMISink {
    
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 7209329056147772091L;

    /**
     * Constructor.
     * 
     * @throws RemoteException
     *             Thrown if failed.
     */
    protected RMISinkDummy() throws RemoteException {
        super();
    }

    @Override
    public ServiceCallResult call(ServiceCallRequest communicationRequest) throws RemoteException {
        if (communicationRequest.getServiceMethod().equals(RMITestConstants.UNKNOWN_METHOD)) {
            throw new RemoteException();
        }
        return new ServiceCallResult(RMITestConstants.RETURN_VALUE);
    }

}
