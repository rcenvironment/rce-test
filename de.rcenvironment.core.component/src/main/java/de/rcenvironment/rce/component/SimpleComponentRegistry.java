/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.List;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Class providing easy access to all registered components in the distributed system. This class
 * does not do a new remote query on each method call but acts on a local cache which has to be
 * manually updated by calling refresh().
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class SimpleComponentRegistry {

    private static DistributedComponentRegistry registry = ServiceUtils.createNullService(DistributedComponentRegistry.class);
    
    private User user;

    /** Only used by OSGi component instantiation. */
    @Deprecated
    public SimpleComponentRegistry() {}

    public SimpleComponentRegistry(User user) {
        this.user = user;
    }

    protected void bindDistributedComponentRegistry(DistributedComponentRegistry newRegistry) {
        registry = newRegistry;
    }

    protected void unbindDistributedComponentRegistry(DistributedComponentRegistry newRegistry) {
        registry = ServiceUtils.createNullService(DistributedComponentRegistry.class);
    }

    /**
     * Returns a list of all {@link ComponentDescription}s. Duplicates (i.e. components with the
     * same id but different platform) are eliminated.
     * @param forceRefresh if <code>false</code> cached descriptions (from last lookup) are returned
     *        otherwise a new lookup is done
     * @return all {@link ComponentDescription}s
     * @deprecated refreshes should be triggered, e.g. via notifications, thus no active refresh
     *             should be called. it needs to be called as long as the trigger mechanism is not
     *             established
     */
    @Deprecated
    public List<ComponentDescription> getAllComponentDescriptions(boolean forceRefresh) {
        return registry.getAllComponentDescriptions(user, forceRefresh);
    }

    /**
     * Returns a list of all {@link ComponentDescription}s. Duplicates (i.e. components with the same id but different
     * platform) are eliminated.
     * @return all {@link ComponentDescription}s
     */
    public List<ComponentDescription> getAllComponentDescriptions() {
        return registry.getAllComponentDescriptions(user, false);
    }
        
    /**
     * Returns the {@link ComponentDescription} for the given component identifier.
     * 
     * @param componentId The component identifier.
     * 
     * @return The {@link ComponentDescription} for the given component identifier.
     */
    public ComponentDescription getComponentDescription(String componentId) {
        for (ComponentDescription desc : getAllComponentDescriptions()) {
            if (desc.getIdentifier().equals(componentId)) {
                return desc.clone();
            }
        }
        return null;
    }

    /**
     * Creates a new component instance of the component given by its identifier.
     * @param description {@link ComponentDescription} to use.
     * @param name Desired name of the component instance.
     * @param context The {@link ComponentContext} of the component to create.
     * @param inputConnected <code>true</code> if at least one {@link Input} of this
     *        {@link Component} to create is connected with at least one {@link Output}.
     * @param platformId {@link PlatformIdentifier} of the platform where the component instance
     *        should be created.
     * @return The {@link ComponentInstanceDescriptor} representing the created component instance.
     * @throws ComponentException if initialization of the {@link Component} failed.
     */
    public ComponentInstanceDescriptor createComponentInstance(ComponentDescription description, String name,
        ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException {
        return registry.createComponentInstance(user, description, name, context, inputConnected, platformId);
    }

    /**
     * Disposes a component instance.
     * @param instanceId Component instance identifier.
     * @param platformId PlatformIdentifier where the component instance was created.
     */
    public void disposeComponentInstance(String instanceId, PlatformIdentifier platformId) {
        registry.disposeComponentInstance(user, instanceId, platformId);
    }

    /**
     * Returns the ComponentInformation.
     * @param instanceId Component instance identifier.
     * @param platformId The PlatformIdentifier.
     * @return The ComponentInformation.
     */
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(String instanceId, PlatformIdentifier platformId) {
        return registry.getComponentInstanceDescriptor(user, instanceId, platformId);
    }

}
