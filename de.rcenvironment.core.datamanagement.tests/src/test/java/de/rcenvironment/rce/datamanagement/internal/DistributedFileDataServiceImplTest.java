/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.ReachabilityChecker;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.datamanagement.DataService;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.DistributableInputStream;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Test cases of {@link DistributedFileDataServiceImpl}.
 * 
 * @author Doreen Seider
 */
public class DistributedFileDataServiceImplTest {

    private final int read = 7;

    private DistributedFileDataServiceImpl dataService;

    private User certificateMock;

    private PlatformIdentifier pi;

    private UUID referenceID;

    private DataReference reference;

    private DataReference notReachableReference;

    private int number = 7;

    private InputStream is;

    private MetaDataSet mds;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        certificateMock = EasyMock.createNiceMock(User.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();
        reference = new DataReference(DataReferenceType.fileObject, referenceID, pi);
        notReachableReference =
            new DataReference(DataReferenceType.fileObject, referenceID,
                PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));
        is = new InputStream() {

            @Override
            public int read() throws IOException {
                return read;
            }
        };
        mds = new MetaDataSet();

        dataService = new DistributedFileDataServiceImpl();
        dataService.bindCommunicationService(new DummyCommunicationService());
        dataService.bindPlatformService(new DummyPlatformService());
        dataService.bindFileDataService(new DummyFileDataService());
        dataService.activate(EasyMock.createNiceMock(BundleContext.class));
    }

    /**
     * Test.
     * 
     * @throws IOException if an error occurs.
     */
    @Test
    public void testGetStreamFromDataReference() throws IOException {
        InputStream stream = dataService.getStreamFromDataReference(certificateMock, reference, number);
        assertEquals(read, stream.read());
        stream = dataService.getStreamFromDataReference(certificateMock, notReachableReference, number);
        assertNull(stream);
    }

    /**
     * Test.
     */
    @Test
    public void testNewReferenceFromStream() {
        DataReference dr = dataService.newReferenceFromStream(certificateMock, is, mds, pi);
        assertEquals(reference, dr);
        dr = dataService.newReferenceFromStream(certificateMock, is, mds, null);
        assertEquals(reference, dr);
        dr = dataService.newReferenceFromStream(certificateMock, is, mds, PlatformIdentifierFactory.fromHostAndNumberString("horst:7"));
        assertNull(dr);
    }

    /**
     * Test.
     */
    @Test
    public void testNewRevisionFromStream() {
        DataReference dr = dataService.newRevisionFromStream(certificateMock, reference, is, mds);
        assertEquals(reference, dr);
        dr = dataService.newRevisionFromStream(certificateMock, notReachableReference, is, mds);
        assertNull(dr);
    }

    /**
     * Test implementation of the {@link CommunicationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

        @Override
        public Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
            if (!forceRefresh) {
                Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
                pis.add(pi);
                pis.add(PlatformIdentifierFactory.fromHostAndNumberString("localhost:1"));
                return pis;
            }
            return null;
        }

        @Override
        public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
            throws IllegalStateException {
            return getService(iface, new HashMap<String, String>(), platformIdentifier, bundleContext);
        }

        @Override
        public Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier,
            BundleContext bundleContext) throws IllegalStateException {
            if (platformIdentifier.equals(pi)) {
                return new DummyFileDataService();
            } else {
                return new NotReachableDummyFileDataService();
            }
        }

        @Override
        public void checkReachability(ReachabilityChecker checker) {}
    }

    /**
     * Dummy implementation of {@link DataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDataService implements DataService {

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

    }

    /**
     * Test implementation of the {@link FileDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyFileDataService extends DummyDataService implements FileDataService {

        @Override
        public InputStream getStreamFromDataReference(User proxyCertificate, DataReference dataReference,
            Integer revisionNumber) throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && dataReference.equals(reference) && revisionNumber == number) {
                return is;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference newReferenceFromStream(User proxyCertificate, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && inputStream.equals(is)
                || proxyCertificate.equals(certificateMock) && inputStream instanceof DistributableInputStream) {
                return reference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference newRevisionFromStream(User proxyCertificate, DataReference dataReference, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && dataReference.equals(reference)
                && inputStream.equals(is) && metaDataSet.equals(mds)
                || proxyCertificate.equals(certificateMock) && dataReference.equals(reference)
                && inputStream instanceof DistributableInputStream && metaDataSet.equals(mds)) {
                return reference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {
            if (!proxyCertificate.equals(certificateMock) || !dataReference.equals(reference)) {
                throw new RuntimeException();
            }
        }

    }

    /**
     * Not reachable test implementation of the {@link FileDataService}.
     * 
     * @author Doreen Seider
     */
    private class NotReachableDummyFileDataService extends DataServiceImpl implements FileDataService {

        @Override
        public InputStream getStreamFromDataReference(User proxyCertificate, DataReference dataReference,
            Integer revisionNumber) throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }

        @Override
        public DataReference newReferenceFromStream(User proxyCertificate, InputStream inputStream, MetaDataSet metaDataSet)
            throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }

        @Override
        public DataReference newRevisionFromStream(User proxyCertificate, DataReference dataReference, InputStream inputStream,
            MetaDataSet metaDataSet) throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }

    }

    /**
     * Test implementation of the {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi;
        }

    }

}
