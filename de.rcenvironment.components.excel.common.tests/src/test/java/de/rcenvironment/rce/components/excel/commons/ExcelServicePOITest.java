/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.excel.commons;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.component.datatype.DateImpl;
import de.rcenvironment.rce.component.datatype.EmptyImpl;
import de.rcenvironment.rce.component.datatype.LogicImpl;
import de.rcenvironment.rce.component.datatype.NumberImpl;
import de.rcenvironment.rce.component.datatype.ShortTextImpl;
import de.rcenvironment.rce.component.datatype.WorkflowDataObjectType;
import de.rcenvironment.rce.component.datatype.Table;
import de.rcenvironment.rce.component.datatype.Number;
import de.rcenvironment.rce.component.datatype.ShortText;
import de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl;
import de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI;

/**
 * Test class for ExcelFile.
 * 
 * @author Markus Kunde
 */
public class ExcelServicePOITest {

    private static final int S15_VAL = 15;

    private static final int S123_VAL = 123;

    private static final double S123_45_VAL = 123.45;

    private static final String FOO = "Foo";

    private static final String EXTERNAL_TEST_EXCELFILE = "externalFiles/ExcelTester.xls";
    
    private static final String EXTERNAL_TEST_EXCELFILE_NOTIMPL = "externalFiles/NotImplementedTest.xls";

    private static final String EXTERNAL_TEMP_EXCELFILE = "externalFiles/ExcelTesterTemp.xls";
    
    private static final String EXTERNAL_TEMP2_EXCELFILE = "externalFiles/ExcelTesterTemp2.xls";

    private static final String EXTERNAL_TEST_NOTEXCELFILE = "externalFiles/Feedback_Tixi.txt";

    private ExcelService excelService;
    
    private File xlFile = new File(EXTERNAL_TEMP_EXCELFILE);
    
    private ExcelAddress addr;
    
    private Table values;

    /**
     * Creates temp Excel file.
     * 
     * @throws java.lang.Exception if something goes wrong
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyFile(new File(EXTERNAL_TEST_EXCELFILE), new File(EXTERNAL_TEMP_EXCELFILE));
        excelService = new ExcelServicePOI();
        
        addr = new ExcelAddress(new File(EXTERNAL_TEMP_EXCELFILE), "Tabelle1!A1:D5");
        
        values = new TableBadTypedValueImpl(2, 3);
        values.setCell(new ShortTextImpl(FOO), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl("test date"), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
    }

    /**
     * Deletes temp Excel file.
     * 
     * @throws java.lang.Exception if something goes wrong
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(new File(EXTERNAL_TEMP_EXCELFILE));
        
        FileUtils.deleteQuietly(new File(EXTERNAL_TEMP2_EXCELFILE));
    }

    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#ExcelFile(java.io.File)}.
     */
    @Test
    public void testExcelFile() {
        excelService = new ExcelServicePOI();
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#ExcelFile(java.io.File)}.
     */
    @Test(expected = ExcelException.class)
    public void testExcelFileWrongFile() {
        excelService = new ExcelServicePOI();
        excelService.getUserDefinedCellNames(new File(EXTERNAL_TEST_NOTEXCELFILE));
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#ExcelFile(java.io.File)}.
     */
    @Test(expected = ExcelException.class)
    public void testGetMacros() {
        excelService = new ExcelServicePOI();
        excelService.getMacros(xlFile);
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#ExcelFile(java.io.File)}.
     */
    @Test(expected = ExcelException.class)
    public void testRunMacro() {
        excelService = new ExcelServicePOI();
        excelService.runMacro(xlFile, FOO);
    }

    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI
     * #setValues(de.rcenvironment.rce.components.excel.commons.ExcelAddress, de.rcenvironment.rce.component.datatype.ITable)}.
     */
    @Test
    public void testSetValuesExcelAddressITable() {
        excelService.setValues(xlFile, addr, values);
        
        Table table = excelService.getValueOfCells(xlFile, addr);
        
        assertEquals(FOO, ((ShortText) table.getCell(0, 0)).getValue());
        assertEquals(S123_VAL, ((Number) table.getCell(1, 2)).getLongValue());
        assertEquals("gerger", ((ShortText) table.getCell(2, 2)).getValue());
        assertEquals(4, table.getMaximumRowIndex());
        assertEquals(3, table.getMaximumColumnIndex());
    }

    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI
     * #setValues(de.rcenvironment.rce.components.excel.commons.ExcelAddress, java.io.File, de.rcenvironment.rce.component.datatype.ITable)}
     * .
     */
    @Test
    public void testSetValuesExcelAddressFileITable() {
        File xlFile2 = new File(EXTERNAL_TEMP2_EXCELFILE); 
        excelService.setValues(xlFile, xlFile2, addr, values);        
        ExcelServicePOI excelFile2 = new ExcelServicePOI();
        
        Number n = (Number) excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!A1")).getCell(0, 0);
        
        assertEquals(1, n.getLongValue());
        
        ShortText st = (ShortText) excelFile2.getValueOfCells(xlFile2, new ExcelAddress(xlFile2, "Tabelle1!A1")).getCell(0, 0);
        assertEquals(FOO, st.getValue());
    }

    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI
     * #getValueOfCells(de.rcenvironment.rce.components.excel.commons.ExcelAddress)}
     * .
     * @throws IOException io error
     * @throws IllegalArgumentException illegal argument
     * @throws InvalidFormatException invalid format
     */
    @Test
    public void testGetValueOfCells() throws InvalidFormatException, IllegalArgumentException, IOException {
        Table vals = excelService.getValueOfCells(xlFile, new ExcelAddress(xlFile, "Tabelle1!A1:D8"));
        
        assertEquals(1, ((Number) vals.getCell(0, 0)).getLongValue());
        assertEquals("x", ((ShortText) vals.getCell(0, 3)).getValue());
        assertEquals(S15_VAL, ((Number) vals.getCell(7, 0)).getLongValue());
        assertEquals(WorkflowDataObjectType.Empty, vals.getCell(7, 3).getType());
        
        
        assertEquals(null, vals.getCell(8, 4));
    }

    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#getUserDefinedCellNames()}.
     */
    @Test
    public void testGetUserDefinedCellNames() {        
        List<String> usernamesList = Arrays.asList("I_einzel", "I_Tabelle", "O_Ausgang", "O_MakroAusgang");
        
        assertEquals(usernamesList.size(), excelService.getUserDefinedCellNames(xlFile).length);
        
        for (ExcelAddress address: excelService.getUserDefinedCellNames(xlFile)) {
            assertTrue(usernamesList.contains(address.getUserDefinedName()));
        }
        
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#recalculateFormulas()}.
     */
    @Test
    public void testrecalculateFormulas() {        
        excelService.recalculateFormulas(xlFile);        
    }
    
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.components.excel.commons.internal.ExcelServicePOI#recalculateFormulas()}.
     */
    @Test(expected = ExcelException.class)
    public void testrecalculateFormulasNotImpl() {       
        excelService.recalculateFormulas(new File(EXTERNAL_TEST_EXCELFILE_NOTIMPL));
    }
}
