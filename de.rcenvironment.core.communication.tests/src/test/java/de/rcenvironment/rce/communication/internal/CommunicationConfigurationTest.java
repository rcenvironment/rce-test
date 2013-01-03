/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.util.Vector;

import junit.framework.TestCase;

/**
 * Unit test for <code>CommunicationConfiguration</code>.
 * 
 * @author Doreen Seider
 */
public class CommunicationConfigurationTest extends TestCase {

    /**
     * The class under test.
     */
    private CommunicationConfiguration myConfiguration = null;

    @Override
    protected void setUp() throws Exception {
        myConfiguration = new CommunicationConfiguration();
        myConfiguration.setFileTransferContacts(new Vector<String>() {

            {
                add("test1");
            }
        });
        myConfiguration.setServiceCallContacts(new Vector<String>() {

            {
                add("test2");
            }
        });
        myConfiguration.setRemotePlatforms(new Vector<String>() {

            {
                add("test3");
            }
        });
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetServiceCallConfigurationForSuccess() {
        myConfiguration.getServiceCallContacts();
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetServiceCallConfigurationForSanity() {
        assertEquals(1, myConfiguration.getServiceCallContacts().size());
        assertEquals("test2", myConfiguration.getServiceCallContacts().get(0));
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetFileTransferConfigurationForSuccess() {
        myConfiguration.getFileTransferContacts();
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetFileTransferConfigurationForSanity() {
        assertEquals(1, myConfiguration.getFileTransferContacts().size());
        assertEquals("test1", myConfiguration.getFileTransferContacts().get(0));
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetRemotePlatformsForSuccess() {
        myConfiguration.getRemotePlatforms();
    }

    /**
     * 
     * Test.
     * 
     */
    public void testGetRemotePlatformsForSanity() {
        assertEquals(1, myConfiguration.getRemotePlatforms().size());
        assertEquals("test3", myConfiguration.getRemotePlatforms().get(0));
    }

}
