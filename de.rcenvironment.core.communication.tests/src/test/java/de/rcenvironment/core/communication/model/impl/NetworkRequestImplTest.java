/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.model.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * Test case for {@link NetworkRequestImpl}.
 * 
 * @author Robert Mischke
 */
public class NetworkRequestImplTest {

    /**
     * Verifies the basic content serialization/deserialization round-trip.
     * 
     * @throws SerializationException on unexpected errors
     */
    @Test
    public void contentSerialization() throws SerializationException {
        String testString = "test";
        NetworkRequestImpl instance1 = new NetworkRequestImpl(testString, MetaDataWrapper.createEmpty().getInnerMap());
        assertEquals(testString, instance1.getDeserializedContent());
        NetworkRequestImpl instance2 = new NetworkRequestImpl(instance1.getContentBytes(), instance1.accessRawMetaData(), "dummy");
        assertEquals(testString, instance2.getDeserializedContent());
    }

}
