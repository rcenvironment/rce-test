/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.MetaDataService;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Test cases for {@link DataServiceImpl}.
 * 
 * @author Doreen Seider
 */
public class DataServiceImplTest {

    private User certificate;

    private PlatformIdentifier pi;

    private UUID drId;

    private int revNumber;

    private FileDataServiceImpl fileDataService;

    private DataReference dr;

    private DataReference newestDr;

    /** Set up. */
    @Before
    public void setUp() {
        pi = PlatformIdentifierFactory.fromHostAndNumberString("na klar:6");
        drId = UUID.randomUUID();
        revNumber = 8;

        certificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(certificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(certificate);

        dr = new DataReference(DataReferenceType.fileObject, drId, pi);
        newestDr = new DataReference(DataReferenceType.fileObject, UUID.randomUUID(), pi);

        fileDataService = new FileDataServiceImpl();
        fileDataService.bindCommunicationService(new DummyCommunicationService());
        fileDataService.bindPlatformService(new PlatformServiceDefaultStub());
        fileDataService.activate(EasyMock.createNiceMock(BundleContext.class));

        new BackendSupportTest().setUp();
        new BackendSupport().activate(BackendSupportTest.createBundleContext(new DummyCatalogBackend(), new DummyDataBackend()));
    }

    /** Test. */
    @Test
    public void testBranch() {
        fileDataService.branch(certificate, dr, revNumber);
    }

    /** Test. */
    @Test
    public void testDeleteReference() {
        fileDataService.deleteReference(certificate, dr);
    }

    /** Test. */
    @Test
    @Ignore("No idea why this should work - the implementation forbids deleting the most current revision ... "
        + "set to 'ignore' after changes though (by Christian Weiss)")
    public void testDeleteRevision() {
        fileDataService.deleteRevision(certificate, dr, DataReference.HEAD_REVISION);
    }

    /**
     * Test implementation of the {@link CommunicationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            Object service = null;
            if (iface.equals(QueryService.class)) {
                service = new DummyQueryService();
            } else if (iface.equals(MetaDataService.class)) {
                service = new DummyMetaDataService();
            } else if (iface.equals(FileDataService.class)) {
                service = new DummyFileDataService();
            }
            return service;
        }

    }

    /**
     * Test implementation of the {@link QueryService}.
     * 
     * @author Doreen Seider
     */
    private class DummyQueryService implements QueryService {

        @Override
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
            if (proxyCertificate == certificate && drId.equals(dataReferenceGuid)) {
                return newestDr;
            }
            return null;
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

    }

    /**
     * Test implementation of the {@link MetaDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyMetaDataService implements MetaDataService {

        @Override
        public void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
            MetaDataSet metaDataSet, boolean includeRevisionIndependent) throws AuthorizationException {

        }

        @Override
        public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
            throws AuthorizationException {
            if (proxyCertificate == certificate && dataReference.equals(newestDr) && revisionNumber == revNumber) {
                return new MetaDataSet();
            }
            return null;
        }

    }

    /**
     * Test implementation of the {@link FileDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyFileDataService implements FileDataService {

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {}

        @Override
        public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
            throws AuthorizationException {
            return null;
        }

        @Override
        public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision)
            throws AuthorizationException {
            return null;
        }

        @Override
        public InputStream getStreamFromDataReference(User proxyCertificate, DataReference dataReference,
            Integer revisionNumber) throws AuthorizationException {
            if (proxyCertificate == certificate && dataReference.equals(newestDr) && revisionNumber == revNumber) {
                return new InputStream() {

                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
            }
            return null;
        }

        @Override
        public DataReference newReferenceFromStream(User proxyCertificate, InputStream inputStream, MetaDataSet metaDataSet)
            throws AuthorizationException {
            return null;
        }

        @Override
        public DataReference newRevisionFromStream(User proxyCertificate, DataReference dataReference, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException {
            return null;
        }

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
                return newestDr;
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
            return null;
        }

    }
}
