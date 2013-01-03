/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.gui.communication.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.rcenvironment.core.communication.model.NetworkStateModel;

/**
 * The network view content provider.
 * 
 * @author Robert Mischke
 */
public class NetworkViewContentProvider implements IStructuredContentProvider,
    ITreeContentProvider {

    /**
     * The object representing the network root; added to avoid creating a class without an actual
     * purpose.
     */
    public static final Object NETWORK_ROOT_NODE = new Object();

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private NetworkStateModel model;

    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        model = (NetworkStateModel) newInput;
    }

    @Override
    public void dispose() {}

    @Override
    public Object[] getElements(Object parent) {
        return new Object[] { NETWORK_ROOT_NODE };
    }

    @Override
    public Object[] getChildren(Object parent) {
        if (parent == NETWORK_ROOT_NODE) {
            synchronized (model) {
                return model.getNodes().toArray();
            }
        }
        return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object child) {
        return null;
    }

    @Override
    public boolean hasChildren(Object parent) {
        if (parent == NETWORK_ROOT_NODE) {
            return true;
        }
        return false;
    }

}
