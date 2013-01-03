/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.Set;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * A registry handling {@link Component}s regarding to {@link ComponentDescription}s and
 * {@link ComponentInstanceInformation}s.
 * 
 * @author Roland Gude
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 */
public interface ComponentRegistry {

    /**
     * Returns all available {@link ComponentDescription}s.
     * 
     * @param user The user's {@link User} to use.
     * @return all {@link ComponentDescription}s accessible by the given user
     */
    Set<ComponentDescription> getComponentDescriptions(User user);
    
    /**
     * Returns all available {@link ComponentDescription}s.
     * 
     * @param user The user's {@link User} to use.
     * @param requestingPlatform the requesting platform
     * @return all {@link ComponentDescription}s accessible by the given user and platform
     */
    Set<ComponentDescription> getComponentDescriptions(User user, PlatformIdentifier requestingPlatform);

    /**
     * Returns a {@link ComponentDescription} that is identified by the given identifier.
     * 
     * @param user The user's {@link User} to use.
     * @param identifier The identifier of the {@link ComponentDescription} to get.
     * @return the description corresponding to the string identifier; <code>null</code> if no description is
     *         found.
     */
    ComponentDescription getComponentDescription(User user, String identifier);

    /**
     * Creates a new component instance described by the given {@link ComponentDescription}.
     * 
     * @param user The user's {@link User} to use.
     * @param description The {@link ComponentDescription} to use.
     * @param context The {@link ComponentContext} of the component to create.
     * @param name The desired name of the instance to create.
     * @param inputConnected <code>true</code> if at least one {@link Input} of this
     *        {@link Component} to create is connected with at least one {@link Output}.
     * @return {@link ComponentInstanceDescriptor} representing the created instance
     * @throws ComponentException if initialization of the {@link Component} failed, e.g. no {@link Component} is installed.
     */
    ComponentInstanceDescriptor createComponentInstance(User user, ComponentDescription description,
        ComponentContext context, String name, Boolean inputConnected) throws ComponentException;

    /**
     * Removes an instantiated {@link Component} from this {@link ComponentRegistry} and unregister it from the OSGi service
     * registry. The instance is not accessible from then on.
     * 
     * @param user The user's {@link User} to use.
     * @param identifier The identifier of the {@link Component} instance which is encapsulated in
     *        the {@link ComponentInstanceDescriptor}.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    void disposeComponentInstance(User user, String identifier) throws AuthorizationException;

    /**
     * Returns the {@link ComponentInstanceDescriptor} that is identified by the given instance identifier.
     * 
     * @param user The user's {@link User} to use.
     * @param identifier The identifier of the {@link Component} instance which is encapsulated in
     *        the {@link ComponentInstanceDescriptor}.
     * @return the {@link ComponentInstanceDescriptor} or <code>null</code> if there is no matching one found.
     */
    ComponentInstanceDescriptor getComponentInstanceDescriptor(User user, String identifier);

    /**
     * Checks if an instance was created by a given user.
     * 
     * @param identifier The identifier of the {@link Component} instance which is encapsulated in
     *        the {@link ComponentInstanceDescriptor}.
     * @param user The user's {@link User} to use.
     * @return <code>true</code> if it is the creator, else <code>false</code>.
     */
    boolean isCreator(String identifier, User user);
}
