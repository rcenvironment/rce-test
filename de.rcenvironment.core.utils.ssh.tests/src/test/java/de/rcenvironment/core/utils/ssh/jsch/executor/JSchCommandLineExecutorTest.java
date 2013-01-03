/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.ssh.jsch.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
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
import de.rcenvironment.core.utils.ssh.jsch.JschSessionFactory;
import de.rcenvironment.core.utils.ssh.jsch.SshParameterException;
import de.rcenvironment.core.utils.ssh.jsch.DummyCommand;
import de.rcenvironment.core.utils.ssh.jsch.DummyPasswordAuthenticator;
import de.rcenvironment.core.utils.ssh.jsch.Utils;

/**
 * Test cases for {@link JSchCommandLineExecutor}.
 * @author Doreen Seider
 */
public class JSchCommandLineExecutorTest {

    private static final String LOCALHOST = "localhost";

    private static final int PORT = 22;
    
    private static SshServer sshServer;
    
    private File workdir;
    
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
    }
    
    /**
     * Set up work dir.
     * 
     * @throws IOException on error
     **/
    @Before
    public void createWorkDir() throws IOException {
        workdir = new File(DummyCommand.WORKDIR);
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
     * @throws SshParameterException on error
     * @throws JSchException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     **/
    @Test(timeout = Utils.TIMEOUT)
    public void testStart() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        final String out = "nice";
        final String err = "not so nice";
        final String commandStdout = "command - exit value: 0, stdout";
        final String commandStdoutStderr = "command - exit value: 0, stdout, stderr";
        final String commandStderr = "command - exit value: 1, stderr";
        final String fullCommandTemplate = "cd %s && %s";
        
        sshServer.setCommandFactory(new CommandFactory() {
            
            @Override
            public Command createCommand(String commandString) {
                Command command;

                if (commandString.equals(String.format(fullCommandTemplate, DummyCommand.WORKDIR,  commandStdout))) {
                    command = new DummyCommand(out, null, 0);
                } else if (commandString.equals(String.format(fullCommandTemplate, DummyCommand.WORKDIR,  commandStdoutStderr))) {
                    command = new DummyCommand(out, err, 0);
                } else if (commandString.equals(String.format(fullCommandTemplate, DummyCommand.WORKDIR,  commandStderr))) {
                    command = new DummyCommand(null, err, 1);
                } else {
                    throw new IllegalArgumentException("Given command not supported: " + commandString);
                }
                return command;
            }
        });
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);
        
        // TODO test with stdin
        executor.start(commandStdout);
        InputStream stdoutStream = executor.getStdout();
        InputStream stderrStream = executor.getStderr();
        int exitValue = executor.waitForTermination();
        assertEquals(out, IOUtils.toString(stdoutStream));
        assertEquals(DummyCommand.EMPTY_STRING, IOUtils.toString(stderrStream));
        assertEquals(0, exitValue);
        
        executor.start(commandStdoutStderr);
        stdoutStream = executor.getStdout();
        stderrStream = executor.getStderr();
        exitValue = executor.waitForTermination();
        assertEquals(out, IOUtils.toString(stdoutStream));
        assertEquals(err, IOUtils.toString(stderrStream));
        assertEquals(0, exitValue);
                
        executor.start(commandStderr);
        stdoutStream = executor.getStdout();
        stderrStream = executor.getStderr();
        exitValue = executor.waitForTermination();
        assertEquals(DummyCommand.EMPTY_STRING, IOUtils.toString(stdoutStream));
        assertEquals(err, IOUtils.toString(stderrStream));
        assertEquals(1, exitValue);
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testGetRemoteWorkDir() throws JSchException, SshParameterException {
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);
        assertTrue(executor.getWorkDirPath().contains(DummyCommand.WORKDIR));
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testRemoteCopy() throws JSchException, SshParameterException, IOException, InterruptedException {
        String src = "src";
        String target = "target";
        // if none of these commands are executed on server side this test will fail with a timeout
        final String cpCommand = "cp " + src + " " + target;
        final String failingCpCommand = "cp " + target + " " + src;
        
        sshServer.setCommandFactory(Utils.createDummyCommandFactory(cpCommand, "stdout", failingCpCommand, "stderr"));
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);
        executor.remoteCopy(src, target);
        try {
            executor.remoteCopy(target, src);
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
    public void testDownloadWorkdir() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        final String fileContent = RandomStringUtils.randomAlphabetic(6);
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);
        
        Utils.createFileOnServerSide(sshServer, session, RandomStringUtils.randomAlphabetic(6), fileContent, executor);
        
        sshServer.setCommandFactory(new ScpCommandFactory());
        
        File dir = TempFileUtils.getDefaultInstance().createManagedTempDir();
        executor.downloadWorkdir(dir);
        assertEquals(1, dir.listFiles().length);
        
        FileUtils.deleteDirectory(dir);
    }
    
    /**
     * Test.
     * @throws JSchException on error
     * @throws SshParameterException on error
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    @Test(timeout = Utils.TIMEOUT)
    public void testDownloadFromWorkdir() throws JSchException, SshParameterException, IOException, InterruptedException {

        final String srcFileName = RandomStringUtils.randomAlphabetic(6);
        final String fileContent = RandomStringUtils.randomAlphabetic(9);

        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);

        Utils.createFileOnServerSide(sshServer, session, srcFileName, fileContent, executor);

        sshServer.setCommandFactory(new ScpCommandFactory());
        
        File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(RandomStringUtils.randomAlphabetic(4));
        
        executor.downloadFromWorkdir(srcFileName, file);
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
    public void testUploadToWorkdir() throws JSchException, SshParameterException, IOException, InterruptedException {
        
        final String srcFilename = RandomStringUtils.randomAlphabetic(5);
        sshServer.setCommandFactory(new ScpCommandFactory());
        
        Session session = JschSessionFactory.setupSession(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            null, DummyPasswordAuthenticator.PASSWORD, null);
        JSchCommandLineExecutor executor = new JSchCommandLineExecutor(session, DummyCommand.WORKDIR);
        File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(srcFilename);
        executor.uploadToWorkdir(file, srcFilename);
        file.delete();
    }
    
}
