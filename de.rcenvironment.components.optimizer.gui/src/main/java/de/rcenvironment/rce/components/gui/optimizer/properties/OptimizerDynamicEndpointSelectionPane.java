/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
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

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.AbstractWorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointSelectionPane;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodeCommand;

/**
 * 
 * Overrides selection pane for optimizer {@link DynamicEndpointSelectionPane}.
 * 
 * @author Sascha Zur
 */
public class OptimizerDynamicEndpointSelectionPane extends DynamicEndpointSelectionPane {

    private int type;

    private FormToolkit tk;

    public OptimizerDynamicEndpointSelectionPane(String genericEndpointTitle, final ComponentDescription.EndpointNature direction,

        TypeSelectionFactory typeSelectionFactory, final WorkflowNodeCommand.Executor executor, int type) {
        super(genericEndpointTitle, direction, typeSelectionFactory, executor);
        this.type = type;
    }

    /**
     * GUI.
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointSelectionPane#createControl
     * (org.eclipse.swt.widgets.Composite, java.lang.String, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    public Control createControl(final Composite parent, String title, FormToolkit toolkit) {
        tk = toolkit;
        section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(title);
        Composite client = toolkit.createComposite(section);
        client.setLayout(new GridLayout(2, false));
        table = toolkit.createTable(client, SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
        final int height = 220;
        tableLayoutData.heightHint = height; // effectively min height
        table.setLayoutData(tableLayoutData);
        TableColumn col1 = new TableColumn(table, SWT.NONE);       col1.setText("Name");
        final int width = 100;
        col1.setWidth(width);
        TableColumn col2 = new TableColumn(table, SWT.NONE);       col2.setText("Data Type");
        col2.setWidth(width);
        TableColumn col3 = new TableColumn(table, SWT.NONE);
        col3.setText("Value");
        col3.setWidth(width);
        if (type == OptimizerComponentConstants.PANE_INPUT){
            TableColumn col4 = new TableColumn(table, SWT.NONE);
            col4.setText("Weight");
            col4.setWidth(width);
            TableColumn col5 = new TableColumn(table, SWT.NONE);
            col5.setText("Goal");
            col5.setWidth(width);
        } else if (type == OptimizerComponentConstants.PANE_CONSTRAINTS){
            TableColumn col4 = new TableColumn(table, SWT.NONE);
            col4.setText("Lower Bound");
            col4.setWidth(width);
            TableColumn col5 = new TableColumn(table, SWT.NONE);
            col5.setText("Upper Bound");
            col5.setWidth(width);
        } else if (type == OptimizerComponentConstants.PANE_OUTPUT){
            TableColumn col4 = new TableColumn(table, SWT.NONE);
            col4.setText("Startvalue");
            col4.setWidth(width);
            TableColumn col5 = new TableColumn(table, SWT.NONE);
            col5.setText("Lower Bound");
            col5.setWidth(width); 
            TableColumn col6 = new TableColumn(table, SWT.NONE);
            col6.setText("Upper Bound");
            col6.setWidth(width); 
        }
        buttonAdd = toolkit.createButton(client, "Add", SWT.FLAT);
        buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonEdit = toolkit.createButton(client, "Edit", SWT.FLAT);
        buttonEdit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonRemove = toolkit.createButton(client, "Remove", SWT.FLAT);
        buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
        SelectionAdapter buttonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == buttonAdd) {
                    // add
                    OptimizerAddVariable dialog =
                        new OptimizerAddVariable(parent.getShell(), "Add variable" , configuration, direction,
                            typeSelectionFactory, tk, type);
                    if (dialog.open() == Dialog.OK) {
                        if (type == OptimizerComponentConstants.PANE_INPUT) {
                            addInput(dialog.getChosenName(), dialog.getChosenDataType(), new TempPara(
                                type, Double.NaN, Double.NaN, Double.NaN, dialog.getWeight(), dialog.getGoal(), dialog.getSolveFor()));
                        } else if (type == OptimizerComponentConstants.PANE_CONSTRAINTS) {
                            addInput(dialog.getChosenName(), dialog.getChosenDataType(), new TempPara(
                                type, dialog.getLowerBound(), dialog.getUpperBound(), Double.NaN, Double.NaN, 
                                Integer.MAX_VALUE, Double.MAX_VALUE));
                        } else if (type == OptimizerComponentConstants.PANE_OUTPUT) {
                            addOutput(dialog.getChosenName(), dialog.getChosenDataType(),  new TempPara(
                                type, dialog.getLowerBound(), dialog.getUpperBound(), 
                                dialog.getStartValue(), Double.NaN, Integer.MAX_VALUE, Double.MAX_VALUE));
                        }
                    }
                } else if (e.widget == buttonEdit) {
                    TableItem[] ti = table.getSelection();
                    String name = ti[0].getText(0);

                    OptimizerAddVariable dialog =
                        new OptimizerAddVariable(parent.getShell(), "Edit", configuration, direction,
                            typeSelectionFactory, tk, type);
                    dialog.setInitialName(name);

                    if (type == OptimizerComponentConstants.PANE_INPUT) {
                        dialog.setInitialDataType(configuration.getInputType(name));
                    } else if (type == OptimizerComponentConstants.PANE_CONSTRAINTS) {
                        dialog.setInitialDataType(configuration.getInputType(name));
                    } else if (type == OptimizerComponentConstants.PANE_OUTPUT) {
                        dialog.setInitialDataType(configuration.getOutputType(name));
                    }

                    dialog.setInitialName(name) ;
                    dialog.setInitialgoal("" + getMetaData(name).get(OptimizerComponentConstants.META_GOAL));
                    dialog.setInitialup("" + getMetaData(name).get(OptimizerComponentConstants.META_UPPERBOUND));
                    dialog.setInitiallow("" + getMetaData(name).get(OptimizerComponentConstants.META_LOWERBOUND));
                    dialog.setInitialstart("" + getMetaData(name).get(OptimizerComponentConstants.META_STARTVALUE));
                    dialog.setInitialweight("" + getMetaData(name).get(OptimizerComponentConstants.META_WEIGHT));
                    dialog.setInitialSolve("" + getMetaData(name).get(OptimizerComponentConstants.META_SOLVEFOR));

                    if (dialog.open() == Dialog.OK) {

                        if (type == OptimizerComponentConstants.PANE_INPUT) {
                            changeInput(name, dialog.getChosenName(), dialog.getChosenDataType(), new TempPara(
                                type, Double.NaN, Double.NaN, Double.NaN, dialog.getWeight(), dialog.getGoal(), dialog.getSolveFor()));
                        } else if (type == OptimizerComponentConstants.PANE_CONSTRAINTS) {
                            double low = dialog.getLowerBound();
                            double up = dialog.getUpperBound();
                            changeInput(name, dialog.getChosenName(), dialog.getChosenDataType(), new TempPara(
                                type, low, up, Double.NaN, Double.NaN, Integer.MAX_VALUE, Double.MAX_VALUE));
                        } else if (type == OptimizerComponentConstants.PANE_OUTPUT) {
                            double low = dialog.getLowerBound();
                            double up = dialog.getUpperBound();
                            changeOutput(name, dialog.getChosenName(), dialog.getChosenDataType(), new TempPara(
                                type, low, up, dialog.getStartValue(), Double.NaN, Integer.MAX_VALUE, Double.MAX_VALUE));
                        }
                    }         
                } else if (e.widget == buttonRemove) {
                    // remove selected; relies on proper button activation
                    String name = (String) table.getSelection()[0].getData();
                    if (type == OptimizerComponentConstants.PANE_INPUT) {
                        removeInput(name);
                    } else if (type == OptimizerComponentConstants.PANE_CONSTRAINTS) {
                        removeInput(name);
                    } else if (type == OptimizerComponentConstants.PANE_OUTPUT) {
                        removeOutput(name);
                    }
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

    private void addInput(final String name, final String types, TempPara tmp) {
        final WorkflowNodeCommand command =
            new AddInputOutputCommand(ComponentDescription.EndpointNature.Input, name, types,
                tmp.vartype, tmp.lowerbound, tmp.upperbound, tmp.startvalue, tmp.weight, tmp.goal, tmp.solve);
        execute(command);
    }

    private void addOutput(final String name, final String types, TempPara tmp) {
        final WorkflowNodeCommand command =
            new AddInputOutputCommand(ComponentDescription.EndpointNature.Output, name, types,
                tmp.vartype, tmp.lowerbound, tmp.upperbound, tmp.startvalue, tmp.weight, tmp.goal, tmp.solve);
        execute(command);
    }
    private void changeInput(final String oldName, String newName, final String newType, TempPara tmp){
        final WorkflowNodeCommand command = new ChangeInputOutputCommand(ComponentDescription.EndpointNature.Input, 
            oldName, newName, newType, tmp.vartype, tmp.lowerbound, tmp.upperbound, tmp.startvalue, tmp.weight, 
            tmp.goal, tmp.solve);
        execute(command);
    }
    private void changeOutput(final String oldName, final String newName, final String newType, TempPara tmp) {
        final WorkflowNodeCommand command = new ChangeInputOutputCommand(ComponentDescription.EndpointNature.Output,
            oldName, newName, newType, tmp.vartype, tmp.lowerbound, tmp.upperbound, tmp.startvalue, tmp.weight, 
            tmp.goal, tmp.solve);
        execute(command);
    }
    private void removeOutput(final String name) {
        final WorkflowNodeCommand command = new RemoveInputOutputCommand(ComponentDescription.EndpointNature.Output,
            name);
        execute(command);
    }

    private void removeInput(final String name) {
        final WorkflowNodeCommand command = new RemoveInputOutputCommand(ComponentDescription.EndpointNature.Input,
            name);
        execute(command);
    }
    /**
     * Loads the current endpoint data into the UI table.
     */
    protected void fillTable() {
        table.removeAll();
        List<String> endpointNames = new LinkedList<String>();
        if (direction == ComponentDescription.EndpointNature.Input) {

            for (String key : configuration.getDynamicInputDefinitions().keySet()){
                if (!getMetaData(key).isEmpty() 
                    && getMetaData(key).get(OptimizerComponentConstants.META_TYPE) != null 
                    && type == (Integer) getMetaData(key).get(OptimizerComponentConstants.META_TYPE)) {
                    endpointNames.add(key);
                }
            }

        } else {
            endpointNames = new ArrayList<String>(configuration.getDynamicOutputDefinitions().keySet());
        }
        Collections.sort(endpointNames);
        for (String name : endpointNames) {
            TableItem item = new TableItem(table, SWT.None);
            item.setText(0, name);
            item.setText(2, "");
            final String endpointType = getType(name);
            // FIXME provide display name of input type directly, e.g. in TypeSelectionFactory
            for (TypeSelectionOption opt : typeSelectionFactory.getTypeSelectionOptions()) {
                if (opt.getTypeName().equals(endpointType)) {
                    item.setText(1, opt.getDisplayName());
                    break;
                }
            }
            if (type == OptimizerComponentConstants.PANE_OUTPUT){
                item.setText(3, "" + (getMetaData(name).get(OptimizerComponentConstants.META_STARTVALUE)));
                item.setText(4, "" + getMetaData(name).get(OptimizerComponentConstants.META_LOWERBOUND));
                item.setText(5, "" + getMetaData(name).get(OptimizerComponentConstants.META_UPPERBOUND));

                item.setData(name);

            }
            if (type == OptimizerComponentConstants.PANE_INPUT){
                item.setText(3, "" + (getMetaData(name).get(OptimizerComponentConstants.META_WEIGHT)));
                if ((getMetaData(name).get(OptimizerComponentConstants.META_GOAL)) != null){
                    if ((Integer) (getMetaData(name).get(OptimizerComponentConstants.META_GOAL)) == 0){
                        item.setText(4, "Minimize");
                    }
                    if ((Integer) (getMetaData(name).get(OptimizerComponentConstants.META_GOAL)) == 1){
                        item.setText(4, "Maximize");
                    }
                    if ((Integer) (getMetaData(name).get(OptimizerComponentConstants.META_GOAL)) == 2){
                        item.setText(4, "Solve for: " + getMetaData(name).get(OptimizerComponentConstants.META_SOLVEFOR));
                    }
                }
                item.setData(name);
            }
            if (type == OptimizerComponentConstants.PANE_CONSTRAINTS){
                item.setText(3, "" + getMetaData(name).get(OptimizerComponentConstants.META_LOWERBOUND));
                item.setText(4, "" + getMetaData(name).get(OptimizerComponentConstants.META_UPPERBOUND));

                item.setData(name);

            }
        }

    }

