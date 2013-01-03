/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import de.rcenvironment.core.communication.model.NodeIdentifier;

/**
 * An identifier for a single RCE platform. PlatformIdentifiers have a n-to-1 mapping with
 * platforms, i.e. different identifiers may identify the same platform.
 * 
 * For now, this interface contains the same methods as the the original PlatformIdentifier
 * implementation. This prepares for a migration to new, more flexible identifiers. Over time, the
 * API will be replaced with more general methods.
 * 
 * @author Robert Mischke (extracted interface; refactoring)
 */
public interface PlatformIdentifier extends NodeIdentifier {

    /**
     * Resolves the provided host string and returns the InetAddress of it. If an
     * {@link UnknownHostException} occurs the hostname is returned.
     * 
     * TODO change behavior for a more reliable return value? -- misc_ro
     * 
     * @return a {@link String} representing the host.
     */
    @Deprecated
    String resolveHost();

    /**
     * Returns the original string representation of the host, as provided at creation time.
     * 
     * @return the host parameter provided at creation time
     */
    @Deprecated
    String getHost();

    /**
     * Returns the platform number of the host, as provided at creation time.
     * 
     * @return the platform number provided at creation time
     */
    @Deprecated
    int getPlatformNumber();

    /**
     * Returns the end-user name of the represented platform, as provided at creation time. This may
     * or may not be the actual name of the identified platform.
     * 
     * TODO only used by unit test; remove?
     * 
     * @return the end-user name provided at creation time, or null if none was given
     */
    @Deprecated
    String getName();
}
