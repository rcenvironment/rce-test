/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.ssh.jsch.executor.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/**
 * Test cases for {@link RemoteTempDirFactory}.
 * @author Doreen Seider
 */
public class RemoteTempDirFactoryTest {

    private final String givenRootDir = RandomStringUtils.random(5);
    
    private final String normalizedRootDir = givenRootDir + "/";
    
    /** Test. */
    @Test
    public void testGetRootDir() {
        RemoteTempDirFactory factory = new RemoteTempDirFactory(givenRootDir);        
        assertEquals(normalizedRootDir, factory.getRootDir());
    }
    
    /** Test. */
    @Test
    public void testCreateTempDirPath() {
        RemoteTempDirFactory factory = new RemoteTempDirFactory(givenRootDir);
        String contextHint = RandomStringUtils.random(7);
        String separator = RandomStringUtils.random(1);
        String path = factory.createTempDirPath(contextHint, separator);
        assertNotNull(path);
        assertTrue(path.length() > 7); // contextHint + separator
    }
}
