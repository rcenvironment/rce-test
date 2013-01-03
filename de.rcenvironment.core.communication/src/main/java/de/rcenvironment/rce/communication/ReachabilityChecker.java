/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import de.rcenvironment.rce.communication.callback.Callback;
import de.rcenvironment.rce.communication.callback.CallbackObject;

/**
 * Used to pass objects of this class to remote platforms which needs to call method above in order
 * to check id this platform is reachable by the remote one.
 * 
 * @author Doreen Seider
 */
public interface ReachabilityChecker extends CallbackObject {

    /**
     * Method to callback from remote in order to check it a platform can be reached from remote.
     * 
     * @param remotePlatform remote platform to check.
     */
    @Callback
    void checkForReachability(PlatformIdentifier remotePlatform);

}
