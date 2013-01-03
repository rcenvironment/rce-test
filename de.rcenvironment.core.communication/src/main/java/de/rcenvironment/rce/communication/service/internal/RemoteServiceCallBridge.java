/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.internal;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.routing.NetworkRoutingService;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;

/**
 * A temporary class to allow switching between "new" and "old" remote service calling.
 * 
 * TODO obsolete; remove
 * 
 * @author Robert Mischke
 */
public final class RemoteServiceCallBridge {

    private static volatile NetworkRoutingService newRoutingService;

    private RemoteServiceCallBridge() {}

    /**
     * Sets the {@link NetworkRoutingService} implementation to use. If one is set, the "new"
     * communication layer is used; if it is left unset (or reset to "null"), the "old"
     * communication layer is used.
     * 
     * @param newRoutingService the routing service implementation
     */
    public static void setNewRoutingService(NetworkRoutingService newRoutingService) {
        RemoteServiceCallBridge.newRoutingService = newRoutingService;
    }

    /**
     * Temporary migration class to centralize all remote service calls. This makes it possible to
     * switch between the new and old communication layers in a single location.
     * 
     * @param serviceCallRequest the call request
     * @return the call result
     * @throws CommunicationException on failure
     */
    public static ServiceCallResult performRemoteServiceCall(ServiceCallRequest serviceCallRequest) throws CommunicationException {
        if (newRoutingService != null) {

            Future<NetworkResponse> responseFuture;
            try {
                responseFuture = newRoutingService.performRoutedRequest(serviceCallRequest, serviceCallRequest.getRequestedPlatform());
            } catch (SerializationException e) {
                throw new CommunicationException("Failed to serialize service call request", e);
            }
            try {
                Serializable resultContent = responseFuture.get().getDeserializedContent();
                return new ServiceCallResult(resultContent);
            } catch (InterruptedException e) {
                throw new CommunicationException(e);
            } catch (ExecutionException e) {
                throw new CommunicationException(e);
            } catch (SerializationException e) {
                throw new CommunicationException(e);
            }
        } else {
            ServiceCallSender requestSender = ServiceCallSenderSupport.getServiceCallSender(serviceCallRequest);
            return requestSender.send(serviceCallRequest);
        }
    }

}