    /**
     * Adds a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private abstract static class SetInputOutputMetadataCommand extends AbstractWorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;

        private final String name;

        private final Map<String, Serializable> newValues = new HashMap<String, Serializable>();

        private final Map<String, Serializable> oldValues = new HashMap<String, Serializable>();

        protected SetInputOutputMetadataCommand(final ComponentDescription.EndpointNature direction,
            final String name, int vartype, final double lowerbound, final double upperbound, 
            final double startvalue, double weight, int goal, double solve) {
            this.direction = direction;
            this.name = name;
            newValues.put(OptimizerComponentConstants.META_TYPE, vartype);
            newValues.put(OptimizerComponentConstants.META_LOWERBOUND, lowerbound);
            newValues.put(OptimizerComponentConstants.META_UPPERBOUND, upperbound);
            newValues.put(OptimizerComponentConstants.META_STARTVALUE, startvalue);
            newValues.put(OptimizerComponentConstants.META_WEIGHT, weight);
            newValues.put(OptimizerComponentConstants.META_GOAL, goal);
            newValues.put(OptimizerComponentConstants.META_SOLVEFOR, solve);

        }

        @Override
        public final void execute() {
            super.execute();
            executeSpecial();
            final WorkflowNode workflowNode = getWorkflowNode();
            final Map<String, Serializable> metadata;
            oldValues.clear();
            switch (direction) {
            case Input:
                metadata = workflowNode.getInputMetaData(name);
                for (final String key : newValues.keySet()) {
                    oldValues.put(key, metadata.get(key));
                    final Serializable newValue = newValues.get(key);
                    workflowNode.setInputMetaData(name, key, newValue);
                }
                break;
            case Output:
                metadata = workflowNode.getOutputMetaData(name);
                for (final String key : newValues.keySet()) {
                    oldValues.put(key, metadata.get(key));
                    final Serializable newValue = newValues.get(key);
                    workflowNode.setOutputMetaData(name, key, newValue);
                }
                break;
            default:
                throw new RuntimeException();
            }
        }

        protected abstract void executeSpecial();

        @Override
        public final void undo() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                for (final String key : newValues.keySet()) {
                    final Serializable oldValue = oldValues.get(key);
                    workflowNode.setInputMetaData(name, key, oldValue);
                }
                break;
            case Output:
                for (final String key : newValues.keySet()) {
                    final Serializable oldValue = oldValues.get(key);
                    workflowNode.setOutputMetaData(name, key, oldValue);
                }
                break;
            default:
                throw new RuntimeException();
            }
            undoSpecial();
            super.undo();
        }

        protected abstract void undoSpecial();

    }


    /**
     * Adds a input to a {@link WorkflowNode}.
     * 
     * @author Sascha Zur
     */
    private static class AddInputOutputCommand extends SetInputOutputMetadataCommand {

