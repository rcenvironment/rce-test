/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * An extension interface for builders that generate the GUI subtrees of history objects.
 * 
 * @author Robert Mischke
 */
public interface HistoryObjectSubtreeBuilder {

    /**
     * @return the canonical class names (as returned by {@link Class#getCanonicalName()}) of all
     *         history object classes this builder implementation can handle.
     */
    String[] getSupportedObjectClassNames();

    /**
     * This interface method delegates object deserialization back to the builder implementation.
     * This causes deserialization to take place within the classloader context of the builder
     * implementation, which has access to the history object class definitions. Without this, the
     * calling UI bundle would require a "Dynamic-PackageImport: *", which is undesirable.
     * 
     * @param ois the {@link ObjectInputStream} to read from
     * @return the deserialized history object
     * @throws IOException if stream reading fails
     * @throws ClassNotFoundException if the read object could not be instantiated
     */
    Serializable deserializeHistoryObject(ObjectInputStream ois) throws IOException, ClassNotFoundException;

    /**
     * Generate the initial subtree elements for the given history object. Currently, no incremental
     * building is supported, so this method must create the whole subtree at once; this may change
     * in the future.
     * 
     * @param historyObject the history object
     * @param parent the parent node to construct the subtree under
     */
    void buildInitialHistoryObjectSubtree(Serializable historyObject, DMBrowserNode parent);

}
