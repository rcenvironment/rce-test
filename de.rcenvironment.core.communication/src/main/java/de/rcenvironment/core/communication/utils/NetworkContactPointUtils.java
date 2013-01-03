/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.impl.NetworkContactPointImpl;

/**
 * Utility methods related to {@link NetworkContactPoint}s.
 * 
 * @author Robert Mischke
 */
public final class NetworkContactPointUtils {

    private static final Pattern NCP_DEFINITION_PATTERN = Pattern.compile("^([\\w\\-]+):([\\w.\\-]+):(\\d+)$");

    private NetworkContactPointUtils() {}

    /**
     * Converts the (transport-specific) String representation of a {@link NetworkContactPoint} into
     * its object representation.
     * 
     * @param contactPointDef the String representation
     * @return the object representation
     * @throws IllegalArgumentException if the String is malformed
     */
    public static NetworkContactPoint parseStringRepresentation(String contactPointDef) throws IllegalArgumentException {
        Matcher m = NCP_DEFINITION_PATTERN.matcher(contactPointDef);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        String host = m.group(2);
        int port = Integer.parseInt(m.group(3));
        String transportId = m.group(1);
        NetworkContactPointImpl ncp = new NetworkContactPointImpl(host, port, transportId);
        return ncp;
    }

}
