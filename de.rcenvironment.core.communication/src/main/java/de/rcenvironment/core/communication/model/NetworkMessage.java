/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model;

import java.io.Serializable;
import java.util.Map;

import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * Represents a single message sent over the communication layer. In the typical request-response
 * communication flow, both the request and the response are {@link NetworkMessage}s.
 * 
 * TODO review: add more convenience methods?
 * 
 * @author Robert Mischke
 */
public interface NetworkMessage {

    /**
     * Provides access to the raw payload bytes without triggering deserialization.
     * 
     * @return the raw payload byte array
     */
    byte[] getContentBytes();

    /**
     * Provides access to the deserialized payload; the result of the deserialization may be cached
     * internally.
     * 
     * @return the result of deserializing the payload byte array
     * @throws SerializationException on deserialization failure
     */
    Serializable getDeserializedContent() throws SerializationException;

    /**
     * Provides access to the internal metadata map. Changes to the returned map affect the internal
     * state of the {@link NetworkMessage}.
     * 
     * @return a mutable reference to the internal metadata map
     */
    Map<String, String> accessRawMetaData();
}
