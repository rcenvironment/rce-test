/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.util.List;
import java.util.Map;

import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;


/**
 * Represents the tuple result parsed from the filtered stderr.
 *
 * @author Arne Bachmann
 */
public class ParseResult {
    
    /**
     * Parsed variables.
     */
    public final List<BoundVariable> variables;
    
    /**
     * Parsed data management handles.
     */
    public final Map<String, String> dmHandles;
    
    /**
     * Parsed array assignments.
     */
    public final List<VariantArray> arrays;
    
    
    /**
     * The constructor.
     * 
     * @param parsedVariables A
     * @param parsedDmHandles B
     * @param parsedArrays C
     */
    ParseResult(final List<BoundVariable> parsedVariables, final Map<String, String> parsedDmHandles,
            final List<VariantArray> parsedArrays) {
        variables = parsedVariables;
        dmHandles = parsedDmHandles;
        arrays = parsedArrays;
    }

}
