/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.internal;

import static org.junit.Assert.assertNotNull;
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
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.datamanagement.MetaDataService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;


/**
 * Test cases for {@link DistributedMetaDataServiceImpl}.
 *
 * @author Doreen Seider
 */
public class DistributedMetaDataServiceImplTest {

    private DistributedMetaDataServiceImpl metaDataService;
    private User certificateMock;
    private PlatformIdentifier pi;
    private UUID referenceID;
    private DataReference reference;
    private DataReference notReachableReference;
    private MetaDataSet mds;
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
        notReachableReference =
            new DataReference(DataReferenceType.fileObject, referenceID,
                PlatformIdentifierFactory.fromHostAndNumberString("notreachable:1"));
        mds = new MetaDataSet();
        
        metaDataService = new DistributedMetaDataServiceImpl();
        metaDataService.activate(EasyMock.createNiceMock(BundleContext.class));
        metaDataService.bindCommunicationService(new DummyCommunicationService());
    }
    
    /**
     * Test.
     */
    @Test
    public void testGetMetaDataSet() {
        MetaDataSet set = metaDataService.getMetaDataSet(certificateMock, reference, number);
        assertNotNull(set);
        set = metaDataService.getMetaDataSet(certificateMock, notReachableReference, number);
        assertNull(set);
    }
    
    /**
     * Test.
     */
    @Test
    public void testUpdateMetaDataSet() {
        metaDataService.updateMetaDataSet(certificateMock, reference, number, mds, false);
        metaDataService.updateMetaDataSet(certificateMock, notReachableReference, number, mds, false);
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
                return new DummyMetaDataService();
            } else {
                return new NotReachableDummyMetaDataService();
            }
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
            if (!(proxyCertificate.equals(certificateMock) && dataReference.equals(reference) && revisionNumber == number)) {
                throw new RuntimeException();
            }
            
        }

        @Override
        public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
            throws AuthorizationException {
            if (proxyCertificate.equals(certificateMock) && dataReference.equals(reference) && revisionNumber == number) {
                return mds;
            } else {
                throw new RuntimeException();
            }
        }

    }
    
    /**
     * Not reachable test implementation of the {@link MetaDataService}.
     *
     * @author Doreen Seider
     */
    private class NotReachableDummyMetaDataService implements MetaDataService {

        @Override
        public void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
                MetaDataSet metaDataSet, boolean includeRevisionIndependent) throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
            
        }

        @Override
        public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
            throws AuthorizationException {
            throw new UndeclaredThrowableException(null);
        }
    }
}
