/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.commons.script;

import java.util.List;

import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.executor.commons.ExecutionException;


/**
 * Thrown if only a partial result is available after an execution.
 *
 * @author Arne Bachmann
 */
public class ExecutionResultException extends ExecutionException {
    
    private static final long serialVersionUID = -8756920757640609813L;

    /**
     * The part that <i>could</i> be parsed.
     */
    private final List<BoundVariable> parsedVariables;
    
    /**
     * The part that <i>could <b>not</b></i> be parsed.
     */
    private final List<String> unparseableVariables;
    

    /**
     * Constructor defining all ocontents.
     * 
     * @param message The message to display
     * @param variables The variables that could be read
     * @param unparseableVariableNames The variables that had problems reading
     */
    public ExecutionResultException(final String message, final List<BoundVariable> variables,
            final List<String> unparseableVariableNames) {
        super(message);
        parsedVariables = variables;
        unparseableVariables = unparseableVariableNames;
    }
    
    public List<BoundVariable> getParsedVariables() {
        return parsedVariables;
    }
    
    public List<String> getUnparseableVariables() {
        return unparseableVariables;
    }
    
}
