/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.testutils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;

import de.rcenvironment.core.communication.model.NetworkStateModel;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.ReachabilityChecker;

/**
 * Common test/mock implementations of {@link CommunicationService}. These can be used directly, or
 * can as superclasses for custom mock classes.
 * 
 * Custom mock implementations of {@link CommunicationService} should use these as superclasses
 * whenever possible to avoid code duplication, and to shield the mock classes from irrelevant API
 * changes.
 * 
 * @author Doreen Seider
 */
public class MockCommunicationService implements CommunicationService {

    @Override
    public Set<PlatformIdentifier> getAvailableNodes(boolean forceRefresh) {
        return new HashSet<PlatformIdentifier>();
    }

    @Override
    public NetworkStateModel getCurrentNetworkState() {
        return null;
    }

    @Override
    public Object getService(Class<?> iface, PlatformIdentifier platformIdentifier, BundleContext bundleContext)
        throws IllegalStateException {
        return null;
    }

    @Override
    public Object getService(Class<?> iface, Map<String, String> properties, PlatformIdentifier platformIdentifier,
        BundleContext bundleContext) throws IllegalStateException {
        return null;
    }

    @Override
    public void addRuntimeNetworkPeer(String contactPointDefinition) throws CommunicationException {}

    @Override
    public void checkReachability(ReachabilityChecker checker) {}

    @Override
    public String getNetworkInformation() {
        return null;
    }

}
