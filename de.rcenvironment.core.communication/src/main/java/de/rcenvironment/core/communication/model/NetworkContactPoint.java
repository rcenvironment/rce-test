/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model;

/**
 * Representation of a network "contact point", which consists of a host string, a port number, and
 * the id of the network transport to use.
 * 
 * @author Robert Mischke
 */
public interface NetworkContactPoint {

    /**
     * @return the host string
     */
    String getHost();

    /**
     * @return the port number
     */
    int getPort();

    /**
     * @return the opaque id of the transport to use for the connection
     */
    String getTransportId();

}
