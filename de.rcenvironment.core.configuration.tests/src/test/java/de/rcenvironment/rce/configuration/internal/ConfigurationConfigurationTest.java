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
import java.util.Random;

import junit.framework.TestCase;

import de.rcenvironment.commons.TempFileUtils;

/**
 * Test case for the class <code>ConfigurationConfiguration</code>.
 *
 * @author Tobias Menden
 */
public class ConfigurationConfigurationTest extends TestCase {

    private final String hostname = "hostname";
    
    private final int instanceIdentifier = 2;
    
    private final String instanceHome = System.getProperty("user.home") + File.separator 
        + ".rce" + File.separator + instanceIdentifier;

    private ConfigurationConfiguration myConfigurationSettings = null;

    @Override
    public void setUp() throws Exception {
        myConfigurationSettings = new ConfigurationConfiguration();
        myConfigurationSettings.setHost(hostname);
        myConfigurationSettings.setPlatformNumber(instanceIdentifier);
        myConfigurationSettings.setPlatformHome(instanceHome);
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.configuration.internal.ConfigurationConfiguration#getHost()}.
     */
    public void testGetHostnameForSuccess() {
        assertEquals(hostname, myConfigurationSettings.getHost());
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.configuration.internal.ConfigurationConfiguration#getPlatformNumber()}.
     */
    public void testGetInstanceIdentifierForSuccess() {
        assertEquals(instanceIdentifier, myConfigurationSettings.getPlatformNumber());
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.configuration.internal.ConfigurationConfiguration#getPlatformHome()}.
     */
    public void testGetInstanceHomeForSuccess() {
        assertEquals(instanceHome, myConfigurationSettings.getPlatformHome());
    }
    
    /**
     * Test method for
     * {@link de.rcenvironment.rce.configuration.internal.ConfigurationConfiguration#getPlatformNumber()}.
     */
    public void testGetInstanceTempDirForSuccess() {
        final String platformTempDirPath = myConfigurationSettings.getPlatformTempDir();
        assertNotNull(platformTempDirPath);
        final File platformTempDir = new File(platformTempDirPath);
        assertTrue(platformTempDir.isDirectory());
        assertTrue(platformTempDir.canWrite());
    }
    
    /**
     * Test. 
     * @throws IOException IOException
     */
    public void testSetPlatformTempDirForSuccess() throws IOException {
        final File tempFile = TempFileUtils.getDefaultInstance().createManagedTempDir();
        String tempFilePath = tempFile.getAbsolutePath();
        myConfigurationSettings.setPlatformTempDir(tempFilePath);
        assertEquals(tempFilePath, myConfigurationSettings.getPlatformTempDir());
        tempFilePath += "_";
        // FIXME: directory does not exists thus is not valid ... should result in an exception!
        myConfigurationSettings.setPlatformTempDir(tempFilePath);
        assertEquals(tempFilePath, myConfigurationSettings.getPlatformTempDir());
    }
    
    /** Test. */
    public void testGetPlatformName() {
        final String platformName = "name" + (new Random()).nextInt();
        myConfigurationSettings.setPlatformName(platformName);
        assertEquals(platformName, myConfigurationSettings.getPlatformName());
    }
    
}
