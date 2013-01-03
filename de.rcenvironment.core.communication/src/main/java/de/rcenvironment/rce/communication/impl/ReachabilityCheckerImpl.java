/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.ReachabilityChecker;
import de.rcenvironment.rce.communication.internal.CommunicationServiceImpl;

/**
 * Implementation of {@link ReachabilityChecker}.
 * 
 * @author Doreen Seider
 */
public class ReachabilityCheckerImpl implements ReachabilityChecker {

    private static final long serialVersionUID = -1204865134650670321L;

    private static final transient Log LOGGER = LogFactory.getLog(CommunicationServiceImpl.class);

    @Override
    public Class<? extends Serializable> getInterface() {
        return ReachabilityChecker.class;
    }

    @Override
    @AllowRemoteAccess
    public void checkForReachability(PlatformIdentifier recieverPlatform) {
        LOGGER.debug("platform reachable by platform: " + recieverPlatform);
    }

}
