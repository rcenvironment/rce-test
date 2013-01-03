/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.excel.commons.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.rcenvironment.rce.component.datatype.Date;
import de.rcenvironment.rce.component.datatype.EmptyImpl;
import de.rcenvironment.rce.component.datatype.Logic;
import de.rcenvironment.rce.component.datatype.LogicImpl;
import de.rcenvironment.rce.component.datatype.Number;
import de.rcenvironment.rce.component.datatype.NumberImpl;
import de.rcenvironment.rce.component.datatype.ShortText;
import de.rcenvironment.rce.component.datatype.ShortTextImpl;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectAtomic;
import de.rcenvironment.rce.component.datatype.Table;
import de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl;
import de.rcenvironment.rce.components.excel.commons.ExcelAddress;
import de.rcenvironment.rce.components.excel.commons.ExcelException;
import de.rcenvironment.rce.components.excel.commons.ExcelService;


/**
 * Excel file representation with access to its data.
 * 
 * @author Markus Kunde
 */
public class ExcelServicePOI implements ExcelService {

    protected static final Log LOGGER = LogFactory.getLog(ExcelServicePOI.class);
    
    protected static final int BLOCKING_ITERATIONMAX = 600; // regarding POI interface unstable behavior

    protected static final int BLOCKING_SLEEP = 50; // regarding POI interface unstable behavior
    
    
    /* Exception messages. */
    private static final String EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED = "Excel file is not found or cannot be opened.";

    private static final String EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT = "Excel file has an invalid format.";

    private static final String EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND = "Excel file cannot not found.";
    
