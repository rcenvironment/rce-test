/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python;

import java.util.List;
import java.util.Map;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.SerializableListImpl;
import de.rcenvironment.commons.SerializableMap;
import de.rcenvironment.commons.SerializableMapImpl;
import de.rcenvironment.commons.TypedProperties;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.executor.python.internal.ParseResult;


/**
 * The results of a python execution.
 *
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public class PythonExecutionResult extends TypedProperties<String> {
    
    /**
     * Defined variables key.
     */
    private static final String OUTPUT_VARIABLES = "outputVariables";
    
    /**
     * Defined variables key.
     */
    private static final String OUTPUT_ARRAYS = "outputArrays";
    
    /**
     * Defined data management handles key.
     */
    private static final String DM_HANDLES = "dmHandles";
    
    /**
     * For the processes exit code.
     */
    private static final String EXIT_CODE = "exitCode";
    
    
    /**
     * Constructor take a list of input variables, which need to be available in the running script.
     * @param variables the input variables
     * @param dataManagementHandles list of all handles to return from the executor to the calling component
     */
    public PythonExecutionResult(final ParseResult result, final int exitCode) {
        put(EXIT_CODE, Integer.valueOf(exitCode));
        final SerializableList<BoundVariable> list = new SerializableListImpl<BoundVariable>();
        list.addAll(result.variables);
        put(OUTPUT_VARIABLES, list);
        final SerializableMap<String, String> dm = new SerializableMapImpl<String, String>();
        dm.putAll(result.dmHandles);
        put(DM_HANDLES, dm);
        final SerializableMap<String, VariantArray> arr = new SerializableMapImpl<String, VariantArray>();
        if (result.arrays != null) {
            for (final VariantArray v: result.arrays) {
                arr.put(v.getName(), v);
            }
        }
        put(OUTPUT_ARRAYS, arr);
    }
    
    /**
     * Returns the extracted output variables after script run.
     * @return the extracted output variables
     */
    @SuppressWarnings("unchecked")
    public List<BoundVariable> getOutputVariables() {
        return get(OUTPUT_VARIABLES, SerializableList.class);
    }
    
    /**
     * Returns the extracted output arrays after script run.
     * 
     * @return the extracted output arrays
     */
    @SuppressWarnings("unchecked")
    public Map<String, VariantArray> getOutputArrays() {
        return get(OUTPUT_ARRAYS, SerializableMap.class);
    }
    
    /**
     * Returns the extracted data management handles as strings.
     * @return the extracted data management handles, key is name of channel or file
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getDataManagementHandles() {
        return get(DM_HANDLES, SerializableMap.class);
    }
    
    /**
     * Return the processes exit code.
     * 
     * @return The exit code
     */
    public int getExitCode() {
        return get(EXIT_CODE, Integer.class).intValue();
    }

}
