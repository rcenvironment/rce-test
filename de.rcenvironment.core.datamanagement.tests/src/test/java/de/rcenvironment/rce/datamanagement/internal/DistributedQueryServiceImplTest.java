/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
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
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Test cases for {@link DistributedQueryServiceImpl}.
 * 
 * @author Doreen Seider
 */
public class DistributedQueryServiceImplTest {

    private DistributedQueryServiceImpl queryService;

    private User certificateMock;

    private Query queryMock;

    private PlatformIdentifier pi;

    private UUID referenceID;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        certificateMock = EasyMock.createNiceMock(User.class);
        queryMock = EasyMock.createNiceMock(Query.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();

        queryService = new DistributedQueryServiceImpl();
        queryService.bindCommunicationService(new DummyCommunicationService());
        queryService.bindPlatformService(new DummyPlatformService());
    }

    /**
     * Test.
     */
    @Test
    public void testExecuteQueryGlobally() {
        Collection<DataReference> references = queryService.executeQuery(certificateMock, queryMock, 10);
        assertNotNull(references);
        assertEquals(1, references.size());
    }

    /**
     * Test.
     */
    @Test
    public void testExecuteQuery() {
        Collection<DataReference> references = queryService.executeQuery(certificateMock, queryMock, 10, pi);
        assertNotNull(references);
        assertEquals(1, references.size());
        references = queryService.executeQuery(certificateMock, queryMock, 10, null);
        assertNotNull(references);
        assertEquals(1, references.size());
        references =
            queryService.executeQuery(certificateMock, queryMock, 10, PlatformIdentifierFactory.fromHostAndNumberString("horst:3"));
        assertNotNull(references);
        assertEquals(0, references.size());
    }

    /**
     * Test.
     */
    @Test
    public void testGetReferenceGlobally() {
        DataReference reference = queryService.getReference(certificateMock, referenceID);
        assertNotNull(reference);
        assertEquals(pi, reference.getPlatformIdentifier());
    }

    /**
     * Test.
     */
    @Test
    public void testGetReference() {
        DataReference reference = queryService.getReference(certificateMock, referenceID, pi);
        assertNotNull(reference);
        assertEquals(pi, reference.getPlatformIdentifier());
        reference = queryService.getReference(certificateMock, referenceID, (PlatformIdentifier) null);
        assertNotNull(reference);
        assertEquals(pi, reference.getPlatformIdentifier());
        reference = queryService.getReference(certificateMock, referenceID, PlatformIdentifierFactory.fromHostAndNumberString("kex:321"));
        assertNull(reference);
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
                return new DummyQueryService();
            } else {
                return new NotReachableDummyQueryService();
            }
        }
    }

    /**
     * Test implementation of the {@link QueryService}.
     * 
     * @author Doreen Seider
     */
    private class DummyQueryService implements QueryService {

        @SuppressWarnings("serial")
        @Override
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
            if (proxyCertificate.equals(certificateMock)) {
                return new ArrayList<DataReference>() {

                    {
                        add(new DataReference(DataReferenceType.fileObject, referenceID, pi));
                    }
                };
            } else {
                return null;
            }
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && dataReferenceGuid.equals(referenceID)) {
                return new DataReference(DataReferenceType.fileObject, referenceID, pi);
            } else {
                return null;
            }
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

    }

    /**
     * Not reachable test implementation of the {@link QueryService}.
     * 
     * @author Doreen Seider
     */
    private class NotReachableDummyQueryService implements QueryService {

        @Override
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
            throw new UndeclaredThrowableException(null);
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
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
