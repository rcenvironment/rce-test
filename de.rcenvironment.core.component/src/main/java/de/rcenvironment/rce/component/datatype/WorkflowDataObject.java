/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;

import java.io.Serializable;


/**
 * Interface for all available workflow and component data objects.
 * This interface represents the most generic data objects available.
 *
 * @author Markus Kunde
 */
public interface WorkflowDataObject extends Serializable {

    /**
     * Returning the type of the workflow data type.
     * 
     * @return the type
     */
    WorkflowDataObjectType getType();
    
    /**
     * Serialize workflow data type to JSON format.
     * 
     * @return JSON representation ('Object' is just a placeholder)
     */
    Object serialize();
    
    /**
     * Deserialize JSON format to workflow data type.
     * 
     * @param jsonWorkflowDataType JSON representation ('Object' is just a placeholder)
     * @return WorkflowDataType
     */
    WorkflowDataObject deserialize(Object jsonWorkflowDataType);
    
    /**
     * Usage for textual representation of value, e.g., in GUI.
     * 
     * @return Value of WorkflowDataObject as String
     */
    String getValueAsString();

}
