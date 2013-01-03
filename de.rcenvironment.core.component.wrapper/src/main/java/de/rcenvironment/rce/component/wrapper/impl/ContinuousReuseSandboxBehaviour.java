/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.wrapper.impl;

import java.io.IOException;

import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.rce.component.wrapper.sandboxed.ExecutionEnvironment;
import de.rcenvironment.rce.component.wrapper.sandboxed.SandboxBehaviour;

/**
 * A sandbox behaviour where a new sandbox directory is created before the first run of the wrapped
 * executable, and reused without modifications for all subsequent runs.
 * 
 * @author Robert Mischke
 * 
 */
public class ContinuousReuseSandboxBehaviour implements SandboxBehaviour {

    private CommandLineExecutor reusableExecutor;

    private ExecutionEnvironment executionEnvironment;

    public ContinuousReuseSandboxBehaviour(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    @Override
    public CommandLineExecutor setupSingleRun() throws IOException {
        // only create one durable executor and sandbox
        if (reusableExecutor == null) {
            reusableExecutor = executionEnvironment.setupExecutorWithSandbox();
        }
        return reusableExecutor;
    }

    @Override
    public void afterSingleRun(CommandLineExecutor executor)
        throws IOException {
        // NOP
    }

    @Override
    public void beforeTearDownStaticEnvironment() throws IOException {
        // tear down only on global/static tear-down
        executionEnvironment.tearDownSandbox(reusableExecutor);
    }

}
