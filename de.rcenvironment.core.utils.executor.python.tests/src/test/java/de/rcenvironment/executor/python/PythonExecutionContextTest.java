/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.executor.python;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.commons.variables.VariableType;


/**
 * Tests if a script really runs.
 * 
 * @version $LastChangedRevision: 10553 $
 * @author Arne Bachmann
 */
public class PythonExecutionContextTest {

    /**
     * Test constant.
     */
    private static final double VALUE = 345.435D;
    
    /**
     * Test construction of the context object.
     */
    @SuppressWarnings({ "serial", "boxing" })
    @Test
    public void testConstruction() {
        // actually the same as a normal ExecutionContext
        final PythonExecutionContext c = new PythonExecutionContext("pass");
        assertTrue(c.containsKey("os"));
        c.setInputVariables(new ArrayList<BoundVariable>());
        assertTrue(c.containsKey("inputVariables"));
        assertThat(c.get("inputVariables", SerializableList.class).size(), is(0));
        c.setOutputVariables(new ArrayList<BoundVariable>() {
            {
                add(new BoundVariable("bla", VariableType.Real, "345.435"));
            }
        });
        assertTrue(c.containsKey("outputVariables"));
        @SuppressWarnings("unchecked") final List<BoundVariable> list =
            c.get("outputVariables", SerializableList.class);
        assertNotNull(list);
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getRealValue(), is(VALUE));
    }
    
}
