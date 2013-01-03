/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;


/**
 * States of a {@link Workflow}.
 * 
 * @author Doreen Seider
 */
public enum WorkflowState {
    
    /** Ready. */
    READY,
    
    /** Preparing. */
    PREPARING,
    
    /** Running. */
    RUNNING,
    
    /** Pausing. */
    PAUSING,
    
    /** Paused. */
    PAUSED,
    
    /** Running. */
    RESUMING,
    
    /** Finished. */
    FINISHED,
    
    /** Canceling. */
    CANCELING,
    
    /** Canceled. */
    CANCELED,
    
    /** Failed. */
    FAILED,
    
    /** Disposing. */
    DISPOSING,
    
    /** Disposed. */
    DISPOSED
}
