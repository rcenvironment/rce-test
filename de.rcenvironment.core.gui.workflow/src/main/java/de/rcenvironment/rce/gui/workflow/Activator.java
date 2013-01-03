/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.Session;
import de.rcenvironment.rce.authentication.User;

/**
 * 
 * Activates the Bundle.
 * <ul>
 * <li>Registers an IWorkbenchListener to display a confirm dialog upon shutdown in case undisposed
 * workflows exist</li>
 * </ul>
 * 
 * @author Christian Weiss
 */
public class Activator extends AbstractUIPlugin {

    private static Activator instance = null;
    
    private UndisposedWorkflowShutdownListener undisposedWorkflowShutdownListener = new UndisposedWorkflowShutdownListener();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = this;
        
        // register the workbench listener
        try {
            PlatformUI.getWorkbench().addWorkbenchListener(undisposedWorkflowShutdownListener);
        } catch (IllegalStateException e) {
            // nothing to do here. if there is no workbench, there is no need for an listener
            undisposedWorkflowShutdownListener = null;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        instance = null;
        
        // remove the workbench listener registered in start(BundleContext)
        if (undisposedWorkflowShutdownListener != null) {
            try {
                PlatformUI.getWorkbench().removeWorkbenchListener(undisposedWorkflowShutdownListener);
            } catch (IllegalStateException e) {
                // nothing to do here. if there is no workbench, there is no listener
                undisposedWorkflowShutdownListener = null;
            }
        }
    }

    public static Activator getInstance() {
        return instance;
    }
    
    /**
     * Returns the currently logged in user.
     * @return logged in user
     * @exception IllegalStateException if no user is logged in
     */
    public User getUser() throws IllegalStateException {
        try {
            return Session.getInstance().getUser();
        } catch (AuthenticationException e) {
            throw new IllegalStateException("no user is logged in", e);
        }
    }
}
