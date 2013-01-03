/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Test cases for ConsoleRow.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class ConsoleRowTest {

    private static final int PREVENT_TIMESTAMP_COLLISION_WAIT_TIME_MSEC = 100;

    private final String workflow = "Kottbusser Platz";

    private final String component = "Eberswalder Strasse";

    private final String text = "Schoenhauser Allee";

    private final ConsoleRow.Type type = ConsoleRow.Type.STDOUT;

    /** Test. */
    @Test
    public void test() {
        ConsoleRow row = new ConsoleRow(workflow, component, type, text);

        assertTrue(row.getComponent().equals(component));
        assertTrue(row.getText().equals(text));
        assertNotNull(row.getTimestamp());
        assertTrue(row.getType().equals(type));
        assertTrue(row.getWorkflow().equals(workflow));
        assertTrue(row.toString().contains(component));
        assertTrue(row.toString().contains(text));
        assertTrue(row.toString().contains(type.toString()));
        assertTrue(row.toString().contains(workflow));

        ConsoleRow row1 = new ConsoleRow(workflow, component, type, text);
        ConsoleRow row2 = new ConsoleRow(workflow, component, type, "another text");

        assertTrue(row2.compareTo(row1) > 0);

        row1.setNumber(1);
        row2.setNumber(2);

        assertTrue(row2.compareTo(row1) > 0);

        // prevent rows 1 and 3 having the same timestamp by accident
        try {
            Thread.sleep(PREVENT_TIMESTAMP_COLLISION_WAIT_TIME_MSEC);
        } catch (InterruptedException e) {
            Assert.fail();
        }

        // create row with same data and new number;
        // should not be equal due to timestamp and number
        ConsoleRow row3 = new ConsoleRow(workflow, component, type, text);
        row3.setNumber(3);
        assertFalse(row3.equals(row1));
        assertFalse(row1.equals(row3));

        // set to same number; should still not be equal due to timestamp
        row3.setNumber(1);
        assertFalse(row3.equals(row1));
        assertFalse(row1.equals(row3));

        // set to same timestamp; now they should be equal
        row3.setTimestamp(row1.getTimestamp());
        assertTrue(row3.equals(row1));
        assertTrue(row1.equals(row3));

        assertEquals(row1.toString().hashCode(), row1.hashCode());

        ConsoleRow row4 = row1.clone();
        assertTrue(row4.equals(row1));
        assertTrue(row1.equals(row4));

    }
}
