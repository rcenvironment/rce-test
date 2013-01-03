/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.rmi.internal;

/**
 * Class providing the configuration of the RMI communication {@link Bundle}.
 * Additionally this class sets the default configuration of the RMI communication {@link Bundle}.
 * 
 * @author Frank Kautz
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class RMIConfiguration {

    private static final int INT16 = 16;
    
    private static final int MIN_PORT = 1;
    
    private static final int MAX_PORT = (int) Math.pow(2, INT16) - 1;

    private final int defaultRegistryPort = 1099;
    private int registryPort = defaultRegistryPort;
    
    /**
     * Setter.
     * 
     * @param newPort the new port.
     * @set the RMI registry port if the given port is greater then 1. Otherwise the default is taken.
     * @throws IllegalArgumentException
     */
    public void setRegistryPort(int newPort) {
        if (newPort < MIN_PORT || newPort > MAX_PORT) {
            throw new IllegalArgumentException(String.format("Provided port number %d is invalid.", newPort));
        } else {
            registryPort = newPort; 
        }
    }
 
    public int getRegistryPort() {
        return registryPort;
    }
}
