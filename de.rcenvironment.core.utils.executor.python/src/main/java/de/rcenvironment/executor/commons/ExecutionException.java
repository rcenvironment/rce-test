/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.executor.commons;


/**
 * General exception thrown from the executor service during execution.
 *
 * @author Arne Bachmann
 */
public class ExecutionException extends RuntimeException {
    
    private static final long serialVersionUID = -1031075643520513230L;

    public ExecutionException(final String message) {
        super(message);
    }
    
    public ExecutionException(final Throwable cause) {
        super(cause);
    }
    
    public ExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
