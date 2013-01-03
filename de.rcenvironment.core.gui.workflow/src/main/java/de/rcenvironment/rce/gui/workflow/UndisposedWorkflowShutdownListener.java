/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.gui.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;

import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;

/**
 * Prevents the Workbench to be closed as long as {@link Workflow} instances are in {@link State}
 * {@link State#RUNNING} or {@link State#READY} and informs about the disposal of Workflow results
 * if there are undisposed Workflows.
 * 
 * @author Christian Weiss¸
 */
final class UndisposedWorkflowShutdownListener implements IWorkbenchListener {

    private static final String WORKFLOW_HANDLE_ERROR = "Running workflows can not be handled during shutdown";
   
    private static final Log LOGGER = LogFactory.getLog(UndisposedWorkflowShutdownListener.class);
    
    /**
     * <p>
     * Prohibits shutdown in case active workflows exist in the {@link SimpleWorkflowRegistry} and
     * displays an according message.
     * </p>
     * <p>
     * In case undisposed workflows exist the user is presented a dialog to confirm the disposal.
     * </p>
     * 
     * @see org.eclipse.ui.IWorkbenchListener#preShutdown(org.eclipse.ui.IWorkbench, boolean)
     */
    @Override
    public boolean preShutdown(final IWorkbench workbench, final boolean forced) {

        boolean shutdown = true;

        try {
            final SimpleWorkflowRegistry workflowRegistry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());

            if (!forced && workflowRegistry.hasActiveWorkflows()) {
                shutdown = MessageDialog.openQuestion(workbench.getActiveWorkbenchWindow().getShell(), Messages.activeWorkflowsTitle,
                    Messages.activeWorkflowsMessage);
            }
        } catch (IllegalStateException e) {
            LOGGER.error(WORKFLOW_HANDLE_ERROR, e);
        }
        return shutdown;
    }

    /**
     * Disposes all workflows in the {@link SimpleWorkflowRegistry}.
     * 
     * @see org.eclipse.ui.IWorkbenchListener#postShutdown(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void postShutdown(final IWorkbench workbench) {
        try {
            final SimpleWorkflowRegistry workflowRegistry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
            workflowRegistry.cancelActiveWorkflows();
            workflowRegistry.disposeWorkflows();
        } catch (IllegalStateException e) {
            LOGGER.error(WORKFLOW_HANDLE_ERROR, e);
        }
    }

}
