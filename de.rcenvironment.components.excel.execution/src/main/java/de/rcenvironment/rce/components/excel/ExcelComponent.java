/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.channel.ChannelDataTypes;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.TypedValue;
import de.rcenvironment.commons.variables.VariableType;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.datatype.Date;
import de.rcenvironment.rce.component.datatype.DateImpl;
import de.rcenvironment.rce.component.datatype.EmptyImpl;
import de.rcenvironment.rce.component.datatype.LogicImpl;
import de.rcenvironment.rce.component.datatype.Number;
import de.rcenvironment.rce.component.datatype.NumberImpl;
import de.rcenvironment.rce.component.datatype.ShortTextImpl;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectAtomic;
import de.rcenvironment.rce.component.datatype.Table;
import de.rcenvironment.rce.component.datatype.Logic;
import de.rcenvironment.rce.component.datatype.ShortText;
import de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectConverter;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectType;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.components.excel.commons.ChannelValue;
import de.rcenvironment.rce.components.excel.commons.ExcelAddress;
import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.components.excel.commons.ExcelHistoryObject;
import de.rcenvironment.rce.components.excel.commons.ExcelUtils;


/**
 * Excel implementation of {@link Component}.
 * 
 * @author Markus Kunde
 * @author Patrick Schaefer
 * @author Doreen Seider
 */
public class ExcelComponent extends ExcelRCEComponent {
    
    private static final String EXCEPTION_MSG_CANNOT_DETERMINE_VALUE = "Value at Excel address was null. Cannot determine value.";

    private static final String EXCEPTION_MSG_WRONGTYPE_2 = " while value is ";

    private static final String EXCEPTION_MSG_WRONGTYPE_1 = "Channel-Type is ";

    /** OLE cannot handle filename longer than 20 characters. */
    private static final int MAXIMUM_FILENAME_OLE_ACCEPTS = 20;

    private long iteration = 0;
    
    private List<ChannelValue> historyPoints;
    
