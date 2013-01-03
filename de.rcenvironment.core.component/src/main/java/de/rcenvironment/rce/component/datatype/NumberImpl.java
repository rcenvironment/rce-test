/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Representing floating point or integer numbers with a maximum positive or negative size.
 *
 * @author Markus Kunde
 */
public class NumberImpl implements Number {

    /** serial version uid.*/
    private static final long serialVersionUID = -8965284812944338827L;
    
    
    /** Data container. */
    private double data;
    
    
    /**
     * Constructor for floating point number.
     * 
     * @param number representation of number
     */
    public NumberImpl(final double number) {
        setValue(number);
    }
    
    /**
     * Constructor for integer number.
     * 
     * @param number representation of number
     */
    public NumberImpl(final long number) {
        setValue(number);
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Number#isInteger()
     */
    @Override
    public boolean isInteger() {
        if (data % 1.0 == 0) {
            return true;
        }
        return false;
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Number#getLongValue()
     */
    @Override
    public long getLongValue() {
        return Math.round(data);
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Number#getDoubleValue()
     */
    @Override
    public double getDoubleValue() {
        return data;
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Number#setValue(long)
     */
    @Override
    public void setValue(final long number) {
        data = new Double(number).doubleValue();
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.Number#setValue(double)
     */
    @Override
    public void setValue(final double number) {
        data = number;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.Number;
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
        return String.valueOf(getDoubleValue());
    }

    
}
