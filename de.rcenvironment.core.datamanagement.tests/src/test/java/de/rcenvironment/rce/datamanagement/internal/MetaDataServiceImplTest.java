/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;


/**
 * Test cases for {@link MetaDataServiceImpl}.
 *
 * @author Juergen Klein
 */
public class MetaDataServiceImplTest {

    private User certificate;
    private PlatformIdentifier pi;
    private UUID drId;
    private int revNumber;
    
    private DataReference dr;
    
    private MetaDataServiceImpl metaDataServiceImpl;

    /** Set up. */
    @Before
    public void setUp() {
        pi = PlatformIdentifierFactory.fromHostAndNumberString("na klar:6");
        drId = UUID.randomUUID();
        revNumber = 8;
        
        certificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(certificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(certificate);
        
        dr = new DataReference(DataReferenceType.fileObject, drId , pi);
        
        metaDataServiceImpl = new MetaDataServiceImpl();
        
        new BackendSupportTest().setUp();
        new BackendSupport().activate(BackendSupportTest.createBundleContext(new DummyCatalogBackend(), null));
    }


    /** Test. */
    @Test
    public void testGetMetaDataSet() {
        assertNotNull(metaDataServiceImpl.getMetaDataSet(certificate, dr, revNumber));
    }
    
    /** Test. */
    @Test
    public void testUpdateMetaDataSetForSuccess() {
        metaDataServiceImpl.updateMetaDataSet(certificate, dr, DataReference.FIRST_REVISION, new MetaDataSet(), true);
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
            return null;
        }

        @Override
        public void updateRevisions(DataReference reference) {
        }

        @Override
        public Collection<DataReference> executeQuery(Query query, int maxResults) {
            return null;
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(Query query, Integer maxResults) {
            return null;
        }

        @Override
        public MetaDataSet getMetaDataSet(UUID referenceIdentifier, int revisionNr) {
            if (referenceIdentifier.equals(drId) && revisionNr == revNumber) {
                return new MetaDataSet();
            }
            return null;
        }

        @Override
        public void storeMetaDataSet(UUID referenceIdentifier, int revisionNr, MetaDataSet metaDataSet,
                boolean includeRevisionIndependent) {          
        }

        @Override
        public boolean isLockedDataReference(UUID dataReferenceUUID) {
            if (dataReferenceUUID.equals(drId)) {
                return false;
            }
            return true;
        }


        @Override
        public boolean releaseLockedDataReference(UUID dataReferenceUUID, User proxyCertificate) {
            return false;
        }

        @Override
        public void lockDataReference(UUID dataReferenceUUID, User proxyCertificate) {
        }
        
        
    }

}
