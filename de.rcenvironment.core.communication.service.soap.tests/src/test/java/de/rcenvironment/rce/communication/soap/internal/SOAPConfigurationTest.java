/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Unit test for the {@link SOAPConfiguration}.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 * @author Christian Weiss
 */ 
public class SOAPConfigurationTest extends TestCase {
    
    private SOAPConfiguration mySOAPConfiguration = null;

    @Override
    public void setUp() throws Exception {
        mySOAPConfiguration = new SOAPConfiguration();
    }
    
    /**
     * Test default values for 'de.rcenvironment.rce.communication.soap.internal.SOAPConfiguration.getMethods'
     * for sanity.
     */ 
    public void testDefault() {
        assertEquals(SOAPConfiguration.DEFAULT_PORT, mySOAPConfiguration.getPort());
    }
    
    /**
     * Test method for 'de.rcenvironment.rce.communication.soap.internal.SOAPConfiguration.getRegistryPort()' for
     * sanity.
     */ 
    public void testGetRegistryPortForSanity() {
        final int[] testValues = { 1, Short.MAX_VALUE, Short.MAX_VALUE * 2 + 1};
        for (final int testValue : testValues) {
            mySOAPConfiguration.setPort(testValue);
            assertEquals(testValue, mySOAPConfiguration.getPort());
        }
    }

    /** Test. */
    public void testGetRegistryPortFailure() {
        final int[] testValues = { Integer.MIN_VALUE, Short.MIN_VALUE, -1, 0, Short.MAX_VALUE * 2 + 2, Integer.MAX_VALUE };
        for (final int testValue : testValues) {
            try {
                mySOAPConfiguration.setPort(testValue);
                Assert.fail();
            } catch (IllegalArgumentException ok) {
                ok = null;
            }
            assertEquals(SOAPConfiguration.DEFAULT_PORT, mySOAPConfiguration.getPort());
        }
    }

}

