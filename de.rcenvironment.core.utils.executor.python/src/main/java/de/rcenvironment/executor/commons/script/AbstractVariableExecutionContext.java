/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.commons.script;

import java.util.List;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.SerializableListImpl;
import de.rcenvironment.commons.TypedProperties;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;


/**
 * A context that supports variables.
 *
 * @author Arne Bachmann
 */
public class AbstractVariableExecutionContext extends TypedProperties<String> {

    /** Execution context key. */
    public static final String INPUT_VARIABLES = "inputVariables";
    
    /** Execution context key. */
    public static final String OUTPUT_VARIABLES = "outputVariables";

    /** Execution context key. */
    public static final String INPUT_ARRAYS = "inputArrays";
    
    /** Execution context key. */
    public static final String OUTPUT_ARRAYS = "outputArrays";
    

    /**
     * Set the input variables.
     * 
     * @param variables The variables
     */
    public void setInputVariables(final List<BoundVariable> variables) {
        final SerializableList<BoundVariable> list = new SerializableListImpl<BoundVariable>();
        list.addAll(variables);
        put(INPUT_VARIABLES, list);
    }
    
    /**
     * Set the input arrays.
     * 
     * @param arrays The arrays
     */
    public void setInputArrays(final List<VariantArray> arrays) {
        final SerializableList<VariantArray> list = new SerializableListImpl<VariantArray>();
        list.addAll(arrays);
        put(INPUT_ARRAYS, list);
    }
    
    /**
     * Set the output variables.
     * 
     * @param variables The variables
     */
    public void setOutputVariables(final List<BoundVariable> variables) {
        final SerializableList<BoundVariable> list = new SerializableListImpl<BoundVariable>();
        list.addAll(variables);
        put(OUTPUT_VARIABLES, list);
    }
    
    /**
     * Set the output array names for wrapper code generation.
     * 
     * @param arrays The array names
     */
    public void setOutputArrays(final List<String> arrays) {
        final SerializableList<String> list = new SerializableListImpl<String>();
        list.addAll(arrays);
        put(OUTPUT_ARRAYS, list);
    }
    
}
