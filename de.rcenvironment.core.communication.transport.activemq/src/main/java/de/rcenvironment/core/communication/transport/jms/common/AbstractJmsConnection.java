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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.RawNetworkResponseHandler;
import de.rcenvironment.core.communication.routing.NetworkResponseFactory;
import de.rcenvironment.core.communication.transport.spi.AbstractNetworkConnection;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;

/**
 * The abstract superclass for both self-initiated and remote-initiated JMS connections.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractJmsConnection extends AbstractNetworkConnection implements JmsNetworkConnection {

    protected final SharedThreadPool threadPool = SharedThreadPool.getInstance();

    protected Connection connection;

    protected final Log log = LogFactory.getLog(getClass());

    protected NodeIdentifier localNodeId;

    private String outgoingRequestQueueName;

    private String shutdownSecurityToken;

    private volatile boolean closed = false;

    /**
     * @param transportContext
     */
    public AbstractJmsConnection(NodeIdentifier localNodeId) {
        this.localNodeId = localNodeId;
    }

    @Override
    public void sendRequest(final NetworkRequest request, final RawNetworkResponseHandler responseHandler, final int timeoutMsec) {

        // Note: this is a very basic approach; optimize? -- misc_ro
        threadPool.execute(new Runnable() {

            @Override
            @TaskDescription("JMS request/response")
            public void run() {
                try {
                    final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    try {
                        final Queue destinationQueue = session.createQueue(outgoingRequestQueueName);
                        // construct message
                        Message jmsRequest = JmsProtocolUtils.createMessageFromNetworkRequest(request, session);
                        Message jmsResponse = performRequestResponse(session, jmsRequest, destinationQueue, timeoutMsec);
                        NetworkResponse response = JmsProtocolUtils.createNetworkResponseFromMessage(jmsResponse, request);
                        responseHandler.onResponseAvailable(response);
                    } finally {
                        session.close();
                    }
                } catch (TimeoutException e) {
                    // do not print the irrelevant stacktrace for this exception; only use message
                    log.warn("Timeout while waiting for response: " + e);
                    NetworkResponse response =
                        NetworkResponseFactory.generateExceptionWhileRoutingResponse(request,
                            localNodeId.getNodeId(), e);
                    responseHandler.onResponseAvailable(response);
                } catch (JMSException e) {
                    // TODO detect broken connections
                    responseHandler.onConnectionBroken(request, AbstractJmsConnection.this);
                    // TODO also log exception here?
                    log.warn("Error while sending request message", e);
                    // convert to IOException, as transport-specific exceptions fail to deserialize
                    IOException safeException = new IOException(e.getMessage() + " (converted from " + e.getClass().getName() + ")");
                    NetworkResponse response =
                        NetworkResponseFactory.generateExceptionWhileRoutingResponse(request, localNodeId.getNodeId(), safeException);
                    responseHandler.onResponseAvailable(response);
                }
            }

        });
    }

    protected void sendShutdownMessage() throws JMSException {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            final Queue destinationQueue = session.createQueue(outgoingRequestQueueName);
            Message shutdownMessage = JmsProtocolUtils.createShutdownMessage(session, shutdownSecurityToken);
            session.createProducer(destinationQueue).send(shutdownMessage);
        } finally {
            session.close();
        }
    }

    @Override
    public String getOutgoingRequestQueueName() {
        return outgoingRequestQueueName;
    }

    @Override
    public void setOutgoingRequestQueueName(String destinationQueueName) {
        this.outgoingRequestQueueName = destinationQueueName;
    }

    @Override
    public void setShutdownSecurityToken(String shutdownSecurityToken) {
        this.shutdownSecurityToken = shutdownSecurityToken;
    }

    protected String getShutdownSecurityToken() {
        return shutdownSecurityToken;
    }

    protected void setClosed(boolean closed) {
        this.closed = closed;
    }

    protected Message performRequestResponse(final Session session, Message message,
        final Queue destinationQueue, int timeoutMsec) throws JMSException, TimeoutException {
        final TemporaryQueue tempResponseQueue = session.createTemporaryQueue();
        message.setJMSReplyTo(tempResponseQueue);
        // send
        MessageProducer producer = session.createProducer(destinationQueue);
        JmsProtocolUtils.configureMessageProducer(producer);
        producer.send(message);
        // receive
        MessageConsumer consumer = session.createConsumer(tempResponseQueue);
        Message response = consumer.receive(timeoutMsec);
        if (response == null) {
            if (closed) {
                throw new TimeoutException("Timeout after connection was already closed; destination queue was "
                    + destinationQueue.getQueueName());
            }
            throw new TimeoutException("Timeout (" + timeoutMsec + " msec) exceeded after sending request to queue "
                + destinationQueue.getQueueName());
        }
        consumer.close();
        tempResponseQueue.delete();
        return response;
    }
}
