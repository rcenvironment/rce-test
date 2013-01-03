/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.jms.common;

/**
 * Constants used as part of JMS-based transport network protocols.
 * 
 * @author Robert Mischke
 */
public abstract class JmsProtocolConstants {

    /**
     * The maximum time JMS messages are preserved after creation by their producer; applied via
     * {@link MessageProducer#setTimeToLive(long)). Its main purpose in RCE is to prevent stale
     * messages from remaining in abandoned queues forever.
     */
    public static final int JMS_MESSAGES_TTL_MSEC = 60 * 1000;

    /**
     * JMS property key for the message type.
     */
    public static final String MESSAGE_FIELD_MESSAGE_TYPE = "messageType";

    /**
     * JMS property key for the metadata map.
     */
    public static final String MESSAGE_FIELD_METADATA = "metadata";

    /**
     * JMS property key for transporting the client-to-broker request queue name during the initial
     * handshake.
     */
    public static final String MESSAGE_FIELD_ACTIVE_C2B_REQUEST_INBOX = "queuename.requests.c2b";

    /**
     * JMS property key for transporting the broker-to-client request queue name during the initial
     * handshake.
     */
    public static final String MESSAGE_FIELD_PASSIVE_B2C_REQUEST_INBOX = "queuename.requests.b2c";

    /**
     * Message type value for the initial handshake request.
     */
    public static final String MESSAGE_TYPE_INITIAL = "initial";

    /**
     * Message type value for general requests.
     */
    public static final String MESSAGE_TYPE_REQUEST = "request";

    /**
     * Message type value for queue shutdown signals.
     */
    public static final String MESSAGE_TYPE_QUEUE_SHUTDOWN = "shutdown";

    /**
     * The JMS queue name for the initial handshake inbox.
     */
    public static final String QUEUE_NAME_INITIAL_BROKER_INBOX = "initial/c2b";

    /**
     * The JMS queue name for the common request inbox.
     */
    public static final String QUEUE_NAME_C2B_REQUEST_INBOX = "requests/c2b/common";

    /**
     * The naming pattern for broker-to-client request JMS queues.
     */
    public static final String QUEUE_NAME_PATTERN_B2C_REQUEST_INBOX = "requests/b2c/%s/%s";

}
