/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.properties;

import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.METADATA_ADDRESS;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.XL_FILENAME;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.DISCOVER_INPUT_REGEX;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.DISCOVER_OUTPUT_REGEX;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.DEFAULT_DATATYPE;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.DEFAULT_TABLEEXPANDING;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.DEFAULT_TABLEPRUNING;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.METADATA_EXPANDING;
import static de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants.METADATA_PRUNING;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ComponentInstanceConfiguration;
import de.rcenvironment.rce.components.excel.commons.ExcelAddress;
import de.rcenvironment.rce.components.excel.commons.ExcelUtils;
import de.rcenvironment.rce.components.excel.commons.ExcelService;
import de.rcenvironment.rce.components.excel.commons.SimpleExcelService;
import de.rcenvironment.rce.gui.workflow.editor.properties.AdvancedDynamicEndpointSelectionPane;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodeCommand;


/**
 * A UI part to display and edit a set of endpoints. 
 *
 * @author Patrick Schaefer
 * @author Markus Kunde
 */
public class VariablesSelectionPane extends AdvancedDynamicEndpointSelectionPane {
 
    private Button buttonAutoDiscover;
    
    public VariablesSelectionPane(String genericEndpointTitle, final ComponentDescription.EndpointNature direction,
        TypeSelectionFactory defaultTypeSelectionFactory, final WorkflowNodeCommand.Executor executor) {
        
        super(genericEndpointTitle, direction, defaultTypeSelectionFactory, executor);
        
        
    }
    
