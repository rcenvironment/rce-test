/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.scripting;


/**
 * Container holding constants for the basic wrapper component.
 * 
 * @author Christian Weiss
 */
public final class ScriptingComponentConstants {

    /** The key for the context 'pre'. */
    public static final String CONTEXT_PRE = "pre";

    /** The key for the context 'post'. */
    public static final String CONTEXT_POST = "post";
    
    /** Placeholder variable for current working directory. */
    public static final String VARIABLE_CWD = "${cwd}";
    
    /** Placeholder variable for current working directory. */
    public static final String VARIABLE = "${%s}";

    private ScriptingComponentConstants() {
        // do nothing
    }

}
