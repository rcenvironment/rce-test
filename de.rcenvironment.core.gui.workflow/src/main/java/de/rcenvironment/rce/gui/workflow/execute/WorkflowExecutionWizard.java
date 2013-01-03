/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.SimpleComponentRegistry;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowDescriptionPersistenceHandler;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionConfigurationHelper;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.view.OpenReadOnlyWorkflowRunEditorAction;
import de.rcenvironment.rce.gui.workflow.view.console.ConsoleModel;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowInformationChangeListener;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * {@link Wizard} to start the execution of a {@link Workflow}.
 * 
 * @author Christian Weiss
 */
public class WorkflowExecutionWizard extends Wizard {

    private static final String UNDERSCORE = "_";

    private static final Log LOGGER = LogFactory.getLog(WorkflowExecutionWizard.class);

    private static final String DE_RCENVIRONMENT_RCE_GUI_WORKFLOW = "de.rcenvironment.rce.gui.workflow";

    private final IFile workflowFile;

    private final WorkflowDescription backingWorkflowDescription;

    private final WorkflowDescription workflowDescriptionforWorkflowPage;

    private final WorkflowExecutionConfigurationHelper executionHelper;

    private WorkflowPage workflowPage;

    private PlaceholderPage placeholderPage;
    
    private PlatformIdentifier localPlatform;


    public WorkflowExecutionWizard(final IFile workflowFile) {
        this.workflowFile = workflowFile;
        // retrieve the services needed for the WorkflowLaunchConfigurationHelper
        final SimpleComponentRegistry scr = new SimpleComponentRegistry(Activator.getInstance().getUser());
        final SimpleCommunicationService scs = new SimpleCommunicationService();
        final SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        // instantiate the WorkflowLaunchConfigurationHelper
        executionHelper = new WorkflowExecutionConfigurationHelper(scr, scs, swr);
        // cache the local platform for later use
        this.localPlatform = executionHelper.getLocalPlatform();
        // load the WorflowDescription from the provided IFile
        backingWorkflowDescription = executionHelper.loadWorkflow(workflowFile);
        // cancel and display an error in case no workflow was selected
        if (backingWorkflowDescription == null) {
            final Status status = new Status(IStatus.ERROR, DE_RCENVIRONMENT_RCE_GUI_WORKFLOW, Messages.illegalExecutionSelectionMessage);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.illegalExecutionSelectionTitle,
                Messages.illegalExecutionSelectionMessage, status);
            throw new IllegalArgumentException(Messages.bind("File %s does not contain a valid workflow description.", workflowFile
                .getFullPath().toString()));
        }
        // clone the WorkflowDescription to operate on a copy instead of the real instance to avoid
        // unclean changes in case the user chooses to 'cancel' the process
        this.workflowDescriptionforWorkflowPage = backingWorkflowDescription.clone(Activator.getInstance().getUser());

        // determine the clean "root" name for the workflow
        String workflowRootName;
        if (workflowDescriptionforWorkflowPage.getName() == null || workflowDescriptionforWorkflowPage.getName().isEmpty()) {
            // if no previous name was stored, use the name of workflow file without ".wf" extension
            workflowRootName = workflowFile.getName();
            if (workflowRootName.toLowerCase().endsWith(".wf")) {
                workflowRootName = workflowRootName.substring(0, workflowRootName.length() - 3);
            }
        } else {
            // if a previous name was stored, clean it of any previous timestamp
            workflowRootName = workflowDescriptionforWorkflowPage.getName();
            workflowRootName = workflowRootName.replaceFirst("^(.*)_\\d+-\\d+-\\d+_\\d+:\\d+:\\d+$", "$1");
        }

        // set root name plus appended timestamp as new name
        workflowDescriptionforWorkflowPage.setName(workflowRootName + UNDERSCORE + generateTimestampString());

