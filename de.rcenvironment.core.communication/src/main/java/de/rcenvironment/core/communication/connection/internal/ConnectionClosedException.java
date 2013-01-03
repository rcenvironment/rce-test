/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.connection.internal;

import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * An exception type for situations when a message should be sent, but the target
 * {@link NetworkConnection} is already closed.
 * 
 * @author Robert Mischke
 */
public class ConnectionClosedException extends CommunicationException {

    private static final long serialVersionUID = 712167269948498160L;

    public ConnectionClosedException(String string) {
        super(string);
    }

}
