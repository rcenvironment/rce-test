/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.executor;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.core.utils.executor.context.SandboxedExecutorLifeCycleFacade;
import de.rcenvironment.core.utils.executor.context.impl.LocalExecutorContextFactory;

/**
 * Tests a {@link SandboxedExecutorLifeCycleFacade} configured with a
 * {@link LocalExecutorContextFactory}.
 * 
 * @author Robert Mischke
 */
public class LocalExecutorContextFacadeTest extends CommonExecutorTests {

    private static final int TEST_TIMEOUT = 10000;

    /**
     * Static test setup.
     * 
     * @throws IOException on unexpected I/O errors
     */
    @BeforeClass
    public static void classSetUp() throws IOException {
        TempFileUtils.getDefaultInstance().setDefaultTestRootDir();
    }

    /**
     * Basic test of {@link SandboxedExecutorLifeCycleFacade} operation.
     * 
     * @throws Exception on unexpected test errors
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testBasicLifeCycle() throws Exception {
        LocalExecutorContextFactory contextFactory = new LocalExecutorContextFactory(false);
        SandboxedExecutorLifeCycleFacade facade = new SandboxedExecutorLifeCycleFacade(contextFactory);
        facade.setUpSession();
        try {
            CommandLineExecutor executor;
            // run once
            executor = facade.setUpExecutionPhase();
            try {
                testCrossPlatformEcho(executor);
            } finally {
                facade.tearDownExecutionPhase(executor);
            }
            // run again
            executor = facade.setUpExecutionPhase();
            try {
                testCrossPlatformEcho(executor);
            } finally {
                facade.tearDownExecutionPhase(executor);
            }
            // TODO improve test: test both sandbox strategies; verify sandbox behavior; ...
        } finally {
            facade.tearDownSession();
        }
    }

}
