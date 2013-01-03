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
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * A single-threaded consumer that listens for initial protocol handshake requests. These requests
 * are the first messages that should be sent by a remote node after it has established a (network
 * level) connection to the local broker.
 * 
 * @author Robert Mischke
 */
public final class InitialInboxConsumer extends AbstractJmsQueueConsumer implements Runnable {

    private final NetworkConnectionEndpointHandler endpointHandler;

    private ServerContactPoint associatedSCP;

    private RemoteInitiatedConnectionFactory passiveConnectionFactory;

    private final SharedThreadPool threadPool = SharedThreadPool.getInstance();

    public InitialInboxConsumer(Connection localJmsConnection, NetworkConnectionEndpointHandler endpointHandler,
        ServerContactPoint associatedSCP, RemoteInitiatedConnectionFactory passiveConnectionFactory) throws JMSException {
        super(localJmsConnection, JmsProtocolConstants.QUEUE_NAME_INITIAL_BROKER_INBOX);
        this.endpointHandler = endpointHandler;
        this.associatedSCP = associatedSCP;
        this.passiveConnectionFactory = passiveConnectionFactory;
    }

    @Override
    @TaskDescription("Incoming JMS connection listener")
    public void run() {
        super.run();
    }

    @Override
    protected void dispatchMessage(final Message message, final Connection connection) {
        threadPool.execute(new Runnable() {

            @Override
            @TaskDescription("Dispatch initial handshake request")
            public void run() {
                try {
                    Session responseSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    try {
                        dispatchMessageInternal(message, responseSession, connection);
                    } finally {
                        if (responseSession != null) {
                            responseSession.close();
                        }
                    }
                } catch (JMSException e) {
                    log.error("JMS exception in response session for request from queue " + queueName, e);
                }
            }
        });
    }

    private void dispatchMessageInternal(Message message, Session session, Connection connection) {
        String messageType;
        try {
            messageType = message.getStringProperty(JmsProtocolConstants.MESSAGE_FIELD_MESSAGE_TYPE);
        } catch (JMSException e) {
            log.warn("Received message with undefined message type");
            return;
        }
        try {
            if (JmsProtocolConstants.MESSAGE_TYPE_INITIAL.equals(messageType)) {
                NetworkNodeInformation initiatingNodeInformation = JmsProtocolUtils.parseCommonHandshakeBody(message);
                NetworkNodeInformation receivingNodeInformation = endpointHandler.exchangeNodeInformation(initiatingNodeInformation);

                log.debug("Received initial handshake request from " + initiatingNodeInformation);
                // initiate the passive connection
                // TODO (review: document as general approach?) do so before sending the response
                // TODO clean up / extract to different place
                JmsNetworkConnection passiveConnection =
                    passiveConnectionFactory.createRemoteInitiatedConnection(receivingNodeInformation, initiatingNodeInformation,
                        associatedSCP, connection);
                endpointHandler.onRemoteInitiatedConnectionEstablished(passiveConnection, associatedSCP);

                log.debug("Passive connection established, sending handshake response to " + initiatingNodeInformation);
                Message jmsResponse = JmsProtocolUtils.createCommonHandshakeMessage(receivingNodeInformation, session);
                // register name of passive (B2C) connection JMS queue
                jmsResponse.setStringProperty(JmsProtocolConstants.MESSAGE_FIELD_PASSIVE_B2C_REQUEST_INBOX,
                    passiveConnection.getOutgoingRequestQueueName());
                // register name of active (C2B) connection JMS queue
                jmsResponse.setStringProperty(JmsProtocolConstants.MESSAGE_FIELD_ACTIVE_C2B_REQUEST_INBOX,
                    JmsProtocolConstants.QUEUE_NAME_C2B_REQUEST_INBOX);
                // send response
                MessageProducer responseProducer = session.createProducer(message.getJMSReplyTo());
                JmsProtocolUtils.configureMessageProducer(responseProducer);
                responseProducer.send(jmsResponse);
                log.debug("Sent response to initial handshake; creating connection to sender");

            } else {
                log.warn("Received message of unhandled type " + messageType + " from queue " + queueName);
            }
        } catch (JMSException e) {
            log.warn("Error while dispatching message of type " + messageType, e);
        } catch (CommunicationException e) {
            log.warn("Error while dispatching message of type " + messageType, e);
        }
    }

}
