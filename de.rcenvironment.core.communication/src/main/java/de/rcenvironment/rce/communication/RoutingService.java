/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.internal.CommunicationType;

/**
 * An abstract service that determines which immediate network peer should be contacted to reach a
 * given logical destination. Currently, the only kind of network peers and logical destinations are
 * platform instances.
 * 
 * @author Robert Mischke
 */
@Deprecated
public interface RoutingService {

    /**
     * Returns the contact description of the immediate peer that is considered the best route
     * towards the given destination.
     * 
     * @param platformIdentifier the target destination/platform
     * @param communicationType the desired type of communication (service call, file transfer, ...)
     * @return the contact information for the chosen network peer
     * @throws CommunicationException if no contact could be determined; TODO return null instead,
     *         and keep this for unexpected errors? - misc_ro
     */
    @AllowRemoteAccess
    NetworkContact getNextRoutingStep(PlatformIdentifier platformIdentifier, CommunicationType communicationType)
        throws CommunicationException;

}
