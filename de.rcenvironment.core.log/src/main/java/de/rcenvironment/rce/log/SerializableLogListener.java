/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log;

import de.rcenvironment.rce.communication.callback.Callback;
import de.rcenvironment.rce.communication.callback.CallbackObject;

/**
 * Serializable version of {@link LogListener}.
 *
 * @author Doreen Seider
 */
public interface SerializableLogListener extends CallbackObject {

    /**
     * Listener method called for each LogEntry object created.

     * As with all event listeners, this method should return to its caller as
     * soon as possible.
     * 
     * This interface extends {@link CallbackObject} to support remote subscription by simply passing an
     * object of the implementing class.
     * 
     * @param logEntry A {@link SerializableLogEntry} object containing log information.
     * @see {@link LogListener}
     */
    @Callback
    void logged(SerializableLogEntry logEntry);
}
