/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import java.util.Calendar;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;


/**
 * Tests for class {@link MetaDataQuery}.
 * 
 * FIXME: clean up if SQL logic is removed
 *
 * @author Juergen Klein
 */
public class MetaDataQueryTest extends TestCase {

    /**
     * A test meta data value.
     */
    private static final String TEST_AUTHOR = "testAuthor";
    
    /**
     * Class under test.
     */
    private MetaDataQuery metaDataQuery;

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("gehtDoch:4");
    /**
     * {@inheritDoc}
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        metaDataQuery = new MetaDataQuery();
    }

    /**
     * {@inheritDoc}
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, java.lang.String, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataStringIntIntForSuccess() {
        metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, TEST_AUTHOR, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, java.lang.String, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataStringIntIntForFailure() {
        try {
            metaDataQuery.addMetaDataConstraint(null, TEST_AUTHOR, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, "", DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataKeyExistsConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, int, int)}.
     */
    public void testAddMetaDataKeyExistsConstraintForSuccess() {
        metaDataQuery.addMetaDataKeyExistsConstraint(MetaData.AUTHOR, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataKeyExistsConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, int, int)}.
     */
    public void testAddMetaDataKeyExistsConstraintForFailure() {
        try {
            metaDataQuery.addMetaDataKeyExistsConstraint(null, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, long, long, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataLongLongIntIntForSuccess() {
        metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, 0, 1, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, long, long, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataLongLongIntIntForFailure() {
        try {
            metaDataQuery.addMetaDataConstraint(null, 0, 1, DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, java.util.Date, java.util.Date, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataDateDateIntIntForSuccess() {
        metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(),
            DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addMetaDataConstraint(de.rcenvironment.rce.datamanagement.commons.MetaData, java.util.Date, java.util.Date, int, int)}.
     */
    public void testAddMetaDataConstraintMetaDataDateDateIntIntForFailure() {
        try {
            metaDataQuery.addMetaDataConstraint(null, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(),
                DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, null, Calendar.getInstance().getTime(),
                DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addMetaDataConstraint(MetaData.AUTHOR, Calendar.getInstance().getTime(), null,
                DataReference.FIRST_REVISION, DataReference.HEAD_REVISION);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addTypeConstraint(de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType[])}.
     */
    public void testAddTypeConstraintForSuccess() {
        DataReferenceType[] dataReferenceTypes = new DataReferenceType[] {DataReferenceType.fileObject};
        metaDataQuery.addTypeConstraint(dataReferenceTypes);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addTypeConstraint(de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType[])}.
     */
    public void testAddTypeConstraintForFailure() {
        try {
            metaDataQuery.addTypeConstraint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addTypeConstraint(new DataReferenceType[]{});
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery#addHasParentConstraint()}.
     */
    public void testAddHasParentConstraint() {
        metaDataQuery.addHasParentConstraint();
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addParentConstraint(de.rcenvironment.rce.datamanagement.commons.DataReference, int, int)}.
     */
    public void testAddParentConstraintForSuccess() {
        metaDataQuery.addParentConstraint(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), 
                                                            pi), 1, 1);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery
     * #addParentConstraint(de.rcenvironment.rce.datamanagement.commons.DataReference, int, int)}.
     */
    public void testAddParentConstraintForFailure() {
        try {
            metaDataQuery.addParentConstraint(null, 1, 1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addParentConstraint(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), 
                                                                pi), 0, 1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            metaDataQuery.addParentConstraint(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), 
                                                                pi), 1, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery#getQuery()}.
     */
    public void testGetSQLQuery() {
        metaDataQuery.getQuery();
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery#toString()}.
     */
    public void testToString() {
        metaDataQuery.toString();
    }

    /**
     * Test method for {@link de.rcenvironment.rce.datamanagement.commons.MetaDataQuery#clear()}.
     */
    public void testClear() {
        metaDataQuery.clear();
    }

    /** Test. */
    public void testCreateRevisionConstraint() {
        final String constraintT11 = metaDataQuery.createRevisionConstraint(true, 1, 1);
        final String independant = "(<T>REVISION_NUMBER = 0)";
        Assert.assertEquals(independant, constraintT11);
        final String constraintF11 = metaDataQuery.createRevisionConstraint(false, 1, 1);
        Assert.assertEquals("(<T>REVISION_NUMBER = 1)", constraintF11);
        final String constraintT12 = metaDataQuery.createRevisionConstraint(true, 1, 2);
        Assert.assertEquals(independant, constraintT12);
        final String constraintF12 = metaDataQuery.createRevisionConstraint(false, 1, 2);
        Assert.assertEquals("(<T>REVISION_NUMBER BETWEEN 1 AND 2)", constraintF12);
        final String constraintTN12 = metaDataQuery.createRevisionConstraint(true, -1, 2);
        Assert.assertEquals(independant, constraintTN12);
        final String constraintFN12 = metaDataQuery.createRevisionConstraint(false, -1, 2);
        Assert.assertEquals("(<T>REVISION_NUMBER BETWEEN head.REVISION_NUMBER AND 2)", constraintFN12);
    }

    /** Test. */
    public void testCreateRevisionConstraintForFailure() {
        final int[][] testdatas = new int[][] { { 2, 1 }, { 0, -2}, { -2, 7 } };
        for (final int[] testdata : testdatas) {
            try {
                metaDataQuery.createRevisionConstraint(true, testdata[0], testdata[1]);
                fail();
            } catch (IllegalArgumentException e) {
                e = null;
            }
            try {
                metaDataQuery.createRevisionConstraint(false, testdata[0], testdata[1]);
                fail();
            } catch (IllegalArgumentException e) {
                e = null;
            }
        }
    }

}
