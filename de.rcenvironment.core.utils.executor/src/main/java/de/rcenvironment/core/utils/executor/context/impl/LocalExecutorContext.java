/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.utils.executor.context.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.commons.executor.LocalCommandLineExecutor;
import de.rcenvironment.core.utils.executor.context.spi.ExecutorContext;

/**
 * An {@link ExecutorContext} implementation for local execution using a
 * {@link LocalCommandLineExecutor}. Acquires temporary directories from the default
 * {@link TempFileUtils} instance.
 * 
 * @author Robert Mischke
 */
public class LocalExecutorContext implements ExecutorContext {

    private File currentSandboxDir;

    private Log log = LogFactory.getLog(getClass());

    @Override
    public void setUpSession() throws IOException {
        // NOP
    }

    @Override
    public void tearDownSession() {
        // TODO actively dispose directories created via #createUniqueTempDir()?
    }

    @Override
    public CommandLineExecutor setUpSandboxedExecutor() throws IOException {
        // prevent coding errors from reusing undisposed contexts
        if (currentSandboxDir != null) {
            throw new IllegalStateException("The previous sandbox has not been disposed yet");
        }
        // create new sandbox
        currentSandboxDir = TempFileUtils.getDefaultInstance().createManagedTempDir("sandbox");
        log.debug("Prepared local sandbox at " + currentSandboxDir);
        return new LocalCommandLineExecutor(currentSandboxDir);
    }

    @Override
    public void tearDownSandbox(CommandLineExecutor executor) throws IOException {
        if (currentSandboxDir == null) {
            // accept tear down of uninitialized sandbox to allow simple cleanup with try...finally
            log.debug("No initialized local sandbox, ignoring tear down request");
            return;
        }
        log.debug("Cleaning local sandbox at " + currentSandboxDir.getAbsolutePath());
        TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(currentSandboxDir);
        currentSandboxDir = null;
    }

    @Override
    public String createUniqueTempDir(String contextHint) throws IOException {
        String tempDirPath = TempFileUtils.getDefaultInstance().createManagedTempDir(contextHint).getAbsolutePath();
        log.debug("Created new local temp directory at " + tempDirPath);
        return tempDirPath;
    }

}
