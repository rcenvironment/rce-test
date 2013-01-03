/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.management.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.communication.configuration.NodeConfigurationService;
import de.rcenvironment.core.communication.connection.NetworkConnectionService;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.management.CommunicationManagementService;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;
import de.rcenvironment.core.communication.routing.NetworkRoutingService;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Default {@link CommunicationManagementService} implementation.
 * 
 * @author Robert Mischke
 */
public class CommunicationManagementServiceImpl implements CommunicationManagementService {

    /**
     * The delay between announcing the shutdown to all neighbors, and actually shutting down.
     */
    private static final int DELAY_AFTER_SHUTDOWN_ANNOUNCE_MSEC = 200;

    private NetworkConnectionService connectionService;

    private NetworkRoutingService networkRoutingService;

    private NetworkNodeInformation ownNodeInformation;

    private NodeConfigurationService configurationService;

    private List<ServerContactPoint> initializedServerContactPoints = new ArrayList<ServerContactPoint>();

    private final Log log = LogFactory.getLog(getClass());

    private ScheduledFuture<?> connectionHealthCheckTaskHandle;

    @Override
    public void startUpNetwork() {

        // start server contact points
        for (NetworkContactPoint ncp : configurationService.getServerContactPoints()) {
            // log.debug(String.format("Virtual instance '%s': Starting server at %s",
            // ownNodeInformation.getLogName(), ncp));
            try {
                synchronized (initializedServerContactPoints) {
                    ServerContactPoint newSCP = connectionService.startServer(ncp);
                    initializedServerContactPoints.add(newSCP);
                }
            } catch (CommunicationException e) {
                log.warn("Error while starting server at " + ncp, e);
            }
        }
        // FIXME temporary fix until connection retry (or similar) is implemented;
        // without this, simultaneous startup of instance groups will usually fail,
        // because some instances will try to connect before others have fully started
        try {
            Thread.sleep(configurationService.getDelayBeforeStartupConnectAttempts());
        } catch (InterruptedException e1) {
            log.error("Interrupted while waiting during startup; not connecting to neighbors", e1);
            return;
        }

        // trigger connections to initial peers
        for (final NetworkContactPoint ncp : configurationService.getInitialNetworkContactPoints()) {
            asyncConnectToNetworkPeer(ncp);
        }

        connectionHealthCheckTaskHandle = SharedThreadPool.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            @TaskDescription("Connection health check (trigger task)")
            public void run() {
                try {
                    connectionService.triggerConnectionHealthChecks();
                } catch (RuntimeException e) {
                    log.error("Uncaught exception during connection health check", e);
                }
            }
        }, CommunicationConfiguration.CONNECTION_HEALTH_CHECK_INTERVAL_MSEC);
    }

    @Override
    public void connectToRuntimePeer(NetworkContactPoint ncp) throws CommunicationException {
        Future<NetworkConnection> future = connectionService.connect(ncp, true);
        try {
            future.get();
        } catch (ExecutionException e) {
            throw new CommunicationException(e);
        } catch (InterruptedException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public void asyncConnectToNetworkPeer(final NetworkContactPoint ncp) {
        SharedThreadPool.getInstance().execute(new Runnable() {

            @Override
            @TaskDescription("Connect to remote node (trigger task)")
            public void run() {
                try {
                    log.debug("Initiating asynchronous connection to " + ncp);
                    connectToRuntimePeer(ncp);
                } catch (CommunicationException e) {
                    log.warn("Failed to contact initial peer at NCP " + ncp, e);
                }
            }
        });
    }

    @Override
    public void shutDownNetwork() {
        connectionHealthCheckTaskHandle.cancel(true);

        networkRoutingService.announceShutdown();

        // FIXME dirty hack until the shutdown LSA broadcast waits for a response or timeout itself;
        // without this, the asynchronous sending might not happen before the connections are closed
        // TODO wait for confirmations from all neighbors (with a short timeout) instead?
        try {
            Thread.sleep(DELAY_AFTER_SHUTDOWN_ANNOUNCE_MSEC);
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting", e);
        }

        // close outgoing connections
        connectionService.closeAllOutgoingConnections();

        // shut down server contact points
        synchronized (initializedServerContactPoints) {
            for (ServerContactPoint scp : initializedServerContactPoints) {
                // log.debug(String.format("Virtual instance '%s': Stopping server at %s",
                // ownNodeInformation.getLogName(), ncp));
                scp.shutDown();
            }
            initializedServerContactPoints.clear();
        }
    }

    /**
     * Intended for use by unit/integration tests; simulates a "hard"/unclean shutdown where the
     * node does not send any network notifications before shutting down.
     * 
     */
    public void simulateUncleanShutdown() {
        // TODO simulate connections "crashing" as well
        synchronized (initializedServerContactPoints) {
            for (ServerContactPoint scp : initializedServerContactPoints) {
                scp.shutDown();
            }
            initializedServerContactPoints.clear();
        }
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param newService the service to bind
     */
    public void bindNetworkConnectionService(NetworkConnectionService newService) {
        // do not allow rebinding for now
        if (connectionService != null) {
            throw new IllegalStateException();
        }
        connectionService = newService;
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param newService the service to bind
     */
    public void bindNetworkRoutingService(NetworkRoutingService newService) {
        // do not allow rebinding for now
        if (networkRoutingService != null) {
            throw new IllegalStateException();
        }
        networkRoutingService = newService;
    }

    /**
     * OSGi-DS bind method; public for integration test access.
     * 
     * @param newService the service to bind
     */
    public void bindNodeConfigurationService(NodeConfigurationService newService) {
        // do not allow rebinding for now
        if (this.configurationService != null) {
            throw new IllegalStateException();
        }
        this.configurationService = newService;
    }

    /**
     * OSGi-DS lifecycle method.
     */
    public void activate() {
        ownNodeInformation = configurationService.getLocalNodeInformation();
        NodeInformationRegistryImpl.getInstance().updateFrom(ownNodeInformation);
    }

    /**
     * OSGi-DS lifecycle method.
     */
    public void deactivate() {}

}
