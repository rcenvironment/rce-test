/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python;

import de.rcenvironment.executor.commons.ExecutorException;

/**
 * Creates and initializes {@link PythonExecutor}s.
 * 
 * @author Arne Bachmann
 * @author Doreen Seider
 */
public interface PythonExecutorFactory {

    /** Windows or Linux? */
    enum SystemType {
        /**
         * All kinds of Microsoft Windows OSs.
         */
        Windows,
        
        /**
         * All kinds of unix, linux, apple, solaris and so on.
         */
        Linux,
        
        /**
         * Default when nothing was selected.
         */
        Unspecified;
    }
    
    /**
     * Creates an PythonExecutor object initialized with the given context.
     * @param executionContext set up context.
     * @return the created {@link PythonExecutor}
     * @throws ExecutorException if something went wrong, e.g. the context is invalid.
     */
    PythonExecutor createExecutor(PythonExecutionContext executionContext) throws ExecutorException;

}