        private final ComponentDescription.EndpointNature direction;

        private final String name;

        private final String type;

        private String oldType;

        protected AddInputOutputCommand(final ComponentDescription.EndpointNature direction,
            final String name, final String type, int vartype, 
            final double lowerbound, final double upperbound, final double startvalue, double weight, int goal, double solve) {
            super(direction, name, vartype, lowerbound, upperbound, startvalue, weight, goal, solve);
            this.direction = direction;
            this.name = name;
            this.type = type;
        }

        @Override
        public void executeSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                oldType = workflowNode.getInputType(name);
                workflowNode.addInput(name, type);
                break;
            case Output:
                oldType = workflowNode.getOutputType(name);
                workflowNode.addOutput(name, type);
                break;
            default:
                throw new RuntimeException();
            }
        }

        @Override
        public void undoSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                if (oldType != null) {
                    workflowNode.addInput(name, oldType);
                } else {
                    workflowNode.removeInput(name);
                }
                break;
            case Output:
                if (oldType != null) {
                    workflowNode.addOutput(name, oldType);
                } else {
                    workflowNode.removeOutput(name);
                }
                break;
            default:
                throw new RuntimeException();
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
     * Changes a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private static class ChangeInputOutputCommand extends SetInputOutputMetadataCommand {

        private final ComponentDescription.EndpointNature direction;

        private final String oldName;

        private final String newName;

        private final String newType;

        private String oldType;

        protected ChangeInputOutputCommand(final ComponentDescription.EndpointNature direction, 
            final String oldName, final String newName, final String newType, int vartype, 
            double lowerbound, double upperbound, final double startvalue, double weight, int goal, double solve) {
            super(direction, newName, vartype, lowerbound, upperbound, startvalue, weight, goal, solve);
            this.direction = direction;
            this.oldName = oldName;
            this.newName = newName;
            this.newType = newType;
        }

        @Override
        public void executeSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                oldType = workflowNode.getInputType(oldName);
                workflowNode.changeInput(oldName, newName, newType);
                break;
            case Output:
                oldType = workflowNode.getOutputType(oldName);
                workflowNode.changeOutput(oldName, newName, newType);
                break;
            default:
                throw new RuntimeException();
            }
        }

        @Override
        public void undoSpecial() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                workflowNode.changeInput(newName, oldName, oldType);
                break;
            case Output:
                workflowNode.changeOutput(newName, oldName, oldType);
                break;
            default:
                throw new RuntimeException();
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
     * Removes a input to a {@link WorkflowNode}.
     *
     * @author Sascha Zur
     */
    private static class RemoveInputOutputCommand extends AbstractWorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;

        private final String name;

        private String type;

        private final Map<String, Serializable> metadata = new HashMap<String, Serializable>();

        protected RemoveInputOutputCommand(final ComponentDescription.EndpointNature direction,
            final String name) {
            this.direction = direction;
            this.name = name;
        }

        @Override
        public void execute() {
            super.execute();
            final WorkflowNode workflowNode = getWorkflowNode();
            metadata.clear();
            switch (direction) {
            case Input:
                type = workflowNode.getInputType(name);
                metadata.putAll(workflowNode.getInputMetaData(name));
                workflowNode.removeInput(name);
                break;
            case Output:
                type = workflowNode.getOutputType(name);
                metadata.putAll(workflowNode.getOutputMetaData(name));
                workflowNode.removeOutput(name);
                break;
            default:
                throw new RuntimeException();
            }
        }

        @Override
        public void undo() {
            final WorkflowNode workflowNode = getWorkflowNode();
            switch (direction) {
            case Input:
                if (type != null) {
                    workflowNode.addInput(name, type);
                    for (final String key : metadata.keySet()) {
                        final Serializable value = metadata.get(key);
                        workflowNode.setInputMetaData(name, key, value);
                    }
                }
                break;
            case Output:
                if (type != null) {
                    workflowNode.addOutput(name, type);
                    for (final String key : metadata.keySet()) {
                        final Serializable value = metadata.get(key);
                        workflowNode.setOutputMetaData(name, key, value);
                    }
                }
                break;
            default:
                throw new RuntimeException();
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

    /**
     * 
     * Temporary class for too much parameters.
     *
     * @author Sascha Zur
     */
    public class TempPara{
        private int vartype;
        private double lowerbound;
        private double upperbound; 
        private double startvalue;
        private double weight;
        private int goal;
        private double solve;

        
        
        protected TempPara(int vartype, double lowerbound, double upperbound, double startvalue, double weight, int goal, double solve) {
            super();
            this.vartype = vartype;
            this.lowerbound = lowerbound;
            this.upperbound = upperbound;
            this.startvalue = startvalue;
            this.weight = weight;
            this.goal = goal;
            this.solve = solve;
        }

    }
}
