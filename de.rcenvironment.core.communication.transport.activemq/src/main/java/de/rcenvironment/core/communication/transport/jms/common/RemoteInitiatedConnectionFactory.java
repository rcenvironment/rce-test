/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.jms.common;

import javax.jms.Connection;
import javax.jms.JMSException;

import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;

/**
 * Factory interface for remote-initiated connections.
 * 
 * @author Robert Mischke
 */
public interface RemoteInitiatedConnectionFactory {

    /**
     * Creates a remote-initiated outgoing connection.
     * 
     * @param receivingNodeInformation the node information for the receiver of the original
     *        connection (ie, the local node)
     * @param initiatingNodeInformation the node information for the initiator of the original
     *        connection (ie, the remote node)
     * @param associatedSCP the {@link ServerContactPoint} the original connection was made to
     * @param localJmsConnection an established JMS connection to the matching JMS broker
     * @return the created {@link AbstractJmsConnection}
     * @throws JMSException on JMS errors
     */
    JmsNetworkConnection createRemoteInitiatedConnection(NetworkNodeInformation receivingNodeInformation,
        NetworkNodeInformation initiatingNodeInformation, ServerContactPoint associatedSCP, Connection localJmsConnection)
        throws JMSException;

}
