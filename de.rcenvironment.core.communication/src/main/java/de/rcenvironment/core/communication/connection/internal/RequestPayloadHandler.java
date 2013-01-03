/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection.internal;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * An interface for the processing of {@link NetworkRequest}s at their final destination. It exists
 * to allow injection of custom request handlers in integrations tests; in actual RCE instances, a
 * handler delegating to local OSGi services is used.
 * 
 * @author Robert Mischke
 */
public interface RequestPayloadHandler {

    /**
     * Performs the handling of a {@link NetworkRequest} at its final destination.
     * 
     * @param request the request
     * @return the generated response; null is not permitted
     * @throws CommunicationException on messaging errors
     * @throws SerializationException on serialization or deserialization failure
     */
    NetworkResponse handleRequest(NetworkRequest request) throws CommunicationException, SerializationException;

}
