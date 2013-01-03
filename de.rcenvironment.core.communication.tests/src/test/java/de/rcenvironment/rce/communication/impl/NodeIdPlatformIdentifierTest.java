/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.communication.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * @author Robert Mischke
 */
public class NodeIdPlatformIdentifierTest {

    /**
     * Verifies that {@link NodeIdPlatformIdentifier} behaves properly on serialization.
     * 
     * @throws SerializationException on unexpected exceptions
     */
    @Test
    public void serializationRoundtrip() throws SerializationException {
        NodeIdPlatformIdentifier original = new NodeIdPlatformIdentifier("id");
        NodeIdPlatformIdentifier deserialized =
            (NodeIdPlatformIdentifier) MessageUtils.deserializeObject(MessageUtils.serializeObject(original),
                NodeIdPlatformIdentifier.class);
        assertEquals(original.getNodeId(), deserialized.getNodeId());
        // check object identity of the assigned meta information holder
        assertTrue(original.getMetaInformationHolder() == deserialized.getMetaInformationHolder());
    }

}
