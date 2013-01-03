/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import java.io.Serializable;

/**
 * Interface implemented by queries to execute on the catalog.
 * 
 * @author Dirk Rossow
 * @author Juergen Klein
 */
public interface Query extends Serializable {

    /**
     * Every visible data reference in catalog.
     */
    Query ALL = new Query() {

        /**
         * Serial number for serialization.
         */
        private static final long serialVersionUID = -4682427911632705028L;

        public String getQuery() {
            return "";
        }
    };

    /**
     * Query result must contain all columns of table DATAREFERENCES.
     * 
     * @return query string to execute.
     */
    String getQuery();
}
