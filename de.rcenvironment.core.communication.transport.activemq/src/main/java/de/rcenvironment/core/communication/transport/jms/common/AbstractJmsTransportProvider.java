/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.jms.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.NetworkConnectionIdFactory;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.transport.spi.NetworkTransportProvider;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Abstract base class for JMS transport providers. This base class provides the aspects of a JMS
 * transport provider that are independent of the JMS implementation used.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractJmsTransportProvider implements NetworkTransportProvider {

    protected final Map<ServerContactPoint, JmsBroker> serverEndpoints =
        new HashMap<ServerContactPoint, JmsBroker>();

    protected final NetworkConnectionIdFactory connectionIdFactory;

    protected final Log log = LogFactory.getLog(getClass());

    protected final SharedThreadPool threadPool = SharedThreadPool.getInstance();

    protected final JmsArtifactFactory artifactFactory;

    /**
     * Common implementation of the {@link RemoteInitiatedConnectionFactory} interface.
     * 
     * @author Robert Mischke
     */
    public class PassiveConnectionFactoryImpl implements RemoteInitiatedConnectionFactory {

        @Override
        public JmsNetworkConnection createRemoteInitiatedConnection(NetworkNodeInformation receivingNodeInformation,
            NetworkNodeInformation initiatingNodeInformation, ServerContactPoint associatedSCP, Connection localJmsConnection)
            throws JMSException {
            String connectionId = connectionIdFactory.generateId(false);
            String destinationQueueName =
                String.format(
                    JmsProtocolConstants.QUEUE_NAME_PATTERN_B2C_REQUEST_INBOX,
                    initiatingNodeInformation.getWrappedNodeId().getNodeId(), connectionId);
            JmsNetworkConnection passiveConnection =
                new RemoteInitiatedJmsConnection(receivingNodeInformation.getWrappedNodeId(), localJmsConnection, associatedSCP);
            passiveConnection.setRemoteNodeInformation(initiatingNodeInformation);
            // FIXME add proper token
            passiveConnection.setShutdownSecurityToken("passive." + initiatingNodeInformation.getNodeId());
            passiveConnection.setConnectionId(connectionId);
            passiveConnection.setInitiatedByRemote(true);
            log.debug("Setting destination queue for passive connection to " + destinationQueueName);

            passiveConnection.setOutgoingRequestQueueName(destinationQueueName);
            return passiveConnection;
        }

    }

    public AbstractJmsTransportProvider(NetworkConnectionIdFactory connectionIdFactory, JmsArtifactFactory artifactFactory) {
        this.connectionIdFactory = connectionIdFactory;
        this.artifactFactory = artifactFactory;
    }

    @Override
    public NetworkConnection connect(NetworkContactPoint ncp, NetworkNodeInformation ownNodeInformation, boolean allowInverseConnection,
        NetworkConnectionEndpointHandler inverseConnectionEndpointHandler, BrokenConnectionListener brokenConnectionListener)
        throws CommunicationException {
        try {
            ConnectionFactory connectionFactory = artifactFactory.createConnectionFactory(ncp);
            NodeIdentifier localNodeId = ownNodeInformation.getWrappedNodeId();
            SelfInitiatedJmsConnection connection =
                new SelfInitiatedJmsConnection(localNodeId, connectionFactory, brokenConnectionListener);
            connection.setConnectionId(connectionIdFactory.generateId(true));
            connection.connect();
            log.debug("Connected to JMS broker; sending initial handshake with identity '" + localNodeId + "'");
            NetworkNodeInformation remoteNodeInformation =
                connection.performInitialHandshake(ownNodeInformation, inverseConnectionEndpointHandler);
            connection.setRemoteNodeInformation(remoteNodeInformation);
            log.debug("Successfully performed JMS handshake with remote node " + remoteNodeInformation.getLogDescription());
            // basic check against duplicate node ids; does not guard against non-neighbor nodes
            // with same id
            if (remoteNodeInformation.getNodeId().equals(localNodeId.getNodeId())) {
                throw new CommunicationException("Invalid setup: Remote and local node share the same node id: " + localNodeId.getNodeId());
            }
            return connection;
        } catch (IOException e) {
            throw new CommunicationException("Failed to initiate JMS connection", e);
        } catch (RuntimeException e) {
            throw new CommunicationException("Failed to establish JMS connection", e);
        } catch (JMSException e) {
            throw new CommunicationException("Failed to establish JMS connection", e);
        } catch (TimeoutException e) {
            throw new CommunicationException("Timeout while establishing JMS connection", e);
        }
    }

    @Override
    public boolean supportsRemoteInitiatedConnections() {
        return true;
    }

    @Override
    public void startServer(ServerContactPoint scp) throws CommunicationException {
        JmsBroker broker = artifactFactory.createBroker(scp, new PassiveConnectionFactoryImpl());
        // CHECKSTYLE:DISABLE (IllegalCatch) - ActiveMQ method declares "throws Exception"
        try {
            broker.start();
        } catch (Exception e) {
            throw new CommunicationException("Failed to start JMS broker for SCP " + scp, e);
        }
        // CHECKSTYLE:ENABLE (IllegalCatch)
        serverEndpoints.put(scp, broker);
        scp.setAcceptingMessages(true);
    }

    @Override
    public void stopServer(ServerContactPoint scp) {
        scp.setAcceptingMessages(false);
        JmsBroker broker = serverEndpoints.get(scp);
        broker.stop();
    }
}
