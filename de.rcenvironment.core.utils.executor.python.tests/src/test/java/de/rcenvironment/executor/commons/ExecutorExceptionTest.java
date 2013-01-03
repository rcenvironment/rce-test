/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.commons;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;


/**
 * Improve test coverage.
 * @author Arne Bachmann
 */
public class ExecutorExceptionTest {

    /**
     * Everything there is to test in this class.
     */
    @Test
    public void test() {
        assertThat(new ExecutorException("bla").getMessage(), is("bla"));
        try {
            throw new ExecutorException("a", new NullPointerException());
        } catch (final ExecutorException e) {
            assertThat(e.getMessage(), is("a"));
            assertTrue(e.getCause().getClass() == NullPointerException.class);
        }
    }

}
