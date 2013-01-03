/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import de.rcenvironment.rce.communication.callback.Callback;
import de.rcenvironment.rce.communication.callback.CallbackObject;

/**
 * Test interface used for test callback object.
 * 
 * @author Doreen Seider
 */
public interface DummyInterface extends CallbackObject {

    /**
     * Dummy method.
     * 
     * @return dummy return value.
     **/
    String method();

    /** Dummy method. */
    @Callback
    void callbackMethod();
}
