/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.view.ReadOnlyWorkflowRunEditor;
import de.rcenvironment.rce.gui.workflow.view.list.WorkflowInformationChangeListener;

/**
 * Abstract base class for workflow handlers.
 * 
 * @author Christian Weiss
 */
public abstract class AbstractWorkflowHandler extends AbstractHandler implements ISelectionListener {

    private final Runnable updateRunnable = new Runnable() {
        
        public void run() {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    updateEnabled();
                }
            });
        }
        
    };
    
    private boolean initialized = false;

    private IWorkbenchWindow currentWindow;

    private IWorkbenchPage currentPage;

    private final IPageListener pageListener = new IPageListener() {

        @Override
        public void pageActivated(IWorkbenchPage page) {
            if (currentPage != null) {
                pageClosed(currentPage);
            }
            currentPage = page;
            page.addSelectionListener(AbstractWorkflowHandler.this);
        }

        @Override
        public void pageClosed(IWorkbenchPage page) {
            if (page != null && page == currentPage) {
                page.removeSelectionListener(AbstractWorkflowHandler.this);
                currentPage = null;
            }
        }

        @Override
        public void pageOpened(IWorkbenchPage page) {
            // do nothing
        }

    };

    private final IPartListener partListener = new IPartListener() {

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
            // do nothing
        }

        @Override
        public void partActivated(IWorkbenchPart part) {
            AbstractWorkflowHandler.this.partActivated(part);
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // do nothing
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
            // do nothing
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            // do nothing
        }

    };

    private final IWindowListener windowListener = new IWindowListener() {

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            if (currentWindow != null) {
                windowDeactivated(currentWindow);
            }
            currentWindow = window;
            window.addPageListener(pageListener);
            window.getPartService().addPartListener(partListener);
            pageListener.pageActivated(window.getActivePage());
        }

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {
            if (window != null && window == currentWindow) {
                pageListener.pageClosed(window.getActivePage());
                window.getPartService().removePartListener(partListener);
                window.removePageListener(pageListener);
                currentWindow = null;
            }
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            // do nothing
        }

        @Override
        public void windowOpened(IWorkbenchWindow window) {
            // do nothing
        }

    };

    protected void partActivated(IWorkbenchPart part) {
        updateEnabled();
    }

    @Override
    public final void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            selectionChanged((IStructuredSelection) selection);
        }
    }

    protected void selectionChanged(IStructuredSelection selection) {
        updateEnabled();
    }

    protected void setup() {
        WorkflowInformationChangeListener.addReaction(updateRunnable);
        PlatformUI.getWorkbench().addWindowListener(windowListener);
        windowListener.windowActivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    protected void teardown() {
        windowListener.windowDeactivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        PlatformUI.getWorkbench().removeWindowListener(windowListener);
        WorkflowInformationChangeListener.removeReaction(updateRunnable);
    }

    protected void updateEnabled() {
        setBaseEnabled(isEnabled());
    }

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {
        if (!initialized) {
            setup();
            initialized = true;
        }
        super.addHandlerListener(handlerListener);
    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
        super.removeHandlerListener(handlerListener);
        if (!hasListeners()) {
            teardown();
            initialized = false;
        }
    }

    protected WorkflowInformation[] getWorkflowInformations() {
        if (PlatformUI.getWorkbench().isClosing()) {
            return new WorkflowInformation[0];
        }
        WorkflowInformation[] result = new WorkflowInformation[0];
        final IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (activePart != null && activePart instanceof ReadOnlyWorkflowRunEditor) {
            final ReadOnlyWorkflowRunEditor workflowView = (ReadOnlyWorkflowRunEditor) activePart;
            final WorkflowInformation workflowInformation = workflowView.getWorkflowInformation();
            if (workflowInformation != null) {
                result = new WorkflowInformation[] { workflowInformation };
            }
        } else {
            boolean validSelection = true;
            final ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
            if (selectionProvider != null && selectionProvider.getSelection() instanceof IStructuredSelection) {
                final IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
                final List<WorkflowInformation> results = new LinkedList<WorkflowInformation>();
                if (selection != null) {
                    @SuppressWarnings("unchecked") final Iterator<Object> iter = selection.iterator();
                    while (iter.hasNext()) {
                        final Object currentObject = iter.next();
                        if (currentObject instanceof WorkflowInformation) {
                            results.add((WorkflowInformation) currentObject);
                        } else {
                            validSelection = false;
                            break;
                        }
                    }
                    if (validSelection) {
                        result = results.toArray(new WorkflowInformation[0]);
                    }
                }
            }
        }
        final WorkflowInformation[] finalResult = result;
        return finalResult;
    }

    protected SimpleWorkflowRegistry getWorkflowRegistry() {
        final User user = Activator.getInstance().getUser();
        final SimpleWorkflowRegistry workflowRegistry = new SimpleWorkflowRegistry(user);
        return workflowRegistry;
    }

}
