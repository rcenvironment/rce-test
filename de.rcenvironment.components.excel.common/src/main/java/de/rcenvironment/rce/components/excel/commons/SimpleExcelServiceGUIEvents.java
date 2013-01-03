/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.commons;

import java.io.File;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.activeX.ActiveXInvocationProxy;

import de.rcenvironment.commons.ServiceUtils;

/**
 * Class providing convenient access to the Excel Service GUI events. It serves as an
 * abstraction.
 *
 * @author Markus Kunde
 */
public class SimpleExcelServiceGUIEvents implements ExcelServiceGUIEvents {

    private static ExcelServiceGUIEvents excelService = ServiceUtils.createNullService(ExcelServiceGUIEvents.class);
    
    /*
     * 
     * Service bindings.
     * 
     */
    
    protected void bindExcelService(ExcelServiceGUIEvents newExcelService) {
        excelService = newExcelService;
    }
    
    protected void unbindExcelService(ExcelServiceGUIEvents oldExcelService) {
        // nothing to do here, but if this unbind method is missing, OSGi DS failed when disposing component
        // (seems to be a DS bug)
        //excelService.closeExcel();
    }
    
    @Override
    public ActiveXComponent openExcelApplicationRegisterListener(File xlFile, String address, ActiveXInvocationProxy listener) {
        return excelService.openExcelApplicationRegisterListener(xlFile, address, listener);
    }

    @Override
    public void quitExcel(ActiveXComponent axc, boolean displayAlerts) {
        excelService.quitExcel(axc, displayAlerts);
    }

}
