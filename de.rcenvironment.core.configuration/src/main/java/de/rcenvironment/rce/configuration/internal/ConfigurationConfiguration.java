/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.commons.TempFileUtils;

/**
 * Configuration for the configuration {@link Bundle}.
 * 
 * @author Roland Gude
 * @author Heinrich Wendel
 * @author Tobias Menden
 * @author Robert Mischke
 */
public class ConfigurationConfiguration {

    /** RCE host. */
    private String host = "";

    /** RCE platform number. Serves for identifiying RCE instances running on one host. */
    private int platformNumber = 1;

    /** RCE platform name. User friendly name of the platform. */
    private String platformName;

    /** Temporary working directory. */
    private File platformTempDir;

    /** Temporary working directory path. */
    private String platformTempDirPath;

    /** Home directory. */
    private String platformHome;

    private boolean isWorkflowHost = false;

    /**
     * Platform-local substitution properties; see
     * {@link ConfigurationService#addSubstitutionProperties(String, Map)}.
     */
    private Map<String, String> platformProperties = new HashMap<String, String>();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPlatformNumber() {
        return platformNumber;
    }

    public void setPlatformNumber(int platformNumber) {
        this.platformNumber = platformNumber;
    }

    public String getPlatformName() {
        return platformName;
    }

    public boolean getIsWorkflowHost() {
        return isWorkflowHost;
    }

    public void setIsWorkflowHost(boolean isWorkflowHost) {
        this.isWorkflowHost = isWorkflowHost;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    /**
     * Returns the configured platformTempDir or the default one constructed.
     * @return platformTempDir.
     */
    public String getPlatformTempDir() {
        if (platformTempDirPath == null) {
            if (platformTempDir == null) {
                try {
                    platformTempDir = TempFileUtils.getDefaultInstance().createManagedTempDir(
                        String.format("rce-instance-%d", getPlatformNumber()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            platformTempDirPath = platformTempDir.getAbsolutePath();
        }
        return platformTempDirPath;
    }

    public void setPlatformTempDir(String platformTempDir) {
        // FIXME: check whether directory exists or create directory to avoid IOExceptions
        this.platformTempDirPath = platformTempDir;
    }

    /**
     * Returns the configured platformHome or the default one constructed.
     * @return platformHome.
     */
    public String getPlatformHome() {

        if (platformHome == null) {
            platformHome = System.getProperty("user.home") + File.separator
                + ".rce" + File.separator + platformNumber;
            new File(platformHome).mkdirs();
        }
        return platformHome;
    }

    public void setPlatformHome(String platformHome) {
        this.platformHome = platformHome;
    }

    public Map<String, String> getPlatformProperties() {
        return platformProperties;
    }

    public void setPlatformProperties(Map<String, String> platformProperties) {
        this.platformProperties = platformProperties;
    }

}
