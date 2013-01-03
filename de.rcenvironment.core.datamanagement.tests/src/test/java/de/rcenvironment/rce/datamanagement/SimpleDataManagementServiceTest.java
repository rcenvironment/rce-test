/*
 * Copyright (C) 2010-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test cases for {@link SimpleDataManagementService}.
 * 
 * @author Arne Bachmann
 */
public class SimpleDataManagementServiceTest {

    private SimpleDataManagementService dataManagementService;
    private User userMock;
    private PlatformIdentifier pi;
    private UUID referenceID;
    private File tmpFile;

    /**
     * Set up.
     * 
     * @throws IOException
     *             Should not happen
     */
    @Before
    public void setUp() throws IOException {
        userMock = EasyMock.createNiceMock(User.class);
        pi = PlatformIdentifierFactory.fromHostAndNumberString("freddy:1");
        referenceID = UUID.randomUUID();
        tmpFile = TempFileUtils.getDefaultInstance().createTempFileFromPattern("test-*");

        dataManagementService = new SimpleDataManagementService(userMock);
        dataManagementService.bindDataManagementService(new DummyDataManagementService());
    }

    /** */
    @After
    public void tearDown() {
        tmpFile.delete();
    }

    /**
     * Test wrong service removal. /
     * 
     * @Test(expected = IllegalStateException.class) public void testForFailure() {
     *                dataManagementService.unbindDataManagementService(new
     *                DummyDataManagementService()); }
     * 
     *                /**
     * @throws IOException
     *             A
     * @throws AuthorizationException
     *             B
     */
    @SuppressWarnings("serial")
    @Test
    public void testReferences() throws AuthorizationException, IOException {
        dataManagementService.createReferenceFromLocalFile(tmpFile, null, pi);
        dataManagementService.copyReferenceToLocalFile(referenceID.toString(), tmpFile);
        dataManagementService.copyReferenceToLocalFile(referenceID.toString(), tmpFile, pi);
        dataManagementService.copyReferenceToLocalFile(referenceID.toString(), tmpFile, new ArrayList<PlatformIdentifier>() {

            {
                add(pi);
            }
        });
    }

    /**
     * Mock implementation of the {@link DataManagementService}. It shows that every call to the
     * simple version is pipelined to the service without any change.
     * 
     * @author Arne Bachmann
     */
    private class DummyDataManagementService implements DataManagementService {

        @Override
        public String createReferenceFromLocalFile(User user, File file, MetaDataSet additionalMetaData) throws IOException,
                AuthorizationException {
            assertThat(user, is(userMock));
            assertThat(file, is(tmpFile));
            return referenceID.toString();
        }

        @Override
        public String createReferenceFromLocalFile(User user, File file, MetaDataSet additionalMetaData,
                PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException {
            assertThat(platformIdentifier, is(pi));
            return this.createReferenceFromLocalFile(user, file, additionalMetaData);
        }

        @Override
        public String createReferenceFromString(User user, String object, MetaDataSet additionalMetaData,
                PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException {
            // FIXME implement - @misc_ro
            return null;
        }

        @Override
        @Deprecated
        public void copyReferenceToLocalFile(User user, String reference, File targetFile) throws IOException, AuthorizationException {
            assertThat(user, is(userMock));
            assertThat(reference, is(referenceID.toString()));
            assertThat(targetFile, is(tmpFile));
        }

        @Override
        public void copyReferenceToLocalFile(User user, String reference, File targetFile, PlatformIdentifier platformIdentifier)
            throws IOException, AuthorizationException {
            assertThat(platformIdentifier, is(pi));
            this.copyReferenceToLocalFile(user, reference, targetFile);
        }

        @Override
        public void copyReferenceToLocalFile(User user, String reference, File targetFile, Collection<PlatformIdentifier> platforms)
            throws IOException, AuthorizationException {
            assertTrue(platforms.contains(pi));
            this.copyReferenceToLocalFile(user, reference, targetFile);
        }

        @Override
        public String retrieveStringFromReference(User user, String reference, Collection<PlatformIdentifier> platforms)
            throws IOException, AuthorizationException {
            // FIXME implement - @misc_ro
            return null;
        }
    };

}
