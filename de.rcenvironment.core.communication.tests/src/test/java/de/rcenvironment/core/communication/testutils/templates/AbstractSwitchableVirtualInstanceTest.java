/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.testutils.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.testutils.AbstractVirtualInstanceTest;
import de.rcenvironment.core.communication.testutils.TestStringRequestPayloadHandler;
import de.rcenvironment.core.communication.testutils.VirtualInstance;

/**
 * Base class providing tests that can operate using duplex as well as non-duplex transports. A
 * common use case is setting up a topology where are topology links are logially bidirectional. In
 * this case, the test code must adapt as this bidirectional linkage is automatically achieved when
 * using a duplex transport, but must be explicitly wired when using a non-duplex transport.
 * 
 * @author Robert Mischke
 * @author Phillip Kroll
 */
public abstract class AbstractSwitchableVirtualInstanceTest extends AbstractVirtualInstanceTest {

    private static final int TEST_SIZE = 10;

    protected final Random randomGenerator = new Random();

    /**
     * @throws Exception on uncaught exceptions
     */
    @BeforeClass
    // TODO transitional; rework this
    public static void setTestParameters() throws Exception {
        testSize = TEST_SIZE;
    }

    /**
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void testClientServerBidirectionalMessaging() throws Exception {

        setupInstances(2, usingDuplexTransport, true);

        VirtualInstance client = testTopology.getInstance(0);
        VirtualInstance server = testTopology.getInstance(1);
        prepareWaitForNextMessage();
        testTopology.connect(0, 1, !usingDuplexTransport);
        waitForNextMessage();
        waitForNetworkSilence();

        NetworkResponse serverResponse = client.performRoutedRequest("c2s", server.getNodeId()).get();
        assertNotNull("C2S communication failed", serverResponse);
        assertTrue("C2S communication failed", serverResponse.isSuccess());
        assertEquals(TestStringRequestPayloadHandler.getTestResponse("c2s", server.getNodeId()),
            serverResponse.getDeserializedContent());
        NetworkResponse clientResponse = server.performRoutedRequest("s2c", client.getNodeId()).get();
        assertNotNull("S2C communication failed", clientResponse);
        assertTrue("S2C communication failed", clientResponse.isSuccess());
        assertEquals(TestStringRequestPayloadHandler.getTestResponse("s2c", client.getNodeId()),
            clientResponse.getDeserializedContent());

        // Systemx.out.println(NetworkFormatter.summary(client.getTopologyMap()));
        // Systemx.out.println(NetworkFormatter.summary(server.getTopologyMap()));

        prepareWaitForNextMessage();
        testTopology.getAsGroup().shutDown();
        waitForNextMessage();
        waitForNetworkSilence();
    }

    /**
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void testIterativelyGrowingLinearNetwork() throws Exception {

        setupInstances(TEST_SIZE, usingDuplexTransport, true);

        for (int i = 0; i < allInstances.length - 1; i++) {
            logIteration(i);
            int newInstanceIndex = i + 1;

            prepareWaitForNextMessage();
            testTopology.connect(i, newInstanceIndex, !usingDuplexTransport);
            waitForNextMessage();
            waitForNetworkSilence();

            // note: the third parameter in Arrays.copyOfRange() is exclusive and must be "last + 1"
            assertTrue("Instances did not converge at i=" + i,
                instanceUtils.allInstancesConverged(Arrays.copyOfRange(allInstances, 0, newInstanceIndex + 1)));
        }
    }

    /**
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void testConcurrentlyGrowingLinearNetwork() throws Exception {

        setupInstances(TEST_SIZE, usingDuplexTransport, true);

        prepareWaitForNextMessage();
        for (int i = 0; i < allInstances.length - 1; i++) {
            logIteration(i);
            testTopology.connect(i, i + 1, !usingDuplexTransport);
        }
        waitForNextMessage();
        waitForNetworkSilence();

        assertTrue(testTopology.allInstancesConverged());
    }

    /**
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void testIterativelyGrowingRandomNetwork() throws Exception {

        setupInstances(TEST_SIZE, usingDuplexTransport, true);

        Random random = new Random();
        for (int i = 0; i < allInstances.length - 1; i++) {
            logIteration(i);
            int newInstanceIndex = i + 1;
            int connectedInstanceIndex = random.nextInt(newInstanceIndex); // 0..newInstanceIndex-1
            prepareWaitForNextMessage();
            testTopology.connect(connectedInstanceIndex, newInstanceIndex, !usingDuplexTransport);
            waitForNextMessage();
            waitForNetworkSilence();

            // note: the third parameter in Arrays.copyOfRange() is exclusive and must be "last + 1"
            assertTrue("Instances did not converge at i=" + i,
                instanceUtils.allInstancesConverged(Arrays.copyOfRange(allInstances, 0, newInstanceIndex + 1)));
        }
    }

    /**
     * @throws Exception on uncaught exceptions
     */
    @Test
    public void testConcurrentlyGrowingRandomNetwork() throws Exception {

        setupInstances(TEST_SIZE, usingDuplexTransport, true);

        prepareWaitForNextMessage();
        Random random = new Random();
        for (int i = 0; i < allInstances.length - 1; i++) {
            logIteration(i);
            int newInstanceIndex = i + 1;
            int connectedInstanceIndex = random.nextInt(newInstanceIndex); // 0..i
            testTopology.connect(connectedInstanceIndex, newInstanceIndex, !usingDuplexTransport);
        }
        waitForNextMessage();
        waitForNetworkSilence();
        assertTrue(ERROR_MSG_INSTANCES_DID_NOT_CONVERGE, instanceUtils.allInstancesConverged(allInstances));
    }

    private void logIteration(int i) {
        log.debug("i: " + i);
    }
}
