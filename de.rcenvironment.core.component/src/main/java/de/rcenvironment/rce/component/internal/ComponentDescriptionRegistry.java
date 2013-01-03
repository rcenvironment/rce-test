/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.ComponentFactory;

import de.rcenvironment.rce.component.ComponentDescription;

/**
 * Class holding information about all local {@link Component}s.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class ComponentDescriptionRegistry {

    /**
     * Map holding the known {@link ComponentDescription} objects.
     */
    private static Map<String, ComponentDescription> componentDescriptions = new HashMap<String, ComponentDescription>();

    /**
     * Map holding the known {@link ComponentFactory} objects.
     */
    private static Map<String, ComponentFactory> componentFactories = new ConcurrentHashMap<String, ComponentFactory>();

    /**
     * Adds a component to the local registry.
     * 
     * @param description The ComponentDescription.
     * @param factory The ComponentFactory.
     */
    public void addComponent(ComponentDescription description, ComponentFactory factory) {
        final String identifier = description.getIdentifier();
        componentDescriptions.put(identifier, description);
        componentFactories.put(identifier, factory);
    }
    
    /**
     * Returns a collection of components available locally.
     * 
     * @return collection of components.
     */
    public Set<ComponentDescription> getComponentDescriptions() {
        Set<ComponentDescription> descriptions = new HashSet<ComponentDescription>();

        for (ComponentDescription cd : componentDescriptions.values()) {
            descriptions.add(cd.clone());
        }
        return descriptions;
    }

    /**
     * Returns a ComponentDescription from the local registry.
     * 
     * @param componentIdentifier Identifier of the component.
     * @return The ComponentDescription or null.
     */
    public ComponentDescription getComponentDescription(String componentIdentifier) {
        ComponentDescription desc = componentDescriptions.get(componentIdentifier);
        if (desc != null) {
            desc = desc.clone();
        }
        return desc;
    }

    /**
     * Returns a ComponentFactory from the local registry.
     * 
     * @param componentIdentifier Identifier of the component.
     * @return The ComponentFactory or null.
     */
    public ComponentFactory getComponentFactory(String componentIdentifier) {
        return componentFactories.get(componentIdentifier);
    }

    /**
     * Removes all Components from the registry.
     */
    public void removeAllComponents() {
        componentDescriptions.clear();
        componentFactories.clear();
    }

    /**
     * Removes a Component from the registry.
     * 
     * @param factory Factory of the component.
     * @return The name of the removed component.
     */
    public String removeComponent(ComponentFactory factory) {
        String name = "";
        for (Entry<String, ComponentFactory> entry : componentFactories.entrySet()) {
            if (entry.getValue() == factory) {
                name = componentDescriptions.get(entry.getKey()).getIdentifier();
                componentDescriptions.remove(entry.getKey());
                componentFactories.remove(entry.getKey());
            }
        }
        return name;
    }
}
