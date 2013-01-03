/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.jms.common;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;
import de.rcenvironment.core.communication.model.impl.NetworkRequestImpl;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.SerializationException;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Utility class providing the mapping between RCE entities and JMS messages, plus related
 * message-related settings.
 * 
 * @author Robert Mischke
 */
public final class JmsProtocolUtils {

    private JmsProtocolUtils() {
        // prevent instantiation
    }

    /**
     * Creates a new JMS {@link Message} with the provided {@link NetworkNodeInformation} as the
     * handshake content, and a message type field of
     * {@link JmsProtocolConstants#MESSAGE_TYPE_INITIAL}.
     * 
     * @param nodeInformation the node information to use as body
     * @param session the JMS session to use
     * @return the created JMS {@link Message}
     * @throws JMSException on JMS errors
     */
    public static Message createCommonHandshakeMessage(NetworkNodeInformation nodeInformation, Session session) throws JMSException {
        ObjectMessage initialMessage = session.createObjectMessage();
        byte[] handshakeBytes = MessageUtils.serializeSafeObject(nodeInformation);
        initialMessage.setObject(handshakeBytes);
        initialMessage.setStringProperty(JmsProtocolConstants.MESSAGE_FIELD_MESSAGE_TYPE, JmsProtocolConstants.MESSAGE_TYPE_INITIAL);
        return initialMessage;
    }

    /**
     * Extracts the {@link NetworkNodeInformation} from a received initial handshake JMS message.
     * 
     * @param message the received message
     * @return the extracted {@link NetworkNodeInformation}
     * @throws JMSException on JMS errors
     * @throws CommunicationException on content errors
     */
    public static NetworkNodeInformation parseCommonHandshakeBody(Message message) throws JMSException, CommunicationException {
        byte[] handshakeRequestBytes = (byte[]) ((ObjectMessage) message).getObject();
        if (handshakeRequestBytes == null || handshakeRequestBytes.length == 0) {
            throw new CommunicationException("Received handshake request without payload");
        }

        NetworkNodeInformation initiatingNodeInformation;
        try {
            initiatingNodeInformation = MessageUtils.deserializeObject(handshakeRequestBytes, NetworkNodeInformationImpl.class);
        } catch (SerializationException e) {
            throw new CommunicationException("Failed to deserialize handshake message", e);
        }
        return initiatingNodeInformation;
    }

    /**
     * Creates a JMS message from a given {@link NetworkRequest}.
     * 
     * @param request the request to transform
     * @param session the JMS session to use
     * @return the equivalent JMS message
     * @throws JMSException on JMS errors
     */
    public static Message createMessageFromNetworkRequest(final NetworkRequest request, Session session) throws JMSException {
        Map<String, String> metadata = request.accessRawMetaData();
        ObjectMessage jmsRequest = session.createObjectMessage();
        jmsRequest.setObject(request.getContentBytes());
        jmsRequest.setObjectProperty(JmsProtocolConstants.MESSAGE_FIELD_METADATA, metadata);
        jmsRequest.setStringProperty(JmsProtocolConstants.MESSAGE_FIELD_MESSAGE_TYPE, JmsProtocolConstants.MESSAGE_TYPE_REQUEST);
        return jmsRequest;
    }

    /**
     * Restores a {@link NetworkRequest} from its JMS message form.
     * 
     * @param jmsRequest the JMS message
     * @return the reconstructed {@link NetworkRequest}
     * @throws JMSException on JMS errors
     * @throws CommunicationException on message format errors
     */
    public static NetworkRequestImpl createNetworkRequestFromMessage(Message jmsRequest) throws JMSException, CommunicationException {
        byte[] content = (byte[]) ((ObjectMessage) jmsRequest).getObject();
        if (content.length == 0) {
            throw new CommunicationException("Received message with zero-length payload");
        }
        @SuppressWarnings("unchecked") Map<String, String> requestMetadata = (Map<String, String>) jmsRequest
            .getObjectProperty(JmsProtocolConstants.MESSAGE_FIELD_METADATA);
        NetworkRequestImpl originalRequest =
            new NetworkRequestImpl(content, requestMetadata, null);
        return originalRequest;
    }

    /**
     * Creates a JMS message from a given {@link NetworkResponse}.
     * 
     * @param response the response to transform
     * @param session the JMS session to use
     * @return the equivalent JMS message
     * @throws JMSException on JMS errors
     */
    public static Message createMessageFromNetworkResponse(NetworkResponse response, Session session) throws JMSException {
        ObjectMessage jmsResponse = session.createObjectMessage();
        jmsResponse.setObject(response.getContentBytes());
        return jmsResponse;
    }

    /**
     * Restores a {@link NetworkResponse} from its JMS message form.
     * 
     * @param jmsResponse the JMS message
     * @param request the {@link NetworkRequest} this response is associated with
     * @return the reconstructed {@link NetworkResponse}
     * @throws JMSException on JMS errors
     */
    public static NetworkResponse createNetworkResponseFromMessage(Message jmsResponse, final NetworkRequest request) throws JMSException {
        byte[] content = (byte[]) ((ObjectMessage) jmsResponse).getObject();
        NetworkResponse response = NetworkResponseFactory.generateSuccessResponse(request, content);
        return response;
    }

    /**
     * Creates a JMS message to send to a JMS queue to terminate one
     * {@link AbstractJmsQueueConsumer} listening on this queue ("poison pill" pattern).
     * 
     * @param session the JMS session to use
     * @param securityToken the shared-secret security token to prevent unauthorized queue shutdown
     * @return the shutdown message
     * @throws JMSException on JMS errors
     */
    public static Message createShutdownMessage(Session session, String securityToken) throws JMSException {
        TextMessage poisonPill = session.createTextMessage();
        poisonPill.setStringProperty(JmsProtocolConstants.MESSAGE_FIELD_MESSAGE_TYPE, JmsProtocolConstants.MESSAGE_TYPE_QUEUE_SHUTDOWN);
        poisonPill.setText(securityToken);
        return poisonPill;
    }

    /**
     * Applies common settings (like message timeouts etc.) to a JMS {@link MessageProducer}. Should
     * be invoked on any {@link MessageProducer} before it is used for sending messages.
     * 
     * @param producer the producer to configure
     * @throws JMSException on JMS errors
     */
    public static void configureMessageProducer(MessageProducer producer) throws JMSException {
        // set the maximum time that messages from this producer are preserved
        // producer.setTimeToLive(ProtocolConstants.JMS_MESSAGES_TTL_MSEC);
    }
}
