/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

/**
 * Provides access to the configuration data used by the SOAP bundle.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
public class SOAPConfiguration {
    
    /** The default port value. */
    public static final int DEFAULT_PORT = 8080;

    private static final int MIN_PORT = 1;
    
    private static final int MAX_PORT = Short.MAX_VALUE * 2 + 1;
    
    private int port = DEFAULT_PORT;
    
    /**
     * Sets a new port if the port number is greater than zero.
     * 
     * @param value New Port.
     * @throws IllegalArgumentException in case the value for the port is invalid
     */
    public void setPort(int value) throws IllegalArgumentException {
        if (value < MIN_PORT || value > MAX_PORT) {
            throw new IllegalArgumentException();
        }
        port = value;
    }
    
    public int getPort() {
        return port;
    }

}
