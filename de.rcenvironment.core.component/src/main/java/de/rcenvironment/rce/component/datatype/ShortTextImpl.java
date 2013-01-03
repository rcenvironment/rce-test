/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;

import org.apache.commons.lang3.StringUtils;


/**
 * Representing a collection of chars (text) with a maximum size.
 *
 * @author Markus Kunde
 */
public class ShortTextImpl implements ShortText {

    /** Maximum length (number of chars) of ShortText. */
    public static final int MAXLENGTH = 255;
    
    /** serial version uid.*/
    private static final long serialVersionUID = -4746741577077755868L;
    
    /** Data container. */
    private String shorttext = null;
     
    
    /**
     * Constructor.
     * 
     * @param text representation of text. If longer than 'ShortText.MAXLENGTH' text will be cut.
     */
    public ShortTextImpl(final String text) {
        setValue(text);
    }
    
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.ShortText#length()
     */
    @Override
    public int length() {
        int length = 0;
        if (shorttext != null) {
            length = shorttext.length();
        }
        return length;
    }
    
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.ShortText#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        if (shorttext == null) {
            return true;
        }
        return false;
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.ShortText#setValue(java.lang.String)
     */
    @Override
    public void setValue(final String text) {
        if (text != null) {
            shorttext = StringUtils.left(text, MAXLENGTH);
        }
    }
    
    
    
    
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.ShortText;
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
     * @see de.rcenvironment.rce.component.datatype.ShortText#getValue()
     */
    @Override
    public String getValue() {
        return shorttext;
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