    private TempFileUtils tempFileUtils = TempFileUtils.getDefaultInstance();
    
      

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.components.excel.ExcelRCEComponent
     *      #executingOneStep(de.rcenvironment.rce.component.endpoint.Input, java.util.Map, boolean)
     */
    @Override
    protected boolean executingOneStep(Input newInput, Map<String, Deque<Input>> inputValues, final boolean ignoreInputs) 
        throws ComponentException {
        
        historyPoints = new ArrayList<ChannelValue>();
        boolean isSuccessful = true;
        File excelWorkingFile = null;
        
        try {
            iteration++;
            
            /* Copy origin Excel file to temp Excel file. */
            
            File originExcelFile = null;
            try {
                originExcelFile = ExcelUtils.getAbsoluteFile(getConfigurationValue(ExcelComponentConstants.XL_FILENAME, String.class));

                if (originExcelFile == null) {
                    throw new ComponentException("Cannot execute Excel component. Maybe filename/path is wrong?");
                }
                excelWorkingFile = tempFileUtils.createTempFileWithFixedFilename(originExcelFile.getName()); 
                FileUtils.copyFile(originExcelFile, excelWorkingFile, true);
                                
                /* Cutting filename to maximum size. */
                String fileName = excelWorkingFile.getName();
                if (fileName.length() > MAXIMUM_FILENAME_OLE_ACCEPTS) {
                    String newFileName = fileName.substring(fileName.length() - MAXIMUM_FILENAME_OLE_ACCEPTS, fileName.length());
                    File dest = new File(excelWorkingFile.getParent() + File.separator + newFileName);
                    boolean renamed = excelWorkingFile.renameTo(dest);
                    if (renamed) {
                        excelWorkingFile = dest;
                    } else {
                        logger.error("Could not shorten file name. "
                            + "It's possible that VBA Codes did not execute. "
                            + "Component will continue to try.");
                    }
                } 
            } catch (IOException e) {
                logger.error(e.getStackTrace());
                isSuccessful = false;
                throw new ComponentException("Could not copy origin Excel file to temp directory.");
            }
               
            
            /* Run macro start */
            excelService.runMacro(excelWorkingFile, getConfigurationValue(ExcelComponentConstants.PRE_MACRO, String.class));
            
            
            /* Processing inputs */
            if (!ignoreInputs) {
                processingInputChannelsOLDDATATYPES(excelWorkingFile, newInput, inputValues);
            }
            
            
            /* Run macro run */
            excelService.runMacro(excelWorkingFile, getConfigurationValue(ExcelComponentConstants.RUN_MACRO, String.class));
            
            
            /* Processing outputs */
            processingOutputChannelsOLDDATATYPES(excelWorkingFile);
            
            /* Run macro close */
            excelService.runMacro(excelWorkingFile, getConfigurationValue(ExcelComponentConstants.POST_MACRO, String.class));

        } finally {
            try {
                ExcelHistoryObject eho = new ExcelHistoryObject();
                eho.setHistoryPoints(historyPoints);
                
                dataManagementService.addHistoryDataPoint(eho, instInformation.getName());
            } catch (IOException e) {
                logger.error("Cannot create history point for Excel component.");
            }
            
            if (excelWorkingFile != null) {
                try {
                    tempFileUtils.disposeManagedTempDirOrFile(excelWorkingFile);
                } catch (IOException e) {
                    logger.error("Cannot delete sandbox Excel file.");
                }
            }
        }
 
        return isSuccessful;
    }

    
    
    
    
    
    /**
     * Old method for usage with old datatypes.
     */
    private void processingOutputChannelsOLDDATATYPES(final File excelFile) throws ComponentException {
        for (final String outputName: instInformation.getOutputDefinitions().keySet()) {
            final Output output = instInformation.getOutput(outputName);
            final boolean expand = getMetaData(output, ExcelComponentConstants.METADATA_EXPANDING, Boolean.class).booleanValue();
            final String address = getMetaData(output, ExcelComponentConstants.METADATA_ADDRESS, String.class);              
            final ExcelAddress addr = new ExcelAddress(excelFile, address);          
            Table value = excelService.getValueOfCells(excelFile, addr);
            WorkflowDataObjectAtomic ndt = null;
            
            
            switch (ChannelDataTypes.toEnum(((Class<?>) output.getType()).getName())) {
            case STRING:
                ndt = value.getCell(0, 0);
                if (ndt != null && ndt instanceof ShortText) {
                    output.write(((ShortText) ndt).getValue());
                } else if (ndt != null) {
                    logger.warn("Trying Excel value to cast to String value.");
                    ShortText st = WorkflowDataObjectConverter.convertWorkflowDataObjectAtomic(ndt, ShortText.class);
                    if (st != null) {
                        output.write(st.getValue());
                    } else {
                        throw new ComponentException(EXCEPTION_MSG_WRONGTYPE_1 + WorkflowDataObjectType.ShortText 
                            + EXCEPTION_MSG_WRONGTYPE_2 + ndt.getType().name());
                    }
                } else {
                    throw new ComponentException(EXCEPTION_MSG_CANNOT_DETERMINE_VALUE);
                }
                break;
            case LONG:
                ndt = value.getCell(0, 0);
                if (ndt != null && ndt instanceof Number) {
                    output.write(((Number) ndt).getLongValue());
                } else if (ndt != null) {
                    logger.warn("Trying Excel value to cast to Long value.");
                    Number st = WorkflowDataObjectConverter.convertWorkflowDataObjectAtomic(ndt, Number.class);
                    if (st != null) {
                        output.write(st.getLongValue());
                    } else {
                        throw new ComponentException(EXCEPTION_MSG_WRONGTYPE_1 + WorkflowDataObjectType.Number 
                            + EXCEPTION_MSG_WRONGTYPE_2 + ndt.getType().name());
                    }
                } else {
                    throw new ComponentException(EXCEPTION_MSG_CANNOT_DETERMINE_VALUE);
                }
                break;
            case DOUBLE:
                ndt = value.getCell(0, 0);
                if (ndt != null && ndt instanceof Number) {
                    output.write(((Number) ndt).getDoubleValue());
                } else if (ndt != null) {
                    logger.warn("Trying Excel value to cast to Double value.");
                    Number st = WorkflowDataObjectConverter.convertWorkflowDataObjectAtomic(ndt, Number.class);
                    if (st != null) {
                        output.write(st.getDoubleValue());
                    } else {
                        throw new ComponentException(EXCEPTION_MSG_WRONGTYPE_1 + WorkflowDataObjectType.Number 
                            + EXCEPTION_MSG_WRONGTYPE_2 + ndt.getType().name());
                    }
                } else {
                    throw new ComponentException(EXCEPTION_MSG_CANNOT_DETERMINE_VALUE);
                }
                break;
            case BOOLEAN:
                ndt = value.getCell(0, 0);
                if (ndt != null && ndt instanceof Logic) {
                    output.write(((Logic) ndt).getValue());
                } else if (ndt != null) {
                    logger.warn("Trying Excel value to cast to Boolean value.");
                    Logic st = WorkflowDataObjectConverter.convertWorkflowDataObjectAtomic(ndt, Logic.class);
                    if (st != null) {
                        output.write(st.getValue());
                    } else {
                        throw new ComponentException(EXCEPTION_MSG_WRONGTYPE_1 + WorkflowDataObjectType.Logic 
                            + EXCEPTION_MSG_WRONGTYPE_2 + ndt.getType().name());
                    }
                } else {
                    throw new ComponentException(EXCEPTION_MSG_CANNOT_DETERMINE_VALUE);
                }
                break;
            case VARIANTARRAY:
                boolean pruning = getMetaData(output, ExcelComponentConstants.METADATA_PRUNING, Boolean.class).booleanValue();
                
                if (pruning) {
                    int latestRowIndex = value.getRowIndexLastCellFilled();
                    int latestColumnIndex = value.getMaximumColumnIndex();
                    
                    value = value.crop(latestRowIndex, latestColumnIndex);
                }
                
                if (value != null) {
                    int rows = value.getMaximumRowIndex() + 1;
                    int cols = value.getMaximumColumnIndex() + 1;
                    VariantArray vArray = new VariantArray("outgoing", rows, cols);                
                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {                            
                            TypedValue tv = null;
                            WorkflowDataObjectAtomic wdoa = value.getCell(row, col);
                            
                            switch (wdoa.getType()) {
                            case ShortText:
                                tv = new TypedValue(VariableType.String);
                                tv.setStringValue(((ShortText) wdoa).getValue());
                                break;
                            case Number:
                                tv = new TypedValue(VariableType.Real);
                                tv.setRealValue(((Number) wdoa).getDoubleValue());
                                break;
                            case Logic:
                                tv = new TypedValue(VariableType.Logic);
                                tv.setLogicValue(((Logic) wdoa).getValue());
                                break;
                            case Date:
                                tv = new TypedValue(VariableType.Date);
                                tv.setStringValue(((Date) wdoa).getValue());
                                break;
                            case Empty:
                                tv = new TypedValue(VariableType.Empty);
                                break;
                            default:
                                tv = new TypedValue(VariableType.Empty);
                                break;
                            }
                            
                            vArray.setValue(tv, row, col);
                        }
                    }
                    output.write(vArray);
                } else {
                    throw new ComponentException("Cannot get Table values from Excel file.");
                }
                break;
            default:
                throw new ComponentException("Wrong output channel type discovered: " + output.getType().getName());
            }
            
            
            //Fill runtime GUI data
            File originExcelFile = ExcelUtils.getAbsoluteFile(getConfigurationValue(ExcelComponentConstants.XL_FILENAME, String.class));
            ChannelValue dataval = new ChannelValue(originExcelFile, addr, output.getName(), false, expand, iteration);
            dataval.setValues(value);
            notificationService.send(instInformation.getIdentifier() + ExcelComponentConstants.NOTIFICATION_SUFFIX, dataval);
        
            historyPoints.add(dataval);
        }
    }
    
    
    /**
     * Processes all input channels in one processing step.
     * 
     * @param excelFile the Excel file where input values should be written to
     * @param newInput {@link #runStep(Input, Map) newInput}
     * @param inputValues {@link #runStep(Input, Map) inputValues}
     * @throws ComponentException thrown if processing output channel has gone wrong
     */
    private void processingInputChannelsOLDDATATYPES(final File excelFile, Input newInput, Map<String, Deque<Input>> inputValues) 
        throws ComponentException {
        for (final Entry<String, Deque<Input>> inputEntry: inputValues.entrySet()) {
            
            Input input = inputEntry.getValue().pollFirst();

            String address = getMetaData(input, ExcelComponentConstants.METADATA_ADDRESS, String.class);
            
            ExcelAddress addr = new ExcelAddress(excelFile, address);
            
            boolean expand = getMetaData(input, ExcelComponentConstants.METADATA_EXPANDING, Boolean.class).booleanValue();
            
            final Serializable ser = input.getValue();
            
            Table value = null;
            switch (ChannelDataTypes.toEnum(((Class<?>) input.getType()).getName())) {
            case STRING:
                value = ExcelUtils.getEmptyDataContainer();
                value.setCell(new ShortTextImpl((String) ser), 0, 0);
                excelService.setValues(excelFile, addr, value);
                break;
            case LONG:
                value = ExcelUtils.getEmptyDataContainer();
                value.setCell(new NumberImpl((Long) ser), 0, 0);
                excelService.setValues(excelFile, addr, value);
                break;
            case DOUBLE:
                value = ExcelUtils.getEmptyDataContainer();
                value.setCell(new NumberImpl((Double) ser), 0, 0);
                excelService.setValues(excelFile, addr, value);
                break;
            case BOOLEAN:
                value = ExcelUtils.getEmptyDataContainer();
                value.setCell(new LogicImpl(((Boolean) ser).booleanValue()), 0, 0);
                excelService.setValues(excelFile, addr, value);
                break;
            case VARIANTARRAY:
                VariantArray va = (VariantArray) ser;
                
                if (expand) {
                    addr = ExcelAddress.getExcelAddressForTableRange(excelFile, addr, va.getDimensions()[0], va.getDimensions()[1]);
                }
                
                int noOfRows = addr.getNumberOfRows();
                int noOfColumns = addr.getNumberOfColumns();
                
                value = new TableBadTypedValueImpl(noOfRows, noOfColumns);
                                
                for (int row = 0; row < noOfRows; row++) {
                    for (int col = 0; col < noOfColumns; col++) {
                        TypedValue tv = va.getValue(row, col);
                        WorkflowDataObjectAtomic wdoa = null;
                        
                        switch(tv.getType()) {
                        case String:
                            wdoa = new ShortTextImpl(tv.getStringValue());
                            break;
                        case Integer:
                            wdoa = new NumberImpl(tv.getIntegerValue());
                            break;
                        case Real:
                            wdoa = new NumberImpl(tv.getRealValue());
                            break;
                        case Logic:
                            wdoa = new LogicImpl(tv.getLogicValue());
                            break;
                        case Date:
                            wdoa = new DateImpl(tv.getStringValue());
                            break;
                        case Empty:
                            wdoa = new EmptyImpl();
                            break;
                        default:
                            wdoa = new EmptyImpl();
                            break;  
                        }
                        value.setCell(wdoa, row, col);
                    }
                }
                                
                excelService.setValues(excelFile, addr, value);
                break;
            default:
                break;
            }
            
            //Fill data for runtime GUI and send it
            File originExcelFile = ExcelUtils.getAbsoluteFile(getConfigurationValue(ExcelComponentConstants.XL_FILENAME, String.class));
            ChannelValue dataval = new ChannelValue(originExcelFile, addr, input.getName(), true, expand, iteration);
            dataval.setValues(value);
            notificationService.send(instInformation.getIdentifier() + ExcelComponentConstants.NOTIFICATION_SUFFIX, dataval);
            
            historyPoints.add(dataval);
        }
    }
}
