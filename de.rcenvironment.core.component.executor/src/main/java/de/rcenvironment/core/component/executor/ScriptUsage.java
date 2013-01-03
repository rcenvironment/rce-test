/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.component.executor;

/**
 * Usage mode for script to execute.
 * @author Doreen Seider
 */
public enum ScriptUsage {

    /** Script exists locally. */
    LOCAL,
    
    /** Script exists remotely. */
    REMOTE,
    
    /** Script is written on at component's configuration time. */
    NEW;
}
