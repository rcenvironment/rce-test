/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.commons;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.component.datatype.NumberImpl;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectType;
import de.rcenvironment.rce.component.datatype.Table;
import de.rcenvironment.rce.component.datatype.Number;
import de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl;
import de.rcenvironment.rce.components.excel.commons.internal.ExcelServiceOLE;


/**
 * Test class for ExcelFile.
 *
 * @author Markus Kunde
 */
public class ExcelServiceOLETest {

    private static final int S20_RESULT = 20;

    private static final int S15_RESULT = 15;

    private static final int S30_RESULT = 30;

    private static final String EXTERNAL_TEST_EXCELFILE = "externalFiles/ExcelTester.xls";

    private static final String EXTERNAL_TEMP_EXCELFILE = "externalFiles/ExcelTesterTemp.xls";
    
    private static final String EXTERNAL_TEST_EXCELFILE_NOTIMPL = "externalFiles/NotImplementedTest.xls";
    
    private ExcelService excelService;
    
    private File xlFile = new File(EXTERNAL_TEMP_EXCELFILE);
        
    /**
     * SetUp method.
     * @throws Exception errors
     * 
     */
    @Before
    public void setUp() throws Exception {
        
        FileUtils.copyFile(new File(EXTERNAL_TEST_EXCELFILE), new File(EXTERNAL_TEMP_EXCELFILE));
        excelService = new ExcelServiceOLE();  
    }
    
    /**
     * Deletes temp Excel file.
     * 
     * @throws java.lang.Exception if something goes wrong
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(new File(EXTERNAL_TEMP_EXCELFILE));
    }

    /**
     * Get macros test.
     * 
     */
    @Test
    public void testGetMacrosOLE() {               
        String[] macros = excelService.getMacros(xlFile);      
        assertEquals(1, macros.length);       
        assertEquals("Modul1.Makro1", macros[0]);
    }

    /**
     * run macros test.
     * @throws IOException io error
     * @throws IllegalArgumentException illegal argument
     * @throws InvalidFormatException  invalid format
     * 
     */
    @Test
    public void testRunMacrosOLE() throws InvalidFormatException, IllegalArgumentException, IOException {        
        assertEquals(WorkflowDataObjectType.Empty, 
            excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!C8")).getCell(0, 0).getType());
        
        
        excelService.runMacro(xlFile, "Modul1.Makro1");
        
        
        assertEquals(S30_RESULT, 
            ((Number) excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!C8")).getCell(0, 0)).getLongValue());
    }

    /**
     * recalculate formulas test.
     * @throws IOException io error
     * @throws IllegalArgumentException illegal argument
     * @throws InvalidFormatException invalid format
     * 
     */
    @Test
    public void testRecalculateFormulasOLE() throws InvalidFormatException, IllegalArgumentException, IOException {
        assertEquals(S15_RESULT, 
            ((Number) excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!A8")).getCell(0, 0)).getLongValue());
    
    
        Table values = new TableBadTypedValueImpl(1, 1);
        values.setCell(new NumberImpl(6), 0, 0);
        excelService.setValues(xlFile, new ExcelAddress(xlFile, "Tabelle1!A1"), values);
        
        excelService.recalculateFormulas(xlFile);
        
        assertEquals(S20_RESULT, 
            ((Number) excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!A8")).getCell(0, 0)).getLongValue());
    
    
        excelService.recalculateFormulas(new File(EXTERNAL_TEST_EXCELFILE_NOTIMPL));
    }

}
