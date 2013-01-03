/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend;

import java.net.URI;
import java.util.UUID;

/**
 * Interface for the data management data backend.
 * 
 * @author Sandra Schroedter
 * @author Juergen Klein
 */
public interface DataBackend {
    
    /**
     * Key for a service property.
     */
    String PROVIDER = "de.rcenvironment.rce.datamanagement.backend.data.provider";
    
    /**
     * Key for a service property.
     */
    String SCHEME = "de.rcenvironment.rce.datamanagement.backend.data.scheme";
    
    /**
     * Suggest location according to the given {@link DataReference} identifier and the given revision number.
     * 
     * @param dataReferenceId
     *            Identifier of {@link DataReference} for which a location is suggested.
     * @param revision
     *            Revision number of {@link DataReference} for which a location is suggested.
     * @return suggested location.
     */
    URI suggestLocation(UUID dataReferenceId, int revision);

    /**
     * Stores the given {@link Object} at the given location.
     * 
     * @param location
     *            Location where object is stored.
     * @param object
     *            Object to store.
     * @return size of the stored object.
     */
    long put(URI location, Object object);

    /**
     * Deletes the object at the given location.
     * 
     * @param location
     *            Location of object to delete.
     * @return <code>true</code> object is deleted, otherwise <code>false</code>.
     */
    boolean delete(URI location);

    /**
     * Returns the object saved at the given location.
     * 
     * @param location
     *            Location if object to return.
     * @return object at location.
     */
    Object get(URI location);
}
