/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;


/**
 * Test cases for {@link SimpleDataService}.
 *
 * @author Doreen Seider
 */
public class SimpleDataServiceTest {

    private TestSimpleDataService dataService;
    private User userMock;
    private PlatformIdentifier pi;
    private UUID referenceID;
    private DataReference reference;
    private DataReference notReachableReference;
    private DataReference branchedReference;
    private int number = 7;
    
    /** Set up. */
    @Before
    public void setUp() {
        userMock = EasyMock.createNiceMock(User.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();
        reference = new DataReference(DataReferenceType.fileObject, referenceID, pi);
        branchedReference =
            new DataReference(DataReferenceType.fileObject, referenceID, PlatformIdentifierFactory.fromHostAndNumberString("branched:1"));
        notReachableReference =
            new DataReference(DataReferenceType.fileObject, referenceID,
                PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));        
        
        dataService = new TestSimpleDataService(userMock);
        dataService.bindDistributedDataService(new DummyDistributedDataService());
    }
    
    /** Test. */
    @Test
    public void testDeleteReference() {
        dataService.deleteReference(reference);
        dataService.deleteReference(notReachableReference);
    }
    
    /** Test. */
    @Test
    public void testDeleteRevision() {
        DataReference dr = dataService.deleteRevision(reference, number);
        assertEquals(reference, dr);
    }
    
    /** Test. */
    @Test
    public void testBranchRevision() {
        DataReference dr = dataService.branch(reference, number, pi);
        assertEquals(reference, dr);
    }
    
    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testForFailure() {
        dataService.unbindDistributedDataService(new DummyDistributedDataService());
        dataService.branch(reference, number, pi);
    }
    
    /**
     * Test implementation of the {@link DistributedDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedDataService implements DistributedDataService {

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {
            if (!(proxyCertificate.equals(userMock) && dataReference.equals(reference))) {
                throw new RuntimeException();
            }
            
        }

        @Override
        public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
            throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReference.equals(reference) && revisionNumber == number) {
                return notReachableReference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision,
                PlatformIdentifier repositoryPlatform) throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && sourceDataReference.equals(reference) && sourceRevision == number
                    && repositoryPlatform.equals(pi)) {
                return branchedReference;
            } else {
                throw new RuntimeException();
            }
        }
        
        
        
    }
    
    /**
     * Test class used to test the abstract {@link SimpleDataService} class.
     * @author Doreen Seider
     */
    class TestSimpleDataService extends SimpleDataService {
        
        public TestSimpleDataService(User certificate) {
            user = certificate;
        }
        
        protected void bindDistributedDataService(DistributedDataService newDataService) {
            dataService = newDataService;
        }

        protected void unbindDistributedDataService(DistributedDataService oldDataService) {
            dataService = ServiceUtils.createNullService(DistributedDataService.class);
        }
    }
}
