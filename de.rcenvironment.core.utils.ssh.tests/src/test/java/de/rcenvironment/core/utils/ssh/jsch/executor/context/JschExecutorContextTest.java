/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.ssh.jsch.executor.context;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.commons.validation.ValidationFailureException;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfiguration;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfigurationFactory;
import de.rcenvironment.core.utils.ssh.jsch.DummyCommand;
import de.rcenvironment.core.utils.ssh.jsch.DummyPasswordAuthenticator;
import de.rcenvironment.core.utils.ssh.jsch.Utils;


/**
 * Test cases for {@link JSchExecutorContext}.
 * @author Doreen Seider
 */
public class JschExecutorContextTest {

    private static final String LOCALHOST = "localhost";

    private static final int PORT = 22;
    
    private static SshServer sshServer;
    
    private volatile boolean failed = false;

    private final SshSessionConfiguration staticConfiguration = SshSessionConfigurationFactory
        .createSshSessionConfigurationWithAuthPhrase(LOCALHOST, PORT, DummyPasswordAuthenticator.USERNAME,
            DummyPasswordAuthenticator.PASSWORD);
    
   
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
        sshServer.setCommandFactory(Utils.createDummyCommandFactory());
        sshServer.start();
    }
    
    /**
     * Set up work dir.
     * 
     * @throws IOException on error
     **/
    @Before
    public void createWorkDir() throws IOException {
        failed = false;
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
     * Test. 
     * @throws IOException on error
     * @throws ValidationFailureException on error
     **/
    @Test(timeout = Utils.TIMEOUT)
    public void test() throws IOException, ValidationFailureException {
        JSchExecutorContext context = new JSchExecutorContext(staticConfiguration);
        
        context.setUpSession();
        sshServer.setCommandFactory(new CommandFactory() {
            
            @Override
            public Command createCommand(String command) {
                if (!command.contains("mkdir -p ")) {
                    failed = true;
                }
                return new DummyCommand();
            }
        });
        
        CommandLineExecutor executor = context.setUpSandboxedExecutor();
        if (failed) {
            fail();
        }
        
        sshServer.setCommandFactory(new CommandFactory() {
            
            @Override
            public Command createCommand(String command) {
                if (!command.contains("rm ") && !command.contains("rmdir")) {
                    failed = true;
                }
                return new DummyCommand();
            }
        });

        context.tearDownSandbox(executor);
        
        context.tearDownSession();
        
    }
    
    /** 
     * Test. 
     * @throws IOException on error
     * @throws ValidationFailureException on error
     **/
    @Test(timeout = Utils.TIMEOUT)
    public void testForLifecycleFailure() throws IOException, ValidationFailureException {
        JSchExecutorContext context = new JSchExecutorContext(staticConfiguration);

        try {
            context.setUpSandboxedExecutor();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

        try {
            context.tearDownSession();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
        
        JSchExecutorContext context2 = new JSchExecutorContext(staticConfiguration);
        context2.setUpSession();
        CommandLineExecutor executor = context2.setUpSandboxedExecutor();
        
        try {
            context.tearDownSandbox(executor);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

    }
    
    /** 
     * Test. 
     * @throws IOException on error
     * @throws ValidationFailureException on error
     **/
    @Test
    public void testCreateUniqueTempDir() throws IOException, ValidationFailureException {
        JSchExecutorContext context = new JSchExecutorContext(staticConfiguration);

        String contextHint = RandomStringUtils.randomAlphanumeric(5);
        assertTrue(context.createUniqueTempDir(contextHint).contains(contextHint));
    }
}
