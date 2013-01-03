/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.commons;

import java.io.File;
import java.io.Serializable;

import de.rcenvironment.rce.component.datatype.Table;


/**
 * Represents one normalized channel value.
 * Normalized means that this class contains plain data regardless of transfer method
 *
 * @author Markus Kunde
 */
public class ChannelValue implements Serializable {

    private static final long serialVersionUID = 4317907370959158263L;

    /**
     * True if value is from input channel.
     */
    private boolean isInputValue = false;
    
    /**
     * Array containing the values of channels.
     */
    private Table values = null;  
    
    /**
     * Plain text name of channel.
     */
    private String channelName = null;
    
    /**
     * if true all data will be written into Excel even if cell area does not fit.
     */
    private boolean expand = false;
    
    private File excelFile;
    
    private long iteration;
    
    private ExcelAddress excelAddress;

    
    
    
    
    /**
     * Constructor.
     * 
     * @param excelFile Excel file
     * @param address Excel address where value(s) are connected to
     * @param channelName name of RCE-Channel
     * @param isInputValue if true channel is an input channel
     * @param expanding if true all data will be written into Excel even if cell area does not fit
     * @param iterationStep iteratin of step
     */
    public ChannelValue(final File excelFile, final ExcelAddress address, final String channelName, final boolean isInputValue, 
        final boolean expanding, final long iterationStep) {
        this.excelFile = excelFile;
        excelAddress = address;
        this.channelName = channelName;
        this.isInputValue = isInputValue;
        iteration = iterationStep;
    }

    
    /**
     * Returns true of value is from input channel.
     * @return Returns the isInputValue.
     */
    public boolean isInputValue() {
        return isInputValue;
    }
    
    /**
     * Returns name of channel.
     * 
     * @return channel name
     */
    public String getChannelName() {
        return channelName;
    }
    
    /**
     * Get iteration step.
     * 
     * @return iteration step
     */
    public long getIteration() {
        return iteration;
    }
    
    /**
     * Gets all values of one concrete channel value. 
     * @return Returns the values or null if no plain values are set.
     */
    public Table getValues() {
        return values;
    }
    
    /**
     * Sets values of a concrete channel value. Should be used if vals are only a few.
     * @param vals The values to set.
     */
    public void setValues(final Table vals) {
        this.values = vals;
    }
   
    
    /**
     * Returns Excel address.
     * 
     * @return excel address
     */
    public ExcelAddress getExcelAddress() {
        return excelAddress;
    }

    
    /**
     * Returns expanding flat.
     * 
     * @return if true all data will be written into Excel even if cell area does not fit
     */
    public boolean isExpanding() {
        return expand;
    }
    
    /**
     * Get Excel file object.
     * 
     * @return excel file
     */
    public File getFile() {
        return excelFile;
    }
}
