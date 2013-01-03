/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.testutils.MockCommunicationService;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;

/**
 * Test cases for {@link SimpleCommunicationService}.
 * 
 * @author Doreen Seider
 */
public class SimpleCommunicationServiceTest {

    private final SimpleCommunicationService simpleService = new SimpleCommunicationService();;

    private final Set<PlatformIdentifier> remotePlatforms = new HashSet<PlatformIdentifier>();

    private final Set<PlatformIdentifier> allRemotePlatforms = new HashSet<PlatformIdentifier>();

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");

    /** Setup. */
    @Before
    public void setUp() {
        simpleService.bindCommunicationService(new DummyCommunicationService());
        simpleService.bindPlatformService(new DummyPlatformService());
        remotePlatforms.add(PlatformIdentifierFactory.fromHostAndNumberString("134.293.5.1:1"));

        allRemotePlatforms.add(PlatformIdentifierFactory.fromHostAndNumberString("134.193.5.1:3"));
        allRemotePlatforms.add(PlatformIdentifierFactory.fromHostAndNumberString("134.23.5.1:6"));
        simpleService.getAvailableNodes();
    }

    /** Test. */
    @Test
    public void testGetPlatforms() {
        Set<PlatformIdentifier> platforms = simpleService.getAvailableNodes();
        assertEquals(remotePlatforms, platforms);
        // second call should return the cached platforms again even if the communication service
        // will return other ones
        platforms = simpleService.getAvailableNodes();
        assertEquals(remotePlatforms, platforms);

        // platforms should be cached for all instances
        platforms = new SimpleCommunicationService().getAvailableNodes();
        assertEquals(remotePlatforms, platforms);
    }

    /** Test. */
    public void testGetAllPlatformsIfServiceIsGone() {
        simpleService.unbindCommunicationService(new DummyCommunicationService());
        simpleService.getAvailableNodes();
    }

    /** Test. */
    @Test
    public void testIsLocalPlatform() {
        assertTrue(simpleService.isLocalPlatform(pi));
        assertFalse(simpleService.isLocalPlatform(PlatformIdentifierFactory.fromHostAndNumberString("horst:2")));
    }

    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void testIsLocalPlatformIfServiceIsGone() {
        simpleService.unbindPlatformService(new DummyPlatformService());
        simpleService.isLocalPlatform(pi);
    }

    /**
     * Test {@link CommunicationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyCommunicationService extends MockCommunicationService {

        private boolean getAllPlatformsCalled = false;

        @Override
        public Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
            if (!getAllPlatformsCalled) {
                getAllPlatformsCalled = true;
                return remotePlatforms;
            } else {
                if (forceRefresh) {
                    return allRemotePlatforms;
                } else {
                    return remotePlatforms;
                }
            }
        }

    }

    /**
     * Test {@link PlatformService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
            if (platformIdentifier == pi) {
                return true;
            }
            return false;
        }
    }
}
