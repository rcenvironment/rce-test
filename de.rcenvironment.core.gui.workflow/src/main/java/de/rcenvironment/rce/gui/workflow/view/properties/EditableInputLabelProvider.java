/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.properties;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider;
import de.rcenvironment.rce.gui.workflow.EndpointLabelProvider;
import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController.Type;


/**
 * {@link LabelProvider} for the contents of the {@link EditableInputTreeViewer}.
 *
 * @author Doreen Seider
 */
public class EditableInputLabelProvider extends EndpointLabelProvider implements ITableLabelProvider {
    
    private WorkflowInformation workflowInformation;
    
    private String workflowId;
    
    private String componentId;
    
    public EditableInputLabelProvider(WorkflowInformation workflowInformation) {
        super(Type.INPUT);
        this.workflowInformation = workflowInformation;
        workflowId = workflowInformation.getIdentifier();
    }
    
    public EditableInputLabelProvider(String workflowId, String componentId) {
        super(Type.INPUT);
        this.workflowId = workflowId;
        this.componentId = componentId;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0) {
            return getImage(element);
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (columnIndex == 0) {
            return getText(element);
        } else {
            String inputValue = ""; //$NON-NLS-1$;
            if (element instanceof EndpointContentProvider.Endpoint) {
                if (componentId == null) {
                    if (columnIndex == 1) {
                        inputValue = InputEditingHelper.getCurrentInputValue(workflowInformation,
                            (EndpointContentProvider.Endpoint) element);                        
                    } else {
                        inputValue = InputEditingHelper.getNextInputValue(workflowInformation,
                            (EndpointContentProvider.Endpoint) element);                                                
                    }
                } else {
                    if (columnIndex == 1) {
                        inputValue = InputEditingHelper.getCurrentInputValueFromEndpoint(workflowId, componentId,
                            (EndpointContentProvider.Endpoint) element);                        
                    } else {
                        inputValue = InputEditingHelper.getNextInputValueFromEndpoint(workflowId, componentId,
                            (EndpointContentProvider.Endpoint) element);
                    }
                }
            }
            return inputValue;
        }
    }

}
