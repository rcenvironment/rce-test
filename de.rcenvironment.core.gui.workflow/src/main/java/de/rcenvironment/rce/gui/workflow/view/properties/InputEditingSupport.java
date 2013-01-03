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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider;

/**
 * Supports editing input values.
 *
 * @author Doreen Seider
 */
public class InputEditingSupport extends EditingSupport {
    
    private static boolean canEdit = false;

    private WorkflowInformation workflowInformation;
    
    private CellEditor editor;
    
    private Map<String, Map<String, Map<String, Integer>>> inputNumbers;

    public InputEditingSupport(ColumnViewer viewer, WorkflowInformation workflowInformation) {
        super(viewer);
        inputNumbers = new HashMap<String, Map<String, Map<String, Integer>>>();
        if (viewer instanceof TreeViewer) {
            editor = new TextCellEditor(((TreeViewer) viewer).getTree());
        } else if (viewer instanceof TableViewer) {
            editor = new TextCellEditor(((TableViewer) viewer).getTable());
        }
        this.workflowInformation = workflowInformation;
    }

    @Override
    protected boolean canEdit(Object element) {
        if (element instanceof Input) {
            Input input = (Input) element;
            String workflowId = input.getWorkflowIdentifier();
            String componentId = input.getComponentIdentifier();
            String inputName = input.getName();
            if (inputNumbers.containsKey(workflowId)
                && inputNumbers.get(workflowId).containsKey(componentId)
                && inputNumbers.get(workflowId).get(componentId).containsKey(inputName)) {
                if (input.getNumber() <= inputNumbers.get(workflowId).get(componentId).get(inputName)) {
                    return false;
                }
            }
        }
        return canEdit;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return editor;
    }

    @Override
    protected Object getValue(Object element) {
        Object object = "";
        if (element instanceof EndpointContentProvider.Endpoint) {
            object = InputEditingHelper.getNextInputValue(workflowInformation, (EndpointContentProvider.Endpoint) element);
        } else if (element instanceof Input) {
            object = ((Input) element).getValue().toString();            
        }
        return object;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof EndpointContentProvider.Endpoint) {
            Input input = InputEditingHelper.getNextInput(workflowInformation, (EndpointContentProvider.Endpoint) element);
            setNewValue(input, value);
            if (ComponentInputSection.getInstance() != null) {
                ComponentInputSection.getInstance().refreshTable();
            }
            if (InputSection.getInstance() != null) {
                InputSection.getInstance().refreshTable();
            } 
        } else if (element instanceof Input) {
            Input input = (Input) element;
            setNewValue(input, value);
            if (InputQueueDialogController.getInstance() != null) {
                InputQueueDialogController.getInstance().redrawTable();
            }
        }
    }
    
    private void setNewValue(Input input, Object newValue) {
        if (input == null) {
            return;
        }
        try {
            if (input.getType() == Double.class) {
                input.setValue(Double.valueOf((String) newValue));
            } else if (input.getType() == Integer.class) {
                input.setValue(Integer.valueOf((String) newValue));
            } else if (input.getType() == Boolean.class) {
                input.setValue(Boolean.valueOf(String.valueOf(newValue)));
            } else if (input.getType() == String.class) {
                input.setValue((String) newValue);
            } else {
                throw new NumberFormatException("given input type is not supported");
            }
        } catch (NumberFormatException e) {
            final IStatus status = new Status(Status.ERROR, "de.rcenvironment.rce.gui.workflow.view.properties",
                Messages.inputEditError + input.getType().getSimpleName());
            StatusManager.getManager().handle(status, StatusManager.SHOW);
        }
        InputModel.getInstance().replaceInput(input);
    }
    
    /**
     * Enable editing support.
     * @param edit <code>true</code> if enabled, else <code>false</code>
     */
    public static void enableEdit(boolean edit) {
        canEdit = edit;
    }
    
    /**
     * Sets number of currently consumed  input.
     * @param workflowId input's workflow identifier
     * @param componentId input's component identifier
     * @param inputName input's name
     * @param number number of currently consumed input
     */
    public void setNumberOfCurrentInput(String workflowId, String componentId, String inputName, int number) {
        if (!inputNumbers.containsKey(workflowId)) {
            inputNumbers.put(workflowId, new HashMap<String, Map<String, Integer>>());
        }
        if (!inputNumbers.get(workflowId).containsKey(componentId)) {
            inputNumbers.get(workflowId).put(componentId, new HashMap<String, Integer>());
        }
        inputNumbers.get(workflowId).get(componentId).put(inputName, number);
    }

}
