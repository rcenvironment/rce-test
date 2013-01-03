/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Test cases for {@link PlatformIdentifier}.
 * 
 * @author Heinrich Wendel
 * @author Robert Mischke (refactoring)
 */
public class HostAndNumberPlatformIdentifierTest {

    private static final String COLON = ":";

    private String host = "127.0.0.1";

    private int platformNo = 1;

    private String name = "Spitzen Platform";

    private PlatformIdentifier platformIdentifier = PlatformIdentifierFactory.fromHostAndNumber(host, platformNo);

    /** Test. */
    @Test
    public void testPlatformIdentifierString() {
        PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumber(host, platformNo);
        assertEquals(pi.getHost(), host);
        assertEquals(pi.getPlatformNumber(), platformNo);

        pi = PlatformIdentifierFactory.fromHostAndNumberString(host + COLON + platformNo);
        assertEquals(host, pi.getHost());
        assertEquals(platformNo, pi.getPlatformNumber());

        pi = PlatformIdentifierFactory.fromHostAndNumberString(name + " (" + host + COLON + platformNo + ")");
        assertEquals(host, pi.getHost());
        assertEquals(platformNo, pi.getPlatformNumber());
        assertEquals(name, pi.getName());

    }

    /** Test. */
    @Test
    public void testResolveHost() {
        assertEquals(host, platformIdentifier.resolveHost());
    }

    /** Test. */
    @Test
    public void testGetHost() {
        assertEquals(host, platformIdentifier.getHost());
    }

    /** Test. */
    @Test
    public void testGetInstanceIdentifier() {
        assertEquals(platformNo, platformIdentifier.getPlatformNumber());
    }

    /** Test. */
    @Test
    public void testEqualsObject() {
        PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumber(host, platformNo);
        assertEquals(platformIdentifier, pi);
    }

    /** Test. */
    @Test
    public void testToString() {
        assertEquals(host + COLON + platformNo, platformIdentifier.toString());
        PlatformIdentifier pi = PlatformIdentifierFactory.fromHostNumberAndName(host, platformNo, name);
        assertEquals(name + " (" + host + COLON + platformNo + ")", pi.toString());
        assertEquals(pi.toString(), PlatformIdentifierFactory.fromHostAndNumberString(pi.toString()).toString());
    }

    /** Test. */
    @Test
    public void testHashCode() {
        assertEquals(platformIdentifier.toString().hashCode(), platformIdentifier.hashCode());
    }

}
