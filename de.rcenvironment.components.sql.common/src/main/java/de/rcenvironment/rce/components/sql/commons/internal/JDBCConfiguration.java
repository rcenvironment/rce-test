/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.sql.commons.internal;

import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.components.sql.commons.JDBCProfile;


/**
 * JDBC configuration.
 * 
 * @author Christian Weiss
 */
public class JDBCConfiguration {

    private List<JDBCProfile> profiles = new LinkedList<JDBCProfile>();

    /**
     * Returns the list of {@link JDBCProfile} contained in this {@link JDBCConfiguration}.
     * 
     * @return the list of {@link JDBCProfile}
     */
    public List<JDBCProfile> getProfiles() {
        return profiles;
    }

    /**
     * Sets the list of {@link JDBCProfile} to be contained in this {@link JDBCConfiguration}.
     * 
     * @param profiles the list of {@link JDBCProfile}
     */
    public void setProfiles(List<JDBCProfile> profiles) {
        this.profiles.clear();
        this.profiles.addAll(profiles);
    }

}
