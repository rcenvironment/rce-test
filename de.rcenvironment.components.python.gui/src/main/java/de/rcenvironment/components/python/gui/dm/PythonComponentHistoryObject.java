/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.components.python.gui.dm;

import java.io.Serializable;
import java.util.List;

import de.rcenvironment.commons.channel.DataManagementFileReference;


/**
 * Information about a script run.
 *
 * @author Arne Bachmann
 */
public class PythonComponentHistoryObject implements Serializable {
    
    private static final long serialVersionUID = -424862833374321195L;

    private List<DataManagementFileReference> dmHandlesIn;
    
    private List<DataManagementFileReference> dmHandlesOut;
    
    private Throwable error = null;
    
    private int exitCode = 0;
    
    
    public void setInputDmHandles(final List<DataManagementFileReference> handles) {
        dmHandlesIn = handles;
    }
    
    public void setOutputDmHandles(final List<DataManagementFileReference> handles) {
        dmHandlesOut = handles;
    }
    
    
    public List<DataManagementFileReference> getInputDmHandles() {
        return dmHandlesIn;
    }

    public List<DataManagementFileReference> getOutputDmHandles() {
        return dmHandlesOut;
    }
    
    public void setError(final Throwable t) {
        error = t;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public void setExitCode(final int code) {
        exitCode = code;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
}
