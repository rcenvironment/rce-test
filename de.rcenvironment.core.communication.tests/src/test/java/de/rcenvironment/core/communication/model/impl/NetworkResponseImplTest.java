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

import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;
import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * Test case for {@link NetworkResponseImpl}.
 * 
 * @author Robert Mischke
 */
public class NetworkResponseImplTest {

    /**
     * Verifies the basic content serialization/deserialization round-trip.
     * 
     * @throws SerializationException on unexpected errors
     */
    @Test
    public void contentSerialization() throws SerializationException {
        String testString = "test";
        NetworkResponseImpl instance1 =
            new NetworkResponseImpl(MessageUtils.serializeObject(testString), MetaDataWrapper.createEmpty().getInnerMap());
        assertEquals(testString, instance1.getDeserializedContent());
        NetworkResponseImpl instance2 = new NetworkResponseImpl(instance1.getContentBytes(), instance1.accessRawMetaData());
        assertEquals(testString, instance2.getDeserializedContent());
    }

}
