/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.testutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformService;

/**
 * A factory for common {@link PlatformService} stub implementations to avoid code duplication.
 * 
 * @author Robert Mischke
 */
public abstract class PlatformServiceStubFactory {

    /**
     * An implementation that wraps a host/number PlatformIdentifier.
     */
    private static class HostAndNumberStub extends PlatformServiceDefaultStub {

        private PlatformIdentifier platformIdentifier;

        private Set<PlatformIdentifier> remotePlatforms;

        public HostAndNumberStub(String host, int platformNumber, Collection<PlatformIdentifier> remotePlatforms) {
            this.platformIdentifier = PlatformIdentifierFactory.fromHostAndNumber(host, platformNumber);
            this.remotePlatforms = new HashSet<PlatformIdentifier>(remotePlatforms);
        }

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return platformIdentifier;
        }

        @Override
        public boolean isLocalPlatform(PlatformIdentifier testedPlatformIdentifier) {
            // perform simple check here; improve if necessary
            return testedPlatformIdentifier.getHost().equals("localhost");
        }

        @Override
        public Set<PlatformIdentifier> getRemotePlatforms() {
            return remotePlatforms;
        }

    }

    private PlatformServiceStubFactory() {}

    /**
     * @return an implementation with a custom host, instance number, and no remote platforms
     * 
     * @param host the host name for the local platform
     * @param platformNumber the platform number for the local platform
     */
    public static PlatformService createHostAndNumberStub(String host, int platformNumber) {
        return new HostAndNumberStub(host, platformNumber, new HashSet<PlatformIdentifier>());
    }

    /**
     * @return an implementation with a custom host, instance number, and remote platforms
     * 
     * @param host the host name for the local platform
     * @param platformNumber the platform number for the local platform
     * @param remotePlatforms the set of known platforms to emulate
     */
    public static PlatformService createHostAndNumberStub(String host, int platformNumber,
        Collection<PlatformIdentifier> remotePlatforms) {
        return new HostAndNumberStub(host, platformNumber, remotePlatforms);
    }

}
