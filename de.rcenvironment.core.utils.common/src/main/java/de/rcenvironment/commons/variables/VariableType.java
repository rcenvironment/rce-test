/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.variables;


/**
 * Represents the type of a bound variable.
 *
 * @author Arne Bachmann
 */
public enum VariableType {
    
    /**
     * Anything that can be represented by a string.
     */
    String,
    
    /**
     * Integer number.
     */
    Integer,
    
    /**
     * Floating point number.
     */
    Real,
    
    /**
     * True or false.
     */
    Logic,
    
    /**
     * Date.
     */
    Date,
    
    /**
     * A file reference.
     */
    File,
    
    /**
     * Empty type for non-defined array cells and Excel cells.
     */
    Empty,
    
    /**
     * Special marker for RLE.
     */
    RLE;

}
