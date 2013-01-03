/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.properties;

import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.Activator;


/**
 * Controller class for the {@link InputQueueDialog}.
 *
 * @author Doreen Seider
 */
public class InputQueueDialogController {

    private static final String SETTINGS_KEY_SROLLLOCK = "de.rcenvironment.rce.gui.workflow.view.properties.settinngs.scrolllock";
    
    private static final long REFRESH_INTERVAL = 500;
    
    private static InputQueueDialogController instance;
    
    private WorkflowInformation workflowInfo;
    
    private String workflowId;
    
    private String componentId;
    
    private String inputName;
    
    private InputModel inputModel;
    
    private InputQueueDialog dialog;
    
    private IDialogSettings dialogSettings;
    
    private boolean isScrollLocked;
    
    private final Timer refreshTimer = new Timer();
    
    /**
     * Periodically refreshes this section.
     * 
     * @author Doreen Seider
     */
    private class RefreshTask extends TimerTask {

        @Override
        public void run() {
            if (!dialog.getInputQueueTableViewer().getTable().isDisposed()) {
                
                dialog.getInputQueueTableViewer().getTable().getDisplay().syncExec(new Runnable() {
    
                    @Override
                    public void run() {
                        if (!dialog.getInputQueueTableViewer().getTable().isDisposed()) {
                            if (inputModel.hasChanged(workflowId, componentId)) {
                                redrawTable();
                            }
                        }
                    }
                });
            } else {
                refreshTimer.cancel();
            }
        }

    }
    
    public InputQueueDialogController(WorkflowInformation workflowInfo, String componentId, String inputName) {
        instance = this;
        this.workflowInfo =  workflowInfo;
        this.workflowId = workflowInfo.getIdentifier();
        this.componentId = componentId;
        this.inputName = inputName;
        dialogSettings = Activator.getInstance().getDialogSettings();
        
        inputModel = InputModel.getInstance();
                
        dialog = new InputQueueDialog(Display.getCurrent().getActiveShell());
        dialog.create();
        initialize();
        
        refreshTimer.schedule(new RefreshTask(), REFRESH_INTERVAL, REFRESH_INTERVAL);
        
    }
    
    public static InputQueueDialogController getInstance() {
        return instance;
    }
    
    /**
     * Shows the dialog.
     * @return The return code (which button pressed).
     */
    public int open() {
        return dialog.open();
    }
    
    /**
     * Redraws the table.
     */
    public void redrawTable() {
        dialog.getInputQueueTableViewer().setInput(inputModel.getInputs(workflowId, componentId, inputName));
        InputEditingSupport editingSupport = new InputEditingSupport(dialog.getInputQueueTableViewer(), workflowInfo);
        dialog.getInputQueueTableViewerColumn().setEditingSupport(editingSupport);
        Table table = dialog.getInputQueueTableViewer().getTable();
        int index = inputModel.getNumberOfCurrentInput(workflowId, componentId, inputName) + 1;
        for (int i = 0; i < index; i++) {
            if (i >= table.getItemCount()) {
                break;
            }
            TableItem item = table.getItem(i);
            item.getText();
            item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
            editingSupport.setNumberOfCurrentInput(workflowId, componentId, inputName, index);
        }
        if (index >= 0 && index < table.getItemCount()) {
            TableItem item = table.getItem(index);
            item.getText();
            item.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
            table.redraw();
        }
        
        if (!isScrollLocked) {
            if (index > 3) {
                table.setTopIndex(index - 3);
            } else if (table.getTopIndex() != 0)  {
                table.setTopIndex(0);
            }
        }
        
    }
        
    private void initialize() {
        dialog.getShell().setText(inputName);
        dialog.getInputQueueTableViewer().setLabelProvider(new InputQueueLabelProvider());
        dialog.getInputQueueTableViewer().setContentProvider(new InputQueueContentProvider());
        dialog.getInputQueueTableViewer().getTable().addListener(SWT.Selection, new Listener() {
            
            @Override
            public void handleEvent(Event event) {
                InputEditingHelper.handleEditingRequest(dialog.getShell(), workflowInfo);
            }
        });
        redrawTable();
        
        isScrollLocked = dialogSettings.getBoolean(SETTINGS_KEY_SROLLLOCK);
        dialog.getScrollLockButton().setSelection(isScrollLocked);
        dialog.getScrollLockButton().addListener(SWT.Selection, new Listener() {
            
            @Override
            public void handleEvent(Event event) {
                isScrollLocked = dialog.getScrollLockButton().getSelection();
                dialogSettings.put(SETTINGS_KEY_SROLLLOCK, isScrollLocked);
            }
        });
    }
        
    /**
     * Provides the concrete label texts to display and images if required.
     * 
     * @author Doreen Seider
     */
    class InputQueueLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object object, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object object, int columnIndex) {
            if (object instanceof Input) {
                return ((Input) object).getValue().toString();
            }
            return null;
        }
        
    }
    
    /**
     * Take the whole content to structured pieces.
     * 
     * @author Doreen Seider
     */
    class InputQueueContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
            // do nothing
        }

        @Override
        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
            // do nothing
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Deque<?>) {
                return ((Deque<Input>) inputElement).toArray();
            } else {
                // empty default
                return new Object[] {};
            }
        }
        
    }

}
