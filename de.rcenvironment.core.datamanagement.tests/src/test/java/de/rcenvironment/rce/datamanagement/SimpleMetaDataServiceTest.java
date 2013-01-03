/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * Test cases for {@link SimpleMetaDataService}.
 *
 * @author Doreen Seider
 */
public class SimpleMetaDataServiceTest {

    private SimpleMetaDataService simpleService;
    private User userMock;
    private PlatformIdentifier pi;
    private UUID referenceID;
    private DataReference reference;
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
        mds = new MetaDataSet();
        
        simpleService = new SimpleMetaDataService(userMock);
        simpleService.bindDistributedMetaDataService(new DummyDistributedMetaDataService());
    }
    
    /**
     * Test.
     */
    @Test
    public void testGetMetaDataSet() {
        MetaDataSet set = simpleService.getMetaDataSet(reference, 10);
        assertNotNull(set);
    }
    
    /**
     * Test.
     */
    @Test
    public void testUpdateMetaDataSet() {
        simpleService.updateMetaDataSet(reference, 10, mds, false);
        assertEquals(mds, simpleService.getMetaDataSet(reference, 10));
    }
    
    /**
     * Test.
     */
    @Test(expected = IllegalStateException.class)
    public void testForFailure() {
        simpleService.unbindDistributedMetaDataService(new DummyDistributedMetaDataService());
        simpleService.getMetaDataSet(reference, 0);
    }
    
    /**
     * Test implementation of the {@link DistributedMetaDataService}.
     * 
     * @author Doreen Seider
     */
    private class DummyDistributedMetaDataService implements DistributedMetaDataService {

        @Override
        public void updateMetaDataSet(User proxyCertificate, DataReference dataReference, Integer revisionNumber,
                MetaDataSet metaDataSet, boolean includeRevisionIndependent) throws AuthorizationException {
            if (!(proxyCertificate.equals(userMock) && dataReference.equals(reference))) {
                throw new RuntimeException();
            }
            
        }

        @Override
        public MetaDataSet getMetaDataSet(User proxyCertificate, DataReference dataReference, int revisionNumber)
            throws AuthorizationException {
            if (proxyCertificate.equals(userMock) && dataReference.equals(reference)) {
                return mds;
            } else {
                throw new RuntimeException();
            }
        }
        
    }
}
