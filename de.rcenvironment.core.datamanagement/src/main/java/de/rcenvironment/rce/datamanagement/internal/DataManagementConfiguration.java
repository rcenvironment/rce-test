/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

/**
 * Configuration of this bundle.
 *
 * @author Frank Kautz
 * @author Juergen Klein
 * @author Tobias Menden
 */
public class DataManagementConfiguration {
    
    private String catalogBackend = "de.rcenvironment.rce.datamanagement.backend.catalog.derby";
    private String fileDataBackend = "de.rcenvironment.rce.datamanagement.backend.data.efs";

    public void setFileDataBackend(String value){
        this.fileDataBackend = value;
    }
    public void setCatalogBackend(String value){
        this.catalogBackend = value;
    }
    
    public String getCatalogBackend(){
        return catalogBackend;
    }
    public String getFileDataBackend(){
        return fileDataBackend;
    }

}
