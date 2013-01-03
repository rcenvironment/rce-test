/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Test cases for {@link FileDataServiceImpl}.
 * 
 * @author Juergen Klein
 */
public class FileDataServiceImplTest {

    private User certificate;

    private URI loc;

    private PlatformIdentifier pi;

    private UUID drId;

    private int revNumber;

    private DataReference dr;

    private DataReference anotherDr;

    private FileDataServiceImpl fileDataService;

    /** Set up. */
    @Before
    public void setUp() {
        try {
            loc = new URI("aha://naja");
        } catch (URISyntaxException e) {
            fail();
        }
        pi = PlatformIdentifierFactory.fromHostAndNumberString("naklar:6");
        drId = UUID.randomUUID();
        revNumber = 8;

        certificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(certificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(certificate);

        dr = new DataReference(DataReferenceType.fileObject, drId, pi);
        dr.addRevision(revNumber, loc);
        anotherDr = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi);

        fileDataService = new FileDataServiceImpl();
        fileDataService.bindPlatformService(new PlatformServiceDefaultStub());

        new BackendSupportTest().setUp();
        new BackendSupport().activate(BackendSupportTest.createBundleContext(new DummyCatalogBackend(), new DummyDataBackend()));
    }

    /** Test. */
    @Test
    public void testGetStreamFromDataReference() {
        fileDataService.getStreamFromDataReference(certificate, dr, revNumber);
    }

    /** Test. */
    @Test
    public void testNewReferenceFromStream() {
        InputStream is = new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        fileDataService.newReferenceFromStream(certificate, is, null);
    }

    /** Test. */
    @Test
    public void testNewRevisionFromStream() {
        InputStream is = new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        fileDataService.newRevisionFromStream(certificate, dr, is, null);
    }

    /**
     * Test implementation of {@link CatalogBackend}.
     * 
     * @author Doreen Seider
     */
    private class DummyCatalogBackend implements CatalogBackend {

        @Override
        public void storeReference(DataReference reference) {}

        @Override
        public boolean deleteReference(UUID referenceIdentifier) {
            return false;
        }

        @Override
        public DataReference getReference(UUID referenceIdentifier) {
            if (referenceIdentifier.equals(drId)) {
                return anotherDr;
            }
            return null;
        }

        @Override
        public void updateRevisions(DataReference reference) {}

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
            return null;
        }

        @Override
        public void storeMetaDataSet(UUID referenceIdentifier, int revisionNr, MetaDataSet metaDataSet,
            boolean includeRevisionIndependent) {}

        @Override
        public boolean isLockedDataReference(UUID dataReferenceUUID) {
            return false;
        }

        @Override
        public boolean releaseLockedDataReference(UUID dataReferenceUUID, User proxyCertificate) {
            return false;
        }

        @Override
        public void lockDataReference(UUID dataReferenceUUID, User proxyCertificate) {}

    }

    /**
     * Test implementation of {@link DataBackend}.
     * 
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
            if (location.equals(loc)) {
                return new InputStream() {

                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
            }
            return null;
        }

    }

}
