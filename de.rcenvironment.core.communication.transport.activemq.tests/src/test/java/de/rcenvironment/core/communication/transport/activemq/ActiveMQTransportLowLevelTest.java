/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.activemq;

import de.rcenvironment.core.communication.testutils.TestConfiguration;
import de.rcenvironment.core.communication.testutils.templates.AbstractTransportLowLevelTest;

/**
 * ActiveMQ implementation of the low-level transport tests.
 * 
 * @author Robert Mischke
 */
public class ActiveMQTransportLowLevelTest extends AbstractTransportLowLevelTest {

    @Override
    protected TestConfiguration defineTestConfiguration() {
        return new ActiveMQTestConfiguration();
    }

}