        // set the title of the wizard dialog
        setWindowTitle(Messages.workflowExecutionWizardTitle);
        // display a progress monitor
        setNeedsProgressMonitor(true);
    }

    private String generateTimestampString() {
        // format: full date and time, connected with underscore
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return dateFormat.format(new Date());
    }

    protected WorkflowDescription getWorkflowDescription() {
        return workflowDescriptionforWorkflowPage;
    }

    protected WorkflowExecutionConfigurationHelper getHelper() {
        return executionHelper;
    }

    @Override
    public void addPages() {
        workflowPage = new WorkflowPage(this);
        addPage(workflowPage);
        placeholderPage = new PlaceholderPage(this);
        addPage(placeholderPage);
    }

    @Override
    public boolean canFinish() {
        // cannot completr the wizard from the first page
        if (this.getContainer().getCurrentPage() == placeholderPage
            || placeholderPage.getComponentPlaceholderTree().getItemCount() == 0) {
            return true;
        }
        return false;
    }



    @Override
    public boolean performFinish() {
        placeholderPage.performFinish(backingWorkflowDescription); // do everything neccessary with placeholders
        Job job = new Job(Messages.workflowExecutionWizardTitle) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    monitor.beginTask(Messages.setupWorkflow, 9);
                    IStatus status;
                    if (performWorkflowExection(monitor)) {
                        status = Status.OK_STATUS;                    
                    } else {
                        status = Status.CANCEL_STATUS;
                    }
                    monitor.worked(9);
                    return status;                    
                } finally {
                    monitor.done();
                }
            };
        };
        job.setUser(true);
        job.schedule();

        return true;
    }

    private boolean performWorkflowExection(IProgressMonitor monitor) {
        if (executionHelper.isValid(workflowDescriptionforWorkflowPage)) {
            monitor.worked(4);
            backingWorkflowDescription.setName(workflowDescriptionforWorkflowPage.getName());
            backingWorkflowDescription.setTargetPlatform(workflowDescriptionforWorkflowPage.getTargetPlatform());

            for (WorkflowNode node : workflowDescriptionforWorkflowPage.getWorkflowNodes()) {
                WorkflowNode backingNode = backingWorkflowDescription.getWorkflowNode(node.getIdentifier());
                PlatformIdentifier platformIdentifier = node.getComponentDescription().getPlatform();
                backingNode.getComponentDescription().setPlatform(platformIdentifier);
            }
            monitor.worked(5);
            backingWorkflowDescription.setAdditionalInformation(workflowDescriptionforWorkflowPage.getAdditionalInformation());
        } else {
            final Status status = new Status(IStatus.ERROR, DE_RCENVIRONMENT_RCE_GUI_WORKFLOW, Messages.illegalConfigMessage);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), Messages.illegalConfigTitle,
                Messages.illegalConfigMessage, status);

            return false;
        }


        boolean executionSuccessful = false;
        try {
            // launch first and save only in case no exceptions occurred
            executeWorkflow();
            executionSuccessful = true;
            monitor.worked(7);
            saveWorkflow();
            monitor.worked(8);
        } catch (RuntimeException e) {
            String message;
            if (!executionSuccessful) {
                message = Messages.workflowLaunchFailed;
            } else {
                message = Messages.workflowSaveFailed;
            }
            LOGGER.error(message, e);
            Status status = new Status(Status.ERROR, DE_RCENVIRONMENT_RCE_GUI_WORKFLOW, e.getLocalizedMessage());
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", message, status);
            return false;
        }
        return true;
    }

    private void saveWorkflow() {
        WorkflowDescriptionPersistenceHandler persistenceHandler = new WorkflowDescriptionPersistenceHandler();
        try {
            ByteArrayOutputStream content = persistenceHandler.writeWorkflowDescriptionToStream(backingWorkflowDescription);
            ByteArrayInputStream input = new ByteArrayInputStream(content.toByteArray());
            workflowFile.setContents(input, // the file content
                true, // keep saving, even if IFile is out of sync with the Workspace
                false, // dont keep history
                null); // progress monitor
        } catch (CoreException e) {
            LOGGER.error(e.getStackTrace());
            throw new RuntimeException("Failed to persist workflow description:", e);
        } catch (IOException e) {
            LOGGER.error(e.getStackTrace());
            throw new RuntimeException("Failed to persist workflow description:", e);
        }
    }

    private void executeWorkflow() {
        WorkflowDescription runtimeWorkflowDescription = backingWorkflowDescription.clone(Activator.getInstance().getUser());
        for (WorkflowNode node : runtimeWorkflowDescription.getWorkflowNodes()) {
            // replace null (representing localhost) with the actual host name
            if (node.getComponentDescription().getPlatform() == null) {
                node.getComponentDescription().setPlatform(localPlatform);
            }
        }

        if (runtimeWorkflowDescription.getTargetPlatform() ==  null) {
            runtimeWorkflowDescription.setTargetPlatform(localPlatform);
        }

        String name = runtimeWorkflowDescription.getName();
        if (name == null) {
            name = Messages.bind(Messages.defaultWorkflowName, workflowFile.getName().toString());
        }
        final SimpleWorkflowRegistry workflowRegistry = executionHelper.getSimpleWorkflowRegistry();
        final WorkflowInformation wi = workflowRegistry.createWorkflowInstance(
            runtimeWorkflowDescription, name, new HashMap<String, Object>());
        if (wi == null) {
            RuntimeException e = new RuntimeException("workflow instance could not be created");
            LOGGER.error("Failed to launch workflow:", e);
            throw e;
        }

        if (wi.getWorkflowDescription().getTargetPlatform() != null
            && !wi.getWorkflowDescription().getTargetPlatform().equals(localPlatform)) {
            new SimpleNotificationService().subscribe(WorkflowConstants.STATE_NOTIFICATION_ID + wi.getIdentifier(),
                WorkflowInformationChangeListener.getInstance(), wi.getWorkflowDescription().getTargetPlatform());            
        }

        // before starting the workflow, ensure that the console model is initialized
        // so that no console output gets lost; this is lazily initialized here
        // so the application startup is not slowed down
        ConsoleModel.ensureConsoleCaptureIsInitialized();
        workflowRegistry.startWorkflow(wi);

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                new OpenReadOnlyWorkflowRunEditorAction(wi).run();
            }

        });

    }
}
