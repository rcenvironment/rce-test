/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.excel.commons;


import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.rcenvironment.rce.component.datatype.Table;
import de.rcenvironment.rce.component.datatype.TableBadTypedValueImpl;



/**
 * Utility class for handling Excel stuff.
 *
 * @author Markus Kunde
 */
public final class ExcelUtils {
        
    private ExcelUtils() {}
    
    
    /**
     * Makes relative Eclipse iFile and absolute files into absolute files.
     * 
     * @param pathOfFile path of file
     * @return absolute file or null if not existing
     */
    public static File getAbsoluteFile(String pathOfFile) {
        File file = null;
        
        if (pathOfFile == null || pathOfFile.isEmpty()) {
            return file;
        }
        
        IPath path = new Path(pathOfFile);
        
        if (path.isAbsolute()) {
            file = new File(pathOfFile);
            if (!file.exists()) {
                file = null;
            }
        } else {
            IFile fileEclipse = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            if (fileEclipse.exists()) {
                file = fileEclipse.getRawLocation().toFile();
            }
        }
        
        return file;
    }
    
     
    
    
    
    /**
     * Returns an empty data container regarding to the tablesize described in an ExcelAddress.
     * This container has no link to any Excel file or Excel address.
     * 
     * @param addr Address which describes the size of a table.
     * @return Empty ITable data container representing datasize
     */
    public static Table getEmptyDataContainerOfAddressSize(ExcelAddress addr) {
        Table retTable;
        
        retTable = new TableBadTypedValueImpl(addr.getNumberOfRows(), addr.getNumberOfColumns());
        
        return retTable;
    }
    
    
    
    
    
    /**
     * Returns an empty data container.
     * 
     * @return Empty ITable data container
     */
    public static Table getEmptyDataContainer() {
        Table retTable;
        
        retTable = new TableBadTypedValueImpl();
        
        return retTable;
    }

}
