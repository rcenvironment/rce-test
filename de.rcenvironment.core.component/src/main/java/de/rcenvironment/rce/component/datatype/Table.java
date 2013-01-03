/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Representation of 2-dimensional grid of INativeDataTypes.
 * Implementation is Zero-based indexes.
 * There is no "null" representation in implementation. 
 * Null values should be replaced with Empty-datatype.
 *
 * @author Markus Kunde
 */
public interface Table extends WorkflowDataObject {
    
    /** Separator for runtime table viewer and copy to clipboard functinality. */
    String TABLE_RUNTIMEUI_SEPARATOR = "; ";
    
    /**
     * Returns maximum row index. 
     * 
     * @return maximum row index
     */
    int getMaximumRowIndex();
    
    /**
     * Returns maximum column index.
     * 
     * @return maximum column index
     */
    int getMaximumColumnIndex();
    
    /**
     * Returns specified cell.
     * 
     * @param rowIndex Row index
     * @param columnIndex Column index
     * @return specified cell or null if out of range.
     */
    WorkflowDataObjectAtomic getCell(final int rowIndex, final int columnIndex);
    
    /**
     * Sets specified cell.
     * If table is not extendable and indexes are out of range, nothing happenes.
     * 
     * @param data native data type
     * @param rowIndex Row index
     * @param columnIndex Column index
     */
    void setCell(final WorkflowDataObjectAtomic data, final int rowIndex, final int columnIndex);
    
    /**
     * Crops table including maximum row and column index.
     * 
     * @param rowIndex Row index
     * @param columnIndex Column index
     * @return Table new table object
     */
    Table crop(final int rowIndex, final int columnIndex);
    
    /**
     * Returns row index of last cell which contains data.
     * 
     * @return row index of last cell which contains data
     */
    int getRowIndexLastCellFilled();
    
    /**
     * Returns column index of last cell which contains data.
     * 
     * @return column index of last cell which contains data
     */
    int getColumnIndexLastCellFilled();
    
    /**
     * Returns if table has a variable number of rows and columns for fixed size.
     * 
     * @return true if number of rows and columns are not fixed
     */
    boolean isExtendable();
}
