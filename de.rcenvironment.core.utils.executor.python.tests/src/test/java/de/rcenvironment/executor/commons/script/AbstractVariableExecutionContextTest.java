/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.commons.script;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;


/**
 * Coverage.
 *
 * @author Arne Bachmann
 */
public class AbstractVariableExecutionContextTest {

    /**
     * Test everything.
     */
    @SuppressWarnings("boxing")
    @Test
    public void test() {
        assertThat(new AbstractVariableExecutionContext() {
            {
                setInputArrays(new LinkedList<VariantArray>());
                setOutputArrays(new ArrayList<String>());
                setInputVariables(new LinkedList<BoundVariable>());
                setOutputVariables(new ArrayList<BoundVariable>());
            }
        }.get(AbstractVariableExecutionContext.INPUT_ARRAYS, SerializableList.class).size(), is(0));
    }

}
