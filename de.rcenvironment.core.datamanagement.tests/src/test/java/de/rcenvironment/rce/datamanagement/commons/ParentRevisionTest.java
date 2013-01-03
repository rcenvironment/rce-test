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

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;


/**
 * Tests cases for {@link ParentRevision}.
 *
 * @author Doreen Seider
 */
public class ParentRevisionTest {

    private ParentRevision parentRevision;
    private UUID identifier;
    private PlatformIdentifier pi;
    private int revisionNumber;

    /** Set up. */
    @Before
    public void setUp() {
        identifier = UUID.randomUUID();
        pi = PlatformIdentifierFactory.fromHostAndNumberString("knax:5");
        revisionNumber = 3;
        parentRevision = new ParentRevision(identifier, pi, revisionNumber);
    }

    /** Test. */
    @Test
    public void test() {
        parentRevision = new ParentRevision(identifier, pi, revisionNumber);
        assertEquals(identifier, parentRevision.getIdentifier());
        assertEquals(pi, parentRevision.getPlatformIdentifier());
        assertEquals(revisionNumber, parentRevision.getRevisionNumber());
    }
    
    /** Test. */
    @Test
    public void testClone() {
        parentRevision.clone();
    }
    
    /** Test. */
    @Test
    public void testEquals() {
        assertTrue(parentRevision.equals(parentRevision));
        assertTrue(parentRevision.equals(parentRevision.clone()));
        assertFalse(parentRevision.equals(new ParentRevision(identifier, pi, 5)));
    }

    /** Test. */
    @Test
    public void testHashCode() {
        parentRevision.hashCode();
    }

}
