/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;


/**
 * Test cases for {@link SimpleQueryService}.
 *
 * @author Doreen Seider
 */
public class SimpleQueryServiceTest {

    private SimpleQueryService simpleService;
    private User userMock;
    private Query queryMock;
    private PlatformIdentifier pi;
    private UUID referenceID;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        userMock = EasyMock.createNiceMock(User.class);
        queryMock = EasyMock.createNiceMock(Query.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();

        simpleService = new SimpleQueryService(userMock);
        simpleService.bindDistributedQueryService(new DummyDistributedQueryService());
    }

    /**
     * Test.
     */
    @Test
    public void testExecuteOverallQuery() {
        Collection<DataReference> references = simpleService.executeQuery(queryMock, 10);
        assertNotNull(references);
        assertEquals(1, references.size());
    }

    /**
     * Test.
     */
    @Test
    public void testExecuteQuery() {
        Collection<DataReference> references = simpleService.executeQuery(queryMock, 10, pi);
        assertNotNull(references);
        assertEquals(0, references.size());
    }

    /**
     * Test.
     */
    @Test
    public void testGetReferenceGlobally() {
        DataReference reference = simpleService.getReference(referenceID);
        assertNotNull(reference);
        assertEquals(pi, reference.getPlatformIdentifier());
    }

    /**
     * Test.
     */
    @Test
    public void testGetReference() {
        DataReference reference = simpleService.getReference(referenceID, pi);
        assertNotNull(reference);
        assertFalse(reference.getPlatformIdentifier().equals(pi));
    }

    /**
     * Test.
     */
    @Test(expected = IllegalStateException.class)
    public void testForFailure() {
        simpleService.unbindDistributedQueryService(new DummyDistributedQueryService());
        simpleService.getReference(referenceID, pi);
    }

    /**
     * Test implementation of the {@link DistributedQueryService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedQueryService implements DistributedQueryService {

        @SuppressWarnings("serial")
        @Override
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
            if (proxyCertificate.equals(userMock) && query.equals(queryMock)) {
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
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults,
                PlatformIdentifier platform) {
            if (proxyCertificate.equals(userMock) && query.equals(queryMock) && platform.equals(pi)) {
                return new ArrayList<DataReference>();
            } else {
                return null;
            }
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults,
                PlatformIdentifier platform) {
            return null;
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid, PlatformIdentifier platform)
            throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReferenceGuid.equals(referenceID) && platform.equals(pi)) {
                return new DataReference(DataReferenceType.fileObject, referenceID,
                    PlatformIdentifierFactory.fromHostAndNumberString("host:1"));
            } else {
                return null;
            }
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReferenceGuid.equals(referenceID)) {
                return new DataReference(DataReferenceType.fileObject, referenceID, pi);
            } else {
                return null;
            }
        }

        @Override
        public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid,
                Collection<PlatformIdentifier> platforms) throws AuthorizationException {
            // FIXME mock method not implemented; needed?
            return null;
        }
    }
}
