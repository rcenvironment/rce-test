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
import de.rcenvironment.core.communication.model.NodeIdentifier;

/**
 * Represents a remote-initiated connection, ie a logical connection that was triggered by a
 * network-level connection by a remote node to the local node. It allows the local node to actively
 * send messages to that node without requiring the remote node to provide a
 * {@link ServerContactPoint}, or from the network perspective, listen on any port.
 * 
 * @author Robert Mischke
 * 
 */
public class RemoteInitiatedJmsConnection extends AbstractJmsConnection {

    /**
     * @param localNodeId
     * @param connection an already-started JMS connection; expected to be managed externally, ie
     *        this class will never attempt to close it
     * @param associatedSCP
     * @throws JMSException
     */
    public RemoteInitiatedJmsConnection(NodeIdentifier localNodeId, Connection connection, ServerContactPoint associatedSCP)
        throws JMSException {
        super(localNodeId);
        this.connection = connection;
        this.associatedSCP = associatedSCP;
    }

    @Override
    public void close() {
        setClosed(true);
        log.debug("Closing remote-initiated connection " + getConnectionId());
        if (getShutdownSecurityToken() != null) {
            try {
                sendShutdownMessage();
            } catch (JMSException e) {
                log.warn("Error sending shutdown message for queue " + getOutgoingRequestQueueName(), e);
            }
        } else {
            log.warn("No shutdown security token set for remote-initiated connection");
        }

    }

}
