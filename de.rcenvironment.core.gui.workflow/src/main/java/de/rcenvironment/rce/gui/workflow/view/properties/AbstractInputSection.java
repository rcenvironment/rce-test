/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider;
import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController.Type;
import de.rcenvironment.rce.gui.workflow.editor.connections.EndpointTreeViewer;

/**
 * Property section for displaying and editing inputs.
 * 
 * @author Doreen Seider
 */
public abstract class AbstractInputSection extends AbstractPropertySection implements NetworkTopologyChangeListener {
    
    private static final long REFRESH_INTERVAL = 1000;
    
    private static final int COLUMN_WIDTH_ONE = 250;
    
    private static final int COLUMN_WIDTH_TWO = 150;
    
    private static final int COLUMN_WIDTH_THREE = 50;
    
    protected SimpleWorkflowRegistry workflowRegistry;

    protected WorkflowInformation workflowInformation;

    protected EndpointTreeViewer inputTreeViewer;

    protected final Timer refreshTimer = new Timer();

    private Composite parent;

    private List<TreeEditor> treeEditors;
    
    private InputModel inputModel = InputModel.getInstance();
    
    private TreeViewerColumn inputQueueViewerColumn;
    
    private ServiceRegistration topologyListenerRegistration;
    
    private Display display;
                    
    /**
     * Periodically refreshes this section.
     * 
     * @author Doreen Seider
     */
    private class RefreshTask extends TimerTask {

        @Override
        public void run() {
            
            if (!inputTreeViewer.getTree().isDisposed()) {
                inputTreeViewer.getTree().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (inputModel.hasChanged(workflowInformation.getIdentifier())) {
                            refreshTable();
                        }
                    }
                });
            } else {
                cancelRefreshTimer();
            }
            
        }
    }
    
    public AbstractInputSection() {
        inputModel = InputModel.getInstance();
        scheduleRefreshTimer();
        workflowRegistry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        treeEditors = new ArrayList<TreeEditor>();
    }
        
    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);

        retrieveWorkflowInformation(part, selection);
        initializeTreeViewer(part, selection);
        
        inputTreeViewer.expandAll();
        setInputQueueButton(inputTreeViewer.getTree());
        inputQueueViewerColumn.setEditingSupport(new InputEditingSupport(inputTreeViewer, workflowInformation));
        refresh();
    }
    
    protected abstract void retrieveWorkflowInformation(IWorkbenchPart part, ISelection selection);
    
    protected abstract void initializeTreeViewer(IWorkbenchPart part, ISelection selection);
    
    @Override
    public void createControls(final Composite aParent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        parent = aParent;
        display = parent.getShell().getDisplay();
        
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);
        
        inputTreeViewer = new EndpointTreeViewer(parent, SWT.FULL_SELECTION);
        inputTreeViewer.setContentProvider(new EndpointContentProvider(Type.INPUT));
        final Tree endpointTree = inputTreeViewer.getTree();
        endpointTree.setHeaderVisible(true);
        endpointTree.addListener(SWT.Selection, new Listener() {
            
            @Override
            public void handleEvent(Event event) {
                InputEditingHelper.handleEditingRequest(parent.getShell(), workflowInformation);
            }
        });
        TreeColumn inputColumn = new TreeColumn(endpointTree, SWT.LEFT);
        inputColumn.setText(Messages.inputs);
        inputColumn.setWidth(COLUMN_WIDTH_ONE);
        TreeColumn currentInputColumn = new TreeColumn(endpointTree, SWT.CENTER);
        currentInputColumn.setText(Messages.currentInput);
        currentInputColumn.setWidth(COLUMN_WIDTH_TWO);
        TreeColumn nextInputColumn = new TreeColumn(endpointTree, SWT.CENTER);
        nextInputColumn.setText(Messages.nextInput);
        nextInputColumn.setWidth(COLUMN_WIDTH_TWO);
        inputQueueViewerColumn = new TreeViewerColumn(inputTreeViewer, nextInputColumn);
        TreeColumn inputQueueColumn = new TreeColumn(endpointTree, SWT.LEFT);
        inputQueueColumn.setText(Messages.inputQueue);
        inputQueueColumn.setWidth(COLUMN_WIDTH_THREE);
        
        endpointTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        endpointTree.setLinesVisible(true);
        
        registerTopologyChangeListener();
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
    public void dispose() {
        // shut down the query timer
        refreshTimer.cancel();
        super.dispose();
        topologyListenerRegistration.unregister();
    }
    
    /**
     * Schedules the refresh timer responsible for refreshing the view every 500 milliseconds.
     */
    public void scheduleRefreshTimer() {
        refreshTimer.schedule(new RefreshTask(), REFRESH_INTERVAL, REFRESH_INTERVAL);
    }

    /**
     * Cancels the refresh timer responsible for refreshing the view every 500 milliseconds.
     */
    public void cancelRefreshTimer() {
        refreshTimer.cancel();
    }
    
    /**
     * Refrehes the table. Same action timer does.
     */
    public void refreshTable() {
        if (!inputTreeViewer.getTree().isDisposed()) {
            inputTreeViewer.getControl().setRedraw(false);
            inputTreeViewer.refresh();
            inputTreeViewer.expandAll();
            inputTreeViewer.getControl().setRedraw(true);
            inputTreeViewer.getControl().redraw();
        }
    }
    
    private void setInputQueueButton(final Tree tree) {
        for (TreeEditor treeEditor : treeEditors) {
            Control oldEditor = treeEditor.getEditor();
            if (oldEditor != null) {
                oldEditor.dispose();
            }
        }
        treeEditors.clear();
        for (final TreeItem treeItem : inputTreeViewer.getTree().getItems()) {
            setInputQueueButton(treeItem);
        }
    }
    
    private void setInputQueueButton(final TreeItem treeItem) {
        if (treeItem.getItemCount() > 0) {
            for (final TreeItem childTreeItem : treeItem.getItems()) {
                setInputQueueButton(childTreeItem);
            }
        } else {
            TreeEditor treeEditor = new TreeEditor(inputTreeViewer.getTree());
            Button button = new Button(inputTreeViewer.getTree(), SWT.PUSH);
            
            button.setText("...");
            button.computeSize(SWT.DEFAULT, inputTreeViewer.getTree().getItemHeight());
            treeEditor.grabHorizontal = true;
            treeEditor.minimumHeight = button.getSize().y;
            treeEditor.minimumWidth = button.getSize().x;

            treeEditor.setEditor(button, treeItem, 3);
            
            treeEditors.add(treeEditor);
            
            button.addSelectionListener(new SelectionAdapter() {
                
                private TreeItem item = treeItem;
                
                @Override
                public void widgetSelected(SelectionEvent event) {
                    openInputDialog(item);
                }

            });
        }
    }
    
    @Override
    public void onNetworkTopologyChanged() {
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                inputModel.updateSubscriptions();
                inputTreeViewer.refresh();
            }
        });
    }
    
    protected abstract void openInputDialog(TreeItem item);
}
