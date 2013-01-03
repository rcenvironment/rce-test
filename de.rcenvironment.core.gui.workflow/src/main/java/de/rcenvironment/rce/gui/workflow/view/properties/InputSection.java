/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowInformationPart;

/**
 * Property section for displaying and editing inputs.
 * 
 * @author Doreen Seider
 */
public class InputSection extends AbstractInputSection {

    private static InputSection instance;
    
    private Map<String, String> componentNameToIdMapping;

    public InputSection() {
        super();
        instance = this;
    }
    
    public static InputSection getInstance() {
        return instance;
    }
    
    @Override
    protected void retrieveWorkflowInformation(IWorkbenchPart part, ISelection selection) {
        final Object firstSelectionElement = ((IStructuredSelection) selection).getFirstElement();
        workflowInformation = (WorkflowInformation) ((WorkflowInformationPart) firstSelectionElement).getModel();
        componentNameToIdMapping = new HashMap<String, String>();
        for (ComponentInstanceDescriptor cid : workflowInformation.getComponentInstanceDescriptors()) {
            componentNameToIdMapping.put(cid.getName(), cid.getIdentifier());
        }
    }
    
    @Override
    protected void initializeTreeViewer(IWorkbenchPart part, ISelection selection) {
        inputTreeViewer.setLabelProvider(new EditableInputLabelProvider(workflowInformation));
        inputTreeViewer.setInput(workflowInformation.getWorkflowDescription());
    }
    
    @Override
    protected void openInputDialog(TreeItem item) {
        TreeItem childItem = item;
        while (item.getParentItem() != null) {
            item = item.getParentItem();
        }
        
        String componentId = componentNameToIdMapping.get(item.getText());
        
        new InputQueueDialogController(workflowInformation, componentId, childItem.getText()).open();
    }
}
