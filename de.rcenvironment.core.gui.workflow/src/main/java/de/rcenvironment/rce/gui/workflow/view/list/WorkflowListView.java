/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.list;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.core.utils.common.concurrent.AsyncExceptionListener;
import de.rcenvironment.core.utils.common.concurrent.CallablesGroup;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.parts.ReadonlyWorkflowNodePart.ComponentStateFigureImpl;
import de.rcenvironment.rce.gui.workflow.view.OpenReadOnlyWorkflowRunEditorAction;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * This view shows all running workflows.
 * 
 * @author Heinrich Wendel
 */
public class WorkflowListView extends ViewPart implements NetworkTopologyChangeListener {

    private static WorkflowListView instance;

    private static Set<String> subscribedRemoteWIIds;

    private final Runnable refreshRunnable = new Runnable() {

        public void run() {
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    refresh();
                }

            });
        }

    };

    private SimpleWorkflowRegistry swr;

    private SimpleNotificationService sns;

    private TableViewer viewer;

    private Table table;

    private WorkflowInformationColumnSorter columnSorter;

    private Action pauseAction;

    private Action resumeAction;

    private Action cancelAction;

    private Action disposeAction;

    private ServiceRegistration topologyListenerRegistration;

    private Display display;

    public WorkflowListView() {
        WorkflowListView.instance = this;
    }

    /**
     * Registers an event listener for network changes as an OSGi service (whiteboard pattern).
     * 
     * @param display
     */
    private void registerTopologyChangeListener() {
        BundleContext bundleContext = Activator.getInstance().getBundle().getBundleContext();
        topologyListenerRegistration =
            bundleContext.registerService(NetworkTopologyChangeListener.class.getName(), this, null);
    }

    @Override
    public void createPartControl(Composite parent) {

        display = parent.getShell().getDisplay();

        viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        table = viewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        columnSorter = new WorkflowInformationColumnSorter();
        viewer.setSorter(columnSorter);

        String[] titles = {
            Messages.name, Messages.status, Messages.platform, Messages.user, Messages.time, Messages.additionalInformation };
        final int width = 150;

        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
            final TableColumn column = viewerColumn.getColumn();
            column.setText(titles[i]);
            column.setWidth(width);
            column.setResizable(true);
            column.setMoveable(true);
            column.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    columnSorter.setColumn(index);
                    int direction = viewer.getTable().getSortDirection();

                    if (viewer.getTable().getSortColumn() == column) {
                        if (direction == SWT.UP) {
                            direction = SWT.DOWN;
                        } else {
                            direction = SWT.UP;
                        }
                    } else {
                        direction = SWT.UP;
                    }
                    viewer.getTable().setSortDirection(direction);
                    viewer.getTable().setSortColumn(column);

                    viewer.refresh();
                }
            });
        }

        // add toolbar actions (right top of view)
        for (Action action : createToolbarActions()) {
            action.setEnabled(false);
            getViewSite().getActionBars().getToolBarManager().add(action);
        }

        table.addMouseListener(new MouseAdapter() {

            public void mouseDoubleClick(MouseEvent e) {
                WorkflowInformation wi = (WorkflowInformation) ((IStructuredSelection) viewer.getSelection()).getFirstElement();

                if (wi == null) {
                    return;
                }

                new OpenReadOnlyWorkflowRunEditorAction(wi).run();

            }
        });

        table.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent eve) {
                widgetDefaultSelected(eve);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent eve) {
                WorkflowInformation wi = (WorkflowInformation) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                final WorkflowState workflowState = WorkflowStateModel.getInstance().getState(wi.getIdentifier());
                if (workflowState == WorkflowState.RUNNING || workflowState == WorkflowState.PREPARING) {
                    setIconsToRunningOrPreparingState();
                } else if (workflowState == WorkflowState.PAUSED) {
                    setIconsToPausedState();
                } else if (workflowState == WorkflowState.FINISHED
                    || workflowState == WorkflowState.CANCELED
                    || workflowState == WorkflowState.FAILED) {
                    setIconsToCanceledOrFinishedOrFailedState();
                } else if (workflowState == WorkflowState.DISPOSED) {
                    setIconsToDisposedState();
                }
            }
        });

        WorkflowInformationChangeListener.addReaction(refreshRunnable);
        swr = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        subscribedRemoteWIIds = Collections.synchronizedSet(new HashSet<String>());

        Job job = new Job(Messages.workflows) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    monitor.beginTask(Messages.fetchingWorkflows, 7);
                    sns = new SimpleNotificationService();
                    // subscribe to all state notifications of the local platform
                    sns.subscribe(WorkflowConstants.STATE_NOTIFICATION_ID + ".*",
                        WorkflowInformationChangeListener.getInstance(), null);
                    monitor.worked(2);
                    // subscribe to get informed about new workflows created to refresh and fetch
                    // them
                    SimpleCommunicationService scs = new SimpleCommunicationService();
                    Set<PlatformIdentifier> pis = scs.getAvailableNodes();
                    monitor.worked(5);
                    for (PlatformIdentifier pi : pis) {
                        sns.subscribe(WorkflowConstants.NEW_WORKFLOW_NOTIFICATION_ID,
                            WorkflowInformationChangeListener.getInstance(), pi);
                    }
                    monitor.worked(7);
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                    return Status.OK_STATUS;
                } finally {
                    monitor.done();
                }
            };
        };
        job.setUser(true);
        job.schedule();

        registerTopologyChangeListener();
    }

    @Override
    public void dispose() {
        WorkflowInformationChangeListener.removeReaction(refreshRunnable);
        super.dispose();
        topologyListenerRegistration.unregister();
    }

    /**
     * Refresh the contents of the table viewer.
     */
    public void refresh() {
        // ignore refresh request in case the table widget is already disposed
        if (table.isDisposed()) {
            return;
        }

        Set<WorkflowInformation> wis = swr.getAllWorkflowInformations(true);
        // subscribe to all new remote ones in parallel and fetch their current states
        CallablesGroup<Void> callablesGroup = SharedThreadPool.getInstance().createCallablesGroup(Void.class);
        for (final WorkflowInformation wi : wis) {
            // TODO this is not safe against race conditions on the set of subscribed ids -- misc_ro
            if (!subscribedRemoteWIIds.contains(wi.getIdentifier())) {
                callablesGroup.add(new Callable<Void>() {

                    @Override
                    @TaskDescription("Subscribe to new workflow")
                    public Void call() throws Exception {
                        // subscribe for workflow notifications
                        sns.subscribe(WorkflowConstants.STATE_NOTIFICATION_ID + wi.getIdentifier(),
                            WorkflowInformationChangeListener.getInstance(), wi.getControllerPlatform());
                        subscribedRemoteWIIds.add(wi.getIdentifier());
                        // fetch state from individual workflow service
                        WorkflowState workflowState = swr.getStateOfWorkflow(wi);
                        WorkflowStateModel.getInstance().setState(wi.getIdentifier(), workflowState);
                        return null;
                    }
                });
            }
        }
        callablesGroup.executeParallel(new AsyncExceptionListener() {

            @Override
            public void onAsyncException(Exception e) {
                LogFactory.getLog(getClass()).warn("Asynchronous exception while subscribing to a new workflow");
            }
        });
        viewer.setContentProvider(new WorkflowInformationContentProvider());
        viewer.setLabelProvider(new WorkflowInformationLabelProvider());
        viewer.setInput(wis);
        getSite().setSelectionProvider(viewer);
    }

    public static WorkflowListView getInstance() {
        return instance;
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    private Action[] createToolbarActions() {

        pauseAction = new Action(Messages.pause, ImageDescriptor.createFromURL(
            WorkflowListView.class.getResource("/resources/icons/suspend_16.gif"))) {
            
            public void run() {
                final WorkflowInformation wi = (WorkflowInformation) ((StructuredSelection) viewer.getSelection()).getFirstElement();
                Job job = new Job(Messages.pausingWorkflow) {
                    @Override
                    protected IStatus run(final IProgressMonitor monitor) {
                        try {
                            table.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    swr.pauseWorkflow(wi);
                                    setIconsToPausedState();
                                    refresh();
                                }
                            });
                        } finally {
                            monitor.done();
                        }
                        return Status.OK_STATUS;
                    };
                };
                job.setUser(false);
                job.schedule();
            }
        };
        
        resumeAction = new Action(Messages.resume, ImageDescriptor.createFromURL(
            WorkflowListView.class.getResource("/resources/icons/resume_16.gif"))) {

            public void run() {
                final WorkflowInformation wi = (WorkflowInformation) ((StructuredSelection) viewer.getSelection()).getFirstElement();
                Job job = new Job(Messages.resumingWorkflow) {
                    @Override
                    protected IStatus run(final IProgressMonitor monitor) {
                        try {
                            table.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    swr.resumeWorkflow(wi);
                                    setIconsToResumedState();
                                    refresh();
                                }
                            });
                        } finally {
                            monitor.done();
                        }
                        return Status.OK_STATUS;
                    };
                };
                job.setUser(false);
                job.schedule();
            }
        };
        
        cancelAction = new Action(Messages.cancel, ImageDescriptor.createFromURL(
            ComponentStateFigureImpl.class.getResource("/resources/icons/cancel_enabled.gif"))) {

            public void run() {
                final WorkflowInformation wi = (WorkflowInformation) ((StructuredSelection) viewer.getSelection()).getFirstElement();
                Job job = new Job(Messages.cancelingWorkflow) {
                    @Override
                    protected IStatus run(final IProgressMonitor monitor) {
                        try {
                            table.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    swr.cancelWorkflow(wi);
                                    setIconsToCanceledOrFinishedOrFailedState();
                                    refresh();
                                }
                            });
                        } finally {
                            monitor.done();
                        }
                        return Status.OK_STATUS;
                    };
                };
                job.setUser(false);
                job.schedule();
            }
        };
        
        disposeAction = new Action(Messages.dispose, ImageDescriptor.createFromURL(
            WorkflowListView.class.getResource("/resources/icons/trash_16.gif"))) {

            public void run() {
                final WorkflowInformation wi = (WorkflowInformation) ((StructuredSelection) viewer.getSelection()).getFirstElement();
                Job job = new Job(Messages.disposingWorkflow) {
                    @Override
                    protected IStatus run(final IProgressMonitor monitor) {
                        try {
                            table.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    swr.disposeWorkflow(wi);
                                    setIconsToDisposedState();
                                    refresh();
                                }
                            });
                        } finally {
                            monitor.done();
                        }
                        return Status.OK_STATUS;
                    };
                };
                job.setUser(false);
                job.schedule();
            }
        };

        return new Action[] { pauseAction, resumeAction, cancelAction, disposeAction };
    }

    private void setIconsToDisposedState() {
        pauseAction.setEnabled(false);
        resumeAction.setEnabled(false);
        cancelAction.setEnabled(false);
        disposeAction.setEnabled(false);
    }

    private void setIconsToCanceledOrFinishedOrFailedState() {
        pauseAction.setEnabled(false);
        resumeAction.setEnabled(false);
        cancelAction.setEnabled(false);
        disposeAction.setEnabled(true);
    }

    private void setIconsToResumedState() {
        pauseAction.setEnabled(true);
        resumeAction.setEnabled(false);
        cancelAction.setEnabled(true);
        disposeAction.setEnabled(false);
    }

    private void setIconsToPausedState() {
        pauseAction.setEnabled(false);
        resumeAction.setEnabled(true);
        cancelAction.setEnabled(true);
        disposeAction.setEnabled(false);
    }

    private void setIconsToRunningOrPreparingState() {
        pauseAction.setEnabled(true);
        resumeAction.setEnabled(false);
        cancelAction.setEnabled(true);
        disposeAction.setEnabled(false);
    }

    @Override
    public void onNetworkTopologyChanged() {
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                refresh();
            }
        });
    }

}
