/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.parts.ReadonlyWorkflowNodePart;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowInformationPart;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowPart;


/**
 * {@link ContextMenuProvider} for the {@link ReadOnlyWorkflowRunEditor}.
 *
 * @author Doreen Seider
 */
public class ReadOnlyWorkflowRunEditorContextMenuProvider extends ContextMenuProvider {

    private GraphicalViewer viewer;

    public ReadOnlyWorkflowRunEditorContextMenuProvider(GraphicalViewer viewer) {
        super(viewer);
        this.viewer = viewer;
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {

        @SuppressWarnings("rawtypes")
        List selection = ((GraphicalViewer) viewer).getSelectedEditParts();
        if (selection.size() == 0) {
            // fixes IndexOutOfBoundsException occurring if no element is currently selected
            return;
        }
        ReadonlyWorkflowNodePart part = null;
        if (selection.get(0) instanceof ReadonlyWorkflowNodePart){
            part = (ReadonlyWorkflowNodePart) selection.get(0);
        }
        if (part != null){
            final WorkflowNode node = ((WorkflowNode) part.getModel());
            WorkflowInformation wi = (WorkflowInformation) ((WorkflowInformationPart) 
                ((WorkflowPart) part.getParent()).getParent()).getModel();

            SimpleWorkflowRegistry registry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
            Set<ComponentInstanceDescriptor> cis = registry.getComponentInstanceDescriptors(wi);

            // Get associated component information
            ComponentInstanceDescriptor compInstDescr = null;
            for (ComponentInstanceDescriptor cid: cis) {
                if (cid.getName().equals(node.getName())) {
                    compInstDescr = cid;
                    break;
                }
            }

            final ComponentInstanceDescriptor cid = compInstDescr;

            // Find registered views
            IExtensionRegistry extReg = Platform.getExtensionRegistry();
            IConfigurationElement[] confElements =
                extReg.getConfigurationElementsFor("de.rcenvironment.rce.gui.workflow.monitoring"); //$NON-NLS-1$
            IConfigurationElement[] viewConfElements =
                extReg.getConfigurationElementsFor("org.eclipse.ui.views"); //$NON-NLS-1$

            for (final IConfigurationElement confElement : confElements) {

                if (compInstDescr.getComponentIdentifier().matches(confElement.getAttribute("component"))) { //$NON-NLS-1$

                    for (final IConfigurationElement viewConfElement : viewConfElements) {

                        if (viewConfElement.getAttribute("id").equals(confElement.getAttribute("view"))) {

                            menu.add(new Action() {
                                @Override
                                public String getText() {
                                    return viewConfElement.getAttribute("name");
                                }

                                @Override
                                public void run() {
                                    IViewPart view;
                                    try {
                                        view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
                                            showView(viewConfElement.getAttribute("class"),
                                                cid.getIdentifier(), IWorkbenchPage.VIEW_VISIBLE); //$NON-NLS-1$

                                        ((ComponentRuntimeView) view).setComponentInstanceDescriptor(cid);
                                        view.setFocus();
                                    } catch (PartInitException e) {
                                        throw new RuntimeException(e);
                                    } catch (InvalidRegistryObjectException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                @Override
                                public boolean isEnabled() {
                                    @SuppressWarnings("rawtypes")
                                    List selection = ((GraphicalViewer) viewer).getSelectedEditParts();
                                    return selection.size() == 1 && selection.get(0).getClass() == ReadonlyWorkflowNodePart.class;
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
    }

}
