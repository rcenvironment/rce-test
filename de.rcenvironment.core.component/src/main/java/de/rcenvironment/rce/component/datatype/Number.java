/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Interface for dataobject number.
 *
 * @author Markus Kunde
 */
public interface Number extends WorkflowDataObjectAtomic {

    /**
     * Checks if number is integer.
     * 
     * @return true if number is integer
     */
    boolean isInteger();
    
    
    /**
     * Returns value of Number.
     * 
     * @return value as long
     */
    long getLongValue();
    
    
    /**
     * Returns value of Number.
     * 
     * @return value as double
     */
    double getDoubleValue();
    
    /**
     * Sets value of Number.
     * 
     * @param number representation of number
     */
    void setValue(final long number);
    
    /**
     * Sets value of Number.
     * 
     * @param number representation of number
     */
    void setValue(final double number);
}
