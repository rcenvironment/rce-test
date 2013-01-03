/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.sql.commons;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * {@link JDBCService} implementation to be used in GUI classes.
 * 
 * @author Christian Weiss
 */
public class SimpleJDBCService implements JDBCService {

    private static SimpleJDBCService instance;
    
    private static JDBCService service;
    
    /**
     * Returns an instance of a {@link JDBCService}.
     * 
     * @return an instance  of a {@link JDBCService}
     */
    public static SimpleJDBCService getInstance() {
        if (SimpleJDBCService.instance == null) {
            SimpleJDBCService.instance = new SimpleJDBCService();
        }
        return SimpleJDBCService.instance;
    }
    
    protected void bindJDBCService(final JDBCService newService) {
        SimpleJDBCService.service = newService;
    }
    
    protected void unbindJDBCService(final JDBCService oldService) {
        SimpleJDBCService.service = null;
    }

    @Override
    public List<JDBCProfile> getProfiles() {
        return SimpleJDBCService.service.getProfiles();
    }

    @Override
    public JDBCProfile getProfileByLabel(String label) {
        return SimpleJDBCService.service.getProfileByLabel(label);
    }

    @Override
    public Connection getConnection(JDBCProfile profile) throws SQLException {
        return SimpleJDBCService.service.getConnection(profile);
    }

}
