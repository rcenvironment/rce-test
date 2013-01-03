/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.executor.commons;


/**
 * General exception thrown by the executor factory.
 *
 * @author Arne Bachmann
 */
public class ExecutorException extends Exception {
    
    private static final long serialVersionUID = 2479065633179988504L;

    public ExecutorException(final String message) {
        super(message);
    }
    
    public ExecutorException(final Throwable cause) {
        super(cause);
    }
    
    public ExecutorException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
