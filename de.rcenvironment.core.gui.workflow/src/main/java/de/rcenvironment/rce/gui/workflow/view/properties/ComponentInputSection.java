/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;

import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.parts.ReadonlyWorkflowNodePart;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowInformationPart;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowPart;

/**
 * Property section for displaying and editing inputs.
 * 
 * @author Doreen Seider
 */
public class ComponentInputSection extends AbstractInputSection {
    
    private static ComponentInputSection instance;
    
    private WorkflowNode workflowNode;
    
    private String componentId;
    
    public ComponentInputSection() {
        super();
        instance = this;
    }
    
    public static ComponentInputSection getInstance() {
        return instance;
    }
    
    @Override
    protected void retrieveWorkflowInformation(IWorkbenchPart part, ISelection selection) {
        final Object firstSelectionElement = ((IStructuredSelection) selection).getFirstElement();
        final WorkflowPart workflowPart = (WorkflowPart) ((ReadonlyWorkflowNodePart) firstSelectionElement).getParent();
        workflowNode = (WorkflowNode) ((ReadonlyWorkflowNodePart) firstSelectionElement).getModel();
        workflowInformation = (WorkflowInformation) ((WorkflowInformationPart) workflowPart.getParent()).getModel();
        componentId = workflowRegistry.getComponentInstanceDescriptor(workflowNode, workflowInformation).getIdentifier();
    }
    
    @Override
    protected void initializeTreeViewer(IWorkbenchPart part, ISelection selection) {
        inputTreeViewer.setLabelProvider(new EditableInputLabelProvider(workflowInformation.getIdentifier(), componentId));
        inputTreeViewer.setInput(workflowNode);
    }

    @Override
    protected void openInputDialog(TreeItem item) {
        new InputQueueDialogController(workflowInformation, componentId, item.getText()).open();
    }
}
