/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;


/**
 * Enumeration of {@link Component} states.
 *
 * @author Doreen Seider
 */
public enum ComponentState {
    
    /** Instantiated. */
    INSTANTIATED,
    
    /** Preparing. */
    PREPARING,
    
    /** Prepared. */
    PREPARED,
    
    /** Starting. */
    STARTING,
    
    /** Ready. */
    READY,
    
    /** Checking. */
    CHECKING,
    
    /** Running. */
    RUNNING,
    
    /** Finished. */
    FINISHED,
    
    /** Finished, but has not been run, although inputs are connected. */
    FINISHED_NO_RUN_STEP,
    
    /** Failed. */
    FAILED,
    
    /** Pausing. */
    PAUSING,
    
    /** Paused. */
    PAUSED,
    
    /** Canceling. */
    CANCELING,
    
    /** Canceled. */
    CANCELED,
    
    /** Disposing. */
    DISPOSING,
    
    /** Disposed. */
    DISPOSED

}
