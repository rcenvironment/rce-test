/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.executor.python;

import java.io.InputStream;
import java.util.concurrent.Future;


/**
 * Executes a given Python script.
 * 
 * @version $LastChangedRevision$
 * @author Arne Bachmann
 */
public interface PythonExecutor {

    /**
     * Separator in the stderr stream for wrapper variables output.
     */
    String VARS_SEPARATOR = "==========";
    
    /**
     * A string written out for variables that were unset inside the script.
     */
    String UNSET = "-";
    
    /**
     * Separator for var name vs value.
     */
    String VAR_SEPARATOR = "=";
    
    /**
     * Switch for keeping the python wrapper script within the temp directory.
     */
    String DEBUG = "debug";
    

    /**
     * Executes the Python script given by the setupContext passed into the constructor.
     * @return a {@link Future} object with the {@link PythonExecutionResult}
     * (at)throws ExecutionException if something went wrong during execution
     */
    Future<PythonExecutionResult> execute();
    
    /**
     * The filtered stderr stream.
     * 
     * @return The filtered stderr stream
     */
    InputStream getStderrStream();

    /**
     * Get the stdout stream.
     * 
     * @return The stdout stream
     */
    InputStream getStdoutStream();

}
