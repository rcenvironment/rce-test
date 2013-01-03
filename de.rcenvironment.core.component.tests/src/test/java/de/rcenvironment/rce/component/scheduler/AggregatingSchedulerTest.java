/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.scheduler;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

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
public class AggregatingSchedulerTest {

    private final String xA = "A";
    private final String xB = "B";
    
    /**
     * In the beginning, there was an error with incoming unmodifiable collections, should be fixed and confirmed by this test. 
     */
    @Test
    @SuppressWarnings({ "serial" })
    public void testSameNameError() {
        final AggregatingScheduler s = new AggregatingScheduler(new Hashtable<String, Class<? extends Serializable>>() {
            {
                put(xA, String.class);
                put(xB, Double.class);
            }
        });
        assertFalse(s.areAllValuesAvailable());
        assertFalse(s.check(new Input(xA, String.class, "a", "na", "guuuut", 9)));
        assertFalse(s.check(new Input(xA, String.class, "a2", "ach", "komm", 8)));
        assertTrue(s.check(new Input(xB, String.class, "b", "jaja", "ok", 6)));
        assertTrue(s.check(new Input(xB, String.class, "b2", "ey", "ja", 6)));
        assertFalse(s.mustRun(true)); // never run on initial
        assertTrue(s.mustRun(false));
        final Map<String, Serializable> map = s.consumeValues();
        assertTrue(s.mustRun(false)); // one more to go in the queues
        assertTrue(map != null);
        assertTrue(map.containsKey(xA));
        assertTrue(map.containsKey(xB));
        assertThat((String) map.get(xA), is("a"));
        assertThat((String) map.get(xB), is("b"));
        assertThat(map.size(), is(2));
        assertTrue(s.mustRun(false));
        s.consumeValues();
        assertFalse(s.mustRun(false)); // should be empty by now
    }
    
}
