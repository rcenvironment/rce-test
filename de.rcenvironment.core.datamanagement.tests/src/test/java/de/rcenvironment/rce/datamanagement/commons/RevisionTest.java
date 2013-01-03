/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;


/**
 * Tests cases for {@link Revision}.
 *
 * @author Doreen Seider
 */
public class RevisionTest {

    private Revision revision;
    private URI location;
    private int revisionNumber;

    /** Set up.
     * @throws Exception if an error occurs.
     * */
    @Before
    public void setUp() throws Exception {
        location = new URI("ey");
        revisionNumber = 3;
        revision = new Revision(revisionNumber, location);
    }

    /** Test. */
    @Test
    public void test() {
        revision = new Revision(revisionNumber, location);
        assertEquals(revisionNumber, revision.getRevisionNumber());
        assertEquals(location, revision.getLocation());
    }
    
    /** Test. */
    @Test
    public void testClone() {
        revision.clone();
    }
    
    /** Test. */
    @Test
    public void testEquals() {
        assertTrue(revision.equals(revision));
        assertTrue(revision.equals(revision.clone()));
        assertFalse(revision.equals(new Revision(5, location)));
    }

    /** Test. */
    @Test
    public void testHashCode() {
        revision.hashCode();
    }
    
    /** Test. */
    @Test
    public void testToString() {
        revision.toString();
    }
    
    /** Test. */
    @Test
    public void testCompareTo() {
        Revision anotherRevision = new Revision(revisionNumber + 1, location);
        final int minusOne = -1;
        assertEquals(minusOne, revision.compareTo(anotherRevision));
        assertEquals(1, anotherRevision.compareTo(revision));
        assertEquals(0, revision.compareTo(new Revision(revisionNumber, location)));
    }

}
