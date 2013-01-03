/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.communication.transport.activemq;

import de.rcenvironment.core.communication.testutils.TestConfiguration;
import de.rcenvironment.core.communication.testutils.VirtualInstance;
import de.rcenvironment.core.communication.testutils.templates.AbstractCommonVirtualInstanceTest;

/**
 * ActiveMQ implementation of the "common" {@link VirtualInstance} tests.
 * 
 * @author Robert Mischke
 */
public class ActiveMQCommonVirtualInstanceTest extends AbstractCommonVirtualInstanceTest {

    @Override
    protected TestConfiguration defineTestConfiguration() {
        return new ActiveMQTestConfiguration();
    }
}
