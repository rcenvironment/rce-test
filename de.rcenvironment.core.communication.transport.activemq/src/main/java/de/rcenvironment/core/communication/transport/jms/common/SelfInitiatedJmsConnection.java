/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.jms.common;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Represents a self-initiated JMS connection, ie a connection that was established from the local
 * node to a remote node.
 * 
 * @author Robert Mischke
 */
public class SelfInitiatedJmsConnection extends AbstractJmsConnection {

    private static final int INITIAL_HANDSHAKE_TIMEOUT_MSEC = 15 * 1000;

    private ConnectionFactory connectionFactory;

    private BrokenConnectionListener brokenConnectionListener;

    public SelfInitiatedJmsConnection(NodeIdentifier localNodeId, ConnectionFactory connectionFactory,
        BrokenConnectionListener brokenConnectionListener) {
        super(localNodeId);
        this.connectionFactory = connectionFactory;
        this.brokenConnectionListener = brokenConnectionListener;
    }

    void connect() throws JMSException {
        connection = connectionFactory.createConnection();
        connection.setExceptionListener(new ExceptionListener() {

            @Override
            public void onException(JMSException exception) {
                log.warn("Asynchronous JMS exception in outgoing connection " + getConnectionId(), exception);
                // for now, always assume the connection is broken on an async exception
                brokenConnectionListener.onConnectionBroken(SelfInitiatedJmsConnection.this);
            }
        });
        connection.start();
    }

    @Override
    public void close() {
        setClosed(true);
        // TODO probably needs improvement; shut down threads etc.
        log.debug("Closing outgoing connection " + getConnectionId());
        try {
            connection.close();
        } catch (JMSException e) {
            log.warn("Exception while closing JMS connection", e);
        }
    }

    NetworkNodeInformation performInitialHandshake(NetworkNodeInformation localNodeInformation,
        NetworkConnectionEndpointHandler remoteInitiatedConnectionEndpointHandler) throws JMSException,
        CommunicationException, TimeoutException, IOException {
        Session initialSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            Queue initialInbox = initialSession.createQueue(JmsProtocolConstants.QUEUE_NAME_INITIAL_BROKER_INBOX);
            // create request
            Message initialMessage = JmsProtocolUtils.createCommonHandshakeMessage(localNodeInformation, initialSession);
            // perform handshake
            ObjectMessage initialResponse =
                (ObjectMessage) performRequestResponse(initialSession, initialMessage, initialInbox, INITIAL_HANDSHAKE_TIMEOUT_MSEC);
            // extract response
            NetworkNodeInformation remoteNodeInformation = JmsProtocolUtils.parseCommonHandshakeBody(initialResponse);
            if (remoteNodeInformation == null) {
                throw new CommunicationException("Received empty node information from initial handshake");
            }

            // TODO clean up / extract to different place
            // extract remote-initiated connection queue name
            String incomingRequestQueueName =
                initialResponse.getStringProperty(JmsProtocolConstants.MESSAGE_FIELD_PASSIVE_B2C_REQUEST_INBOX);
            log.debug("Local (B2C) queue for incoming requests (received via outgoing connection "
                + getConnectionId() + "): "
                + incomingRequestQueueName);
            // spawn incoming request listener
            SharedThreadPool.getInstance().execute(
                new RequestInboxConsumer(incomingRequestQueueName, connection, remoteInitiatedConnectionEndpointHandler));

            String outgoingQueueName = initialResponse.getStringProperty(JmsProtocolConstants.MESSAGE_FIELD_ACTIVE_C2B_REQUEST_INBOX);
            log.debug("Remote (C2B) queue for requests using outgoing connection "
                + getConnectionId() + ": " + outgoingQueueName);
            setOutgoingRequestQueueName(outgoingQueueName);

            return remoteNodeInformation;
        } finally {
            initialSession.close();
        }
    }

}
