/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.executor.commons.ExecutorException;

/**
 * Convenient class abstracting the OSGi API.
 * 
 * @author Doreen Seider
 **/
public class SimplePythonExecutorFactory implements PythonExecutorFactory {

    private static PythonExecutorFactory executorFactory = ServiceUtils.createNullService(PythonExecutorFactory.class);

    protected void bindPythonExecutorFactory(final PythonExecutorFactory newExecutorFactory) {
        executorFactory = newExecutorFactory;
    }

    protected void unbindPythonExecutorFactory(final PythonExecutorFactory oldExecutorFactory) {
        executorFactory = ServiceUtils.createNullService(PythonExecutorFactory.class);
    }
    
    @Override
    public PythonExecutor createExecutor(final PythonExecutionContext executionContext) throws ExecutorException {
        return executorFactory.createExecutor(executionContext);
    }

}
