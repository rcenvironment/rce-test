/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.testutils.templates;

import org.junit.Test;

import de.rcenvironment.core.communication.model.NetworkContactPoint;
import de.rcenvironment.core.communication.testutils.AbstractVirtualInstanceTest;
import de.rcenvironment.core.communication.testutils.VirtualInstance;
import de.rcenvironment.core.communication.testutils.VirtualInstanceGroup;

/**
 * Base class providing common tests using virtual node instances. "Common" tests are those that do
 * not depend on duplex vs. non-duplex mode. This may also include test that require duplex mode,
 * and are skipped/ignored when the tested transport does not support this.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractCommonVirtualInstanceTest extends AbstractVirtualInstanceTest {

    /**
     * Test with two clients connecting to a single server. The server instance is started before
     * and shut down after the clients.
     * 
     * @throws Exception on unexpected test exceptions
     */
    @Test
    public void testBasicClientServer() throws Exception {

        // TODO old test; could be improved by using new test utilities

        VirtualInstance client1 = new VirtualInstance("Client1Id", "Client1");
        VirtualInstance client2 = new VirtualInstance("Client2Id", "Client2");
        VirtualInstance server = new VirtualInstance("ServerId", "Server");

        VirtualInstanceGroup allInstances = new VirtualInstanceGroup(server, client1, client2);
        VirtualInstanceGroup clients = new VirtualInstanceGroup(client1, client2);

        allInstances.registerNetworkTransportProvider(transportProvider);
        addGlobalTrafficListener(allInstances);

        NetworkContactPoint serverContactPoint = contactPointGenerator.createContactPoint();
        server.addServerConfigurationEntry(serverContactPoint);

        server.start();

        // TODO validate server network knowledge, internal state etc.

        prepareWaitForNextMessage();
        // configure & start clients
        clients.addInitialNetworkPeer(serverContactPoint);
        clients.start();
        // wait for network traffic to end
        // FIXME check: this succeeds on its own, but fails when run together with other tests
        waitForNextMessage();
        waitForNetworkSilence();

        // Systemx.out.println(NetworkFormatter.summary(client1.getTopologyMap()));
        // Systemx.out.println(NetworkFormatter.summary(client2.getTopologyMap()));
        // Systemx.out.println(NetworkFormatter.summary(server.getTopologyMap()));

        // TODO validate server/client network knowledge, internal state etc.

        prepareWaitForNextMessage();
        // stop clients
        clients.shutDown();
        // wait for network traffic to end
        waitForNextMessage();
        waitForNetworkSilence();

        // TODO validate server network knowledge, internal state etc.

        allInstances.shutDown();
    }

}
