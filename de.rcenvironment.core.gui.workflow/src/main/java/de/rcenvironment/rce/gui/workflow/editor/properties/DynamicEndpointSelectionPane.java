/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.component.workflow.ChannelListener;
import de.rcenvironment.rce.component.workflow.ComponentInstanceConfiguration;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;

/**
 * A UI part to display and edit a set of endpoints managed by a {@link DynamicEndpointManager). 
 *
 * @author Robert Mischke
 * @author Christian Weiss
 */
public class DynamicEndpointSelectionPane implements PropertyChangeListener, ChannelListener {
    
    protected final ComponentDescription.EndpointNature direction;

    /** The display text describing individual endpoints; usually "Input" or "Output". */
    protected String genericEndpointTitle;

    protected Section section;
    
    protected Composite client;

    protected ReadableComponentInstanceConfiguration configuration;

    protected TypeSelectionFactory typeSelectionFactory;

    protected Table table;

    protected Button buttonAdd;

    protected Button buttonEdit;

    protected Button buttonRemove;
    
    protected SelectionAdapter buttonListener; 

    private final WorkflowNodeCommand.Executor executor;

    private ReadableComponentInstanceConfiguration modelBindingTarget;

    /**
     * @param genericEndpointTitle the display text describing individual endpoints (like "Input" or
     *        "Output"); used in dialog texts
     */
    @Deprecated
    public DynamicEndpointSelectionPane(String genericEndpointTitle, final ComponentDescription.EndpointNature direction,
            TypeSelectionFactory typeSelectionFactory) {
        this.genericEndpointTitle = genericEndpointTitle;
        this.direction = direction;
        this.typeSelectionFactory = typeSelectionFactory;
        this.executor = null;
    }
    
    /**
     * @param genericEndpointTitle the display text describing individual endpoints (like "Input" or
     *        "Output"); used in dialog texts
     */
    public DynamicEndpointSelectionPane(String genericEndpointTitle, final ComponentDescription.EndpointNature direction,
            TypeSelectionFactory typeSelectionFactory, final WorkflowNodeCommand.Executor executor) {
        this.genericEndpointTitle = genericEndpointTitle;
        this.direction = direction;
        this.typeSelectionFactory = typeSelectionFactory;
        this.executor = executor;
    }
    
