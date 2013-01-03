/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Representing a date.
 *
 * @author Markus Kunde
 */
public class DateImpl implements Date {

    /** serial version uid.*/
    private static final long serialVersionUID = 2377294006621067485L;
    
    /** Data container. */
    private String date;
    
    /**
     * Constructor.
     * 
     * @param dateAsString representation of date
     */
    public DateImpl(final String dateAsString) {
        setValue(dateAsString);
        
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.Date;
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
     * @see de.rcenvironment.rce.component.datatype.Date#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        date = value;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Date#getValue()
     */
    @Override
    public String getValue() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return getValue();
    }

    
    
}
