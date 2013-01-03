/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Interface for dataobject logic.
 *
 * @author Markus Kunde
 */
public interface Logic extends WorkflowDataObjectAtomic {

    /**
     * Setter method for value of data object.
     * 
     * @param value data value
     */
    void setValue(boolean value);
    
    
    /**
     * Getter method for value of data object.
     * 
     * @return data value as String
     */
    boolean getValue();
}
