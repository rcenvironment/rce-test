/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.view;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The {@link BundleActivator} of this bundle. Responsibilities:
 * <ul>
 * <li>govern the resources used by this bundle</li>
 * </ul>
 * 
 * @author Christian Weiss
 */
public class Activator implements BundleActivator {

       /** The {@link BundleContext}. */
    private static BundleContext bundleContext;

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        Activator.bundleContext = context;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
      
    }

    /**
     * Returns the {@link BundleContext} of this bundle.
     * 
     * @return the {@link BundleContext}
     */
    public static BundleContext getContext() {
        return bundleContext;
    }




}
