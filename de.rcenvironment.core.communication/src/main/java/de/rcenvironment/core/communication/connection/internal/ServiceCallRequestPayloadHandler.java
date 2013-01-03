/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection.internal;

import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * A {@link RequestPayloadHandler} implementation that delegates to the "old"
 * {@link ServiceCallHandler} service. This is the default implementation used in actual RCE
 * instances.
 * 
 * @author Robert Mischke
 */
public class ServiceCallRequestPayloadHandler implements RequestPayloadHandler {

    private static final long SLOW_SERVICE_CALL_LOGGING_THRESHOLD_NANOS = 2 * 1000000000L;

    private static final float NANOS_PER_SECOND = 1000000000f;

    // the service of the "old" communication layer used to dispatch remote service calls to
    private ServiceCallHandler serviceCallHandlerDelegate;

    @Override
    public NetworkResponse handleRequest(NetworkRequest request) throws CommunicationException, SerializationException {
        ServiceCallRequest serviceCallRequest = (ServiceCallRequest) request.getDeserializedContent();
        long startTime = System.nanoTime();
        ServiceCallResult result = serviceCallHandlerDelegate.handle(serviceCallRequest);
        long duration = System.nanoTime() - startTime;
        if (duration > SLOW_SERVICE_CALL_LOGGING_THRESHOLD_NANOS) {
            LogFactory.getLog(getClass()).debug(
                "Slow service call (" + (duration / NANOS_PER_SECOND) + " sec): " + serviceCallRequest.getService() + "#"
                    + serviceCallRequest.getServiceMethod() + "; caller=" + serviceCallRequest.getCallingPlatform() + ", target="
                    + serviceCallRequest.getRequestedPlatform());
        }
        if (result.getThrowable() != null) {
            return NetworkResponseFactory.generateExceptionAtDestinationResponse(request, result.getThrowable());
        }
        return NetworkResponseFactory.generateSuccessResponse(request, result.getReturnValue());
    }

    protected void bindServiceCallHandler(ServiceCallHandler serviceCallHandler) {
        this.serviceCallHandlerDelegate = serviceCallHandler;
    }

}
