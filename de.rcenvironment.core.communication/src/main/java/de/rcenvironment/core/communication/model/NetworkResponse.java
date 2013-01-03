/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model;

import de.rcenvironment.core.communication.connection.NetworkRequestHandler;

/**
 * A connection-level response to a {@link NetworkRequest}.
 * 
 * @author Robert Mischke
 */
public interface NetworkResponse extends NetworkMessage {

    /**
     * Value for marking an undefined result code.
     */
    int RESULT_CODE_UNDEFINED = 0;

    /**
     * Result code: request successful.
     */
    int RESULT_CODE_SUCCESS = 1;

    /**
     * Result code: No {@link NetworkRequestHandler} on the receiving node was able to handle this
     * message.
     */
    int RESULT_CODE_NO_MATCHING_HANDLER = 101;

    /**
     * Result code: An exception occurred while handling the request at its final destination node.
     */
    int RESULT_CODE_EXCEPTION_AT_DESTINATION = 102;

    /**
     * Result code: An exception occurred while forwarding/routing the request towards its final
     * destination node.
     */
    int RESULT_CODE_EXCEPTION_WHILE_FORWARDING = 103;

    /**
     * @return the internal id associated with the original request; can be used to correlate
     *         responses to original requests
     */
    String getRequestId();

    /**
     * @return true if the request was successfully processed
     */
    boolean isSuccess();

    /**
     * @return the numerical result code; see constants for possible values
     */
    int getResultCode();

}
