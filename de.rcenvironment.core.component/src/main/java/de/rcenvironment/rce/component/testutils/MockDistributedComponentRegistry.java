/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.testutils;

import java.util.List;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.DistributedComponentRegistry;


/**
 * Common test/mock implementations of {@link DistributedComponentRegistry}. These can be used directly, or
 * can as superclasses for custom mock classes.
 * 
 * Custom mock implementations of {@link DistributedComponentRegistry} should use these as superclasses
 * whenever possible to avoid code duplication, and to shield the mock classes from irrelevant API
 * changes.
 *
 * @author Doreen Seider
 */
public class MockDistributedComponentRegistry implements DistributedComponentRegistry {

    @Override
    public List<ComponentDescription> getAllComponentDescriptions(User user, boolean forceRefresh) {
        return null;
    }

    @Override
    public ComponentInstanceDescriptor createComponentInstance(User proxyCertificate, ComponentDescription description, String name,
        ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException {
        return null;
    }

    @Override
    public void disposeComponentInstance(User proxyCertificate, String identifier, PlatformIdentifier platformId)
        throws AuthorizationException {
    }

    @Override
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(User proxyCertificate, String identifier,
        PlatformIdentifier platformId) throws AuthorizationException {
        return null;
    }

}
