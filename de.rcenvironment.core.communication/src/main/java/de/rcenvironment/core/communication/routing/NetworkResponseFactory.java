/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing;

import java.io.Serializable;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.impl.NetworkResponseImpl;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * Convenience factory for {@link NetworkResponse}s.
 * 
 * @author Robert Mischke
 */
public final class NetworkResponseFactory {

    private NetworkResponseFactory() {}

    /**
     * Creates a response using raw response bytes.
     * 
     * @param request the associated request
     * @param responseBody the byte array to send as response payload
     * @return the generated response
     */
    public static NetworkResponse generateSuccessResponse(NetworkRequest request, byte[] responseBody) {
        NetworkResponseImpl response =
            new NetworkResponseImpl(responseBody, request.getRequestId(), NetworkResponse.RESULT_CODE_SUCCESS);
        return response;
    }

    /**
     * Creates a response using a Serializable response object.
     * 
     * @param request the associated request
     * @param responseBody the {@link Serializable} to send as response payload
     * @return the generated response
     * @throws SerializationException on serialization failure
     */
    public static NetworkResponse generateSuccessResponse(NetworkRequest request, Serializable responseBody) throws SerializationException {
        byte[] contentBytes = MessageUtils.serializeObject(responseBody);
        NetworkResponseImpl response =
            new NetworkResponseImpl(contentBytes, request.getRequestId(), NetworkResponse.RESULT_CODE_SUCCESS);
        return response;
    }

    /**
     * Generates a {@link NetworkResponse} indicating that an exception has occured at the final
     * destination of the request.
     * 
     * @param request the request
     * @param cause the exception
     * @return the generated response
     */
    public static NetworkResponse generateExceptionAtDestinationResponse(NetworkRequest request, Throwable cause) {
        // note: this assumes that all locally-generated exceptions are safe for serialization
        byte[] contentBytes = MessageUtils.serializeSafeObject(cause);
        NetworkResponseImpl response =
            new NetworkResponseImpl(contentBytes, request.getRequestId(), NetworkResponse.RESULT_CODE_EXCEPTION_AT_DESTINATION);
        return response;
    }

    /**
     * Generates a {@link NetworkResponse} indicating that an exception has occured while
     * forwarding/routing the request to its final destination.
     * 
     * @param request the request
     * @param eventNodeId the id of the node where the exception occured
     * @param cause the exception
     * @return the generated response
     */
    public static NetworkResponse generateExceptionWhileRoutingResponse(NetworkRequest request, String eventNodeId, Throwable cause) {
        // note: this assumes that all locally-generated exceptions are safe for serialization
        byte[] contentBytes = MessageUtils.serializeSafeObject(cause);
        // TODO actually set event node id
        NetworkResponseImpl response =
            new NetworkResponseImpl(contentBytes, request.getRequestId(), NetworkResponse.RESULT_CODE_EXCEPTION_WHILE_FORWARDING);
        return response;
    }
}