    private static final String EXCMSG_CANNOT_SAVE_FILE_WITH_RESULT_DATA = "Cannot save file with result data.";
   
    
    /**
     * Just a simple test method if Excel file is really an Excel file.
     * 
     * @param xlFile Excel file
     * @throws ExcelException thrown if not a real Excel file
     */
    protected void initialTest(final File xlFile) throws ExcelException {        
        try {
            // Trying with POI
            InputStream inp = null;
            inp = new FileInputStream(xlFile);
            WorkbookFactory.create(inp);

            if (inp != null) {
                try {
                    inp.close();
                } catch (IOException e) {
                    LOGGER.debug("Apache Poi: Closing of input stream does not work.");
                }
            }
        } catch (FileNotFoundException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND, e);
        } catch (InvalidFormatException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT, e);
        } catch (IllegalArgumentException e) {
            throw new ExcelException("Excel file is maybe no Excel file?", e);
        } catch (IOException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED, e);
        }
    }
    
    
    
    @Override
    public void setValues(File xlFile, ExcelAddress addr, Table values) throws ExcelException {
        initialTest(xlFile);
        
        setValues(xlFile, xlFile, addr, values);
    }

    
    
    @Override
    public void setValues(File xlFile, File newFile, ExcelAddress addr, Table values) throws ExcelException {
        initialTest(xlFile);
        
        try {
            if (xlFile != null) {
                InputStream inp = null;
                FileOutputStream fileOut = null;
                inp = new FileInputStream(xlFile);

                try {
                    // Setting values in Excel file 
                    org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inp);
                    Sheet sheet = wb.getSheet(addr.getWorkSheetName());

                    int addressRowCorrection = addr.getBeginningRowNumber() - 1; // Excel address is
                                                                                 // 1-based while POI is
                                                                                 // 0-based
                    int addressColumnCorrection = addr.getBeginningColumnNumber() - 1; // Excel address
                                                                                       // is 1-based
                                                                                       // while POI is
                                                                                       // 0-based

                    for (int row = addressRowCorrection; row < addressRowCorrection + addr.getNumberOfRows()
                        && row <= addressRowCorrection + values.getMaximumRowIndex(); row++) {
                        Row r = sheet.getRow(row);
                        if (r == null) {
                            r = sheet.createRow(row);
                        }
                        for (int col = addressColumnCorrection; col < addressColumnCorrection + addr.getNumberOfColumns()
                            && col <= addressColumnCorrection + values.getMaximumColumnIndex(); col++) {
                            Cell cell = r.createCell(col);
                            WorkflowDataObjectAtomic data =
                                values.getCell(row - addressRowCorrection, col - addressColumnCorrection);

                            if (data == null) {
                                continue;
                            }

                            switch (data.getType()) {
                            case ShortText:
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(((ShortText) data).getValue());
                                break;
                            case Number:
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                cell.setCellValue(((Number) data).getDoubleValue());
                                break;
                            case Logic:
                                cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                                cell.setCellValue(((Logic) data).getValue());
                                break;
                            case Date:
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(((Date) data).getValue());
                                break;
                            case Empty:
                                cell.setCellType(Cell.CELL_TYPE_BLANK);
                                break;
                            default:
                                break;
                            }
                        }
                    }
                    
                    
                    
                    /* 
                     * Solves temporarily the problem with reading from I-Stream and writing to O-Stream
                     * with the same file handle. Causes sometimes exceptions if I-Stream is blocked
                     * when trying to write. Should be reported to Apache POI.
                     */  
                    for (int i = 0; i < BLOCKING_ITERATIONMAX; i++) {
                        try {
                            if (newFile != null) {
                                // Write to file
                                fileOut = new FileOutputStream(newFile);
                                wb.write(fileOut);
                                break;
                            }
                        } catch (FileNotFoundException e) {
                            LOGGER.debug("File not found. (Method: setValueOfCells). Iteration: " + i + ". Retrying.");
                            if (i == (BLOCKING_ITERATIONMAX - 1)) {
                                // Last iteration was not successful
                                LOGGER.error(EXCMSG_CANNOT_SAVE_FILE_WITH_RESULT_DATA);
                                throw new ExcelException(EXCMSG_CANNOT_SAVE_FILE_WITH_RESULT_DATA, e);
                            }
                        } catch (IOException e) {
                            throw new ExcelException(EXCMSG_CANNOT_SAVE_FILE_WITH_RESULT_DATA, e);
                        }
                        try {
                            Thread.sleep(BLOCKING_SLEEP);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getStackTrace());
                        }
                    }
                } finally {
                    if (inp != null) {
                        try {
                            inp.close();
                        } catch (IOException e) {
                            LOGGER.debug("Apache Poi: Closing of input stream does not work. (Method: setValueOfCells)");
                        }
                    }
                    if (fileOut != null) {
                        try {
                            fileOut.flush();
                            fileOut.close();
                        } catch (IOException e) {
                            LOGGER.debug("Apache Poi: Closing of output stream does not work. (Method: setValueOfCells)");
                        }
                    }
                }

                //Recalculate formulas 
                recalculateFormulas(xlFile);
            }
        } catch (FileNotFoundException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND, e);
        } catch (InvalidFormatException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT, e);
        } catch (IOException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED, e);
        }
    }

    
    
    @Override
    public Table getValueOfCells(File xlFile, ExcelAddress addr) throws ExcelException {
        initialTest(xlFile);
        
        // recalculate Formulas
        recalculateFormulas(xlFile);


        // Read with POI
        Table retValues = null;

        if (xlFile != null) {
            // Reads with POI
            InputStream inp = null;
            try {
                inp = new FileInputStream(xlFile);

                org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inp);
                Sheet sheet = wb.getSheet(addr.getWorkSheetName());

                retValues = new TableBadTypedValueImpl(addr.getNumberOfRows(), addr.getNumberOfColumns());

                int addressRowCorrection = addr.getBeginningRowNumber() - 1; // Excel address is
                                                                             // 1-based while POI is
                                                                             // 0-based
                int addressColumnCorrection = addr.getBeginningColumnNumber() - 1; // Excel address
                                                                                   // is 1-based
                                                                                   // while POI is
                                                                                   // 0-based

                for (int row = addressRowCorrection; row < addressRowCorrection + addr.getNumberOfRows(); row++) {
                    Row r = sheet.getRow(row);
                    if (r == null) {
                        r = sheet.createRow(row);
                    }
                    for (int col = addressColumnCorrection; col < addressColumnCorrection + addr.getNumberOfColumns(); col++) {
                        Cell cell = r.getCell(col);
                        WorkflowDataObjectAtomic data;

                        // If cell is empty
                        if (cell == null) {
                            data = new EmptyImpl();
                            retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            continue;
                        }

                        switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            data = new ShortTextImpl(cell.getRichStringCellValue().getString());
                            retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                data = new ShortTextImpl(cell.getDateCellValue().toString());
                                retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            } else {
                                data = new NumberImpl(cell.getNumericCellValue());
                                retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            }
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            data = new LogicImpl(cell.getBooleanCellValue());
                            retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                            try {
                                CellValue cellValue = evaluator.evaluate(cell);
                                switch (cellValue.getCellType()) {
                                case Cell.CELL_TYPE_BOOLEAN:
                                    data = new LogicImpl(cellValue.getBooleanValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    data = new NumberImpl(cellValue.getNumberValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                case Cell.CELL_TYPE_STRING:
                                    data = new ShortTextImpl(cellValue.getStringValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                default:
                                    data = new EmptyImpl();
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                }
                            } catch (org.apache.poi.ss.formula.eval.NotImplementedException e) {
                                // In case evaluator.evalualte(cell) does not implement a specific
                                // formula
                                switch (cell.getCachedFormulaResultType()) {
                                case Cell.CELL_TYPE_BOOLEAN:
                                    data = new LogicImpl(cell.getBooleanCellValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    data = new NumberImpl(cell.getNumericCellValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                case Cell.CELL_TYPE_STRING:
                                    data = new ShortTextImpl(cell.getStringCellValue());
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                default:
                                    data = new EmptyImpl();
                                    retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                                    break;
                                }
                            }
                            break;
                        default:
                            data = new EmptyImpl();
                            retValues.setCell(data, row - addressRowCorrection, col - addressColumnCorrection);
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new ExcelException(EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND, e);
            } catch (InvalidFormatException e) {
                throw new ExcelException(EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT, e);
            } catch (IOException e) {
                throw new ExcelException(EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED, e);
            } finally {
                if (inp != null) {
                    try {
                        inp.close();
                    } catch (IOException e) {
                        LOGGER.debug("Apache Poi: Closing of input stream does not work. (Method: getValueOfCells)");
                    }
                }
            }
        }
        return retValues;
    }

    
    
    @Override
    public ExcelAddress[] getUserDefinedCellNames(File xlFile) throws ExcelException {
        initialTest(xlFile);
        
        InputStream inp = null;
        ExcelAddress[] names;

        try {
            inp = new FileInputStream(xlFile);
            org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inp);
            int noNames = wb.getNumberOfNames();
            names = new ExcelAddress[noNames];
            for (int i = 0; i < noNames; i++) {
                names[i] = new ExcelAddress(xlFile, wb.getNameAt(i).getNameName());
            }
        } catch (FileNotFoundException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND, e);
        } catch (InvalidFormatException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT, e);
        } catch (IOException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED, e);
        }

        return names;
    }

    
    
    @Override
    public String[] getMacros(File xlFile) throws ExcelException {
        initialTest(xlFile);
        throw new ExcelException("Excel is using POI implementation only. Cannot receive macro names.");
    }

    
    
    @Override
    public void runMacro(File xlFile, String macroname) throws ExcelException {
        initialTest(xlFile);
        throw new ExcelException("Excel is using POI implementation only. Cannot execute macro.");
    }

    
    
    @Override
    public void recalculateFormulas(File xlFile) throws ExcelException {
        initialTest(xlFile);
        
        try {
            InputStream inp = new FileInputStream(xlFile);
            org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inp);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);
                for (Row r : sheet) {
                    for (Cell c : r) {
                        if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            evaluator.evaluateFormulaCell(c);
                        }
                    }
                }
            }
        } catch (NotImplementedException e) {
            throw new ExcelException("Formula will be tried to evaluate which is not known in Apache POI.", e);
        } catch (FileNotFoundException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_CANNOT_NOT_FOUND, e);
        } catch (InvalidFormatException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_HAS_AN_INVALID_FORMAT, e);
        } catch (IOException e) {
            throw new ExcelException(EXCMSG_EXCEL_FILE_IS_NOT_FOUND_OR_CANNOT_BE_OPENED, e);
        } 
    }



    @Override
    public boolean isValidExcelFile(File xlFile) {
        try {
            initialTest(xlFile);
        } catch (ExcelException e) {
            return false;
        }
        return true;
    }
}
