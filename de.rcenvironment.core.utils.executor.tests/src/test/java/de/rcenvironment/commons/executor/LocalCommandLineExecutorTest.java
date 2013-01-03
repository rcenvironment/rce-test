/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.executor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rcenvironment.commons.TempFileUtils;

/**
 * Wrapper for running the {@link CommonExecutorTests} with a {@link LocalCommandLineExecutor}.
 * 
 * @author Robert Mischke
 * 
 */
public class LocalCommandLineExecutorTest extends CommonExecutorTests {

    private static final int TEST_TIMEOUT = 10000;

    private static final String MESSAGE_LINUX_SPECIFIC_TEST_SKIPPED = "Test is only runnable on Linux; skipping";

    private static final String MESSAGE_WINDOWS_SPECIFIC_TEST_SKIPPED = "Test is only runnable on Windows; skipping";

    private static final String OS_NAME = "os.name";
    private LocalCommandLineExecutor executor;

    private File workDir;

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
     * Setup.
     * 
     * @throws IOException on I/O errors
     */
    @Before
    public void setUp() throws IOException {
        workDir = TempFileUtils.getDefaultInstance().createManagedTempDir("-unittest");
        executor = new LocalCommandLineExecutor(workDir);
    }

    /**
     * Cleanup.
     * 
     * @throws IOException on I/O errors
     */
    @After
    public void tearDown() throws IOException {
        if (workDir != null) {
            TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(workDir);
        }
    }

    /**
     * Tests {@link CommandLineExecutor#start(String)} with a single "echo" command.
     * 
     * @throws IOException on test exception
     * @throws InterruptedException on test exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testCrossPlatformEcho() throws IOException, InterruptedException {
        testCrossPlatformEcho(executor);
    }

    /**
     * Tests {@link CommandLineExecutor#startMultiLineCommand(String[])} with multiple "echo"
     * commands.
     * 
     * @throws IOException on test exception
     * @throws InterruptedException on test exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testCrossPlatformMultiLineEcho() throws IOException, InterruptedException {
        testCrossPlatformMultiLineEcho(executor);
    }

    /**
     * Tests if environment variables are being properly set under Linux.
     * 
     * @throws IOException on I/O errors
     * @throws InterruptedException on thread interruption
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testLinuxEnvironmentSetting() throws IOException, InterruptedException {
        if (!System.getProperty(OS_NAME).toLowerCase().contains("linux")) {
            log.info(MESSAGE_LINUX_SPECIFIC_TEST_SKIPPED);
            return;
        }
        testLinuxEnvironmentSetting(executor);
    }

    /**
     * Tests if a provided input stream properly reaches the command line under Linux.
     * 
     * @throws UnsupportedEncodingException if UTF-8 somehow ceased to exist
     * @throws IOException on I/O errors
     * @throws InterruptedException on thread interruption
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testLinuxProvidedInputStream() throws UnsupportedEncodingException, IOException, InterruptedException {
        if (!System.getProperty(OS_NAME).toLowerCase().contains("linux")) {
            log.info(MESSAGE_LINUX_SPECIFIC_TEST_SKIPPED);
            return;
        }
        testLinuxProvidedInputStream(executor);
    }
    /**
     * Tests if environment variables are being properly set under Windows.
     * 
     * @throws IOException on I/O errors
     * @throws InterruptedException on thread interruption
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testWindowsEnvironmentSetting() throws IOException, InterruptedException {
        if (!System.getProperty(OS_NAME).toLowerCase().contains("windows")) {
            log.info(MESSAGE_WINDOWS_SPECIFIC_TEST_SKIPPED);
            return;
        }
        testWindowsEnvironmentSetting(executor);
    }

    /**
     * Tests if a provided input stream properly reaches the command line under Windows.
     * 
     * @throws UnsupportedEncodingException if UTF-8 somehow ceased to exist
     * @throws IOException on I/O errors
     * @throws InterruptedException on thread interruption
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testWindowsProvidedInputStream() throws UnsupportedEncodingException, IOException, InterruptedException {
        if (!System.getProperty(OS_NAME).toLowerCase().contains("windows")) {
            log.info(MESSAGE_WINDOWS_SPECIFIC_TEST_SKIPPED);
            return;
        }
        testWindowsProvidedInputStream(executor);
    }

}
