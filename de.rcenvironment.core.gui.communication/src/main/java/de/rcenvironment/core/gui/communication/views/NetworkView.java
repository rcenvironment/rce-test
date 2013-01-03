/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.gui.communication.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.rcenvironment.core.communication.model.NetworkStateModel;
import de.rcenvironment.core.communication.model.NetworkStateNode;
import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.rce.communication.SimpleCommunicationService;

/**
 * A view that shows a tree of all known network nodes and connections.
 * 
 * @author Sascha Zur
 * @author Robert Mischke
 */
public class NetworkView extends ViewPart implements NetworkTopologyChangeListener {

    /**
     * Sorter implementation.
     * 
     * @author Robert Mischke
     */
    private static final class NetworkViewSorter extends ViewerSorter {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if ((e1 instanceof NetworkStateNode) && (e2 instanceof NetworkStateNode)) {
                return ((NetworkStateNode) e1).getDisplayName().compareTo(((NetworkStateNode) e2).getDisplayName());
            }
            return super.compare(viewer, e1, e2);
        }
    }

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "de.rcenvironment.core.gui.communication.views.NetworkView";

    private final SimpleCommunicationService simpleCommunicationService;

    private NetworkStateModel model;

    private Display display;

    private TreeViewer viewer;

    private ServiceRegistration topologyListenerRegistration;

    private Action toggleNodeIdsVisibleAction;

    private NetworkViewLabelProvider labelProvider;

    public NetworkView() {
        simpleCommunicationService = new SimpleCommunicationService();
        model = new NetworkStateModel();
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        IMenuManager menuManager = site.getActionBars().getMenuManager();

        // add "show nodeIds" option
        toggleNodeIdsVisibleAction = new Action("Show Unique Node Ids", SWT.TOGGLE) {

            public void run() {
                labelProvider.setNodeIdsVisible(this.isChecked());
                viewer.refresh(true);
            }
        };
        menuManager.add(toggleNodeIdsVisibleAction);
    }

    @Override
    public void createPartControl(Composite parent) {
        // store display reference for asyncExec calls
        display = parent.getShell().getDisplay();

        labelProvider = new NetworkViewLabelProvider();
        labelProvider.setNodeIdsVisible(false);

        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new NetworkViewContentProvider());
        viewer.setLabelProvider(labelProvider);
        viewer.setSorter(new NetworkViewSorter());

        registerTopologyChangeListener();
        initializeModel();
        viewer.setInput(model);
        viewer.expandToLevel(2);
    }

    @Override
    public void dispose() {
        topologyListenerRegistration.unregister();
        super.dispose();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void onNetworkTopologyChanged() {
        synchronized (model) {
            updateModel();
        }
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                viewer.setInput(model);
                viewer.expandToLevel(2);
            }
        });
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

    private void initializeModel() {
        updateModel();
    }

    private void updateModel() {
        model = simpleCommunicationService.getCurrentNetworkState();
    }

}
