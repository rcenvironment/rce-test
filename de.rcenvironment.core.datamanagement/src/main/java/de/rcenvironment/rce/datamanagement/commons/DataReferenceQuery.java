/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import java.util.UUID;

/**
 * Class to create a query for a given {@link DataReference} UUID.
 * 
 * @author Dirk Rossow
 * @author Juergen Klein
 */
public class DataReferenceQuery implements Query {

    private static final long serialVersionUID = -134313714592249872L;

    private UUID dateReferenceidentifier;

    public DataReferenceQuery(UUID dataReferenceIdentifier) {
        dateReferenceidentifier = dataReferenceIdentifier;
    }

    @Override
    public String getQuery() {
        String query = CatalogConstants.GUID + "='" + dateReferenceidentifier.toString() + "'";
        return query;
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
