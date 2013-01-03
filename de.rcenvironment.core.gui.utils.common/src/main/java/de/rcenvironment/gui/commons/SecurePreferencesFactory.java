/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.gui.commons;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.security.storage.ISecurePreferences;

import de.rcenvironment.rce.configuration.SimpleConfigurationService;


/**
 * RCE specific wrapper class for Eclipse's {@link SecurePreferencesFactory}.
 *
 * @author Doreen Seider
 */
public final class SecurePreferencesFactory {

    private static final Log LOGGER = LogFactory.getLog(SecurePreferencesFactory.class);
    
    private SecurePreferencesFactory() {}
    
    /**
     * Opens and returns RCE's secure storage.
     * @return secure storage as {@link ISecurePreferences} object
     * @throws IOException on error
     */
    public static ISecurePreferences getSecurePreferencesStore() throws IOException {
        try {
            return org.eclipse.equinox.security.storage.SecurePreferencesFactory.open(new File(new SimpleConfigurationService()
                .getPlatformHome() + "/secure_storage").toURI().toURL(), null);
        } catch (MalformedURLException e) {
            LOGGER.error("Opening RCE's secure storage failed", e);
            throw new IOException(e);
        }
    }
}
