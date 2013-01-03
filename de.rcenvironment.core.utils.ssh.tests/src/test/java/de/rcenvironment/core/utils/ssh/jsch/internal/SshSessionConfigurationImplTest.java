/*
 * Copyright (C) 2006-2012 DLR SC, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.utils.ssh.jsch.internal;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/**
 * Test cases for {@link SshSessionConfigurationImpl}.
 *
 * @author Doreen Seider
 */
public class SshSessionConfigurationImplTest {

    private final String destinationHost = RandomStringUtils.random(5);
    
    private final int port = 9;
    
    private final String sshAuthUser = RandomStringUtils.random(5);
    
    private final String sshAuthPassPhrase = RandomStringUtils.random(5);
    
    private final String sshKeyFileLocation = RandomStringUtils.random(5);
    
    /** Test. */
    @Test
    public void test() {
        SshSessionConfigurationImpl config = new SshSessionConfigurationImpl(destinationHost, port, sshAuthUser,
            sshAuthPassPhrase, sshKeyFileLocation);
        
        assertEquals(destinationHost, config.getDestinationHost());
        assertEquals(port, config.getPort());
        assertEquals(sshAuthUser, config.getSshAuthUser());
        assertEquals(sshAuthPassPhrase, config.getSshAuthPhrase());
        assertEquals(sshKeyFileLocation, config.getSshKeyFileLocation());
    }
}
