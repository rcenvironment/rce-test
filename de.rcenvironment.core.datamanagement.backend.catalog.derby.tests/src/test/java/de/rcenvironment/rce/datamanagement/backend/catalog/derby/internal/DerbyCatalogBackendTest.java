/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.FileSupport;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.commons.DMQLQuery;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.DataReferenceQuery;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataQuery;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResult;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;
import de.rcenvironment.rce.datamanagement.commons.Revision;

/**
 * Test cases for {@link DerbyCatalogBackend}.
 * 
 * @author Juergen Klein
 */
public class DerbyCatalogBackendTest {

    private static File tempDirectory;

    private DerbyCatalogBackend derbyCatalogBackend;

    private final String bundleName = "superderby";

    private User certificate;

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("passt:3");

    private final UUID drId = UUID.randomUUID();

    private final DataReference dr = new DataReference(DataReferenceType.fileObject, drId, pi);

    private MetaDataSet metaDataSet;

    private Revision revision;

    /**
     * Sets the whole test case up.
     * 
     */
    @BeforeClass
    public static void setUpTestcase() {
        try {
            final File file = File.createTempFile("derby_test_", ".db");
            file.delete();
            file.mkdir();
            tempDirectory = file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // perform db startup so the time is not added to the run time of the first test case
        new DerbyCatalogBackendTest().setUp();
    }

    /** Set up. */
    @Before
    public void setUp() {
        certificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(certificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(certificate);

        try {
            dr.addRevision(2, new URI("test:/location"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        metaDataSet = new MetaDataSet();
        metaDataSet.setValue(new MetaData("testkey", true), "testvalue");

        try {
            revision = new Revision(Integer.MAX_VALUE, new URI("efs:///" + dr.getIdentifier() + "/" + "file-" + Integer.MAX_VALUE));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        derbyCatalogBackend = new DerbyCatalogBackend();
        derbyCatalogBackend.bindConfigurationService(new DummyConfigurationService());
        derbyCatalogBackend.bindPlatformService(new DummyPlatformService());

        Bundle bundle = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundle.getSymbolicName()).andReturn(bundleName).anyTimes();
        EasyMock.replay(bundle);
        BundleContext bundleContext = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContext.getBundle()).andReturn(bundle).anyTimes();
        EasyMock.replay(bundleContext);

        derbyCatalogBackend.activate(bundleContext);
    }

    /** Tear down. */
    @After
    public void tearDown() {
        derbyCatalogBackend.deactivate();
    }

    /**
     * Tears down the whole test case.
     * 
     */
    @AfterClass
    public static void tearDownTestcase() {
        FileSupport.deleteFile(tempDirectory);
    }

    /** Test. */
    @Test
    public void testExecuteQuery() {
        UUID uuid = UUID.randomUUID();
        derbyCatalogBackend.storeReference(new DataReference(DataReferenceType.fileObject, uuid, pi));

        // test specific query
        DataReferenceQuery query = new DataReferenceQuery(uuid);
        Collection<DataReference> dataReferences = new HashSet<DataReference>();
        dataReferences = derbyCatalogBackend.executeQuery(query, 1);
        assertEquals(1, dataReferences.size());
        DataReference dataReference = dataReferences.iterator().next();
        assertNotNull(dataReference);
        assertEquals(uuid, dataReference.getIdentifier());

        // test query for more drs
        for (int i = 0; i < 10; i++) {
            derbyCatalogBackend.storeReference(new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi));
        }

        Collection<DataReference> moreDataReferences = new HashSet<DataReference>();
        moreDataReferences = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        DataReference reference = null;
        Iterator<DataReference> iterator = moreDataReferences.iterator();
        while (iterator.hasNext()) {
            reference = iterator.next();
            assertNotNull(reference.getIdentifier());
        }

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testExecuteMetaDataQuery() {
        final Insert insert = new Insert(derbyCatalogBackend);
        insert.createDataReference().set("test.node", "true", true).set("test.leaf", "true", true).store();

        final UUID uuid = insert.getUUID();
        final Map<String, String> testValues = insert.getValues();

        MetaDataQuery metaDataQuery = new MetaDataQuery();
        metaDataQuery.addMetaDataKeyExistsConstraint(new MetaData("test.node", true));

        MetaDataResultList results = derbyCatalogBackend.executeMetaDataQuery(metaDataQuery, Integer.MAX_VALUE);
        assertNotNull(results);
        MetaDataResult result = results.getResultById(uuid);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(uuid, result.getId());
        assertNotNull(result.getMetaDataSet());

        int resultsSize = 0;
        for (final Iterator<MetaData> iter = result.getMetaDataSet().iterator(); iter.hasNext();) {
            iter.next();
            resultsSize++;
        }
        assertEquals(testValues.size(), resultsSize);
        for (final String key : testValues.keySet()) {
            final MetaData metaData = new MetaData(key, true);
            assertEquals(testValues.get(key), result.getMetaDataSet().getValue(metaData));
        }
    }

    /** Test. */
    @Test
    public void testExecuteDMQLQuery() {
        final String v1 = "test.dmql.value1";
        final String v2 = "test.dmql.value2";
        final String v11 = "abc";
        final String v12 = "def";
        final String v21 = "123";
        final String v22 = "456";
        final String v222 = "789";
        new Insert(derbyCatalogBackend).createDataReference().addRevision().set(v1, v11).set(v2, v21).store();
        new Insert(derbyCatalogBackend).createDataReference().addRevision().set(v1, v11).set(v2, v22).store();
        new Insert(derbyCatalogBackend).createDataReference().addRevision().set(v1, v12).set(v2, v21).store();
        final Insert insert = new Insert(derbyCatalogBackend).createDataReference().addRevision().set(v1, v12).set(v2, v22).store();
        insert.addRevision().store().set(v2, v222).store();

        DMQLQuery query;
        MetaDataResultList results;

        query = new DMQLQuery("SELECT test.dmql.value1, test.dmql.value2");
        results = derbyCatalogBackend.executeMetaDataQuery(query, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(4, results.size());

        query = new DMQLQuery("SELECT test.dmql.value1, test.dmql.value2 GROUP BY test.dmql.value1, test.dmql.value2");
        results = derbyCatalogBackend.executeMetaDataQuery(query, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(4, results.size());

        query = new DMQLQuery("SELECT test.dmql.value1 GROUP BY test.dmql.value1");
        results = derbyCatalogBackend.executeMetaDataQuery(query, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(2, results.size());

        query = new DMQLQuery("SELECT test.dmql.value1, MIN(test.dmql.value2) GROUP BY test.dmql.value1");
        results = derbyCatalogBackend.executeMetaDataQuery(query, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /** Test. */
    @Test
    public void testInsertNewReference() {
        UUID uuid = UUID.randomUUID();
        DataReference dataReference = new DataReference(DataReferenceType.fileObject, uuid, pi);
        derbyCatalogBackend.storeReference(dataReference);
        // test specific query
        DataReferenceQuery query = new DataReferenceQuery(uuid);
        Collection<DataReference> dataReferences = derbyCatalogBackend.executeQuery(query, 1);
        DataReference reference = dataReferences.iterator().next();
        assertNotNull(reference);
        assertEquals(uuid, reference.getIdentifier());
        assertEquals(pi, reference.getPlatformIdentifier());

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testDeleteReference() {
        UUID uuid = UUID.randomUUID();
        DataReference dataReference = new DataReference(DataReferenceType.fileObject, uuid, pi);
        derbyCatalogBackend.storeReference(dataReference);

        // test specific query
        DataReferenceQuery query = new DataReferenceQuery(uuid);
        Collection<DataReference> dataReferences = derbyCatalogBackend.executeQuery(query, 1);
        DataReference reference = dataReferences.iterator().next();
        assertNotNull(reference);
        assertEquals(uuid, reference.getIdentifier());

        derbyCatalogBackend.deleteReference(dataReference.getIdentifier());

        // test specific query
        DataReferenceQuery queryAfterDelete = new DataReferenceQuery(uuid);
        Collection<DataReference> queryResultAfterDelete = derbyCatalogBackend.executeQuery(queryAfterDelete, 1);
        assertTrue(queryResultAfterDelete.isEmpty());

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testGetReference() {
        DataReference dataReference = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi);
        derbyCatalogBackend.storeReference(dataReference);
        DataReference reference = derbyCatalogBackend.getReference(dataReference.getIdentifier());
        assertNotNull(reference);
        assertTrue(reference instanceof DataReference);

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testUpdateRevisions() {
        derbyCatalogBackend.storeReference(dr);

        dr.addRevision(Integer.MAX_VALUE, revision.getLocation());

        derbyCatalogBackend.updateRevisions(dr);
        // test if revision is stored
        DataReference dataReferenceAfterUpdate = derbyCatalogBackend.getReference(dr.getIdentifier());
        assertTrue(dataReferenceAfterUpdate.getHighestRevisionNumber() == Integer.MAX_VALUE);

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testGetMetaDataSet() {
        dr.addRevision(DataReference.FIRST_REVISION, revision.getLocation());
        derbyCatalogBackend.storeReference(dr);
        derbyCatalogBackend.storeMetaDataSet(dr.getIdentifier(), DataReference.HEAD_REVISION, metaDataSet, true);
        MetaDataSet mds = derbyCatalogBackend.getMetaDataSet(dr.getIdentifier(), DataReference.HEAD_REVISION);
        // method was removed
        Iterator<MetaData> iterator = mds.iterator();
        while (iterator.hasNext()) {
            MetaData metaData = iterator.next();
            assertTrue(metaData.getKey().equals("testkey"));
            assertTrue(mds.getValue(metaData).equals("testvalue"));
        }

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testStoreMetaDataSet() {
        derbyCatalogBackend.storeReference(dr);
        derbyCatalogBackend.storeMetaDataSet(dr.getIdentifier(), DataReference.HEAD_REVISION, metaDataSet, true);

        // delete references after test
        Collection<DataReference> dataReferencesDeleteOnExit = derbyCatalogBackend.executeQuery(Query.ALL, Integer.MAX_VALUE);
        Iterator<DataReference> iteratorDeleteOnExit = dataReferencesDeleteOnExit.iterator();
        while (iteratorDeleteOnExit.hasNext()) {
            DataReference referenceFromIterator = iteratorDeleteOnExit.next();
            derbyCatalogBackend.deleteReference(referenceFromIterator.getIdentifier());
        }
    }

    /** Test. */
    @Test
    public void testIsLockedDataReference() {
        UUID dataReferenceUUID = UUID.randomUUID();
        derbyCatalogBackend.lockDataReference(dataReferenceUUID, certificate);
        assertTrue(derbyCatalogBackend.isLockedDataReference(dataReferenceUUID));
        assertFalse(derbyCatalogBackend.isLockedDataReference(UUID.randomUUID()));
    }

    /** Test. */
    @Test
    public void testLockDataReference() {
        derbyCatalogBackend.lockDataReference(UUID.randomUUID(), certificate);
    }

    /** Test. */
    @Test
    public void testReleaseLockedDataReference() {
        UUID dataReferenceUUID = UUID.randomUUID();
        derbyCatalogBackend.lockDataReference(dataReferenceUUID, certificate);
        assertTrue(derbyCatalogBackend.isLockedDataReference(dataReferenceUUID));
        derbyCatalogBackend.releaseLockedDataReference(dataReferenceUUID, certificate);
        assertFalse(derbyCatalogBackend.isLockedDataReference(dataReferenceUUID));
    }

    /**
     * Test implementation of {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi;
        }
    }

    /**
     * Test implementation of <code>ConfigurationService</code>.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            if (identifier.equals(bundleName) && clazz == DerbyCatalogBackendConfiguration.class) {
                return (T) new DerbyCatalogBackendConfiguration();
            }
            return null;
        }

        @Override
        public String getPlatformHome() {
            return tempDirectory.getAbsolutePath();
        }

        @Override
        public String getConfigurationArea() {
            return tempDirectory.getAbsolutePath();
        }

    }

    /**
     * Helper for insertions.
     * 
     * @author Christian Weiss
     */
    protected class Insert {

        private final UUID uuid;

        private final Map<String, String> values = new HashMap<String, String>();

        private final MetaDataSet metaDataSet = new MetaDataSet();

        private final CatalogBackend backend;

        private DataReference dataReference;

        private boolean stored = false;

        protected Insert() {
            this(null, null);
        }

        protected Insert(final CatalogBackend backend) {
            this(null, backend);
        }

        protected Insert(final UUID uuid, final CatalogBackend backend) {
            if (uuid == null) {
                this.uuid = UUID.randomUUID();
            } else {
                this.uuid = uuid;
            }
            this.backend = backend;
        }

        protected UUID getUUID() {
            return uuid;
        }

        protected Map<String, String> getValues() {
            return Collections.unmodifiableMap(values);
        }

        protected Insert createDataReference() {
            setDataReference(new DataReference(DataReferenceType.fileObject, uuid, pi));
            return this;
        }

        protected Insert setDataReference(final DataReference reference) {
            if (!uuid.equals(reference.getIdentifier())) {
                throw new RuntimeException("UUID does not match.");
            }
            this.dataReference = reference;
            return this;
        }

        protected Insert addRevision() {
            if (dataReference == null) {
                createDataReference();
            }
            final int hightestRevision = dataReference.getHighestRevisionNumber();
            final int revisionNumber;
            if (hightestRevision < DataReference.FIRST_REVISION) {
                revisionNumber = DataReference.FIRST_REVISION;
            } else {
                revisionNumber = hightestRevision + 1;
            }
            dataReference.addRevision(revisionNumber, URI.create("ftp://loc-" + UUID.randomUUID().toString()));
            return this;
        }

        protected Insert set(final String key, final String value) {
            return set(key, value, false);
        }

        protected Insert set(final String key, final String value, final boolean revisionIndependent) {
            values.put(key, value);
            final MetaData metaData = new MetaData(key, revisionIndependent);
            metaDataSet.setValue(metaData, value);
            return this;
        }

        protected Insert store() {
            if (!stored) {
                if (dataReference != null) {
                    backend.storeReference(dataReference);
                }
                stored = true;
            }
            backend.storeMetaDataSet(uuid, DataReference.HEAD_REVISION, metaDataSet, true);
            return this;
        }

    }

}
