/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.scheduler;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;

import org.junit.Test;

import de.rcenvironment.rce.component.endpoint.Input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.is;


/**
 * Unit tests.
 * 
 * @version $LastChangedRevision: 0 $
 * @author Arne Bachmann
 */
public class DefaultSchedulerTest {

    private final String xA = "A";
    private final String xB = "B";
    private final String xC = "C";
    
    /**
     * In the beginning, there was an error with incoming unmodifiable collections, should be fixed and confirmed by this test. 
     */
    @Test
    @SuppressWarnings({ "serial" })
    public void testSameNameError() {
        final DefaultScheduler s = new DefaultScheduler(new Hashtable<String, Class<? extends Serializable>>() {
            {
                put(xA, String.class);
                put(xB, Double.class);
            }
        }, /* dynamic channels */ new HashSet<String>() {
            {
                add(xA); // same as static: wrong!
                add(xC);
            }
        });
        assertFalse(s.getDynamicInputs().keySet().contains(xA));
        assertFalse(s.getDynamicInputs().keySet().contains(xB));
        assertTrue(s.getDynamicInputs().keySet().contains(xC));
    }
    
    /**
     * Test each method of the class.
     */
    @SuppressWarnings("serial")
    @Test
    public void testMethods() {
        final double x34 = 34D;
        final String xAbc = "abc";
        // test source component logic
        final DefaultScheduler a = new DefaultScheduler(null, null, /* is a source */ true);
        assertTrue(a.mustRun(true));
        
        // test all methods
        final DefaultScheduler s = new DefaultScheduler(new Hashtable<String, Class<? extends Serializable>>() {
            {
                put(xA, String.class);
            }
        }, new HashSet<String>() {
            {
                add(xB);
                add(xC);
            }
        });
        assertFalse(s.areAllDynamicValuesAvailable());
        assertFalse(s.areAnyFurtherMustRunValuesAvailable());
        assertFalse(s.areAnyMustRunValuesAvailable());
        assertThat(s.getLastValue(xA, String.class), is((String) null));
        assertFalse(s.mustRun());
        assertFalse(s.mustRun(true));
        assertFalse(s.check(new Input(xB, Double.class, x34, "", "", 6))); // one dynamic doesn't suffice
        assertTrue(s.check(new Input(xA, String.class, xAbc, "", "", 9))); // one static always suffices
        assertFalse(s.mustRun(true)); // don't run on initial check unless we are a source
        assertFalse(s.mustRun(false)); // not all dynamic available yet
        assertTrue(s.check(new Input(xC, Float.class, 4F, "", "", 7)));
        assertTrue(s.mustRun(false)); // not all dynamic available yet
        assertTrue(s.mustRun()); // still needs to run unless consumed the values
        assertThat(s.consumeValue(xA, String.class), is(xAbc)); // consume value
        assertThat((Double) s.getDynamicInputs().get(xB).poll().getValue(), is(x34)); // consume dynamic value
        assertFalse(s.mustRun()); // still needs to run unless consumed the values
        assertThat(s.getLastValue(xB, Double.class), is((Double) null)); // cannot get last value for dynamic channel)
        assertThat(s.getLastValue(xA, String.class), is(xAbc)); // still there
    }

}
