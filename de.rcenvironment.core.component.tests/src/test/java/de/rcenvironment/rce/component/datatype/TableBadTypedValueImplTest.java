/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for TableBadTypedValueImpl.
 *
 * @author Markus Kunde
 */
public class TableBadTypedValueImplTest {

    private static final String FOO2_VAL = "Foo2";

    private static final int MAX_ROW_NUMBER_NEGATIVE = -1;

    private static final int S123_VAL = 123;

    private static final String TEST_DATE_VAL = "test date";

    private static final double S123_45_VAL = 123.45;

    private static final String FOO_VAL = "Foo";

    private Table values;
    
    private Table valuesGrow;
    
    /**
     * SetUp method.
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        values = new TableBadTypedValueImpl(2, 3);
        values.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
        
        valuesGrow = new TableBadTypedValueImpl();
        valuesGrow.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        valuesGrow.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        valuesGrow.setCell(new LogicImpl(true), 0, 2);
        valuesGrow.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        valuesGrow.setCell(new EmptyImpl(), 1, 1);
        valuesGrow.setCell(new NumberImpl(S123_VAL), 1, 2);
    }


    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#TableBadTypedValueImpl()}.
     */
    @Test
    public void testTableBadTypedValueImplIntInt() {
        values = new TableBadTypedValueImpl(2, 3);
        values.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#TableBadTypedValueImpl()}.
     */
    @Test
    public void testTableBadTypedValueImplIntInt2() {
        values = new TableBadTypedValueImpl(1, 1);
        values.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#TableBadTypedValueImpl()}.
     */
    @Test
    public void testTableBadTypedValueImplIntInt3() {
        values = new TableBadTypedValueImpl(MAX_ROW_NUMBER_NEGATIVE, MAX_ROW_NUMBER_NEGATIVE);
        values.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#TableBadTypedValueImpl(int, int)}.
     */
    @Test
    public void testTableBadTypedValueImpl() {
        values = new TableBadTypedValueImpl();
        values.setCell(new ShortTextImpl(FOO_VAL), 0, 0);
        values.setCell(new NumberImpl(S123_45_VAL), 0, 1);
        values.setCell(new LogicImpl(true), 0, 2);
        values.setCell(new DateImpl(TEST_DATE_VAL), 1, 0);
        values.setCell(new EmptyImpl(), 1, 1);
        values.setCell(new NumberImpl(S123_VAL), 1, 2);
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getType()}.
     */
    @Test
    public void testGetType() {
        assertEquals(values.getType(), WorkflowDataObjectType.Table);
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#serialize()}.
     */
    @Test
    public void testSerialize() {
        /*
         * TODO There is no concept how a serialization with JSON should look like. 
         * Therefore there is no test.
         */
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#deserialize(java.lang.Object)}.
     */
    @Test
    public void testDeserialize() {
        /*
         * TODO There is no concept how a serialization with JSON should look like. 
         * Therefore there is no test.
         */
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getMaximumRowIndex()}.
     */
    @Test
    public void testGetMaximumRowIndex() {
        assertEquals(1, values.getMaximumRowIndex());
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getMaximumRowIndex()}.
     */
    @Test
    public void testGetMaximumRowIndex2() {
        assertEquals(1, valuesGrow.getMaximumRowIndex());
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getMaximumColumnIndex()}.
     */
    @Test
    public void testGetMaximumColumnIndex() {
        assertEquals(2, values.getMaximumColumnIndex());
    }
    
    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getMaximumColumnIndex()}.
     */
    @Test
    public void testGetMaximumColumnIndex2() {
        assertEquals(2, valuesGrow.getMaximumColumnIndex());
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getCell(int, int)}.
     */
    @Test
    public void testGetCell() {
        assertEquals(FOO_VAL, ((ShortTextImpl) values.getCell(0, 0)).getValue());
        assertEquals(WorkflowDataObjectType.Empty, ((EmptyImpl) values.getCell(1, 1)).getType());
        assertEquals(S123_VAL, ((NumberImpl) values.getCell(1, 2)).getLongValue());
        
        assertEquals(FOO_VAL, ((ShortTextImpl) valuesGrow.getCell(0, 0)).getValue());
        assertEquals(WorkflowDataObjectType.Empty, ((EmptyImpl) valuesGrow.getCell(1, 1)).getType());
        assertEquals(S123_VAL, ((NumberImpl) valuesGrow.getCell(1, 2)).getLongValue());
        
        assertEquals(null, values.getCell(MAX_ROW_NUMBER_NEGATIVE, 0));
        assertEquals(null, values.getCell(0, MAX_ROW_NUMBER_NEGATIVE));
        assertEquals(null, values.getCell(2, 2));
        assertEquals(null, values.getCell(1, 3));
        
        assertEquals(null, valuesGrow.getCell(MAX_ROW_NUMBER_NEGATIVE, 0));
        assertEquals(null, valuesGrow.getCell(0, MAX_ROW_NUMBER_NEGATIVE));
        assertEquals(null, valuesGrow.getCell(2, 2));
        assertEquals(null, valuesGrow.getCell(1, 3));
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl
     * #setCell(de.rcenvironment.rce.component.datatype.INativeDataType, int, int)}.
     */
    @Test
    public void testSetCell() {
        values.setCell(new ShortTextImpl(FOO2_VAL), 1, 1);
        assertEquals(FOO2_VAL, ((ShortTextImpl) values.getCell(1, 1)).getValue());
        
        values.setCell(new ShortTextImpl(FOO2_VAL), 3, 3);
        assertEquals(null, values.getCell(3, 3));
        
        
        valuesGrow.setCell(new ShortTextImpl(FOO2_VAL), 1, 1);
        assertEquals(FOO2_VAL, ((ShortTextImpl) valuesGrow.getCell(1, 1)).getValue());
        
        valuesGrow.setCell(new ShortTextImpl(FOO2_VAL), 2, 2);
        assertEquals(WorkflowDataObjectType.Empty, valuesGrow.getCell(2, 0).getType());
        assertEquals(WorkflowDataObjectType.Empty, valuesGrow.getCell(2, 1).getType());
        assertEquals(FOO2_VAL, ((ShortTextImpl) valuesGrow.getCell(2, 2)).getValue());
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#crop(int, int)}.
     */
    @Test
    public void testCrop() {      
        values.crop(1, 0);
        
        //old  values data should be the same
        assertEquals(2, values.getMaximumColumnIndex());
        assertEquals(1, values.getMaximumRowIndex());
        assertEquals(S123_VAL, ((NumberImpl) values.getCell(1, 2)).getLongValue());
        assertEquals(FOO_VAL, ((ShortTextImpl) values.getCell(0, 0)).getValue());
        
        
        values = values.crop(1, 0);
        //test new one
        assertEquals(0, values.getMaximumColumnIndex());
        assertEquals(1, values.getMaximumRowIndex());
        assertEquals(FOO_VAL, ((ShortTextImpl) values.getCell(0, 0)).getValue());
        assertEquals(TEST_DATE_VAL, ((DateImpl) values.getCell(1, 0)).getValue());
        assertEquals(null, values.getCell(1, 1));
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getRowIndexLastCellFilled()}.
     */
    @Test
    public void testGetRowIndexLastCellFilled() {
        assertEquals(1, values.getRowIndexLastCellFilled());
        
        values.setCell(new EmptyImpl(), 4, 4);
        assertEquals(1, values.getRowIndexLastCellFilled());
        
        values.setCell(new NumberImpl(S123_VAL), 4, 4);
        assertEquals(1, values.getRowIndexLastCellFilled());
        
        
        assertEquals(1, valuesGrow.getRowIndexLastCellFilled());
        
        valuesGrow.setCell(new EmptyImpl(), 4, 4);
        assertEquals(1, valuesGrow.getRowIndexLastCellFilled());
        
        valuesGrow.setCell(new NumberImpl(S123_VAL), 4, 4);
        assertEquals(4, valuesGrow.getRowIndexLastCellFilled());
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#getColumnIndexLastCellFilled()}.
     */
    @Test
    public void testGetColumnIndexLastCellFilled() {
        assertEquals(2, values.getColumnIndexLastCellFilled());
        
        values.setCell(new EmptyImpl(), 4, 4);
        assertEquals(2, values.getColumnIndexLastCellFilled());
        
        values.setCell(new NumberImpl(S123_VAL), 4, 4);
        assertEquals(2, values.getColumnIndexLastCellFilled());
        
        
        assertEquals(2, valuesGrow.getColumnIndexLastCellFilled());
        
        valuesGrow.setCell(new EmptyImpl(), 4, 4);
        assertEquals(2, valuesGrow.getColumnIndexLastCellFilled());
        
        valuesGrow.setCell(new NumberImpl(S123_VAL), 4, 4);
        assertEquals(4, valuesGrow.getColumnIndexLastCellFilled());
    }

    /**
     * Test method for {@link de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl#isExtendable()}.
     */
    @Test
    public void testIsExtendable() {
        assertFalse(values.isExtendable());
        assertTrue(valuesGrow.isExtendable());
    }

}
