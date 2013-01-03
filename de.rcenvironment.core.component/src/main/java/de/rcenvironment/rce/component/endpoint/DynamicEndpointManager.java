/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the management of dynamic additions to the inputs or outputs of a component. Besides
 * preventing collisions with existing "static" entries, this class also defines valid dynamic
 * names.
 * 
 * <p>
 * Rationale for making this a separate class:
 * <li>avoids code duplication between inputs and outputs</li>
 * 
 * @author Robert Mischke
 * @author Christian Weiss
 */
public class DynamicEndpointManager implements Serializable {

    private static final long serialVersionUID = -8460550655735697403L;

    /** The "declarative" entries; used to prevent collisions. */
    private Map<String, Class<? extends Serializable>> declarativeEntries;
    
    /** Dynamic outputs that can be added or removed at configuration time (e.g. from the workflow editor). */
    private final Map<String, Class<? extends Serializable>> dynamicEntries;

    public DynamicEndpointManager(Map<String, Class<? extends Serializable>> newDeclarativeEntries) {
        declarativeEntries = newDeclarativeEntries;
        dynamicEntries = new HashMap<String, Class<? extends Serializable>>();
    }

    /**
     * Checks whether the given name could be used for a new dynamic endpoint. Also checks for
     * collisions with parent entries.
     * 
     * @param name The name of an endpoint to validate.
     * 
     * @return true if this name could be used for a new dynamic endpoint
     */
    public boolean validateNewName(String name) {
        return !name.isEmpty()
            && !dynamicEntries.containsKey(name)
            && !declarativeEntries.containsKey(name);
    }

    /**
     * Checks whether a given fully qualified type name is valid as an endpoint type.
     * 
     * @param type the fully qualified type name
     * @return true, if the given type is valid
     */
    public boolean validateTypeName(String type) {
        if (type == null) {
            return false;
        }
        
        // class must be a java standard type
//        if (type.startsWith("java.")) {
        try {
            // class must exist
            Class<?> clazz = Class.forName(type);
            // class must implement Serializable
            return Serializable.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            e = null;
        }
//        }
        return false;
    }

    /**
     * Adds a new dynamic endpoint.
     * 
     * @param name The name of the endpoint.
     * @param type The type (class) of the endpoint.
     * 
     * @throws IllegalArgumentException if the given name collides with an existing input/output.
     */
    @SuppressWarnings("unchecked")
    public void addEndpoint(String name, String type) throws IllegalArgumentException {

        if (!validateNewName(name) || !validateTypeName(type)) {
            throw new IllegalArgumentException();
        }
        
        Class<? extends Serializable> clazz;
        try {
            clazz = (Class<? extends Serializable>) Class.forName(type);
        } catch (ClassNotFoundException e) { // actually not reachable because validate method is already loading the class
            throw new IllegalArgumentException(e);
        }

        dynamicEntries.put(name, clazz);
    }

    /**
     * Removes a dynamic endpoint.
     * 
     * @param name The name of the endpoint to remove.
     */
    public void removeEndpoint(String name) {
        dynamicEntries.remove(name);
    }
    /**
     * Removes all dynamic endpoints.
     * 
     */
    public void removeAllEndpoints() {
        dynamicEntries.clear();
    }
    /**
     * Changes the definition of an existing dynamic endpoint.
     * 
     * @param name The name of the output to change.
     * @param newName The new name of the output.
     * @param newType The new type of the output.
     */
    @SuppressWarnings("unchecked")
    public void changeEndpoint(String name, String newName, String newType) {

        if (!name.equals(newName) && !validateNewName(newName)) {
            throw new IllegalArgumentException();
        }
        if (!validateTypeName(newType)) {
            throw new IllegalArgumentException();
        }

        Class<? extends Serializable> clazz;
        try {
            clazz = (Class<? extends Serializable>) Class.forName(newType);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        dynamicEntries.remove(name);
        dynamicEntries.put(newName, clazz);
    }

    /**
     * @param name The name of the endpoint.
     * @return the fully qualified type name for the given endpoint.
     */
    public String getEndpointType(String name) {
        String type = null;
        Class<? extends Serializable> clazz = dynamicEntries.get(name);
        if (clazz != null) {
            type = clazz.getName();
        }
        return type;
    }

    /**
     * @return all endpoints represented by its name.
     */
    public List<String> getEndpointNames() {
        List<String> result = new ArrayList<String>(dynamicEntries.keySet());
        Collections.sort(result);
        return result;
    }

    /**
     * @return all registered dynamic entries
     */
    public Map<String, Class<? extends Serializable>> getEndpointDefinitions() {
        return Collections.unmodifiableMap(dynamicEntries);
    }

}
