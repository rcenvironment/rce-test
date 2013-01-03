/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Interface for dataobject shorttext.
 *
 * @author Markus Kunde
 */
public interface ShortText extends WorkflowDataObjectAtomic {

    /**
     * Setter method for value of data object.
     * 
     * @param value data value
     */
    void setValue(String value);
    
    
    /**
     * Getter method for value of data object.
     * 
     * @return data value as String
     */
    String getValue();
    
    /**
     * Returns the length of this string.
     * 
     * @return the length of the sequence of characters represented by this object
     */
    int length();
    
    /**
     * Returns if ShortText is empty.
     * 
     * 
     * @return true if ShortText contains no text. 
     *         false if ShortText contains text of size 0 or more.
     */
    boolean isEmpty();
}
