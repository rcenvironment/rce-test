/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.ssh.jsch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.rcenvironment.commons.TempFileUtils;

/**
 * Test cases for {@link JschSessionFactory}.
 * 
 * @author Doreen Seider
 */
public class JschFileTransferTest {

    private static final String LOCALHOST = "localhost";

    private static final int PORT = 22;
    
    private static SshServer sshServer;
    
    private static File workdir;
    
    /**
     * Set up test environment. 
     * @throws IOException on error
     **/
    @SuppressWarnings("serial")
    @BeforeClass
    public static void setUp() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(PORT);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setUserAuthFactories(new ArrayList<NamedFactory<UserAuth>>() {{ add(new UserAuthPassword.Factory()); }});
        sshServer.setPasswordAuthenticator(new DummyPasswordAuthenticator());
        sshServer.start();
        workdir = new File(DummyCommand.WORKDIR);
    }
    
    /**
     * Set up work dir.
     * 
     * @throws IOException on error
     **/
    @Before
    public void createWorkDir() throws IOException {
        workdir.mkdir();
    }
    
    /**
     * Tear down test environment.
     * @throws InterruptedException 
     **/
    @AfterClass
    public static void tearDown() throws InterruptedException {
        sshServer.stop();
    }
    
    /**
     *Delete work dir.
     * 
     * @throws IOException on error
     **/
    @After
    public void deleteWorkDir() throws IOException {
        FileUtils.deleteDirectory(workdir);
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testRemoteToRemoteCopy() throws JSchException, SshParameterException, IOException, InterruptedException {
        String src = "src";
        String target = "target";
        // if none of these commands are executed on server side this test won't terminate
        final String cpCommand = "cp " + src + " " + target;
        final String failingCpCommand = "cp " + target + " " + src;
        
        sshServer.setCommandFactory(Utils.createDummyCommandFactory(cpCommand, RandomStringUtils.randomAlphabetic(2),
            failingCpCommand, RandomStringUtils.randomAlphabetic(6)));
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JschFileTransfer.remoteToRemoteCopy(session, src, target);
        try {
            JschFileTransfer.remoteToRemoteCopy(session, target, src);
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testDownloadFile() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        final String srcFileName = RandomStringUtils.randomAlphabetic(6);
        final String fileContent = RandomStringUtils.randomAlphabetic(9);

        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        
        Utils.createFileOnServerSide(sshServer, session, srcFileName, fileContent);

        sshServer.setCommandFactory(new ScpCommandFactory());
        
        File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(RandomStringUtils.randomAlphabetic(4));
        
        JschFileTransfer.downloadFile(session, DummyCommand.WORKDIR + srcFileName, file);
        assertEquals(fileContent, FileUtils.readFileToString(file));
        file.delete();
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testUploadFile() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        final String srcFilename = RandomStringUtils.randomAlphabetic(3);
        
        sshServer.setCommandFactory(new ScpCommandFactory());
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        
        File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(srcFilename);
        
        JschFileTransfer.uploadFile(session, file, srcFilename);
        file.delete();
        
        new File(srcFilename).delete();
    }
    
    /**
     * Test.
     * @throws SshParameterException on error
     * @throws JSchException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     **/
    @Test(timeout = Utils.TIMEOUT)
    public void testDownloadDirectory() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME, null,
            DummyPasswordAuthenticator.PASSWORD, null);

        String fileContent = RandomStringUtils.randomAlphabetic(7);
        Utils.createFileOnServerSide(sshServer, session, RandomStringUtils.randomAlphabetic(5), fileContent);

        sshServer.setCommandFactory(new ScpCommandFactory());

        File dir = TempFileUtils.getDefaultInstance().createManagedTempDir();
        JschFileTransfer.downloadDirectory(session, DummyCommand.WORKDIR, dir);
        assertEquals(1, dir.listFiles().length);
        
        FileUtils.deleteDirectory(dir);
    }
}
