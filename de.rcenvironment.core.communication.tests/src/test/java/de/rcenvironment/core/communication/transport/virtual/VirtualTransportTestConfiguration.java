/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.virtual;

import de.rcenvironment.core.communication.connection.NetworkConnectionIdFactory;
import de.rcenvironment.core.communication.connection.impl.DefaultNetworkConnectionIdFactoryImpl;
import de.rcenvironment.core.communication.testutils.TestConfigurationImpl;
import de.rcenvironment.core.communication.transport.virtual.testutils.VirtualNetworkContactPointGenerator;

/**
 * Test configuration for {@link VirtualNetworkTransportProvider} tests.
 * 
 * @author Robert Mischke
 */
public class VirtualTransportTestConfiguration extends TestConfigurationImpl {

    private static final int TRAFFIC_WAIT_TIME = 150;

    private static final int SILENCE_WAIT_TIME = 250;

    private static final int SILENCE_WAIT_TIMEOUT = 10000;

    public VirtualTransportTestConfiguration(boolean useDuplexTransport) {
        setContactPointGenerator(new VirtualNetworkContactPointGenerator("virtualhost"));
        NetworkConnectionIdFactory connectionIdFactory = new DefaultNetworkConnectionIdFactoryImpl();
        setTransportProvider(new VirtualNetworkTransportProvider(useDuplexTransport, connectionIdFactory));
        setDefaultTrafficWaitTimeout(TRAFFIC_WAIT_TIME);
        setDefaultNetworkSilenceWait(SILENCE_WAIT_TIME);
        setDefaultNetworkSilenceWaitTimeout(SILENCE_WAIT_TIMEOUT);
    }

}
