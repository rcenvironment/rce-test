/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * The {@link HostAndNumberPlatformIdentifier} is an identifier for a RCE platform.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
@Deprecated
public final class HostAndNumberPlatformIdentifier implements PlatformIdentifier {

    private static final long serialVersionUID = 5097675792109536579L;

    private static final String COLON = ":";

    private final String host;

    private final Integer platformNumber;

    private final String hostAndNumber;

    private final String name;

    /**
     * Constructor creates a {@link HostAndNumberPlatformIdentifier} object from the passed
     * {@link String} s.
     * 
     * @param host Host where the RCE Platform is running, either as name or IP address.
     * @param platformNumber Number of the platform. (used to identify different ones on one host).
     */
    public HostAndNumberPlatformIdentifier(String host, Integer platformNumber) {
        this(host, platformNumber, null);
    }

    /**
     * Constructor creates a {@link HostAndNumberPlatformIdentifier} object from a single
     * {@link String}.
     * 
     * @param identifier The identifier {@link String} to (re-)create the
     *        {@link HostAndNumberPlatformIdentifier} from
     */
    public HostAndNumberPlatformIdentifier(String identifier) {
        if (identifier.contains("(") && identifier.contains(")")) {
            this.name = identifier.substring(0, identifier.lastIndexOf("(") - 1);
            identifier = identifier.substring(identifier.lastIndexOf("(") + 1, identifier.lastIndexOf(")"));
        } else {
            this.name = identifier;
        }

        this.hostAndNumber = identifier;
        StringTokenizer tokenizer = new StringTokenizer(identifier, COLON);
        this.host = tokenizer.nextToken();
        this.platformNumber = Integer.parseInt(tokenizer.nextToken());

        if (this.host == null || this.platformNumber == null) {
            throw new IllegalArgumentException("given platform identifier does not contain [host]:[number]");
        }

    }

    /**
     * Constructor creates a {@link HostAndNumberPlatformIdentifier} object from the passed
     * {@link String} s.
     * 
     * @param host Host where the RCE Platform is running, either as name or IP address.
     * @param platformNo Number of the platform. (used to identify different ones on one host).
     * @param name User-friendly name of the platform.
     */
    public HostAndNumberPlatformIdentifier(String host, Integer platformNo, String name) {
        this.host = host;
        this.platformNumber = platformNo;
        this.hostAndNumber = host + COLON + platformNo;

        if (name != null && !name.isEmpty()) {
            this.name = name;
        } else {
            this.name = hostAndNumber;
        }
    }

    @Override
    public String resolveHost() {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return host;
        }
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPlatformNumber() {
        return platformNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNodeId() {
        // FIXME this does not actually fulfill the contract of this method; for migration only
        return String.format("%s:%d:%s", host, platformNumber, name);
    }

    @Override
    public String getAssociatedDisplayName() {
        return toString(); // for migration only
    }

    @Override
    public HostAndNumberPlatformIdentifier clone() {
        return new HostAndNumberPlatformIdentifier(host, platformNumber, name);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof HostAndNumberPlatformIdentifier) {
            HostAndNumberPlatformIdentifier pi = (HostAndNumberPlatformIdentifier) object;
            return pi.resolveHost().equals(resolveHost()) && pi.getPlatformNumber() == platformNumber;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hostAndNumber.hashCode();
    }

    @Override
    public String toString() {
        if (!name.equals(hostAndNumber)) {
            return name + " (" + hostAndNumber + ")";
        } else {
            return hostAndNumber;
        }
    }

}
