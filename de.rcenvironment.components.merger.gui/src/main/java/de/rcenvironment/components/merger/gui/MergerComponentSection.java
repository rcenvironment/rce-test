/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.merger.gui;


import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.components.merger.common.MergerComponentConstants;
import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.component.workflow.ComponentInstanceConfiguration;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodeCommand;
import de.rcenvironment.commons.channel.DataManagementFileReference;
/**
 * Provides a GUI for the parametric study component.
 *
 * @author Sascha Zur
 */
public class MergerComponentSection extends ValidatingWorkflowNodePropertySection {
    
    private static final int MAXIMUM_INPUT_COUNT = 100;

    private int lastInputCountValue = 1;
    private String lastInputType = MergerComponentConstants.INPUT_DATATYPE_LIST[0];
    private String mergeInputName = "In_";
    
    private Combo dataTypes;
    private Combo inputCount; 
    
    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {

        final Section sectionProperties = PropertyTabGuiHelper.createSingleColumnSectionComposite(
            parent, getWidgetFactory(), "");
        final Composite sectionInstallationClient = getWidgetFactory().createComposite(sectionProperties);
        sectionProperties.setClient(sectionInstallationClient);
        sectionInstallationClient.setLayout(new GridLayout(2, false));

        Label inputTypeLabel = new Label(sectionInstallationClient, SWT.NONE);
        inputTypeLabel.setText(Messages.inputType);
        dataTypes = new Combo(sectionInstallationClient, SWT.READ_ONLY);
        for (String str : MergerComponentConstants.INPUT_DATATYPE_LIST){
            dataTypes.add(str);
        }
        dataTypes.select(0);
        dataTypes.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                String newInput = dataTypes.getText();
                
                for (int i = 1; i <= lastInputCountValue; i++){
                    final WorkflowNodeCommand removeCommand = new RemoveDynamicEndpointCommand(
                        mergeInputName + getString(i), lastInputType);
                    execute(removeCommand);
                    final WorkflowNodeCommand command = new AddDynamicEndpointCommand(mergeInputName 
                        + getString(i), newInput);
                    execute(command);
                }
                final WorkflowNodeCommand removeCommand = new RemoveOutputEndpointCommand(
                    MergerComponentConstants.OUTPUT_NAME, lastInputType);
                execute(removeCommand);
                final WorkflowNodeCommand command = new AddOutputEndpointCommand(MergerComponentConstants.OUTPUT_NAME, newInput);
                execute(command);
                lastInputType = newInput;
                setProperty(MergerComponentConstants.INPUT_DATATYPE, newInput);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }
        });


        Label inputCountLabel = new Label(sectionInstallationClient, SWT.NONE);
        inputCountLabel.setText(Messages.inputCount);     
        inputCount = new Combo(sectionInstallationClient, SWT.READ_ONLY);
        for (int i = 1; i <= MAXIMUM_INPUT_COUNT; i++){
            inputCount.add("" + i);
        }
        inputCount.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                int newCount = inputCount.getSelectionIndex() + 1;

                if (getInputs().size() != lastInputCountValue){
                    for (int i = 2; i <= getInputs().size(); i++){
                        final WorkflowNodeCommand command = new RemoveDynamicEndpointCommand(mergeInputName + getString(i), lastInputType);
                        execute(command);
                    }
                }

                if (newCount > lastInputCountValue){
                    for (int i = lastInputCountValue + 1; i <= newCount; i++){
                        final WorkflowNodeCommand command = new AddDynamicEndpointCommand(mergeInputName + getString(i), lastInputType);
                        execute(command);
                    }
                } else if (newCount < lastInputCountValue){
                    for (int i = lastInputCountValue; i > newCount && i > 1; i--){
                        final WorkflowNodeCommand command = new RemoveDynamicEndpointCommand(mergeInputName + getString(i), lastInputType);
                        execute(command);
                    }
                }
                lastInputCountValue = newCount;
                setProperty(MergerComponentConstants.OUTPUT_NAME, newCount);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }
        });
    }
    
    // Adds zeros if i is less than 100 so that the order is right.
    private String getString(int i) {
        String result = "";
        if (i < 10){
            result += "0";
        }
        if (i < 10 * 10){
            result += "0";
        }
        result += i;
        return result;
    }

    // converts the strings in the combobox to the real type RCE uses.
    private String getRCEType(String type){
        String result = null;
        if (type.equals(MergerComponentConstants.INPUT_DATATYPE_LIST[0])){
            result = Double.class.getName();
        }
        if (type.equals(MergerComponentConstants.INPUT_DATATYPE_LIST[1])){
            result = Integer.class.getName();
        }
        if (type.equals(MergerComponentConstants.INPUT_DATATYPE_LIST[2])){
            result =  String.class.getName();
        }
        if (type.equals(MergerComponentConstants.INPUT_DATATYPE_LIST[3])){
            result =  Serializable.class.getName();
        }
        if (type.equals(MergerComponentConstants.INPUT_DATATYPE_LIST[4])){
            result =  DataManagementFileReference.class.getName();
        }
        return result;
    }
    
    @Override
    public void aboutToBeShown() {
        super.aboutToBeShown();
        if (getProperty(MergerComponentConstants.INPUT_DATATYPE) != null){
            lastInputType = (String) getProperty(MergerComponentConstants.INPUT_DATATYPE);
        } else {
            lastInputType = MergerComponentConstants.INPUT_DATATYPE_DEFAULT;
        }
        dataTypes.select(dataTypes.indexOf(lastInputType));
        
        if (getProperty(MergerComponentConstants.OUTPUT_NAME) != null) {
            lastInputCountValue = (Integer) getProperty(MergerComponentConstants.OUTPUT_NAME);
        } else {
            lastInputCountValue = 1;
        }
        inputCount.select(lastInputCountValue - 1);

        if (getInputs().size() == 0){
            final WorkflowNodeCommand command = new AddDynamicEndpointCommand(mergeInputName + "001", lastInputType);
            execute(command);
        }
        if (getOutputs().size() == 0){
            final WorkflowNodeCommand command = new AddOutputEndpointCommand(MergerComponentConstants.OUTPUT_NAME, lastInputType);
            execute(command);
        }
    }
    
    
    @Override
    public void refresh() {
        aboutToBeShown();        
    };
    
    
    
    /**
     * {@link WorkflowNodeCommand} adding dynamic endpoints to a <code>WorkflowNode</code>.
     * 
     * @author Christian Weiss
     */
    private final class AddDynamicEndpointCommand extends WorkflowNodeCommand {


        private String name;

        private String type;

        private boolean executable = true;

        private boolean undoable = false;

        /**
         * The constructor.
         * 
         */
        private AddDynamicEndpointCommand(String name, String type) {
            this.name = name;
            this.type = type;
        }



        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                // perform changes
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.addInput(name, getRCEType(type));
                // update view
                executable = false;
                undoable = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.removeInput(name);

                executable = true;
                undoable = false;
            }
        }



        @Override
        public void initialize() {

        }
    }
    /**
     * {@link WorkflowNodeCommand} adding dynamic endpoints to a <code>WorkflowNode</code>.
     * 
     * @author Christian Weiss
     */
    private final class AddOutputEndpointCommand extends WorkflowNodeCommand {


        private String name;

        private String type;

        private boolean executable = true;

        private boolean undoable = false;

        /**
         * The constructor.
         * 
         */
        private AddOutputEndpointCommand(String name, String type) {
            this.name = name;
            this.type = type;
        }



        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                // perform changes
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.addOutput(name, getRCEType(type));
                // update view
                executable = false;
                undoable = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.removeOutput(name);

                executable = true;
                undoable = false;
            }
        }



        @Override
        public void initialize() {

        }
    }
    /**
     * {@link WorkflowNodeCommand} editing dynamic endpoints in a <code>WorkflowNode</code>.
     *
     * @author Christian Weiss
     */
    private final class RemoveDynamicEndpointCommand extends WorkflowNodeCommand {
        private final String name;

        private final String type;

        private boolean executable = true;

        private boolean undoable = false;

        /**
         * The constructor.
         * @param name the name of the endpoint
         * @param type the type of the endpoint
         */
        private RemoveDynamicEndpointCommand(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public void initialize() {
            // do nothing
        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.removeInput(name);
                executable = false;
                undoable = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.addInput(name,  getRCEType(type));

                executable = true;
                undoable = false;
            }
        }
    }
    /**
     * {@link WorkflowNodeCommand} editing dynamic endpoints in a <code>WorkflowNode</code>.
     *
     * @author Christian Weiss
     */
    private final class RemoveOutputEndpointCommand extends WorkflowNodeCommand {
        private final String name;

        private final String type;

        private boolean executable = true;

        private boolean undoable = false;

        /**
         * The constructor.
         * @param name the name of the endpoint
         * @param type the type of the endpoint
         */
        private RemoveOutputEndpointCommand(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public void initialize() {
            // do nothing
        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.removeOutput(name);
                executable = false;
                undoable = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.addOutput(name,  getRCEType(type));

                executable = true;
                undoable = false;
            }
        }
    }
    /**
     * {@link WorkflowNodeCommand} editing dynamic endpoints in a <code>WorkflowNode</code>.
     *
     * @author Christian Weiss
     */
    private final class EditDynamicEndpointCommand extends WorkflowNodeCommand {

        private final String oldName;

        private final String oldType;

        private String newName;

        private String newType;

        private boolean executable = true;

        private boolean undoable = false;

        /**
         * The constructor.
         * @param name the name of the endpoint
         * @param type the type of the endpoint
         */
        private EditDynamicEndpointCommand(final String oldName, final String oldType, String newType) {
            this.oldName = oldName;
            this.oldType = oldType;
            this.newType = newType;
            this.newName = this.oldName;
        }

        @Override
        public void initialize() {

        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                // perform changes
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.changeInput(oldName, newName,  getRCEType(newType));
                // update view
                executable = false;
                undoable = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                componentInstanceConfiguration.changeInput(newName, oldName,  getRCEType(oldType));
                executable = true;
                undoable = false;
            }
        }
    }
}
