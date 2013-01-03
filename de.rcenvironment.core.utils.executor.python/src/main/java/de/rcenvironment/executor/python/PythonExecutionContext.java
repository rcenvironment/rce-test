/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python;

import java.util.Map;

import de.rcenvironment.commons.SerializableMap;
import de.rcenvironment.commons.SerializableMapImpl;
import de.rcenvironment.executor.commons.script.AbstractVariableExecutionContext;
import de.rcenvironment.executor.python.PythonExecutorFactory.SystemType;


/**
 * The necessary context for a native Python execution.
 *
 * @version $LastChangedRevision$
 * @author Arne Bachmann
 */
public class PythonExecutionContext extends AbstractVariableExecutionContext {
    
    /** Execution context key. */
    public static final String OS = "os";
    /** Execution context key. */
    public static final String PYTHON_EXECUTABLE_PATH = "pythonExecutablePath";
    /** Execution context key. */
    public static final String PYTHON_SCRIPT = "pythonScript";
    /** Execution context key. */
    public static final String TEMPLATE = "template";
    /** The handles to give into the script. */
    public static final String DM_HANDLES = "dmHandles";
    
    
    /**
     * Constructor.
     * 
     * @param script The script to run
     */
    public PythonExecutionContext(final String script) {
        super();
        put(OS, SystemType.Unspecified.name());
        setPythonScript(script);
    }
    
    /**
     * Sets the path to the Python executable.
     * @param path the path
     * @return self
     */
    public PythonExecutionContext setPythonExecutablePath(final String path) {
        put(PYTHON_EXECUTABLE_PATH, path);
        return this;
    }

    /**
     * Sets the name of the script to run.
     * @param script the name of script
     * @return self
     */
    public PythonExecutionContext setPythonScript(final String script) {
        if ((script == null) || (script.replaceAll("\\s", "").equals(""))) {
            put(PYTHON_SCRIPT, "pass");
        } else {
            put(PYTHON_SCRIPT, script);
        }
        return this;
    }
    
    /**
     * Sets the template for the wrapper code.
     * @param wrapperTemplate the template wrapper code
     * @return self
     */
    public PythonExecutionContext setWrapperTemplate(final String wrapperTemplate) {
        put(TEMPLATE, wrapperTemplate);
        return this;
    }
    
    /**
     * Sets the system's OS.
     * @param type the type of the OS.
     * @return self
     */
    public PythonExecutionContext setSystemType(final SystemType type) {
        put(OS, type.name());
        return this;
    }
    
    /**
     * This is symmetric to PythonExcutionResult.getDataManagementHandles.
     * 
     * @param dmHandles A map from (file)name to dm entry uuid
     * @return self
     */
    public PythonExecutionContext setDataManagementHandles(final Map<String, String> dmHandles) {
        final SerializableMap<String, String> map = new SerializableMapImpl<String, String>();
        map.putAll(dmHandles);
        put(DM_HANDLES, map);
        return this;
    }
    
    /**
     * Returns the extracted data management handles as strings.
     * @return the extracted data management handles, key is name of channel or file
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getDataManagementHandles() {
        return get(DM_HANDLES, SerializableMap.class);
    }

}
