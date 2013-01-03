/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;


/**
 * Test cases for {@link QueryServiceImpl}.
 *
 * @author Doreen Seider
 */
public class QueryServiceImplTest {

    private int max = 9;
    private User certificate;
    private Collection<DataReference> drs;

    private PlatformIdentifier pi;
    private UUID drId;
    private DataReference dr;
    
    private QueryServiceImpl queryServiceImpl;

    /** Test. */
    @Before
    public void setUp() {
        certificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(certificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(certificate);
        drs = new ArrayList<DataReference>();
        
        pi = PlatformIdentifierFactory.fromHostAndNumberString("na klar:6");
        drId = UUID.randomUUID();
        dr = new DataReference(DataReferenceType.fileObject, drId , pi);
        
        queryServiceImpl = new QueryServiceImpl();
        
        new BackendSupportTest().setUp();
        new BackendSupport().activate(BackendSupportTest.createBundleContext(new DummyCatalogBackend(), new DummyDataBackend()));
    }

    /** Test. */
    @Test
    public void testExecuteQuery() {
        assertEquals(drs, queryServiceImpl.executeQuery(certificate, Query.ALL, max));
    }
    
    /** Test. */
    @Test
    public void testGetReference() {
        assertEquals(dr, queryServiceImpl.getReference(certificate, drId));
    }

    /**
     * Test implementation of {@link CatalogBackend}.
     * @author Doreen Seider
     */
    private class DummyCatalogBackend implements CatalogBackend {

        @Override
        public void storeReference(DataReference reference) {
        }

        @Override
        public boolean deleteReference(UUID referenceIdentifier) {
            return false;
        }

        @Override
        public DataReference getReference(UUID referenceIdentifier) {
            if (referenceIdentifier.equals(drId)) {
                return dr;
            }
            return null;
        }

        @Override
        public void updateRevisions(DataReference reference) {
        }

        @Override
        public Collection<DataReference> executeQuery(Query query, int maxResults) {
            if (query.equals(Query.ALL) && maxResults == max) {
                return drs;
            }
            return null;
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(Query query, Integer maxResults) {
            return null;
        }

        @Override
        public MetaDataSet getMetaDataSet(UUID referenceIdentifier, int revisionNr) {
            return null;
        }

        @Override
        public void storeMetaDataSet(UUID referenceIdentifier, int revisionNr, MetaDataSet metaDataSet,
                boolean includeRevisionIndependent) {          
        }

        @Override
        public boolean isLockedDataReference(UUID dataReferenceUUID) {
            return false;
        }


        @Override
        public boolean releaseLockedDataReference(UUID dataReferenceUUID, User proxyCertificate) {
            return false;
        }

        @Override
        public void lockDataReference(UUID dataReferenceUUID, User proxyCertificate) {
        }
        
        
    }

    /**
     * Test implementation of {@link DataBackend}.
     * @author Doreen Seider
     */
    private class DummyDataBackend implements DataBackend {

        @Override
        public URI suggestLocation(UUID guid, int revision) {
            return null;
        }

        @Override
        public long put(URI location, Object object) {
            return 0;
        }

        @Override
        public boolean delete(URI location) {
            return false;
        }

        @Override
        public Object get(URI location) {
            return null;
        }
        
    }
}
