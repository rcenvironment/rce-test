/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.List;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;


/**
 * Service that provides easy access to all registered components in the distributed system.
 *
 * @author Heinrich Wendel
 */
public interface DistributedComponentRegistry {
    
    /**
     * Returns all available {@link ComponentDescription}s registered in the distributed system.
     * Duplicates (i.e. components with the same id but different platform) are eliminated.
     * 
     * @param user Calling {@link User}.
     * @param forceRefresh if <code>false</code> cached descriptions (from last lookup) are returned
     *        otherwise a new lookup is done
     * 
     * @return all available {@link ComponentDescription}s
     */
    List<ComponentDescription> getAllComponentDescriptions(User user, boolean forceRefresh);
     
    /**
     * Creates a new component instance described by the given {@link ComponentDescription} on the given platform.
     * 
     * @param user The user's {@link User} to use.
     * @param description The {@link ComponentDescription} to use.
     * @param name The desired name of the instance to create.
     * @param context The {@link ComponentContext} of the component to create.
     * @param inputConnected <code>true</code> if at least one {@link Input} of this
     *        {@link Component} to create is connected with at least one {@link Output}.
     * @param platformId The {@link PlatformIdentifier} of the platform to create the component instance.
     * @return {@link ComponentInstanceDescriptor} about the created instance
     * @throws ComponentException if initialization of the {@link Component} failed, e.g. no {@link Component} is installed.
     */
    ComponentInstanceDescriptor createComponentInstance(User user, ComponentDescription description, String name,
        ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException;

    /**
     * Removes an instantiated {@link Component} from the {@link ComponentRegistry} on the given
     * platform and unregister it from the OSGi service registry. The instance is not accessible
     * from then on.
     * 
     * @param user The user's {@link User} to use.
     * @param identifier The identifier of the {@link Component} instance which is encapsulated in
     *        the {@link ComponentInstanceDescriptor}.
     * @param platformId The {@link PlatformIdentifier} of the platform to create the component
     *        instance.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    void disposeComponentInstance(User user, String identifier, PlatformIdentifier platformId)
        throws AuthorizationException;

    /**
     * Returns a {@link ComponentDescription} that is identified by the given identifier.
     * 
     * @param user The user's {@link User} to use.
     * @param identifier The identifier of the {@link ComponentDescription} to get.
     * @param platformId The {@link PlatformIdentifier} of the platform to get the {@link ComponentInstanceDescriptor}.
     * @return the description corresponding to the string identifier; <code>null</code> if no description is
     *         found.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    ComponentInstanceDescriptor getComponentInstanceDescriptor(User user, String identifier,
        PlatformIdentifier platformId) throws AuthorizationException;
}
