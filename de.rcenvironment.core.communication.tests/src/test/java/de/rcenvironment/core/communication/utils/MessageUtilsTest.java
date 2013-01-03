/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;

/**
 * {@link MessageUtils} test case.
 * 
 * @author Robert Mischke
 */
public class MessageUtilsTest {

    /**
     * Tests basic serialization/deserialization.
     * 
     * @throws SerializationException on unexpected errors
     */
    @Test
    public void basicRoundTrip() throws SerializationException {
        // create arbitrary serializable object
        NetworkNodeInformationImpl testObject = new NetworkNodeInformationImpl();
        String testValue = "test value";
        testObject.setDisplayName(testValue);
        // perform round-trip
        byte[] serialized = MessageUtils.serializeObject(testObject);
        Serializable restored1 = MessageUtils.deserializeObject(serialized);
        NetworkNodeInformationImpl restored2 = MessageUtils.deserializeObject(serialized, NetworkNodeInformationImpl.class);
        // verify
        assertTrue(restored1 instanceof NetworkNodeInformationImpl);
        assertEquals(testValue, ((NetworkNodeInformation) restored1).getDisplayName());
        assertEquals(testValue, restored2.getDisplayName());
    }

    /**
     * Tests proper serialization/deserialization of 'null'.
     * 
     * @throws SerializationException on unexpected errors
     */
    @Test
    public void nullRoundTrip() throws SerializationException {
        byte[] serialized = MessageUtils.serializeObject(null);
        Serializable restored1 = MessageUtils.deserializeObject(serialized);
        NetworkNodeInformationImpl restored2 = MessageUtils.deserializeObject(serialized, NetworkNodeInformationImpl.class);
        // verify
        assertNotNull(serialized);
        assertNull(restored1);
        assertNull(restored2);
    }

    /**
     * Verifies that null is not accepted as a serialized form. but causes an exception on
     * deserialization instead.
     * 
     * @throws SerializationException on unexpected errors
     */
    @Test(expected = SerializationException.class)
    public void exceptionOnNullDeserialization() throws SerializationException {
        MessageUtils.deserializeObject(null);
    }

}
