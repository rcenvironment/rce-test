/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.testutils.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import de.rcenvironment.core.communication.connection.NetworkConnectionEndpointHandler;
import de.rcenvironment.core.communication.connection.ServerContactPoint;
import de.rcenvironment.core.communication.model.BrokenConnectionListener;
import de.rcenvironment.core.communication.model.NetworkConnection;
import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.model.NetworkNodeInformation;
import de.rcenvironment.core.communication.model.NetworkRequest;
import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.model.RawNetworkResponseHandler;
import de.rcenvironment.core.communication.model.impl.NetworkNodeInformationImpl;
import de.rcenvironment.core.communication.model.impl.NetworkRequestImpl;
import de.rcenvironment.core.communication.model.impl.NetworkResponseImpl;
import de.rcenvironment.core.communication.testutils.AbstractTransportBasedTest;
import de.rcenvironment.core.communication.utils.MessageUtils;
import de.rcenvironment.core.communication.utils.MetaDataWrapper;

/**
 * A common base class that defines common tests to verify proper transport operation. Subclasses
 * implement {@link #defineTestConfiguration()} to create a transport-specific test.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractTransportLowLevelTest extends AbstractTransportBasedTest {

    private static final int DEFAULT_REQUEST_TIMEOUT = 1000;

    /**
     * This test verifies the basic functions of a network transport. It covers
     * {@link ServerContactPoint} startup, connection initiation, initial node information
     * handshake, and the basic request/response loop.
     * 
     * @author Robert Mischke
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void basicTransportOperation() throws Exception {

        int messageRepetitions = 10;

        // create mock server config
        NetworkNodeInformationImpl mockServerNodeInformation = new NetworkNodeInformationImpl("serverId");
        mockServerNodeInformation.setDisplayName("Mock Server");
        // configure mock endpoint handler
        NetworkConnectionEndpointHandler serverEndpointHandler = EasyMock.createMock(NetworkConnectionEndpointHandler.class);
        // configure handshake response
        EasyMock.expect(serverEndpointHandler.exchangeNodeInformation(EasyMock.anyObject(NetworkNodeInformation.class))).andReturn(
            mockServerNodeInformation);
        // expect passive connection event (if applicable)
        if (transportProvider.supportsRemoteInitiatedConnections()) {
            serverEndpointHandler.onRemoteInitiatedConnectionEstablished(EasyMock.anyObject(NetworkConnection.class),
                EasyMock.anyObject(ServerContactPoint.class));
        }
        EasyMock.replay(serverEndpointHandler);
        BrokenConnectionListener brokenConnectionListener = EasyMock.createMock(BrokenConnectionListener.class);
        EasyMock.replay(brokenConnectionListener);
        // create server contact point
        NetworkContactPoint ncp = contactPointGenerator.createContactPoint();
        ServerContactPoint scp = new ServerContactPoint(transportProvider, ncp, serverEndpointHandler);
        // start it
        assertFalse(scp.isAcceptingMessages());
        scp.start();
        assertTrue(scp.isAcceptingMessages());

        // create mock client config
        NetworkNodeInformationImpl clientNodeInformation =
            new NetworkNodeInformationImpl("clientNodeId");
        // connect
        // (allows duplex connections, but omits client endpoint handler as it should not be used)
        NetworkConnection connection =
            transportProvider.connect(ncp, clientNodeInformation, true, null, brokenConnectionListener);

        // verify server side of handshake
        EasyMock.verify(serverEndpointHandler);
        // verify client side of handshake
        assertNotNull(connection.getConnectionId());
        assertNotNull(connection.getRemoteNodeInformation());
        assertEquals(mockServerNodeInformation, connection.getRemoteNodeInformation());

        // define server response behavior
        final String requestString = "Hi world";
        final String responseSuffix = "#response"; // arbitrary
        EasyMock.reset(serverEndpointHandler);
        serverEndpointHandler.onRawRequestReceived(EasyMock.isA(NetworkRequestImpl.class), EasyMock.isA(NodeIdentifier.class));

        EasyMock.expectLastCall().andAnswer(new IAnswer<NetworkResponse>() {

            @Override
            public NetworkResponse answer() throws Throwable {
                try {
                    NetworkRequest request = (NetworkRequest) EasyMock.getCurrentArguments()[0];
                    String responseString = request.getDeserializedContent().toString() + responseSuffix;
                    byte[] responseBytes = MessageUtils.serializeSafeObject(responseString);
                    return new NetworkResponseImpl(responseBytes, MetaDataWrapper.createEmpty().getInnerMap());
                } catch (RuntimeException e) {
                    log.warn("RTE in mock", e);
                    return null;
                }
            }
        }).times(messageRepetitions);

        // set up mock client response handler
        RawNetworkResponseHandler responseHandler = EasyMock.createMock(RawNetworkResponseHandler.class);
        // define expected callback
        Capture<NetworkResponse> responseCapture = new Capture<NetworkResponse>(CaptureType.ALL);
        responseHandler.onResponseAvailable(EasyMock.capture(responseCapture));
        EasyMock.expectLastCall().times(messageRepetitions);

        // enter test mode
        EasyMock.replay(serverEndpointHandler, responseHandler);
        // send request(s)
        for (int i = 0; i < messageRepetitions; i++) {
            connection.sendRequest(new NetworkRequestImpl(requestString, MetaDataWrapper.createEmpty().getInnerMap()), responseHandler,
                DEFAULT_REQUEST_TIMEOUT);
        }
        // TODO improve; quick&dirty hack to test larger message repetition counts
        Thread.sleep(testConfiguration.getDefaultTrafficWaitTimeout() + messageRepetitions * 10);

        // first, verify that the endpoint handler was called
        EasyMock.verify(serverEndpointHandler);
        // then, verify that the response handler was called
        EasyMock.verify(responseHandler);
        // verify response content
        List<NetworkResponse> responses = responseCapture.getValues();
        assertEquals(messageRepetitions, responses.size());
        for (NetworkResponse response : responses) {
            assertEquals(requestString + responseSuffix, response.getDeserializedContent());
        }

        // TODO close passive connection as well?
        connection.close();

        transportProvider.stopServer(scp);
        assertFalse(scp.isAcceptingMessages());
    }

}
