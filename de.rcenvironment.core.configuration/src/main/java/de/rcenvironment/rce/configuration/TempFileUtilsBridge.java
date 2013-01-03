/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;

/**
 * A bridge class to configure {@link TempFileUtils} from RCE while providing access to the global
 * {@link ConfigurationService} injected via OSGi-DS.
 * 
 * @author Robert Mischke
 */
public class TempFileUtilsBridge {

    private boolean isBound = false;

    private Log log = LogFactory.getLog(getClass());

    /**
     * Constructor for OSGi-DS; do not use.
     */
    @Deprecated
    public TempFileUtilsBridge() {}

    protected void bindConfigurationService(ConfigurationService newConfigurationService) throws IOException {
        if (isBound) {
            log.warn("Duplicate bind()");
        }
        // do not create temp directories in the .rce folder, but in the system temp dir instead
        // TODO make user-configurable?
        File newRootDir = new File(System.getProperty("java.io.tmpdir"), "rce-tmp");
        TempFileUtils.getDefaultInstance().setGlobalRootDir(newRootDir);
        isBound = true;
    }

    protected void unbindConfigurationService(ConfigurationService oldConfigurationService) {
        if (!isBound) {
            log.warn("Unexpected unbind()");
        }
        isBound = false;
    }

}
