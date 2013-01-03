/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.service.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection.FileType;
import de.rcenvironment.rce.datamanagement.FileDataService;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Test cases for the {@link FileServiceImpl}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke (changed to classpath resource loading)
 */
public class FileServiceImplTest {

    private static final String INVALID_UUID = "uuid";

    private final UUID dmUuid = UUID.fromString("e293a96a-ddf2-41c5-b94e-c95a3a5cecc2");
    
    private final Integer noOfRevision = 5;
    
    private final String dmUri = dmUuid.toString() + "/" + noOfRevision;

    private final Integer noOfBytes = 4;

    private FileServiceImpl fileService;

    private DataReference dataRef;

    private InputStream inputStream = EasyMock.createNiceMock(InputStream.class);

    private User user = EasyMock.createNiceMock(User.class);


    /** Set up. 
     * @throws IOException */
    @Before
    public void setUp() throws IOException {
        fileService = new FileServiceImpl();
        fileService.bindFileDataService(new DummyFileDataService());
        fileService.bindQueryService(new DummyQueryService());

        dataRef = new DataReference(DataReferenceType.fileObject, dmUuid, PlatformIdentifierFactory.fromHostAndNumberString("lump:6"));
        
        inputStream = EasyMock.createNiceMock(InputStream.class);
        EasyMock.expect(inputStream.read()).andReturn(noOfBytes).anyTimes();
        EasyMock.expect(inputStream.read(EasyMock.aryEq(new byte[noOfBytes]),
            EasyMock.eq(0), EasyMock.eq(noOfBytes.intValue()))).andReturn(noOfBytes).anyTimes();
        EasyMock.replay(inputStream);
        
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testOpenForSuccess() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        fileService.close(uuid);
        assertNotNull(uuid);
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testOpenForFailure() throws Exception {
        try {
            fileService.open(user, FileType.RCE_DM, UUID.randomUUID().toString() + "/6");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testReadForSuccess() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        int read = fileService.read(uuid);
        assertTrue(read > 0);
        fileService.close(uuid);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testReadForFailure() throws Exception {
        try {
            fileService.read(INVALID_UUID);
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testRead2ForSuccess() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        byte[] byteArray = fileService.read(uuid, noOfBytes);
        assertNotNull(byteArray);
        fileService.close(uuid);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testRead2ForSanity() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        byte[] byteArray = fileService.read(uuid, noOfBytes);
        assertEquals(noOfBytes.intValue(), byteArray.length);
        fileService.close(uuid);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testRead2ForFailure() throws Exception {
        try {
            fileService.read(INVALID_UUID, new Integer(4));
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testCloseForSuccess() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        fileService.close(uuid);
        assertTrue(true);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testCloseForSanity() throws Exception {
        String uuid = null;

        uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        fileService.close(uuid);
        assertTrue(true);

        try {
            fileService.read(uuid, new Integer(4));
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        fileService.close(INVALID_UUID);
    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testSkipForSuccess() throws Exception {
        String uuid = fileService.open(user, FileType.RCE_DM, dmUri);
        fileService.skip(uuid, new Long(4));
        byte[] byteArray = fileService.read(uuid, new Integer(6));
        assertNotNull(byteArray);
        fileService.close(uuid);

    }

    /**
     * Test.
     * 
     * @throws Exception if the test fails.
     */
    @Test
    public void testSkipForFailure() throws Exception {
        try {
            fileService.skip(INVALID_UUID, new Long(2));
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test {@link FileDataService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyFileDataService implements FileDataService {

        @Override
        public InputStream getStreamFromDataReference(User u, DataReference dr, Integer rn)
            throws AuthorizationException {
            if (dr != null && dr.equals(dataRef) && rn.equals(noOfRevision)) {
                return inputStream;
            }
            return null;
        }

        @Override
        public DataReference newReferenceFromStream(User c, InputStream is, MetaDataSet mds)
            throws AuthorizationException {
            return null;
        }

        @Override
        public DataReference newRevisionFromStream(User c, DataReference cr, InputStream is, MetaDataSet mds)
            throws AuthorizationException {
            return null;
        }

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
     * Test {@link QueryService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyQueryService implements QueryService {

        @Override
        public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

        @Override
        public DataReference getReference(User c, UUID uuid) throws AuthorizationException {
            if (uuid.equals(dmUuid)) {
                return dataRef;
            }
            return null;
        }

        @Override
        public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults) {
            return null;
        }

    }

}
