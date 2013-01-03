/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;


/**
 * Test cases for {@link Input}.
 *
 * @author Doreen Seider
 */
public class InputTest {

    /** Test. */
    @Test
    public void test() {
        final String name = "unheimlich cooler name";
        final Class<? extends Serializable> type = Integer.class;
        final Serializable value = new Integer(9);
        final String workflowId = "puuuh";
        final String compId = "ui";
        Input input = new Input(name, type, value, workflowId, compId, 7);
        
        assertEquals(name, input.getName());
        assertEquals(type, input.getType());
        assertEquals(value, input.getValue());
        assertEquals(workflowId, input.getWorkflowIdentifier());
        assertEquals(compId, input.getComponentIdentifier());
        
        input.setValue(workflowId);
        
        assertEquals(workflowId, input.getValue());
    }
}
