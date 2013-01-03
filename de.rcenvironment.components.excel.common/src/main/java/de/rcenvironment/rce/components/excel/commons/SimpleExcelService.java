/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.commons;

import java.io.File;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.component.datatype.Table;


/**
 * Class providing convenient access to the Excel Service. It serves as an
 * abstraction.
 *
 * @author Markus Kunde
 */
public class SimpleExcelService implements ExcelService {

    private static ExcelService excelService = ServiceUtils.createNullService(ExcelService.class);
    
    /*
     * 
     * Service bindings.
     * 
     */
    
    protected void bindExcelService(ExcelService newExcelService) {
        excelService = newExcelService;
    }
    
    protected void unbindExcelService(ExcelService oldExcelService) {
        // nothing to do here, but if this unbind method is missing, OSGi DS failed when disposing component
        // (seems to be a DS bug)
        //excelService.closeExcel();
    }

    @Override
    public boolean isValidExcelFile(File xlFile) {
        return excelService.isValidExcelFile(xlFile);
    }

    @Override
    public void setValues(File xlFile, ExcelAddress addr, Table values) throws ExcelException {
        excelService.setValues(xlFile, addr, values);
    }

    @Override
    public void setValues(File xlFile, File newFile, ExcelAddress addr, Table values) throws ExcelException {
        excelService.setValues(xlFile, newFile, addr, values);
    }

    @Override
    public Table getValueOfCells(File xlFile, ExcelAddress addr) throws ExcelException {
        return excelService.getValueOfCells(xlFile, addr);
    }

    @Override
    public ExcelAddress[] getUserDefinedCellNames(File xlFile) throws ExcelException {
        return excelService.getUserDefinedCellNames(xlFile);
    }

    @Override
    public String[] getMacros(File xlFile) throws ExcelException {
        return excelService.getMacros(xlFile);
    }

    @Override
    public void runMacro(File xlFile, String macroname) throws ExcelException {
        excelService.runMacro(xlFile, macroname);
        
    }

    @Override
    public void recalculateFormulas(File xlFile) throws ExcelException {
        excelService.recalculateFormulas(xlFile);
    }
}
