/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Representing a boolean value.
 *
 * @author Markus Kunde
 */
public class LogicImpl implements Logic {

    /** serial version uid.*/
    private static final long serialVersionUID = -5260467473278856763L;
    
    /** Data container. */
    private boolean bool;
    
    /**
     * Constructor.
     * 
     * @param b representation of Boolean.
     */
    public LogicImpl(final boolean b) {
        setValue(b);
    }
    
    

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.Logic;
    }



    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#serialize()
     */
    @Override
    public Object serialize() {
        // TODO Auto-generated method stub
        return null;
    }



    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#deserialize(java.lang.Object)
     */
    @Override
    public WorkflowDataObject deserialize(Object jsonWorkflowDataType) {
        // TODO Auto-generated method stub
        return null;
    }



    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Logic#setValue(boolean)
     */
    @Override
    public void setValue(boolean value) {
        bool = value;
    }



    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Logic#getValue()
     */
    @Override
    public boolean getValue() {
        return bool;
    }



    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return String.valueOf(getValue());
    }

    
}