    @Override
    public Control createControl(final Composite parent, String title, FormToolkit toolkit) {
        Control superControl = super.createControl(parent, title, toolkit);
        
        Label autodiscoverDescription = toolkit.createLabel(client, "");
        autodiscoverDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        buttonAutoDiscover = toolkit.createButton(client, 
            de.rcenvironment.rce.components.excel.gui.properties.Messages.autoDiscover, SWT.FLAT);
        buttonAutoDiscover.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        
        
        SelectionAdapter excelButtonListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == buttonAutoDiscover) {
                    // autodiscover
                    Serializable serial = configuration.getProperty(XL_FILENAME);
                    if (serial instanceof String) {
                        final File xlFile = ExcelUtils.getAbsoluteFile((String) serial);
                        if (xlFile != null) {
                            final WorkflowNodeCommand command = new AddDiscoveredEndpointCommand(direction, xlFile);
                            execute(command);
                        }
                    }
                } else if (e.widget == buttonAdd) {
                    Serializable serial = configuration.getProperty(XL_FILENAME);
                    if (serial instanceof String) {
                        final File xlFile = ExcelUtils.getAbsoluteFile((String) serial);
 
                        // add
                        final WorkflowNodeCommand command = new AddDynamicEndpointCommand(direction, xlFile);
                        execute(command);  
                    } 
                } else if (e.widget == buttonEdit) {
                    Serializable serial = configuration.getProperty(XL_FILENAME);
                    if (serial instanceof String) {
                        final File xlFile = ExcelUtils.getAbsoluteFile((String) serial);
                        
                        // edit selected; relies on proper button activation
                        final String name = (String) table.getSelection()[0].getData();
                        final String type = getType(name);
                        final String dataUse = (String) getMetaData(name).get(ComponentConstants.METADATAKEY_INPUT_USAGE);
                        final boolean expanding = (Boolean) getMetaData(name).get(METADATA_EXPANDING);
                        final boolean pruning = (Boolean) getMetaData(name).get(METADATA_PRUNING);

                        final String address = (String) getMetaData(name).get(METADATA_ADDRESS);

                        final WorkflowNodeCommand command = new EditDynamicEndpointCommand(direction,
                            name, type, dataUse, new ExcelAddress(xlFile, address), expanding, pruning, xlFile);
                        execute(command);
                    }

                }
            }

        };
                
        buttonAutoDiscover.addSelectionListener(excelButtonListener);
        buttonAdd.addSelectionListener(excelButtonListener);
        buttonEdit.addSelectionListener(excelButtonListener);
        buttonAdd.removeSelectionListener(buttonListener);
        buttonEdit.removeSelectionListener(buttonListener);
        
        return superControl;
    }
    
    
    
    /**
     * {@link WorkflowNodeCommand} adding dynamic endpoints to a <code>WorkflowNode</code>.
     * 
     * @author Markus Kunde
     */
    protected final class AddDynamicEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;
        
        private File xlFile;
        
        private boolean executable = true;
        
        private boolean undoable = false;
        
        private String name;
        
        private String type;
        
        private String dataUse;
        
        private boolean expand;
        
        private boolean pruning;
        
        private ExcelAddress addr;
        
        
        /**
         * The constructor.
         * 
         * @param direction the direction
         * @param xlFile Excel file
         */
        public AddDynamicEndpointCommand(final ComponentDescription.EndpointNature direction, final File xlFile) {
            this.direction = direction;
            this.xlFile = xlFile;
        }
        
        @Override
        public void initialize() {
            // TODO Read all configuration values from dialog
            VariablesEditDialog dialog = new VariablesEditDialog(Display.getDefault().getActiveShell(), 
                Messages.newChannel + genericEndpointTitle, getWorkflowNode(), direction, typeSelectionFactory, xlFile);
            
            if (dialog.open() == Dialog.OK) {
                name = dialog.getChosenName();
                addr = dialog.getExcelAddress();
                
                // check for duplicate name, should not happen as it is avoided in the dialog
                String dataType;
                if (direction == EndpointNature.Input) {
                    dataType = configuration.getInputType(name);
                } else {
                    dataType = configuration.getOutputType(name);
                }
                if (dataType != null) {
                    throw new RuntimeException();
                }
                type = dialog.getChosenDataType();
                
                if (direction == EndpointNature.Input){
                    dataUse = dialog.getDataUseSelection();
                    expand = dialog.getExpanding();
                    
                    if (!configuration.validateInputName(name)) {
                        throw new RuntimeException("Channelname is not allowed. '" + name + "'");
                    }
                } else {
                    pruning = dialog.getPruning();
                    if (!configuration.validateOutputName(name)) {
                        throw new RuntimeException("Channelname is not allowed. '" + name + "'");
                    }
                }
            } else {
                executable = false;
                undoable = false;
            }
        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void execute() {
            if (executable) {
                // perform changes

                final ComponentInstanceConfiguration instConfig = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    // Add as Input
                    instConfig.addInput(name, type);
                    instConfig.setInputMetaData(name, METADATA_ADDRESS, addr.getFullAddress());
                    instConfig.setInputMetaData(name, METADATA_EXPANDING, expand);
                    instConfig.setInputMetaData(name, METADATA_PRUNING, DEFAULT_TABLEPRUNING);
                    instConfig.setInputMetaData(name, ComponentConstants.METADATAKEY_INPUT_USAGE, dataUse);
                } else {
                    // Add as Output
                    instConfig.addOutput(name, type);
                    instConfig.setOutputMetaData(name, METADATA_ADDRESS, addr.getFullAddress());
                    instConfig.setOutputMetaData(name, METADATA_EXPANDING, DEFAULT_TABLEEXPANDING);
                    instConfig.setOutputMetaData(name, METADATA_PRUNING, pruning);
                }

                // update view
                executable = false;
                undoable = true;
            }
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    componentInstanceConfiguration.removeInput(name);
                } else {
                    componentInstanceConfiguration.removeOutput(name);
                }
                executable = true;
                undoable = false;
            }
        }
        
    }
    
    
    /**
     * {@link WorkflowNodeCommand} editing dynamic endpoints in a <code>WorkflowNode</code>.
     *
     * @author Markus Kunde
     */
    protected final class EditDynamicEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;
        
        private File xlFile;
        
        private boolean executable = true;
        
        private boolean undoable = false;
        
        private final String oldName;
        
        private final String oldType; 
        
        private String oldDataUse;
        
        private ExcelAddress oldAddr;
        
        private boolean oldExpand;
        
        private boolean oldPruning;
        
        private String newName;

        private String newType;
        
        private String newDataUse;
        
        private boolean newExpand;
        
        private boolean newPruning;
        
        private ExcelAddress newAddr;
        
        /**
         * The constructor.
         * @param name the name of the endpoint
         * @param type the type of the endpoint
         */
        public EditDynamicEndpointCommand(final ComponentDescription.EndpointNature direction,
                final String oldName, final String oldType, final String oldDataUse, final ExcelAddress oldAddr,
                final boolean oldExpand, final boolean oldPruning, final File xlFile) {
            this.direction = direction;
            this.oldName = oldName;
            this.oldType = oldType;
            this.oldDataUse = oldDataUse;
            this.oldAddr = oldAddr;
            this.oldExpand = oldExpand;
            this.oldPruning = oldPruning;
            this.xlFile = xlFile;
        }
        
        
        @Override
        public void initialize() {
            VariablesEditDialog dialog = new VariablesEditDialog(Display.getDefault().getActiveShell(), 
                Messages.editChannel + genericEndpointTitle, getWorkflowNode(), direction, typeSelectionFactory, xlFile);
            dialog.setInitialName(oldName);
            dialog.setInitialDataType(oldType);
            dialog.setInitialDataUse(oldDataUse);
            dialog.setPruning(oldPruning);
            dialog.setExpanding(oldExpand);
            dialog.setExcelAddress(oldAddr);
            
            if (dialog.open() == Dialog.OK) {
                newName = dialog.getChosenName();
                newType = dialog.getChosenDataType();
                newDataUse = dialog.getDataUseSelection();
                newAddr = dialog.getExcelAddress();
                newExpand = dialog.getExpanding();
                newPruning = dialog.getPruning();
            } else {
                executable = false;
                undoable = false;
            }
        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void execute() {
            if (executable) {
                final ComponentInstanceConfiguration instConfig = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    instConfig.changeInput(oldName, newName, newType);
                    instConfig.setInputMetaData(newName, ComponentConstants.METADATAKEY_INPUT_USAGE, newDataUse);
                    instConfig.setInputMetaData(newName, METADATA_ADDRESS, newAddr.getFullAddress());
                    instConfig.setInputMetaData(newName, METADATA_EXPANDING, newExpand);
                    instConfig.setInputMetaData(newName, METADATA_PRUNING, DEFAULT_TABLEPRUNING);
                } else {
                    instConfig.changeOutput(oldName, newName, newType);
                    instConfig.setOutputMetaData(newName, METADATA_ADDRESS, newAddr.getFullAddress());
                    instConfig.setOutputMetaData(newName, METADATA_EXPANDING, DEFAULT_TABLEEXPANDING);
                    instConfig.setOutputMetaData(newName, METADATA_PRUNING, newPruning);
                }
                // update view
                executable = false;
                undoable = true;
            }
        }

        @Override
        public void undo() {
            if (undoable) {
                final ComponentInstanceConfiguration instConfig = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    instConfig.changeInput(newName, oldName, oldType);
                    instConfig.getInputMetaData(oldName).remove(ComponentConstants.METADATAKEY_INPUT_USAGE);
                    instConfig.setInputMetaData(oldName, ComponentConstants.METADATAKEY_INPUT_USAGE, oldDataUse);
                    instConfig.setInputMetaData(oldName, METADATA_ADDRESS, oldAddr.getFullAddress());
                    instConfig.setInputMetaData(oldName, METADATA_EXPANDING, oldExpand);
                    instConfig.setInputMetaData(oldName, METADATA_PRUNING, DEFAULT_TABLEPRUNING);
                } else {
                    instConfig.changeOutput(newName, oldName, oldType);
                    instConfig.setInputMetaData(oldName, METADATA_ADDRESS, oldAddr.getFullAddress());
                    instConfig.setInputMetaData(oldName, METADATA_EXPANDING, DEFAULT_TABLEEXPANDING);
                    instConfig.setInputMetaData(oldName, METADATA_PRUNING, oldPruning);
                }
                executable = true;
                undoable = false;
            }
        }
        
    }
    
    


    /**
     * {@link WorkflowNodeCommand} adding discovered endpoints to a <code>WorkflowNode</code>.
     * 
     * @author Markus Kunde
     */
    protected final class AddDiscoveredEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;
        
        private File xlFile;
        
        private List<ExcelAddress> addedAsInput = new ArrayList<ExcelAddress>();

        private List<ExcelAddress> addedAsOutput = new ArrayList<ExcelAddress>();
                
        private boolean executable = true;
        
        private boolean undoable = false;
        
        private ExcelService excelService = new SimpleExcelService();

        /**
         * The constructor.
         * 
         * @param direction the direction
         */
        public AddDiscoveredEndpointCommand(final ComponentDescription.EndpointNature direction, final File xlFile) {
            this.direction = direction;
            this.xlFile = xlFile;
        }
        
        @Override
        public void initialize() {
 
            if (!excelService.isValidExcelFile(xlFile)) {
                executable = false;
                undoable = false;
            }
        }

        @Override
        public boolean canExecute() {
            return executable;
        }

        @Override
        public void execute() {
            if (executable) {
                // perform changes               
                String regex = null;
                if (direction == ComponentDescription.EndpointNature.Input) {
                    regex = DISCOVER_INPUT_REGEX;
                } else {
                    regex = DISCOVER_OUTPUT_REGEX;
                }
                
                final ComponentInstanceConfiguration instConfig = getComponentInstanceConfiguration();
                for (ExcelAddress addr: excelService.getUserDefinedCellNames(xlFile)) {
                    if (addr.isUserDefindNameOfScheme(regex)) {        
                        if (direction == ComponentDescription.EndpointNature.Input) {
                            if (configuration.validateInputName(addr.getUserDefinedName())) {
                                //Add as Input
                                final String inputName = addr.getUserDefinedName();
                                instConfig.addInput(inputName, DEFAULT_DATATYPE);
                                instConfig.setInputMetaData(inputName, METADATA_ADDRESS, addr.getFullAddress());
                                instConfig.setInputMetaData(inputName, METADATA_EXPANDING, DEFAULT_TABLEEXPANDING);
                                instConfig.setInputMetaData(inputName, METADATA_PRUNING, DEFAULT_TABLEPRUNING);
                                instConfig.setInputMetaData(inputName, ComponentConstants.METADATAKEY_INPUT_USAGE, 
                                    ComponentConstants.INPUT_USAGE_TYPE_DEFAULT);
                                addedAsInput.add(addr);
                            }
                        } else {
                            if (configuration.validateOutputName(addr.getUserDefinedName())) {
                                //Add as Output
                                final String outputName = addr.getUserDefinedName();
                                instConfig.addOutput(addr.getUserDefinedName(), DEFAULT_DATATYPE);
                                instConfig.setOutputMetaData(outputName, METADATA_ADDRESS, addr.getFullAddress());
                                instConfig.setOutputMetaData(outputName, METADATA_EXPANDING, DEFAULT_TABLEEXPANDING);
                                instConfig.setOutputMetaData(outputName, METADATA_PRUNING, DEFAULT_TABLEPRUNING);
                                addedAsOutput.add(addr);
                            }
                        }
                    }
                }
                
                
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
                if (direction == ComponentDescription.EndpointNature.Input) {
                    for (ExcelAddress addr: addedAsInput) {
                        componentInstanceConfiguration.removeInput(addr.getUserDefinedName());
                    }
                } else {
                    for (ExcelAddress addr: addedAsOutput) {
                        componentInstanceConfiguration.removeOutput(addr.getUserDefinedName());
                    }
                }
                executable = true;
                undoable = false;
            }
        }
    }
}
