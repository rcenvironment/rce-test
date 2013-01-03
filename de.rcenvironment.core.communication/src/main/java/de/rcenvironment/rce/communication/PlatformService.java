/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import java.util.Set;

import de.rcenvironment.rce.communication.impl.NodeIdPlatformIdentifier;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Configuration management service for the local platform instance.
 * 
 * TODO rename to "de.rcenvironment.rce.communication.configuration.PlatformConfigurationService" or
 * similar
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public interface PlatformService {

    /**
     * Returns information about this platform's identity, including a persistent unique identifier,
     * and optionally, a public key and an end-user display name for this platform.
     * 
     * @return an immutable identity information object
     */
    PlatformIdentityInformation getIdentityInformation();

    /**
     * Returns the identifier of the local RCE platform.
     * 
     * @return the identifier of the local RCE platform.
     */
    PlatformIdentifier getPlatformIdentifier();

    /**
     * A transitional method that returns a legacy {@link HostAndNumberPlatformIdentifier} for this
     * platform instance.
     * 
     * @return a {@link HostAndNumberPlatformIdentifier} for the local platform
     */
    @Deprecated
    PlatformIdentifier getLegacyPlatformIdentifier();

    /**
     * A transitional method that returns a {@link NodeIdPlatformIdentifier} for this platform
     * instance.
     * 
     * @return a {@link NodeIdPlatformIdentifier} for the local platform
     */
    @Deprecated
    PlatformIdentifier getPersistentIdPlatformIdentifier();

    /**
     * Checks if the specified {@link PlatformIdentifier} represents the local platform instance.
     * 
     * @param platformIdentifier the {@link PlatformIdentifier} to verify
     * @return true if the given {@link PlatformIdentifier} matches the local platform instance
     */
    boolean isLocalPlatform(PlatformIdentifier platformIdentifier);

    /**
     * Returns the statically-configured remote RCE platforms.
     * 
     * @return Returns the known remote RCE platforms.
     * 
     * @deprecated to be replaced with new methods
     */
    @Deprecated
    Set<PlatformIdentifier> getRemotePlatforms();

    /**
     * Returns the local address that listen services should bind to.
     * 
     * @return a String representing the IP address to use
     */
    String getServiceBindAddress();

    /**
     * @return the communication bundle configuration object
     */
    CommunicationConfiguration getConfiguration();
}
