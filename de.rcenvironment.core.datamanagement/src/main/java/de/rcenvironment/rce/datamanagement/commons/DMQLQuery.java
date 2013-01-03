/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.commons;


/**
 * {@link Query} being configured by a plain query string.
 *
 * @author Christian Weiss
 */
public class DMQLQuery implements Query {

    private static final long serialVersionUID = 8775844215148670330L;

    private final String query;
    
    public DMQLQuery(final String query) {
        this.query = query;
    }

    @Override
    public String getQuery() {
        return query;
    }

}
