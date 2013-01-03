/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

/**
 * Provides the configuration of this bundle. 
 *
 * @author Juergen Klein
 * @author Tobias Menden
 */
public class DerbyCatalogBackendConfiguration {

    private String databaseUrl = "";

    public void setDatabaseUrl(String value) {
        this.databaseUrl = value;
    }

    public String getDatabaseURL() {
        return databaseUrl;
    }
}
