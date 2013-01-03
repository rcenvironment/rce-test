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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;


/**
 * Test cases for {@link DataReference}.
 *
 * @author Juergen Klein
 * @author Doreen Seider
 */
public class DataReferenceTest {

    private final int revisionNumber = 123;
    private DataReference dataReference;
    private PlatformIdentifier pi;
    private ParentRevision parentRevision;
    private URI location;

    /**
     * Set up.
     * 
     * @throws Exception if an error occurs.
     */
    @Before
    public void setUp() throws Exception {
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:3");
        dataReference = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi);
        parentRevision = new ParentRevision(UUID.randomUUID(), pi, revisionNumber);
        location = new URI("ftp://url");
    }

    /** Test. */
    @Test
    public void testHashCode() {
        assertNotNull(dataReference.hashCode());
        assertTrue(dataReference.hashCode() == dataReference.hashCode());
        assertTrue(dataReference.hashCode() != new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi).hashCode());
        
        DataReference branchedDataReference = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi, parentRevision);

        assertNotNull(branchedDataReference.getParentRevision().hashCode());
        assertFalse(branchedDataReference.hashCode() == dataReference.hashCode());
        assertTrue(branchedDataReference.getParentRevision().hashCode() == parentRevision.hashCode());
        
    }

    /** Test. */
    @Test
    public void testConstructor() {
        assertTrue(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi) instanceof DataReference);
    }


    /** Test. */
    @Test
    public void testAddRevision() {
        assertNull(dataReference.getRevision(revisionNumber));
        dataReference.addRevision(revisionNumber, location);
        assertNotNull(dataReference.getRevision(revisionNumber));
        assertEquals(dataReference.getRevision(revisionNumber).getRevisionNumber(), revisionNumber);
    }

    /** Test. */
    @Test
    public void testClear() {
        dataReference.addRevision(revisionNumber, location);
        dataReference.addRevision(2, location);
        dataReference.addRevision(3, location);
        assertEquals(3, dataReference.getRevisionNumbers().length);
        dataReference.clear();
        assertEquals(0, dataReference.getRevisionNumbers().length);
        assertNull(dataReference.getRevision(revisionNumber));
    }

    /** Test. */
    @Test
    public void testClone() {
        assertEquals(dataReference, dataReference.clone());
        DataReference dr = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi, parentRevision);
        dr.addRevision(revisionNumber, location);
        assertEquals(dr, dr.clone());

    }

    /** Test. */
    @Test
    public void testEquals() {
        assertTrue(dataReference.equals(dataReference));
        assertFalse(dataReference.equals(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi)));
        DataReference dataReferenceClone = dataReference.clone();
        assertTrue(dataReference.equals(dataReferenceClone));
        dataReferenceClone.addRevision(Integer.MAX_VALUE, location);
        assertFalse(dataReference.equals(dataReferenceClone));
        DataReference dr = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi, parentRevision);
        assertFalse(dr.equals(dataReference));
    }

    /** Test. */
    @Test
    public void testGetGuid() {
        UUID uuid = UUID.randomUUID();
        DataReference dr = new DataReference(DataReferenceType.fileObject, uuid, pi);
        assertNotNull(dr.getIdentifier());
        assertEquals(uuid, dr.getIdentifier());
    }

    /** Test. */
    @Test
    public void testGetHighestRevisionNumber() {
        dataReference.addRevision(revisionNumber, location);
        assertEquals(revisionNumber, dataReference.getHighestRevisionNumber());
        dataReference.addRevision(Integer.MAX_VALUE, location);
        assertEquals(Integer.MAX_VALUE, dataReference.getHighestRevisionNumber());
    }

    /** Test. */
    @Test
    public void testGetLocation() {
        assertNull(dataReference.getLocation(revisionNumber));
        dataReference.addRevision(revisionNumber, location);
        URI anotherLocation = null;
        try {
            anotherLocation = new URI("funzt");
        } catch (URISyntaxException e) {
            fail();
        }
        dataReference.addRevision(Integer.MAX_VALUE, anotherLocation);
        assertEquals(location, dataReference.getLocation(revisionNumber));
        assertEquals(anotherLocation, dataReference.getLocation(Integer.MAX_VALUE));
    }

    /** Test. */
    @Test
    public void testGetParentRevision() {
        assertNull(dataReference.getParentRevision());
        UUID identifier = UUID.randomUUID();
        ParentRevision pr = new ParentRevision(identifier, pi, revisionNumber);
        DataReference dr = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi, pr);
        assertNotNull(dr.getParentRevision());
        assertEquals(pi, dr.getParentRevision().getPlatformIdentifier());
        assertEquals(identifier, dr.getParentRevision().getIdentifier());
    }

    /** Test. */
    @Test
    public void testGetPlatformIdentifier() {
        assertEquals(pi, dataReference.getPlatformIdentifier());
    }

    /** Test. */
    @Test
    public void testGetRevision() {
        assertNull(dataReference.getRevision(revisionNumber));
        assertNull(dataReference.getRevision(DataReference.HEAD_REVISION));
        dataReference.addRevision(revisionNumber, location);
        assertNotNull(dataReference.getRevision(revisionNumber));
        assertNotNull(dataReference.getRevision(DataReference.HEAD_REVISION));
        assertEquals(dataReference.getRevision(revisionNumber), dataReference.getRevision(DataReference.HEAD_REVISION));
        URI anotherLocation = null;
        try {
            anotherLocation = new URI("funzt");
        } catch (URISyntaxException e) {
            fail();
        }
        dataReference.addRevision(Integer.MAX_VALUE, anotherLocation);
        assertFalse(dataReference.getRevision(revisionNumber).equals(dataReference.getRevision(DataReference.HEAD_REVISION)));
        assertEquals(dataReference.getRevision(Integer.MAX_VALUE), dataReference.getRevision(DataReference.HEAD_REVISION));
        assertEquals(revisionNumber, dataReference.getRevision(revisionNumber).getRevisionNumber());
        assertEquals(location, dataReference.getRevision(revisionNumber).getLocation());
    }

    /** Test. */
    @Test
    public void testGetRevisionNumbers() {
        dataReference.addRevision(Integer.MAX_VALUE, location);
        dataReference.addRevision(revisionNumber, location);
        dataReference.addRevision(Integer.MAX_VALUE / 2, location);
        int[] revisionNumbers = dataReference.getRevisionNumbers();
        assertEquals(revisionNumbers[0], revisionNumber);
        assertEquals(revisionNumbers[1], Integer.MAX_VALUE / 2);
        assertEquals(revisionNumbers[2], Integer.MAX_VALUE);
    }

    /** Test. */
    @Test
    public void testGetType() {
        assertEquals(DataReferenceType.fileObject, dataReference.getDataType());
    }

    /** Test. */
    @Test
    public void testIsValidRevision() {
        assertFalse(dataReference.isValidRevision(revisionNumber));
        dataReference.addRevision(revisionNumber, location);
        assertTrue(dataReference.isValidRevision(revisionNumber));
    }

    /** Test. */
    @Test
    public void testIterator() {
        dataReference.addRevision(Integer.MAX_VALUE, location);
        dataReference.addRevision(revisionNumber, location);
        dataReference.addRevision(Integer.MAX_VALUE / 2, location);
        Iterator<Revision> iterator = dataReference.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        iterator.next();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    /** Test. */
    @Test
    public void testRemoveRevision() {
        dataReference.addRevision(Integer.MAX_VALUE, location);
        assertNotNull(dataReference.getRevision(Integer.MAX_VALUE));
        assertFalse(dataReference.removeRevision(revisionNumber));
        assertTrue(dataReference.removeRevision(Integer.MAX_VALUE));
        assertNull(dataReference.getRevision(Integer.MAX_VALUE));
    }

    /** Test. */
    @Test
    public void testSize() {
        assertEquals(dataReference.size(), 0);
        dataReference.addRevision(Integer.MAX_VALUE, location);
        dataReference.addRevision(revisionNumber, location);
        dataReference.addRevision(Integer.MAX_VALUE / 2, location);
        assertEquals(dataReference.size(), 3);
        dataReference.removeRevision(Integer.MAX_VALUE);
        assertEquals(dataReference.size(), 2);
    }

    /** Test. */
    @Test
    public void testToString() {
        dataReference.addRevision(Integer.MAX_VALUE, location);
        assertNotNull(dataReference.toString());
    }

}
