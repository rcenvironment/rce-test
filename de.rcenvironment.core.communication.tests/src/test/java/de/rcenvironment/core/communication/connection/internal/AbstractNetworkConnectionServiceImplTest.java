/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.connection.internal;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import org.easymock.EasyMock;
import org.junit.Before;

import de.rcenvironment.core.communication.connection.NetworkConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;
import de.rcenvironment.rce.communication.CommunicationException;

/**
 * Base class for {@link NetworkConnectionServiceImpl} unit tests providing common tests that can be
 * performed with various network transports.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractNetworkConnectionServiceImplTest {

    protected NetworkNodeInformation node1Information;

    protected NetworkContactPoint node1ContactPoint;

    protected NetworkConnectionServiceImpl node1Service;

    protected NetworkNodeInformation node2Information;

    protected NetworkContactPoint node2ContactPoint;

    protected NetworkConnectionServiceImpl node2Service;

    /**
     * Common test setup.
     */
    @Before
    public void setUp() {
        node1Information = new NetworkNodeInformationImpl("node1.testId");
        node1Service = new NetworkConnectionServiceImpl();
        node1Service.setNodeInformation(node1Information);
        node2Information = new NetworkNodeInformationImpl("node2.testId");
        node2Service = new NetworkConnectionServiceImpl();
        node2Service.setNodeInformation(node2Information);
    }

    /**
     * Tests two nodes connecting to each other with duplex disabled. Each node should receive a
     * single {@link NetworkConnection} to the other node.
     * 
     * @throws Exception on unexpected test errors
     */
    protected void commonTestActiveConnectionsNoDuplex() throws Exception {
        defineNetworkSetup();

        node1Service.startServer(node1ContactPoint);
        node2Service.startServer(node2ContactPoint);

        // TODO test return values more precisely
        NetworkConnectionListener connectionListener = EasyMock.createMock(NetworkConnectionListener.class);
        // TODO improve test of sequential behaviour
        connectionListener.onOutgoingConnectionEstablished(EasyMock.anyObject(NetworkConnection.class));
        EasyMock.replay(connectionListener);
        node1Service.addConnectionListener(connectionListener);
        node2Service.addConnectionListener(connectionListener);

        NetworkConnection node1SelfConnection = node1Service.connect(node1ContactPoint, false).get();
        assertEquals(node1Information.getWrappedNodeId(), node1SelfConnection.getRemoteNodeInformation().getWrappedNodeId());

        EasyMock.verify(connectionListener);
        EasyMock.reset(connectionListener);

        connectionListener.onOutgoingConnectionEstablished(EasyMock.anyObject(NetworkConnection.class));
        EasyMock.replay(connectionListener);

        NetworkConnection node1To2Connection = node1Service.connect(node2ContactPoint, false).get();
        assertEquals(node2Information.getWrappedNodeId(), node1To2Connection.getRemoteNodeInformation().getWrappedNodeId());

        EasyMock.verify(connectionListener);
    }

    /**
     * Tests two nodes, with one connecting to the other with duplex enabled. Each node should
     * receive a single {@link NetworkConnection} to the other node.
     * 
     * Note that the test behaviour expects the tested transport to support passive connections;
     * future tests may need to adapt this.
     * 
     * @throws Exception on unexpected test errors
     */
    protected void commonTestSingleDuplexConnection() throws CommunicationException, InterruptedException, ExecutionException {
        defineNetworkSetup();

        node1Service.startServer(node1ContactPoint);
        node2Service.startServer(node2ContactPoint);

        NetworkConnectionListener connectionListener = EasyMock.createMock(NetworkConnectionListener.class);
        connectionListener.onOutgoingConnectionEstablished(EasyMock.anyObject(NetworkConnection.class));
        EasyMock.replay(connectionListener);

        node2Service.addConnectionListener(connectionListener);

        NetworkConnection node1To2Connection = node1Service.connect(node2ContactPoint, true).get();
        assertEquals(node2Information.getWrappedNodeId(), node1To2Connection.getRemoteNodeInformation().getWrappedNodeId());

        EasyMock.verify(connectionListener);
    }

    protected abstract void defineNetworkSetup();
}
