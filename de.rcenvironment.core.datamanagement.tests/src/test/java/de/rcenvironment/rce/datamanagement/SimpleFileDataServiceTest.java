/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
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
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Test cases for {@link SimpleFileDataService}.
 *
 * @author Doreen Seider
 */
public class SimpleFileDataServiceTest {

    private SimpleFileDataService dataService;
    private User userMock;
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
        userMock = EasyMock.createNiceMock(User.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();
        reference = new DataReference(DataReferenceType.fileObject, referenceID, pi);
        notReachableReference =
            new DataReference(DataReferenceType.fileObject, referenceID,
                PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));
        is = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        mds = new MetaDataSet();
        
        dataService = new SimpleFileDataService(userMock);
        dataService.bindDistributedFileDataService(new DummyDistributedFileDataService());

    }
    
    /**
     * Test.
     */
    @Test
    public void testGetStreamFromDataReference() {
        InputStream stream = dataService.getStreamFromDataReference(reference, number);
        assertEquals(is, stream);
    }

    /**
     * Test.
     */
    @Test
    public void testNewReferenceFromStream() {
        DataReference dr = dataService.newReferenceFromStream(is, mds, pi);
        assertEquals(reference, dr);
    }
    
    /**
     * Test.
     */
    @Test
    public void testNewRevisionFromStream() {
        DataReference dr = dataService.newRevisionFromStream(reference, is, mds);
        assertEquals(reference, dr);
    }

    /**
     * Test.
     */
    @Test(expected = IllegalStateException.class)
    public void testForFailure() {
        dataService.unbindDistributedFileDataService(EasyMock.createNiceMock(DistributedFileDataService.class));
        dataService.branch(reference, number, pi);
    }
    
    /**
     * Test implementation of the {@link DistributedFileDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedFileDataService implements DistributedFileDataService {

        @Override
        public InputStream getStreamFromDataReference(User proxyCertificate, DataReference dataReference,
                Integer revisionNumber) throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReference.equals(reference) && revisionNumber == number) {
                return is;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference newReferenceFromStream(User proxyCertificate, InputStream inputStream, MetaDataSet metaDataSet,
                PlatformIdentifier platform) throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && inputStream.equals(is) && metaDataSet.equals(mds)) {
                return reference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference newRevisionFromStream(User proxyCertificate, DataReference dataReference, InputStream inputStream,
                MetaDataSet metaDataSet) throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReference.equals(reference)
                    && inputStream.equals(is) && metaDataSet.equals(mds)) {
                return notReachableReference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {
        }

        @Override
        public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
            throws AuthorizationException {
            return null;
        }

        @Override
        public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision,
                PlatformIdentifier repositoryPlatform) throws AuthorizationException {
            return null;
        }
        
    }
}