    protected void execute(final WorkflowNodeCommand command) {
        if (executor == null) {
            throw new RuntimeException("No executor set for execution of workflow node commands");
        }
        executor.execute(command);
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
                DynamicEndpointSelectionPane.this.dispose();
            }

        });

        client = toolkit.createComposite(section);
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
        // layout data for the columns
        final int allColumnsWeight = 100;
        final int firstColumnWeight = 30;
        tableLayout.setColumnData(col1, new ColumnWeightData(firstColumnWeight, true));
        tableLayout.setColumnData(col2, new ColumnWeightData(allColumnsWeight - firstColumnWeight, false));

        buttonAdd = toolkit.createButton(client, Messages.add, SWT.FLAT);
        buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonEdit = toolkit.createButton(client, Messages.edit, SWT.FLAT);
        buttonEdit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonRemove = toolkit.createButton(client, Messages.remove, SWT.FLAT);
        buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

        buttonListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == buttonAdd) {
                    // add
                    final WorkflowNodeCommand command = new AddDynamicEndpointCommand(direction);
                    execute(command);
                } else if (e.widget == buttonEdit) {
                    // edit selected; relies on proper button activation
                    final String name = (String) table.getSelection()[0].getData();
                    final String type = getType(name);
                    final WorkflowNodeCommand command = new EditDynamicEndpointCommand(direction,
                        name, type);
                    execute(command);
                } else if (e.widget == buttonRemove) {
                    // remove selected; relies on proper button activation
                    final String name = (String) table.getSelection()[0].getData();
                    final String type = getType(name);
                    final Map<String, Serializable> metadata = getMetaData(name);
                    final WorkflowNodeCommand command = new RemoveDynamicEndpointCommand(direction, name, type, metadata);
                    execute(command);
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
     * Set the component instance configuration for configuration handling & storage; must not be
     * null.
     * 
     * @param configuration Component configuration
     */
    public void setConfiguration(final ReadableComponentInstanceConfiguration configuration) {
        this.configuration = configuration;
        initializeModelBinding();
        updateTable();
    }

    protected ReadableComponentInstanceConfiguration getConfiguration() {
        return configuration;
    }

    private void initializeModelBinding() {
        final ReadableComponentInstanceConfiguration configurationInst = getConfiguration();
        if (configurationInst != modelBindingTarget) {
            tearDownModelBinding();
        }
        if (modelBindingTarget == null) {
            modelBindingTarget = configurationInst;
            modelBindingTarget.addPropertyChangeListener(this);
            modelBindingTarget.addChannelListener(this);
        }
    }
    /**
     * Dispose.
     */
    public void dispose() {
        tearDownModelBinding();
    }

    private void tearDownModelBinding() {
        if (modelBindingTarget != null) {
            modelBindingTarget.removePropertyChangeListener(this);
            modelBindingTarget.removeChannelListener(this);
            modelBindingTarget = null;
        }
    }

    /**
     * Loads the current endpoint data into the UI table.
     */
    protected void fillTable() {
        table.removeAll();
        final List<String> endpointNames;
        if (direction == ComponentDescription.EndpointNature.Input) {
            endpointNames = new ArrayList<String>(configuration.getDynamicInputDefinitions().keySet());
        } else {
            endpointNames = new ArrayList<String>(configuration.getDynamicOutputDefinitions().keySet());
        }
        Collections.sort(endpointNames);
        for (String name : endpointNames) {
            TableItem item = new TableItem(table, SWT.None);
            item.setText(0, name);
            final String endpointType = getType(name);
            // FIXME provide display name of input type directly, e.g. in TypeSelectionFactory
            for (TypeSelectionOption opt : typeSelectionFactory.getTypeSelectionOptions()) {
                if (opt.getTypeName().equals(endpointType)) {
                    item.setText(1, opt.getDisplayName());
                    break;
                }
            }
            item.setData(name);
        }
    }

    /**
     * Enabled or disables the "edit" and "remove" buttons.
     */
    protected void updateButtonActivation() {
        TableItem[] selection = table.getSelection();
        boolean hasSelection = selection.length != 0;
        buttonEdit.setEnabled(hasSelection);
        buttonRemove.setEnabled(hasSelection);
    }

    /**
     * @return the main Control
     */
    public Control getControl() {
        return section;
    }

    protected void updateTable() {
        if (!getControl().isDisposed()) {
            fillTable();
            updateButtonActivation();
        }
    }

    protected String getType(String name) {
        final String type;
        if (direction == ComponentDescription.EndpointNature.Input) {
            type = configuration.getInputType(name);
        } else {
            type = configuration.getOutputType(name);
        }
        return type;
    }

    protected Map<String, Serializable> getMetaData(String name) {
        final Map<String, Serializable> metadata;
        if (direction == ComponentDescription.EndpointNature.Input) {
            metadata = configuration.getInputMetaData(name);
        } else {
            metadata = configuration.getOutputMetaData(name);
        }
        return metadata;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // final String propertyName = event.getPropertyName();
    }

    @Override
    public void handleChannelEvent(ChannelEvent event) {
        updateTable();
    }

    /**
     * {@link WorkflowNodeCommand} adding dynamic endpoints to a <code>WorkflowNode</code>.
     * 
     * @author Christian Weiss
     */
    public final class AddDynamicEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;
        
        private String name;

        private String type;
        
        private boolean executable = true;
        
        private boolean undoable = false;

        /**
         * The constructor.
         * 
         * @param direction the direction
         */
        public AddDynamicEndpointCommand(final ComponentDescription.EndpointNature direction) {
            this.direction = direction;
        }
        
        @Override
        public void initialize() {
            DynamicEndpointEditDialog dialog = new DynamicEndpointEditDialog(Display.getDefault().getActiveShell(),
                    "New " + genericEndpointTitle, getWorkflowNode(), direction,
                    typeSelectionFactory);
            if (dialog.open() == Dialog.OK) {
                name = dialog.getChosenName();
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
        public void execute() {
            if (executable) {
                // perform changes
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    componentInstanceConfiguration.addInput(name, type);
                } else {
                    componentInstanceConfiguration.addOutput(name, type);
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
     * @author Christian Weiss
     */
    public final class EditDynamicEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;

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
        public EditDynamicEndpointCommand(final ComponentDescription.EndpointNature direction,
                final String oldName, final String oldType) {
            this.direction = direction;
            this.oldName = oldName;
            this.oldType = oldType;
        }
        
        @Override
        public void initialize() {
            DynamicEndpointEditDialog dialog = new DynamicEndpointEditDialog(Display.getDefault().getActiveShell(),
                "Edit " + genericEndpointTitle, configuration, direction,
                typeSelectionFactory);
            dialog.setInitialName(oldName);
            dialog.setInitialDataType(oldType);
            if (dialog.open() == Dialog.OK) {
                newName = dialog.getChosenName();
                newType = dialog.getChosenDataType();
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
        public void execute() {
            if (executable) {
                // perform changes
                final ComponentInstanceConfiguration componentInstanceConfiguration = getComponentInstanceConfiguration();
                if (direction == ComponentDescription.EndpointNature.Input) {
                    componentInstanceConfiguration.changeInput(oldName, newName, newType);
                } else {
                    componentInstanceConfiguration.changeOutput(oldName, newName, newType);
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
                    componentInstanceConfiguration.changeInput(newName, oldName, oldType);
                } else {
                    componentInstanceConfiguration.changeOutput(newName, oldName, oldType);
                }
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
    public final class RemoveDynamicEndpointCommand extends WorkflowNodeCommand {

        private final ComponentDescription.EndpointNature direction;

        private final String name;

        private final String type;

        private final Map<String, Serializable> metadata;

        private boolean executable = true;
        
        private boolean undoable = false;

        /**
         * The constructor.
         * @param name the name of the endpoint
         * @param type the type of the endpoint
         */
        public RemoveDynamicEndpointCommand(final ComponentDescription.EndpointNature direction,
                final String name, final String type, final Map<String, Serializable> metadata) {
            this.direction = direction;
            this.name = name;
            this.type = type;
            this.metadata = metadata;
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
                if (direction == ComponentDescription.EndpointNature.Input) {
                    componentInstanceConfiguration.removeInput(name);
                } else {
                    componentInstanceConfiguration.removeOutput(name);
                }
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
                    componentInstanceConfiguration.addInput(name, type);
                    for (final Map.Entry<String, Serializable> entry : metadata.entrySet()) {
                        final String metaDataKey = entry.getKey();
                        final Serializable metaDataValue = entry.getValue();
                        componentInstanceConfiguration.setInputMetaData(name, metaDataKey, metaDataValue);
                    }
                } else {
                    componentInstanceConfiguration.addOutput(name, type);
                    for (final Map.Entry<String, Serializable> entry : metadata.entrySet()) {
                        final String metaDataKey = entry.getKey();
                        final Serializable metaDataValue = entry.getValue();
                        componentInstanceConfiguration.setOutputMetaData(name, metaDataKey, metaDataValue);
                    }
                }
                executable = true;
                undoable = false;
            }
        }
    }

}
