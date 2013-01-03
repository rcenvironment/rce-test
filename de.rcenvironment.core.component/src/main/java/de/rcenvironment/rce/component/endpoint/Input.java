/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;

/**
 * Holds an {@link Input} value and all corresponding information.
 * 
 * @author Doreen Seider
 */
public class Input implements Serializable {

    private static final long serialVersionUID = 4562978720420314084L;

    private String name;

    private final Class<? extends Serializable> type;

    private Serializable value;
    
    private String workflowIdentifier;
    
    private String componentIdentifier;
    
    private int number;
    
    /**
     * Constructor.
     * 
     * @param type Type of containing value.
     * @param newCompInstanceId The Id of the owning Component.
     * @param newName The name of the {@link Input}.
     */
    public Input(String newName, Class<? extends Serializable> newType, Serializable newValue,
        String newWorkflowIdentifier, String newComponentIdentifier, int number) {
        name = newName;
        type = newType;
        value = newValue;
        workflowIdentifier = newWorkflowIdentifier;
        componentIdentifier = newComponentIdentifier;
        this.number = number;
    }
    /**
     * Clones the input.
     * @see java.lang.Object#clone()
     * @return an exact copy
     */
    public Input clone(){
        return new Input(name, type, value, workflowIdentifier, componentIdentifier, number);
    }
    
    public Serializable getValue() {
        return value;
    }
    
    public void setValue(Serializable value) {
        this.value = value;
    }

    public Class<? extends Serializable> getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public String getWorkflowIdentifier() {
        return workflowIdentifier;
    }
    
    public String getComponentIdentifier() {
        return componentIdentifier;
    }
    
    public int getNumber() {
        return number;
    }
}
