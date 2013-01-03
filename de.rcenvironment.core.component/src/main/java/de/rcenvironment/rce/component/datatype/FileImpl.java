/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * File representation.
 *
 * @author Markus Kunde
 */
public class FileImpl implements File {

    /** serial version uid.*/
    private static final long serialVersionUID = 1103641316445126002L;

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.File;
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
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        // TODO Auto-generated method stub
        return null;
    }

    

}
