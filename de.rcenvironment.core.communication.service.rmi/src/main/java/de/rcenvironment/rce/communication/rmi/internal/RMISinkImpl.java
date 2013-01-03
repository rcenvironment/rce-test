/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Implementation of the {@link RMISink} interface.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public final class RMISinkImpl extends UnicastRemoteObject implements RMISink {

    /**
     * Name for binding at the RMI registry.
     */
    public static final String RMI_METHOD_NAME = "RCE-Call";

    /**
     * Constant.
     */
    private static final String COLON = ":";

    /**
     * Serial UID.
     */
    private static final long serialVersionUID = -2398923478L;

    /**
     * Error thrown if binding to the RMI registry failed.
     */
    private static final String ERROR_COULD_NOT_BIND = "Could not bind method to RMI registry.";

    /**
     * Error thrown if unbinding from the RMI sink failed.
     */
    private static final String ERROR_COULD_NOT_UNBIND = "Could not unbind RMI sink.";

    /**
     * The RMI hostname property.
     */
    private static final String HOSTNAME_PROPERTY = "java.rmi.server.hostname";

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RMISinkImpl.class);

    /**
     * The RMI registry.
     */
    private static Registry registry = null;

    /**
     * The instance of this singleton.
     */
    private static RMISink instance = null;
    
    private static ServiceCallHandler serviceCallHandler;
        
    /**
     * The Constructor which simply calls the Constructor of <code>UnicastRemoteObject</code>.
     * 
     * @throws RemoteException Thrown if the server could not be started.
     */
    private RMISinkImpl() throws RemoteException {
        super();
    }

    @Override
    public ServiceCallResult call(ServiceCallRequest serviceCallRequest) throws RemoteException {

        Assertions.isDefined(serviceCallRequest, "The parameter \"communicationRequest\" must not be null.");

        try {
            return serviceCallHandler.handle(serviceCallRequest);
        } catch (CommunicationException e) {
            String call = serviceCallRequest.getService() + COLON
                + serviceCallRequest.getServiceMethod() + "@"
                + serviceCallRequest.getRequestedPlatform();

            LOGGER.warn("An error occured when a remote platform requested this instance: " + call, e);
            throw new RemoteException("Service call failed: " + call, e);
        }
    }

    /**
     * Start the {@link RMISink}.
     * 
     * @throws CommunicationException Thrown if {@link RMISinkImpl} could not be started.
     */
    protected static synchronized void start(RMIConfiguration configuration, ServiceCallHandler newServiceCallHandler)
        throws CommunicationException {

        serviceCallHandler = newServiceCallHandler;
        
        String host = ServiceHandler.getPlatformService().getServiceBindAddress();
        try {
            host = InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            throw new CommunicationException("Host unknown, could not start RMI registry: " + host , e);
        }
        System.setProperty(HOSTNAME_PROPERTY, host);

        // Already started?
        if (registry != null || instance != null) {
            throw new CommunicationException("RMI sink already started.");
        }

        // Detect port
        int port = configuration.getRegistryPort();

        // Try to start registry
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            throw new CommunicationException("Could not start RMI registry.", e);
        }

        // Start server
        RMISink server = null;
        try {
            server = new RMISinkImpl();
        } catch (RemoteException e) {
            // Cleanup
            stopRegistry();

            // Throw exception
            throw new CommunicationException("Could not start RMI sink.", e);
        }

        // Bind the call function
        try {
            registry.bind(RMI_METHOD_NAME, server);
        } catch (AccessException e) {
            stopRegistry();
            throw new CommunicationException(ERROR_COULD_NOT_BIND, e);
        } catch (RemoteException e) {
            stopRegistry();
            throw new CommunicationException(ERROR_COULD_NOT_BIND, e);
        } catch (AlreadyBoundException e) {
            stopRegistry();
            throw new CommunicationException(ERROR_COULD_NOT_BIND, e);
        }

        instance = server;

        LOGGER.info("Started RMI sink on port: " + port);
    }

    /**
     * Stop the {@link RMISink}.
     */
    protected static synchronized void stop() {
        try {
            registry.unbind(RMI_METHOD_NAME);
            stopRegistry();
            instance = null;
            LOGGER.info("Stopped RMI sink.");
        } catch (AccessException e) {
            LOGGER.warn(ERROR_COULD_NOT_UNBIND, e);
        } catch (RemoteException e) {
            LOGGER.warn(ERROR_COULD_NOT_UNBIND, e);
        } catch (NotBoundException e) {
            LOGGER.warn(ERROR_COULD_NOT_UNBIND, e);
        } catch (NullPointerException e) {
            LOGGER.warn(ERROR_COULD_NOT_UNBIND, e);
        }
    }

    /**
     * Stop the RMI {@link Registry}.
     */
    private static void stopRegistry() {
        try {
            UnicastRemoteObject.unexportObject(registry, true);
            registry = null;
        } catch (NoSuchObjectException e) {
            LOGGER.warn("Could not stop the RMI registry.");
        }
    }
}
