/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection.impl;

import java.util.concurrent.atomic.AtomicInteger;

import de.rcenvironment.commons.IdGenerator;
import de.rcenvironment.core.communication.connection.NetworkConnectionIdFactory;

/**
 * A {@link NetworkConnectionIdFactory} implementation generating JVM-wide unique integer ids.
 * 
 * @author Robert Mischke
 */
public class DefaultNetworkConnectionIdFactoryImpl implements NetworkConnectionIdFactory {

    // assuming that 2^31 connections will suffice for now...
    private static AtomicInteger sequence = new AtomicInteger();

    @Override
    public String generateId(boolean selfInitiated) {
        // embed a flag that indicates whether this connection was self- or remote-initiated
        String directionFlag;
        // blame CheckStyle for the verbosity...
        if (selfInitiated) {
            directionFlag = "s";
        } else {
            directionFlag = "r";
        }
        // the running index is for easy identification in log output; the UUID part ensures
        // uniqueness; the leading "c" is to make it recognizable as a connection id -- misc_ro
        return String.format("c%d%s-%s", sequence.incrementAndGet(), directionFlag, IdGenerator.randomUUIDWithoutDashes());
    }
}
