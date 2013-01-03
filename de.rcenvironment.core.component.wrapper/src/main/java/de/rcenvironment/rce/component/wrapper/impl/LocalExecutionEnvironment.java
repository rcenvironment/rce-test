/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.wrapper.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.commons.executor.LocalCommandLineExecutor;
import de.rcenvironment.rce.component.wrapper.sandboxed.ExecutionEnvironment;

/**
 * An {@link ExecutionEnvironment} that sets up {@link CommandLineExecutor}s for the local system.
 * 
 * @author Robert Mischke
 */
public class LocalExecutionEnvironment implements ExecutionEnvironment {

    private File localSandboxDir;

    private Log log = LogFactory.getLog(getClass());

    @Override
    public void setupStaticEnvironment() throws IOException {
        // NOP
    }

    @Override
    public CommandLineExecutor setupExecutorWithSandbox()
        throws IOException {
        localSandboxDir = TempFileUtils.getDefaultInstance()
            .createManagedTempDir("sandbox");
        log.debug("Prepared local sandbox at " + localSandboxDir);
        return new LocalCommandLineExecutor(localSandboxDir);
    }

    @Override
    public String createUniqueTemporaryStoragePath() throws IOException {
        return TempFileUtils.getDefaultInstance()
            .createManagedTempDir("static-session-storage")
            .getAbsolutePath();
    }

    @Override
    public void tearDownSandbox(CommandLineExecutor executor)
        throws IOException {
        log.debug("Cleaning local sandbox at "
            + localSandboxDir.getAbsolutePath());
        TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(
            localSandboxDir);
    }

    @Override
    public void tearDownStaticEnvironment() {
        // NOP
    }
}
