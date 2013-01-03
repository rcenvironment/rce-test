/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Test case for {@link ReachabilityCheckerImpl}.
 * 
 * @author Doreen Seider
 */
public class ReachabilityCheckerImplTest {

    /** Test. */
    @Test
    public void test() {
        PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("Wat Plattform (horst:1)");
        new ReachabilityCheckerImpl().checkForReachability(pi);
    }
}
