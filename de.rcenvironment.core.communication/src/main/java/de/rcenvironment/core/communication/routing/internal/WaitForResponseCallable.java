/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.routing.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NetworkResponseHandler;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;

/**
 * A helper class for waiting asynchronously for a single {@link NetworkResponse}. Used for
 * connecting callback- and Future-based call variants.
 * 
 * @author Robert Mischke
 */
public class WaitForResponseCallable implements Callable<NetworkResponse>, NetworkResponseHandler {

    private static final boolean VERBOSE_LOGGING = false;

    private Exchanger<NetworkResponse> exchanger = new Exchanger<NetworkResponse>();

    private Log log = LogFactory.getLog(getClass());

    private String logMarker = null;

    @Override
    public void onResponseAvailable(NetworkResponse response) {
        try {
            exchanger.exchange(response);
        } catch (InterruptedException e) {
            log.warn("Interrupted while passing received response", e);
        }
    }

    @Override
    @TaskDescription("Waiting for communication response")
    public NetworkResponse call() throws Exception {
        if (VERBOSE_LOGGING) {
            log.debug("Waiting for response callback (" + logMarker + ")");
        }
        NetworkResponse response = exchanger.exchange(null);
        if (VERBOSE_LOGGING) {
            log.debug("Received response callback (" + logMarker + ")");
        }
        return response;
    }

    public void setLogMarker(String logMarker) {
        this.logMarker = logMarker;
    }
}
