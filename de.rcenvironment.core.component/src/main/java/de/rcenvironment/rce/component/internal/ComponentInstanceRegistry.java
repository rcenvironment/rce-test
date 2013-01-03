/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.ComponentInstance;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;

/**
 * Class holding information about all local instantiated components.
 *
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class ComponentInstanceRegistry {

    private static Map<String, ComponentInstanceDescriptor> compInstanceDescs =
        new ConcurrentHashMap<String, ComponentInstanceDescriptor>();
    
    private static Map<String, ComponentInstance> compInstances = new ConcurrentHashMap<String, ComponentInstance>();
    
    private static Map<String, User> usedProxyCertificates = new ConcurrentHashMap<String, User>();
    
    /**
     * Constructor to change visibility to this package.
     */
    protected ComponentInstanceRegistry() { }
    
    /**
     * Adds a new {@link ComponentInstance} to this {@link ComponentInstanceRegistry}.
     * 
     * @param instance The {@link ComponentInstance}.
     * @param desc The associated {@link ComponentInstanceDescriptor}.
     * @param proxyCertificate The {@link User} this {@link Component} was started with.
     */
    public void addCompControllerInstance(ComponentInstance instance, ComponentInstanceDescriptor desc, User proxyCertificate) {
        final String identifier = desc.getIdentifier();
        compInstances.put(identifier, instance);
        compInstanceDescs.put(identifier, desc);
        usedProxyCertificates.put(identifier, proxyCertificate);
    }

    /**
     * Returns the {@link ComponentInstance} of a given componentInstanceIdentifier.
     * 
     * @param componentInstanceIdentifier Instance identifier of the {@link Component}.
     * @return The {@link ComponentInstance} or null.
     */
    public ComponentInstance getComponentInstance(String componentInstanceIdentifier) {
        return compInstances.get(componentInstanceIdentifier);
    }

    /**
     * Returns the {@link ComponentInstanceDescriptor} of a given componentInstanceIdentifier.
     * 
     * @param componentInstanceIdentifier Instance identifier of the {@link Component}.
     * @return The {@link ComponentInstanceDescriptor} or null.
     */
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(String componentInstanceIdentifier) {
        return compInstanceDescs.get(componentInstanceIdentifier);
    }
    
    /**
     * Returns all {@link ComponentInstanceDescriptor}s.
     * 
     * @return The {@link ComponentInstanceDescriptor} or null.
     */
    public Collection<ComponentInstanceDescriptor> getAllComponentInstanceDescriptors() {
        return Collections.unmodifiableCollection(compInstanceDescs.values());
    }
    
    /**
     * Removes a {@link ComponentInstance} from the table.
     * 
     * @param componentInstanceIdentifier Instance identifier of the {@link Component}.
     */
    public void removeComponentInstance(String componentInstanceIdentifier) {
        compInstances.remove(componentInstanceIdentifier);
        compInstanceDescs.remove(componentInstanceIdentifier);
        usedProxyCertificates.remove(componentInstanceIdentifier);
    }
    
    /**
     * Checks if a component instance was created by the given {@link User}s owner.
     * 
     * @param componentInstanceIdentifier The componentInstanceIdentifier.
     * @param certificate The ProxyCertificate.
     * @return True or false.
     */
    public boolean isCreator(String componentInstanceIdentifier, User certificate) {
        User creatorsCert = usedProxyCertificates.get(componentInstanceIdentifier);
        if (creatorsCert != null) {
            return creatorsCert.equals(certificate);            
        } else {
            return false;
        }
    }

}
