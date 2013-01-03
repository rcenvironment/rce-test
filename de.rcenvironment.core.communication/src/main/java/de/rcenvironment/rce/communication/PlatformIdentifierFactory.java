/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import de.rcenvironment.rce.communication.impl.HostAndNumberPlatformIdentifier;
import de.rcenvironment.rce.communication.impl.NodeIdPlatformIdentifier;

/**
 * Factory class to encapsulate the creation of {@link PlatformIdentifier}s.
 * 
 * @author Robert Mischke
 */
public final class PlatformIdentifierFactory {

    private PlatformIdentifierFactory() {
        // prevent instantiation
    }

    /**
     * Creates a {@link PlatformIdentifier} from a host-and-number string, with an optional end-user
     * name. Input examples: "127.0.0.1:2", "127.0.0.1:5 (Test Platform)".
     * 
     * @param hostAndNumberString the platform definition string (see class JavaDoc)
     * @return a new {@link PlatformIdentifier}
     */
    @Deprecated
    public static PlatformIdentifier fromHostAndNumberString(String hostAndNumberString) {
        return new HostAndNumberPlatformIdentifier(hostAndNumberString);
    }

    /**
     * Creates a {@link PlatformIdentifier} from separate host and number parameters, with an empty
     * end-user name.
     * 
     * @param host the String representation (IPv4 number or name) of the host
     * @param platformNumber the platform number
     * @return a new {@link PlatformIdentifier}
     */
    @Deprecated
    public static PlatformIdentifier fromHostAndNumber(String host, Integer platformNumber) {
        return new HostAndNumberPlatformIdentifier(host, platformNumber);
    }

    /**
     * Creates a {@link PlatformIdentifier} from separate host and number parameters, and a given
     * end-user name.
     * 
     * @param host the String representation (IPv4 number or name) of the host
     * @param platformNumber the platform number
     * @param name the end-user name for this platform
     * @return a new {@link PlatformIdentifier}
     */
    @Deprecated
    public static PlatformIdentifier fromHostNumberAndName(String host, Integer platformNumber, String name) {
        return new HostAndNumberPlatformIdentifier(host, platformNumber, name);
    }

    /**
     * Creates a {@link PlatformIdentifier} from a persistent platform id.
     * 
     * @param id the persistent id to use
     * @return a new {@link PlatformIdentifier}
     */
    public static PlatformIdentifier fromNodeId(String id) {
        return new NodeIdPlatformIdentifier(id);
    }

}
