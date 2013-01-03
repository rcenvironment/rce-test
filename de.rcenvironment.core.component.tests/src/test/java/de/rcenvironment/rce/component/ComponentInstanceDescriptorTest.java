/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;


/**
 * Test cases for {@link ComponentInstanceDescriptor}.
 *
 * @author Doreen Seider
 */
public class ComponentInstanceDescriptorTest {

    /** Test. */
    @Test
    public void test() {
        final String identifier = "Lange Lulatsch";
        final String name = "Schwangere Auster";
        final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("Erichs Lampenladen:89");
        final String workDir = "Goldelse";
        final String compIdentifier = "berlin";
        final String compContextName = "hansestadt";
        final Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
        pis.add(pi);
        pis.add(PlatformIdentifierFactory.fromHostAndNumber("jubidu", 5));
        
        ComponentInstanceDescriptor cid = new ComponentInstanceDescriptor(identifier, name, pi, workDir,
            compIdentifier, compContextName, pis);
        
        assertEquals(identifier, cid.getIdentifier());
        assertEquals(name, cid.getName());
        assertEquals(pi, cid.getPlatform());
        assertEquals(workDir, cid.getWorkingDirectory());
        assertEquals(compIdentifier, cid.getComponentIdentifier());
        assertEquals(compContextName, cid.getComponentContextName());
        assertEquals(pis, cid.getPlatformsInvolvedInComponentContext());
    }
}
