/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.activemq;

import de.rcenvironment.core.communication.connection.NetworkConnectionIdFactory;
import de.rcenvironment.core.communication.connection.impl.DefaultNetworkConnectionIdFactoryImpl;
import de.rcenvironment.core.communication.transport.activemq.internal.ActiveMQJmsFactory;
import de.rcenvironment.core.communication.transport.jms.common.AbstractJmsTransportProvider;

/**
 * ActiveMQ variant of the generic JMS transport provider.
 * 
 * @author Robert Mischke
 */
public class ActiveMQTransportProvider extends AbstractJmsTransportProvider {

    /**
     * The transport id of this provider.
     */
    public static final String TRANSPORT_ID = "activemq-tcp";

    public ActiveMQTransportProvider() {
        this(new DefaultNetworkConnectionIdFactoryImpl());
    }

    // explicit constructor for unit tests
    public ActiveMQTransportProvider(NetworkConnectionIdFactory connectionIdFactory) {
        super(connectionIdFactory, new ActiveMQJmsFactory());
    }

    @Override
    public String getTransportId() {
        return TRANSPORT_ID;
    }

    // OSGi-DS component lifecycle method
    protected void activate() {
        log.debug("Activating ActiveMQ transport");
    }

    // OSGi-DS component lifecycle method
    protected void deactivate() {
        log.debug("Deactivating ActiveMQ transport");
    }

}
