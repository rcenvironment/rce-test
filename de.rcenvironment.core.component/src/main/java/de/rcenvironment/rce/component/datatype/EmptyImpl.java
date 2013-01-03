/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Representing an empty value.
 *
 * @author Markus Kunde
 */
public class EmptyImpl implements Empty {

    /** serial version uid.*/
    private static final long serialVersionUID = -885315565329389180L;

    /**
     * Constructor.
     * 
     */
    public EmptyImpl() {}

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.Empty;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#serialize()
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
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return new String();
    }
}
