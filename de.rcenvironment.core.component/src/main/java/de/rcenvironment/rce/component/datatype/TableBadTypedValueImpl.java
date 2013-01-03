/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datatype;

import de.rcenvironment.commons.ArrayUtils;
import de.rcenvironment.commons.variables.TypedValue;
import de.rcenvironment.commons.variables.VariableType;

/**
 * Implementation of old-style table (only temporarily).
 * 
 * @author Markus Kunde
 */
public class TableBadTypedValueImpl implements Table {

    /** Separator for runtime table viewer and copy to clipboard functinality. */
    private static final String TABLE_RUNTIMEUI_SEPARATOR = "; ";
    
    /** serial version uid.*/
    private static final long serialVersionUID = -8250733002673360943L;

    private TypedValue[][] dataStore;

    private boolean isExpendable;

    /**
     * Creates an expendable table.
     * 
     */
    public TableBadTypedValueImpl() {
        dataStore = new TypedValue[1][1];
        dataStore[0][0] = new TypedValue(VariableType.Empty);
        isExpendable = true;
    }

    /**
     * Creates a fixed-size table.
     * 
     * @param maxRowNumber number of rows
     * @param maxColumnNumber number of columns
     */
    public TableBadTypedValueImpl(final int maxRowNumber, final int maxColumnNumber) {
        int row = 0;
        int column = 0;
        if (maxRowNumber > 0) {
            row = maxRowNumber;
        }
        if (maxColumnNumber > 0) {
            column = maxColumnNumber;
        }

        dataStore = new TypedValue[row][column];

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                dataStore[r][c] = new TypedValue(VariableType.Empty);
            }
        }

        isExpendable = false;
    }
    
    /**
     * Private constructor to create a new table object.
     * 
     * @param data data of table
     * @param expendable true is table is expendable
     */
    private TableBadTypedValueImpl(final TypedValue[][] data, final boolean expendable) {
        dataStore = data;
        isExpendable = expendable;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#getType()
     */
    @Override
    public WorkflowDataObjectType getType() {
        return WorkflowDataObjectType.Table;
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
     * @see de.rcenvironment.rce.component.datatype.IWorkflowDataType#deserialize(java.lang.Object)
     */
    @Override
    public WorkflowDataObject deserialize(Object jsonWorkflowDataType) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#getMaximumRowIndex()
     */
    @Override
    public int getMaximumRowIndex() {
        return dataStore.length - 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#getMaximumColumnIndex()
     */
    @Override
    public int getMaximumColumnIndex() {
        return dataStore[0].length - 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#getCell(int, int)
     */
    @Override
    public WorkflowDataObjectAtomic getCell(int rowIndex, int columnIndex) {
        WorkflowDataObjectAtomic data;
        
        TypedValue tv;
        try {
            tv = dataStore[rowIndex][columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

        
        switch (tv.getType()) {
        case String:
            data = new ShortTextImpl(tv.getStringValue());
            break;
        case Integer:
            data = new NumberImpl(tv.getIntegerValue());
            break;
        case Logic:
            data = new LogicImpl(tv.getLogicValue());
            break;
        case Real:
            data = new NumberImpl(tv.getRealValue());
            break;
        case Date:
            data = new DateImpl(tv.getStringValue());
            break;
        case Empty:
            data = new EmptyImpl();
            break;
        default:
            data = new EmptyImpl();
            break;
        }
               
        return data;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#setCell(de.rcenvironment.rce.component.datatype.INativeDataType,
     *      int, int)
     */
    @Override
    public void setCell(WorkflowDataObjectAtomic data, int rowIndex, int columnIndex) {
        TypedValue tv;
        
        // Does not fit but can be extended
        if (isExpendable && !((dataStore.length >= rowIndex + 1) && (dataStore[0].length >= columnIndex + 1))) {
            //extend
            
            int row = rowIndex + 1;
            int column = columnIndex + 1;
            
            if (dataStore.length > row) {
                row = dataStore.length;
            }
            if (dataStore[0].length > column) {
                column = dataStore[0].length;
            }
            
            TypedValue[][] newDataStore = new TypedValue[row][column];
            for (int r = 0; r < row; r++) {
                for (int c = 0; c < column; c++) {
                    newDataStore[r][c] = new TypedValue(VariableType.Empty);
                }
            }
            dataStore = multiArrayCopy(dataStore, newDataStore);
        } else if (!isExpendable && !((dataStore.length >= rowIndex + 1) && (dataStore[0].length >= columnIndex + 1))) {
            return;
        }

        
        switch (data.getType()) {
        case ShortText:
            tv = new TypedValue(VariableType.String);
            tv.setStringValue(((ShortTextImpl) data).getValue());
            dataStore[rowIndex][columnIndex] = tv;
            break;
        case Number:
            tv = new TypedValue(VariableType.Real);
            tv.setRealValue(((NumberImpl) data).getDoubleValue());
            dataStore[rowIndex][columnIndex] = tv;
            break;
        case Logic:
            tv = new TypedValue(VariableType.Logic);
            tv.setLogicValue(((LogicImpl) data).getValue());
            dataStore[rowIndex][columnIndex] = tv;
            break;
        case Date:
            tv = new TypedValue(VariableType.Date);
            tv.setStringValue(((DateImpl) data).getValue());
            dataStore[rowIndex][columnIndex] = tv;
            break;
        case Empty:
            dataStore[rowIndex][columnIndex] = new TypedValue(VariableType.Empty);
            break;
        default:
            dataStore[rowIndex][columnIndex] = new TypedValue(VariableType.Empty);
            break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#crop(int, int)
     */
    @Override
    public Table crop(int rowIndex, int columnIndex) {
        TypedValue[][] destination = null;
        
        if (rowIndex >= dataStore.length - 1 && columnIndex >= dataStore[0].length - 1) {
            // Crop indexes are bigger or equal size than origin data
            destination = new TypedValue[dataStore.length][dataStore[0].length];
            destination = multiArrayCopy(this.dataStore, destination);
        } else {
            destination = new TypedValue[rowIndex + 1][columnIndex + 1];
            
            for (int row = 0; row <= rowIndex; row++) {
                for (int column = 0; column <= columnIndex; column++) {
                    TypedValue tv = new TypedValue(dataStore[row][column]);
                    destination[row][column] = tv;
                }
            }
        }
        
        return new TableBadTypedValueImpl(destination, this.isExpendable);
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#getRowIndexLastCellFilled()
     */
    @Override
    public int getRowIndexLastCellFilled() {
        for (int row = dataStore.length - 1; row >= 0; row--) {
            for (int column = dataStore[0].length - 1; column >= 0; column--) {
                if (dataStore[row][column] != null && dataStore[row][column].getType() != VariableType.Empty) {
                    return row;
                }
            }
        }
        
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#getColumnIndexLastCellFilled()
     */
    @Override
    public int getColumnIndexLastCellFilled() {
        for (int row = dataStore.length - 1; row >= 0; row--) {
            for (int column = dataStore[0].length - 1; column >= 0; column--) {
                if (dataStore[row][column] != null && dataStore[row][column].getType() != VariableType.Empty) {
                    return column;
                }
            }
        }
        
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.component.datatype.Table#isExtendable()
     */
    @Override
    public boolean isExtendable() {
        return isExpendable;
    }
    
    
    /**
     * Copy content of a 2d array into a new 2d array.
     * Destination array should be bigger or equal sized.
     * 
     * @param source the source array to be copied
     * @param destination the destination array
     */
    private TypedValue[][]  multiArrayCopy(TypedValue[][] source, TypedValue[][] destination) {
        for (int a = 0; a < source.length; a++) {
            System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
        }
        
        return destination;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.component.datatype.WorkflowDataObject#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return ArrayUtils.arrayToString(dataStore, TABLE_RUNTIMEUI_SEPARATOR);
    }

}
