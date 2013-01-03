/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.converger.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.rcenvironment.components.converger.common.ConvergerComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.properties.AbstractWorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointSelectionPane;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodeCommand;
/**
 * 
 *  New use for the  {@link}DynamicEndpointSelectionPane.
 *   
 * @author Sascha Zur
 */
public class ConvergerDynamicEndpointSelectionPane extends DynamicEndpointSelectionPane{

    public ConvergerDynamicEndpointSelectionPane(String genericEndpointTitle, EndpointNature direction,
        TypeSelectionFactory typeSelectionFactory) {
        super(genericEndpointTitle, direction, typeSelectionFactory);
    }

    public ConvergerDynamicEndpointSelectionPane(String string, EndpointNature input, TypeSelectionFactory defaultTypeSelectionFactory,
        ConvergerInputSection parametersSection) {
        super("Input", 
            ComponentDescription.EndpointNature.Input, 
            defaultTypeSelectionFactory, parametersSection);
    }


    /**
     * Creating gui.
     * 
     * @param parent parent Composite
     * @param title Title of selection pane
     * @param toolkit Formtoolkit to use
     * @return control
     */
    public Control createControl(final Composite parent, String title, FormToolkit toolkit) {

        section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(title);

        // dispose this 'component', when the real component gets disposed
        section.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
                dispose();
            }

        });

        Composite client = toolkit.createComposite(section);
        client.setLayout(new GridLayout(2, false));

        final Composite tableComposite = toolkit.createComposite(client);
        final TableColumnLayout tableLayout = new TableColumnLayout();
        tableComposite.setLayout(tableLayout);
        table = toolkit.createTable(tableComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        table.setHeaderVisible(true);

        GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
        final int minHeight = 140;
        tableLayoutData.heightHint = minHeight; // effectively min height
        tableComposite.setLayoutData(tableLayoutData);

        // first column - name
        TableColumn col1 = new TableColumn(table, SWT.NONE);
        col1.setText(Messages.name);
        // second column - data type
        TableColumn col2 = new TableColumn(table, SWT.NONE);
        col2.setText(Messages.dataType);
        TableColumn col3 = new TableColumn(table, SWT.NONE);
        col3.setText(Messages.startValue);
        // layout data for the columns
        final int allColumnsWeight = 100;
        final int firstColumnWeight = 30;

        tableLayout.setColumnData(col1, new ColumnWeightData(firstColumnWeight, true));
        tableLayout.setColumnData(col2, new ColumnWeightData(allColumnsWeight - firstColumnWeight/2, false));
        tableLayout.setColumnData(col3, new ColumnWeightData(allColumnsWeight - firstColumnWeight/2, false));

        buttonAdd = toolkit.createButton(client, Messages.add, SWT.FLAT);
        buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonEdit = toolkit.createButton(client, Messages.edit, SWT.FLAT);
        buttonEdit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonRemove = toolkit.createButton(client, Messages.remove, SWT.FLAT);
        buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

        SelectionAdapter buttonListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                if (e.widget == buttonAdd) {
                    // add
                    ConvergerAddVariable dialog =
                        new ConvergerAddVariable(parent.getShell(), Messages.addInput , configuration, typeSelectionFactory);
                    if (dialog.open() == Dialog.OK){
                        addInputAndOutput(dialog.getChosenName(), dialog.getChosenDataType(), 
                            dialog.hasStartValue(), dialog.getStartValue());
                    }
                } else if (e.widget == buttonEdit) {
                    TableItem[] ti = table.getSelection();
                    String name = ti[0].getText(0);

                    ConvergerAddVariable dialog =
                        new ConvergerAddVariable(parent.getShell(), Messages.editInput, configuration, typeSelectionFactory);
                    dialog.setInitialName(name);
                    dialog.setInitialDataType(configuration.getInputType(name));
                    if (getMetaData(name) != null && getMetaData(name).get(ConvergerComponentConstants.META_HAS_STARTVALUE) != null) {
                        dialog.setInitialHasStart((Boolean) getMetaData(name).get(ConvergerComponentConstants.META_HAS_STARTVALUE));
                    }
                    dialog.setInitialstart("" + getMetaData(name).get(ConvergerComponentConstants.META_STARTVALUE));
                    if (dialog.open() == Dialog.OK) {
                        changeInput(name, dialog.getChosenName(), dialog.getChosenDataType(), 
                            dialog.hasStartValue(), dialog.getStartValue());
                    }         
                } else if (e.widget == buttonRemove) {
                    // remove selected; relies on proper button activation
                    String name = (String) table.getSelection()[0].getData();
                    removeInput(name);
                }
            }

        };

        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonActivation();
            }

        });

        buttonAdd.addSelectionListener(buttonListener);
        buttonEdit.addSelectionListener(buttonListener);
        buttonRemove.addSelectionListener(buttonListener);

        section.setClient(client);
        toolkit.paintBordersFor(client);
        section.setExpanded(true);

        return section;
    }
    /**
     * Loads the current endpoint data into the UI table.
     */
    protected void fillTable() {
        table.removeAll();
        List<String> endpointNames = new LinkedList<String>();
        endpointNames = new ArrayList<String>(configuration.getDynamicOutputDefinitions().keySet());
        Collections.sort(endpointNames);
        for (String name : endpointNames) {
            TableItem item = new TableItem(table, SWT.None);
            item.setText(0, name);
            final String endpointType = getType(name);
            for (TypeSelectionOption opt : typeSelectionFactory.getTypeSelectionOptions()) {
                if (opt.getTypeName().equals(endpointType)) {
                    item.setText(1, opt.getDisplayName());
                    break;
                }
            }  
            if (getMetaData(name) != null && getMetaData(name).get(ConvergerComponentConstants.META_HAS_STARTVALUE) != null
                && (Boolean) getMetaData(name).get(ConvergerComponentConstants.META_HAS_STARTVALUE)) {
                item.setText(2, "" + (getMetaData(name).get(ConvergerComponentConstants.META_STARTVALUE)));
            } else {
                item.setText(2, Messages.none);
            }
            
            item.setData(name);
        }

    }

    private void addInputAndOutput(final String name, final String types, boolean hasStartValue, double startValue) {
        final WorkflowNodeCommand command =
            new AddInputOutputCommand(name, types, hasStartValue, startValue);
        execute(command);
    }


    private void changeInput(final String oldName, String newName, String newType, final boolean hasStartValue, final double startValue){
        final WorkflowNodeCommand command = new ChangeInputOutputCommand(oldName, newName, newType, 
            hasStartValue, startValue);
        execute(command);
    }

    private void removeInput(final String name) {
        final WorkflowNodeCommand command = new RemoveInputOutputCommand(name);
        execute(command);
    }

    /**
     * Adds a input to a {@link WorkflowNode}.
     * 
     * @author Sascha Zur
     */
    private static class AddInputOutputCommand extends SetInputOutputMetadataCommand {

        private final String name;

        private final String type;

        private String oldType;

        protected AddInputOutputCommand(final String name, final String type, final boolean hasStartValue, final double startValue) {
            super(name, hasStartValue, startValue);
            this.name = name;
            this.type = type;
        }

        @Override
        public void executeSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            oldType = workflowNode.getInputType(name);
            workflowNode.addInput(name, type);
            oldType = workflowNode.getOutputType(name);
            workflowNode.addOutput(name, type);

        }

        @Override
        public void undoSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            if (oldType != null) {
                workflowNode.addInput(name, oldType);
            } else {
                workflowNode.removeInput(name);
            }
            if (oldType != null) {
                workflowNode.addOutput(name, oldType);
            } else {
                workflowNode.removeOutput(name);
            }

        }

        @Override
        protected void execute2() {
        }

        @Override
        protected void undo2() {
        }

    }
    /**
     * Adds a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private abstract static class SetInputOutputMetadataCommand extends AbstractWorkflowNodeCommand {

        private final String name;

        private final Map<String, Serializable> newValues = new HashMap<String, Serializable>();

        private final Map<String, Serializable> oldValues = new HashMap<String, Serializable>();

        protected SetInputOutputMetadataCommand(final String name, final boolean hastStartValue, double startValue) {
            newValues.put(ConvergerComponentConstants.META_HAS_STARTVALUE, hastStartValue);
            newValues.put(ConvergerComponentConstants.META_STARTVALUE, startValue);
            this.name = name;

        }

        @Override
        public final void execute() {
            super.execute();
            executeSpecial();
            final WorkflowNode workflowNode = getWorkflowNode();
            final Map<String, Serializable> metadata;
            oldValues.clear();
            metadata = workflowNode.getInputMetaData(name);
            for (final String key : newValues.keySet()) {
                oldValues.put(key, metadata.get(key));
                final Serializable newValue = newValues.get(key);
                workflowNode.setInputMetaData(name, key, newValue);
            }

            final Map<String, Serializable> metadata2;

            metadata2 = workflowNode.getOutputMetaData(name);
            for (final String key : newValues.keySet()) {
                oldValues.put(key, metadata2.get(key));
                final Serializable newValue = newValues.get(key);
                workflowNode.setOutputMetaData(name, key, newValue);
            }


        }

        protected abstract void executeSpecial();

        @Override
        public final void undo() {
            final WorkflowNode workflowNode = getWorkflowNode();

            for (final String key : newValues.keySet()) {
                final Serializable oldValue = oldValues.get(key);
                workflowNode.setInputMetaData(name, key, oldValue);
            }

            for (final String key : newValues.keySet()) {
                final Serializable oldValue = oldValues.get(key);
                workflowNode.setOutputMetaData(name, key, oldValue);
            }

            undoSpecial();
            super.undo();
        }

        protected abstract void undoSpecial();

    }
    /**
     * Changes a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private static class ChangeInputOutputCommand extends SetInputOutputMetadataCommand {


        private final String oldName;

        private final String newName;

        private final String newType;

        private String oldType;

        protected ChangeInputOutputCommand(final String oldName, final String newName, final String newType, 
            final boolean hasStartValue, final double startValue) {
            super(newName, hasStartValue, startValue);
            this.oldName = oldName;
            this.newName = newName;
            this.newType = newType;
        }

        @Override
        public void executeSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            oldType = workflowNode.getInputType(oldName);
            workflowNode.changeInput(oldName, newName, newType);
            oldType = workflowNode.getOutputType(oldName);
            workflowNode.changeOutput(oldName, newName, newType);
        }

        @Override
        public void undoSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            workflowNode.changeInput(newName, oldName, oldType);
            workflowNode.changeOutput(newName, oldName, oldType);
        }

        @Override
        protected void execute2() {
        }

        @Override
        protected void undo2() {
        }

    }

    /**
     * Removes a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private static class RemoveInputOutputCommand extends AbstractWorkflowNodeCommand {

        private final String name;

        private String type;

        private final Map<String, Serializable> metadata = new HashMap<String, Serializable>();

        protected RemoveInputOutputCommand(final String name) {
            this.name = name;
        }

        @Override
        public void execute() {
            super.execute();
            final WorkflowNode workflowNode = getWorkflowNode();
            metadata.clear();
            type = workflowNode.getInputType(name);
            metadata.putAll(workflowNode.getInputMetaData(name));
            workflowNode.removeInput(name);

            type = workflowNode.getOutputType(name);
            metadata.putAll(workflowNode.getOutputMetaData(name));
            workflowNode.removeOutput(name);
        }

        @Override
        public void undo() {
            final WorkflowNode workflowNode = getWorkflowNode();

            if (type != null) {
                workflowNode.addInput(name, type);
                for (final String key : metadata.keySet()) {
                    final Serializable value = metadata.get(key);
                    workflowNode.setInputMetaData(name, key, value);
                }
            }

            if (type != null) {
                workflowNode.addOutput(name, type);
                for (final String key : metadata.keySet()) {
                    final Serializable value = metadata.get(key);
                    workflowNode.setOutputMetaData(name, key, value);
                }
            }
            super.undo();
        }

        @Override
        protected void execute2() {
        }

        @Override
        protected void undo2() {
        }

    }
}
