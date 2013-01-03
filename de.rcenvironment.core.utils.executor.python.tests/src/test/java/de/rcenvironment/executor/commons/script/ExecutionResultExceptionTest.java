/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.commons.script;

import org.junit.Test;

import de.rcenvironment.commons.SerializableListImpl;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.commons.variables.VariableType;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;


/**
 * Improve test coverage.
 * @author Arne Bachmann
 */
public class ExecutionResultExceptionTest {

    /**
     * Everything there is to test in this class.
     */
    @SuppressWarnings({ "boxing", "serial" })
    @Test
    public void test() {
        final ExecutionResultException ere = new ExecutionResultException("bla",
            new SerializableListImpl<BoundVariable>() {
                {
                    add(new BoundVariable("x", 1.0));
                    add(new BoundVariable("y", true));
                }
            },
            new SerializableListImpl<String>() {
                {
                    add("--");
                }
            });
        assertThat(ere.getParsedVariables().size(), is(2));
        assertThat(ere.getParsedVariables().get(1).getName(), is("y"));
        assertThat(ere.getParsedVariables().get(1).getType(), is(VariableType.Logic));
        
        assertThat(ere.getUnparseableVariables().size(), is(1));
        assertThat(ere.getUnparseableVariables().get(0), is("--"));
    }

}
