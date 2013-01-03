/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.activemq.internal;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.transport.jms.common.InitialInboxConsumer;
import de.rcenvironment.core.communication.transport.jms.common.JmsBroker;
import de.rcenvironment.core.communication.transport.jms.common.JmsProtocolConstants;
import de.rcenvironment.core.communication.transport.jms.common.JmsProtocolUtils;
import de.rcenvironment.core.communication.transport.jms.common.RemoteInitiatedConnectionFactory;
import de.rcenvironment.core.communication.transport.jms.common.RequestInboxConsumer;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;

/**
 * ActiveMQ implementation of the common {@link JmsBroker} interface. It provides an embedded JMS
 * broker for a given {@link ServerContactPoint} that accepts incoming connections and creates
 * matching remote-initiated ("passive") connections for them.
 * 
 * @author Robert Mischke
 */
public class ActiveMQBroker implements JmsBroker {

    private static final int SHUTDOWN_WAIT_AFTER_ANNOUNCE_MSEC = 1000;

    private final String brokerName;

    private final String externalUrl;

    private final String jvmLocalUrl;

    private BrokerService brokerService;

    private Connection localBrokerConnection;

    private final ServerContactPoint scp;

    private final RemoteInitiatedConnectionFactory remoteInitiatedConnectionFactory;

    private final SharedThreadPool threadPool = SharedThreadPool.getInstance();

    private final Log log = LogFactory.getLog(getClass());

    private int numRequestConsumers;

    public ActiveMQBroker(ServerContactPoint scp, RemoteInitiatedConnectionFactory remoteInitiatedConnectionFactory) {
        this.scp = scp;
        this.remoteInitiatedConnectionFactory = remoteInitiatedConnectionFactory;
        NetworkContactPoint ncp = scp.getNetworkContactPoint();
        int port = ncp.getPort();
        String host = ncp.getHost();
        this.brokerName = "RCE_ActiveMQ_" + host + "_" + port;
        this.externalUrl = "tcp://" + host + ":" + port;
        this.jvmLocalUrl = "vm://" + brokerName;

        this.numRequestConsumers = 1;
        String property = System.getProperty("jms.numRequestConsumers");
        if (property != null) {
            try {
                numRequestConsumers = Integer.parseInt(property);
            } catch (NumberFormatException e) {
                log.warn("Ignoring invalid property value: " + property);
            }
        }
    }

    @Override
    public void start() throws Exception {
        brokerService = createTransientEmbeddedBroker(brokerName, externalUrl, jvmLocalUrl);
        ConnectionFactory localConnectionFactory = new ActiveMQConnectionFactory(jvmLocalUrl);
        localBrokerConnection = localConnectionFactory.createConnection();
        localBrokerConnection.setExceptionListener(new ExceptionListener() {

            @Override
            public void onException(JMSException exception) {
                log.warn("Asynchronous JMS exception in local broker connection", exception);
            }
        });
        localBrokerConnection.start();
        spawnInboxConsumers(getLocalConnection());
    }

    @Override
    public void stop() {
        try {
            Session shutdownSession = localBrokerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = shutdownSession.createProducer(null);
            JmsProtocolUtils.configureMessageProducer(producer);
            log.debug("Sending internal queue shutdown commands");
            String securityToken = "secToken"; // FIXME use proper token
            Message poisonPill = JmsProtocolUtils.createShutdownMessage(shutdownSession, securityToken);
            
            producer.send(shutdownSession.createQueue(JmsProtocolConstants.QUEUE_NAME_INITIAL_BROKER_INBOX), poisonPill);
            for (int i = 0; i < numRequestConsumers; i++) {
                producer.send(shutdownSession.createQueue(JmsProtocolConstants.QUEUE_NAME_C2B_REQUEST_INBOX), poisonPill);
            }
            shutdownSession.close();
            Thread.sleep(SHUTDOWN_WAIT_AFTER_ANNOUNCE_MSEC);
        } catch (JMSException e) {
            log.error("Error while shutting down queue listeners", e);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for queue shutdown", e);
        }
        try {
            localBrokerConnection.close();
        } catch (JMSException e) {
            log.warn("Error closing local connection to broker " + brokerService.getBrokerName(), e);
        }
        // CHECKSTYLE:DISABLE (IllegalCatch) - ActiveMQ method declares "throws Exception"
        try {
            brokerService.stop();
            log.info("Stopped JMS broker " + brokerService.getBrokerName());
        } catch (Exception e) {
            log.warn("Error shutting down JMS broker " + brokerService.getBrokerName(), e);
        }
        // CHECKSTYLE:ENABLE (IllegalCatch)
    }

    @Override
    public Connection getLocalConnection() {
        return localBrokerConnection;
    }

    private static BrokerService createTransientEmbeddedBroker(String brokerName,
        final String... urls) throws Exception {
        final BrokerService broker = new BrokerService();
        broker.setBrokerName(brokerName);
        broker.setPersistent(false);
        broker.setUseJmx(false); // default=true
        // TODO ActiveMQ broker properties to set/evaluate:
        // - schedulePeriodForDestinationPurge
        // - inactiveTimoutBeforeGC
        // - timeBeforePurgeTempDestinations
        // ...
        for (String url : urls) {
            broker.addConnector(url);
        }
        broker.start();
        return broker;
    }

    private void spawnInboxConsumers(Connection connection) throws JMSException {
        NetworkConnectionEndpointHandler endpointHandler = scp.getEndpointHandler();
        log.debug("Spawning initial inbox consumer for SCP " + scp);
        threadPool.execute(new InitialInboxConsumer(connection, endpointHandler, scp, remoteInitiatedConnectionFactory));
        log.debug("Spawning " + numRequestConsumers + " request inbox consumer(s) for SCP " + scp);
        for (int i = 0; i < numRequestConsumers; i++) {
            threadPool.execute(new RequestInboxConsumer(JmsProtocolConstants.QUEUE_NAME_C2B_REQUEST_INBOX, connection, endpointHandler));
        }
    }
}
