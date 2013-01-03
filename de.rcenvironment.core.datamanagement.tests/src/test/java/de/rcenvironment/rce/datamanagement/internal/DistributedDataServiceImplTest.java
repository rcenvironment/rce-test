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
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.datamanagement.DataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;


/**
 * Test cases for {@link DistributedDataServiceImpl}.
 *
 * @author Doreen Seider
 */
public class DistributedDataServiceImplTest {

    private TestDistributedDataServiceImpl dataService;
    private User certificateMock;
    private PlatformIdentifier pi;
    private UUID referenceID;
    private DataReference reference;
    private DataReference notReachableReference;
    private DataReference branchedReference;
    private int number = 7;
    
    /**
     * Set up.
     */
    @Before
    public void setUp() {
        certificateMock = EasyMock.createNiceMock(User.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
        referenceID = UUID.randomUUID();
        reference = new DataReference(DataReferenceType.fileObject, referenceID, pi);
        branchedReference =
            new DataReference(DataReferenceType.fileObject, referenceID, PlatformIdentifierFactory.fromHostAndNumberString("branched:1"));
        notReachableReference =
            new DataReference(DataReferenceType.fileObject, referenceID,
                PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));        
        
        dataService = new TestDistributedDataServiceImpl();
        dataService.activate(EasyMock.createNiceMock(BundleContext.class));
        dataService.bindCommunicationService(new DummyCommunicationService());
    }
    
    /**
     * Test.
     */
    @Test
    public void testDeleteReference() {
        dataService.deleteReference(certificateMock, reference);
        dataService.deleteReference(certificateMock, notReachableReference);
    }
    
    /**
     * Test.
     */
    @Test
    public void testDeleteRevision() {
        DataReference dr = dataService.deleteRevision(certificateMock, reference, number);
        assertEquals(reference, dr);
        dr = dataService.deleteRevision(certificateMock, notReachableReference, number);
        assertNull(dr);
    }
    
    /**
     * Test.
     */
    @Test
    public void testBranchRevision() {
        DataReference dr = dataService.branch(certificateMock, reference, number, pi);
        assertEquals(reference, dr);
        dr = dataService.branch(certificateMock, reference, number, PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));
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
                return new DummyDataService();
            } else {
                return new NotReachableDummyDataService();
            }
        }

    }
    
    /**
     * Test implementation of the {@link DataService}.
     *
     * @author Doreen Seider
     */
    private class DummyDataService implements DataService {

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {
            
        }

        @Override
        public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
            throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && dataReference.equals(reference) && revisionNumber == number) {
                return reference;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision)
            throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && sourceDataReference.equals(reference) && sourceRevision == number) {
                return branchedReference;
            } else {
                throw new RuntimeException();
            }
        }

    }
    
    /**
     * Not reachable test implementation of the {@link DataService}.
     *
     * @author Doreen Seider
     */
    private class NotReachableDummyDataService implements DataService {

        @Override
        public void deleteReference(User proxyCertificate, DataReference dataReference) throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }


        @Override
        public DataReference deleteRevision(User proxyCertificate, DataReference dataReference, Integer revisionNumber)
            throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }


        @Override
        public DataReference branch(User proxyCertificate, DataReference sourceDataReference, Integer sourceRevision)
            throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }
        
    }
    
    /**
     * Test class used to test the abstract {@link DistributedDataServiceImpl} class.
     * @author Doreen Seider
     */
    class TestDistributedDataServiceImpl extends DistributedDataServiceImpl {
        
        protected void activate(BundleContext bundleContext) {
            context = bundleContext;
        }
        
        protected void bindCommunicationService(CommunicationService newCommunicationService) {
            communicationService = newCommunicationService;
        }

    }

}
