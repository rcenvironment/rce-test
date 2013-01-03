/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.SimpleComponentRegistry;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionConfigurationHelper;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowEditor;

/**
 * Opens the {@link WorkflowExecutionWizard}.
 * 
 * @author Christian Weiss
 */
public class ShowWorkflowExecutionWizardHandler extends AbstractHandler {

    private static final String DE_RCENVIRONMENT_RCE_GUI_WORKFLOW = "de.rcenvironment.rce.gui.workflow";
    
    private static final Pattern WORKFLOW_FILENAME_PATTERN = Pattern.compile("^.*\\.wf$");

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        // retrieve the services needed for the WorkflowLaunchConfigurationHelper
        final SimpleComponentRegistry scr = new SimpleComponentRegistry(Activator.getInstance().getUser());
        final SimpleCommunicationService scs = new SimpleCommunicationService();
        final SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        // instantiate the WorkflowLaunchConfigurationHelper
        final WorkflowExecutionConfigurationHelper helper = new WorkflowExecutionConfigurationHelper(scr, scs, swr);
        // retrieve the WorkflowDescription
        IFile workflowFile;
        workflowFile = getFirstSelectedWorkflowFile(event, helper);
        if (workflowFile == null) {
            workflowFile = getDisplayedWorkflowFile(event, helper);
        }
        if (workflowFile != null) {
            final WorkflowDescription workflowDescription = helper.loadWorkflow(workflowFile);
            if (workflowDescription != null) {
                // instantiate the WorkflowExecutionWizard with the WorkflowDescription
                final Wizard workflowExecutionWizard = new WorkflowExecutionWizard(workflowFile);
                final WorkflowWizardDialog wizardDialog =
                    new WorkflowWizardDialog(HandlerUtil.getActiveShell(event), workflowExecutionWizard);
                wizardDialog.setBlockOnOpen(false);
                wizardDialog.open();
                return null;
            }
        }
        // cancel and display an error in case no workflow was selected
        final Status status = new Status(IStatus.ERROR, DE_RCENVIRONMENT_RCE_GUI_WORKFLOW, Messages.illegalExecutionSelectionMessage);
        ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.illegalExecutionSelectionTitle,
            Messages.illegalExecutionSelectionMessage, status);
        return null;
    }
    
    private IFile getDisplayedWorkflowFile(final ExecutionEvent event, final WorkflowExecutionConfigurationHelper helper) {
        final IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (part instanceof IEditorPart) {
            final IEditorPart editor = (IEditorPart) part;
            if (editor.isDirty()) {
                final boolean save = MessageDialog.openQuestion(part.getSite().getShell(), Messages.askToSaveUnsavedEditorChangesTitle,
                    Messages.askToSaveUnsavedEditorChangesMessage);
                if (save) {
                    editor.doSave(null);
                }
            }
            if (editor instanceof WorkflowEditor) {
                WorkflowEditor workflowEditor = (WorkflowEditor) editor;
                IEditorInput input = workflowEditor.getEditorInput();
                if (input instanceof FileEditorInput) {
                    return ((FileEditorInput) input).getFile();
                }
            }
        }
        return null;
    }

    private IFile getFirstSelectedWorkflowFile(final ExecutionEvent event, final WorkflowExecutionConfigurationHelper helper) {
        final ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            for (Iterator<?> iter = structuredSelection.iterator(); iter.hasNext();) {
                Object next = iter.next();
                if (!(next instanceof IFile)) {
                    continue;
                }
                final IFile file = (IFile) next;
                String filename = file.getName();
                if (!WORKFLOW_FILENAME_PATTERN.matcher(filename).matches()) {
                    continue;
                }
                return file;
            }
        }
        return null;
    }

    /**
     * {@link WizardDialog} sub type to be adapted to the needs of a workflow execution.
     * 
     * @author Christian Weiss
     */
    private static final class WorkflowWizardDialog extends WizardDialog {

        /**
         * The Constructor.
         * 
         * @param activeShell the parent shell
         * @param workflowExecutionWizard the wizard this dialog is working on
         */
        public WorkflowWizardDialog(Shell activeShell, Wizard workflowExecutionWizard) {
            super(activeShell, workflowExecutionWizard);
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.eclipse.jface.wizard.WizardDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            Button okButton = getButton(IDialogConstants.FINISH_ID);
            if (okButton != null) {
                okButton.setText(Messages.executionWizardFinishButtonLabel);
            }
        }

    }

}
