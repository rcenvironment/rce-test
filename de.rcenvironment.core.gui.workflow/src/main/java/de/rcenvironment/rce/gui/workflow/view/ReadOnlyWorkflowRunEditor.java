/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowEditorHelpContextProvider;
import de.rcenvironment.rce.gui.workflow.parts.ReadonlyEditPartFactory;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowInformationChangeListener;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowStateModel;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * Graphical View for a running workflow instance.
 *
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
public class ReadOnlyWorkflowRunEditor extends GraphicalEditor implements ITabbedPropertySheetPageContributor {

    private static ReadOnlyWorkflowRunEditor instance;

    private WorkflowStateChangeListener workflowStateChangeListener;

    private TabbedPropertySheetPage tabbedPropertySheetPage;

    private final Runnable workflowDisposalReaction = new Runnable() {

        @Override
        public void run() {
            if (workflowInformation != null) {
                final WorkflowState workflowState = WorkflowStateModel.getInstance().getState(workflowInformation.getIdentifier());
                if (workflowState == WorkflowState.DISPOSING
                    || workflowState == WorkflowState.DISPOSED) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            ReadOnlyWorkflowRunEditor.this.getSite().getPage().closeEditor(ReadOnlyWorkflowRunEditor.this, false);
                        }
                    });
                }
            }
        }

    };

    private GraphicalViewer viewer;

    private WorkflowInformation workflowInformation;

    private ZoomManager zoomManager;

    public ReadOnlyWorkflowRunEditor() {
        setEditDomain(new DefaultEditDomain(this));
        ReadOnlyWorkflowRunEditor.instance = this;
    }

    public static ReadOnlyWorkflowRunEditor getInstance() {
        return instance;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getGraphicalViewer().setEditPartFactory(new ReadonlyEditPartFactory());
        getGraphicalViewer().getControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IPropertySheetPage.class) {
            return tabbedPropertySheetPage;
        }  else if (type == IContextProvider.class) {
            return new WorkflowEditorHelpContextProvider(viewer);
        }
        return super.getAdapter(type);
    }

    /**
     * Called externally from WorkflowListView to set a new workflow.
     * 
     * @param workflowInformationId The workflow identifier of the workflow to find.
     */
    public void setWorkflowInformation(final String workflowInformationId) {
        // retrieve the WorkflowInformation from the registry
        final SimpleWorkflowRegistry registry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        final WorkflowInformation wi = registry.getWorkflowInformation(workflowInformationId, false);
        // set the WorkflowInformation
        setWorkflowInformation(wi);
    }

    public WorkflowInformation getWorkflowInformation() {
        return workflowInformation;
    }


    /**
     * Retrieves the current state of the workflow and sets it as title of the view.
     */
    public void updateTitle() {
        setPartName(workflowInformation.getName());
    }

    /**
     * Called externally from WorkflowListView to set a new workflow.
     * 
     * @param workflowInformation The workflow.
     */
    public void setWorkflowInformation(final WorkflowInformation workflowInformation) {
        this.workflowInformation = workflowInformation;
        // set the model of the editor
        viewer.setContents(workflowInformation);
        updateTitle();

        workflowStateChangeListener = new WorkflowStateChangeListener();
        new SimpleNotificationService().subscribe(WorkflowConstants.STATE_NOTIFICATION_ID + workflowInformation.getIdentifier(),
            workflowStateChangeListener, workflowInformation.getWorkflowDescription().getTargetPlatform());

        WorkflowInformationChangeListener.addReaction(workflowDisposalReaction);
    }

    @Override
    protected void initializeGraphicalViewer() {
        viewer = getGraphicalViewer();
        viewer.setRootEditPart(new ScalableFreeformRootEditPart());
        final ContextMenuProvider cmProvider = new ReadOnlyWorkflowRunEditorContextMenuProvider(viewer);
        viewer.setContextMenu(cmProvider);

        tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
        zoomManager = ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();

        viewer.getControl().addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseScrolled(MouseEvent arg0) {
                int notches = arg0.count;
                if (notches < 0) {
                    zoomManager.zoomOut();
                } else {
                    zoomManager.zoomIn();
                }

            }
        });
    }

    @Override
    public void dispose() {
        WorkflowInformationChangeListener.removeReaction(workflowDisposalReaction);
        super.dispose();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public String getContributorId() {
        return getSite().getId();
    }

}
