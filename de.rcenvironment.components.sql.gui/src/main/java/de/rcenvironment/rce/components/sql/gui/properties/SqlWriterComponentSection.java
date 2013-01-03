/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.sql.gui.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.rce.components.sql.commons.ColumnType;
import de.rcenvironment.rce.components.sql.commons.InputMapping;
import de.rcenvironment.rce.components.sql.commons.InputMapping.ColumnMapping;
import de.rcenvironment.rce.components.sql.commons.InputMode;
import de.rcenvironment.rce.components.sql.commons.JDBCProfile;
import de.rcenvironment.rce.components.sql.commons.SimpleJDBCService;
import de.rcenvironment.rce.components.sql.commons.SqlComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection;

/**
 * Configuration section to configure a {@link SqlReaderComponent}.
 * 
 * @author Christian Weiss
 */
public class SqlWriterComponentSection extends WorkflowNodePropertySection {

    protected static final String COMPONENT_DATA_KEY_ID = "ID";

    private static final String PROPERTY_NAME = Messages.writerSectionColumnTableNameLabel;

    private static final String PROPERTY_TYPE = Messages.writerSectionColumnTableTypeLabel;

    private CCombo jdbcProfileCombo;

    private Text tableNameText;
    
    private Button createTableButton;
    
    private Button dropTableButton;

    private CCombo modeCombo;
    
    private TableViewer mappingTableViewer;

    private InputMappingLabelProvider inputMappingLabelProvider;
    
    private InputMapping inputMapping;
    
    private Button moveUpInputMappingButton;
    
    private Button moveDownInputMappingButton;
    
    private Button addInputMappingButton;
    
    private Button removeInputMappingButton;

    private final PropertyChangeListener inputMappingTableUpdateListener = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            setInputMapping(getInputMapping());
            mappingTableViewer.refresh();
        }
        
    };
    
    @Override
    public void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();
        
        final Composite content = new LayoutComposite(parent);
        content.setLayout(new GridLayout(2, true));
        GridData layoutData;
        /*
         * Connection Settings
         */
        final Composite jdbcContainer = toolkit.createFlatFormComposite(content);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        jdbcContainer.setLayoutData(layoutData);
        jdbcContainer.setLayout(new FillLayout());
        final Section jdbcSection = toolkit.createSection(jdbcContainer, Section.TITLE_BAR | Section.EXPANDED);
        jdbcSection.setText(Messages.writerSectionConnectionSettingTitle);
        final Composite jdbcClient = toolkit.createComposite(jdbcSection);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        jdbcClient.setLayoutData(layoutData);
        jdbcClient.setLayout(new GridLayout(2, false));
        // JDBC Profile
        toolkit.createLabel(jdbcClient, Messages.writerSectionJDBCProfileLabel);
        jdbcProfileCombo = toolkit.createCCombo(jdbcClient, SWT.DROP_DOWN | SWT.READ_ONLY);
        jdbcProfileCombo.setData(CONTROL_PROPERTY_KEY, SqlComponentConstants.METADATA_JDBC_PROFILE_PROPERTY);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        jdbcProfileCombo.setLayoutData(layoutData);
        // set client
        jdbcSection.setClient(jdbcClient);
        /*
         * SQL Configuration
         */
        final Composite sqlContainer = toolkit.createFlatFormComposite(content);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        sqlContainer.setLayoutData(layoutData);
        sqlContainer.setLayout(new FillLayout());
        final Section sqlSection = toolkit.createSection(sqlContainer, Section.TITLE_BAR | Section.EXPANDED);
        sqlSection.setText(Messages.writerSectionSQLConfigurationTitle);
        final Composite sqlClient = toolkit.createComposite(sqlSection);
        sqlClient.setLayout(new GridLayout(2, false));
        // Table Name
        toolkit.createLabel(sqlClient, Messages.writerSectionTableNameLabel);
        tableNameText = toolkit.createText(sqlClient, "", SWT.SINGLE);
        tableNameText.setData(CONTROL_PROPERTY_KEY, SqlComponentConstants.METADATA_TABLE_NAME_PROPERTY);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        tableNameText.setLayoutData(layoutData);
        // <Table Buttons>
        // spacer for missing select all button label
        toolkit.createLabel(sqlClient, "");
        final Composite tableButtonsComposite = new Composite(sqlClient, SWT.NONE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        tableButtonsComposite.setLayoutData(layoutData);
        tableButtonsComposite.setLayout(new GridLayout(2, false));
        // create
        createTableButton = toolkit.createButton(tableButtonsComposite, "", SWT.CHECK);
        toolkit.createLabel(tableButtonsComposite, Messages.writerSectionTableCreateCheckBoxText);
        // drop
        dropTableButton = toolkit.createButton(tableButtonsComposite, "", SWT.CHECK);
        toolkit.createLabel(tableButtonsComposite, Messages.writerSectionTableDropCheckBoxText);
        // set client
        sqlSection.setClient(sqlClient);
        /*
         * Mode Settings
         */
        final Composite modeContainer = toolkit.createFlatFormComposite(content);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
        modeContainer.setLayoutData(layoutData);
        modeContainer.setLayout(new FillLayout());
        final Section modeSection = toolkit.createSection(modeContainer, Section.TITLE_BAR | Section.EXPANDED);
        modeSection.setText(Messages.writerSectionInputModeConfigurationTitle);
        final Composite modeClient = toolkit.createComposite(modeSection);
        modeClient.setLayout(new GridLayout(2, false));
        // Mode
        toolkit.createLabel(modeClient, Messages.writerSectionModeLabel);
        modeCombo = toolkit.createCCombo(modeClient, SWT.DROP_DOWN | SWT.READ_ONLY);
        modeCombo.setData(CONTROL_PROPERTY_KEY, SqlComponentConstants.METADATA_INPUT_MODE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        modeCombo.setLayoutData(layoutData);
        // fill Mode Combo
        final String[] labels = InputMode.getLabels();
        final String[] items = new String[labels.length + 1];
        System.arraycopy(labels, 0, items, 1, labels.length);
        items[0] = Messages.writerSectionModeNullItem;
        modeCombo.setItems(items);
        modeCombo.setEnabled(false);
        // set client
        modeSection.setClient(modeClient);
        /*
         * Input Settings
         */
        final Composite inputContainer = toolkit.createFlatFormComposite(content);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        inputContainer.setLayoutData(layoutData);
        inputContainer.setLayout(new FillLayout());
        final Section inputSection = toolkit.createSection(inputContainer, Section.TITLE_BAR | Section.EXPANDED);
        inputSection.setText(Messages.writerSectionBlockInputConfigurationTitle);
        final Composite inputClient = toolkit.createComposite(inputSection);
        inputClient.setLayout(new GridLayout(2, true));
        final Composite mappingTableViewerComposite = new Composite(inputClient, SWT.NONE);
        mappingTableViewerComposite.setLayout(new GridLayout(2, false));
        mappingTableViewer = createMappingTableViewer(mappingTableViewerComposite,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
        layoutData.horizontalSpan = 2;
        mappingTableViewerComposite.setLayoutData(layoutData);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
        layoutData.verticalSpan = 2;
        final int minimumHeight = 75;
        layoutData.minimumHeight = minimumHeight;
        layoutData.heightHint = minimumHeight;
        mappingTableViewer.getTable().setLayoutData(layoutData);
        addInputMappingButton = toolkit.createButton(inputClient, Messages.writerSectionAddColumnButtonLabel, SWT.PUSH);
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        addInputMappingButton.setLayoutData(layoutData);
        removeInputMappingButton = toolkit.createButton(inputClient, Messages.writerSectionRemoveColumnButtonLabel, SWT.PUSH);
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        removeInputMappingButton.setLayoutData(layoutData);
        moveUpInputMappingButton = toolkit.createButton(mappingTableViewerComposite, "", SWT.ARROW | SWT.UP);
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        moveUpInputMappingButton.setLayoutData(layoutData);
        moveDownInputMappingButton = toolkit.createButton(mappingTableViewerComposite, "", SWT.ARROW | SWT.DOWN);
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        moveDownInputMappingButton.setLayoutData(layoutData);
        // set client
        inputSection.setClient(inputClient);
    }

    @Override
    protected Controller createController() {
        return new ControllerImpl();
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new SynchronizerImpl();
    }

    private TableViewer createMappingTableViewer(final Composite parent, final int style) {
        final TableViewer result = new TableViewer(parent, style);
        final String[] columnTitles = new String[] {
            Messages.writerSectionColumnTableColumnLabel, PROPERTY_NAME, PROPERTY_TYPE };
        final int[] bounds = { 50, 100, 100 };
        final int[] columnAlignments = { SWT.RIGHT, SWT.LEFT, SWT.LEFT };
        for (int index = 0; index < columnTitles.length; index++) {
            final TableViewerColumn viewerColumn = new TableViewerColumn(result, SWT.NONE);
            final TableColumn column = viewerColumn.getColumn();
            // set column properties
            column.setText(columnTitles[index]);
            column.setWidth(bounds[index]);
            column.setAlignment(columnAlignments[index]);
            column.setResizable(true);
            column.setMoveable(true);
        }
        result.setContentProvider(new InputMappingContentProvider());
        inputMappingLabelProvider = new InputMappingLabelProvider();
        result.setLabelProvider(inputMappingLabelProvider);
        final Table table = result.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        result.setColumnProperties(columnTitles);
        // editing support
        result.setCellEditors(new CellEditor[] {
            null,
            new TextCellEditor(result.getTable()),
            new ComboBoxCellEditor(result.getTable(), ColumnType.labelValues())});
        result.setCellModifier(new ICellModifier() {

            @Override
            public boolean canModify(final Object element, final String property) {
                return true;
            }

            @Override
            public Object getValue(final Object element, final String property) {
                final Object value;
                final ColumnMapping columnMapping;
                if (element instanceof ColumnMapping) {
                    columnMapping = (ColumnMapping) element;
                } else {
                    columnMapping = extractColumnMapping(element);
                }
                if (property.equals(PROPERTY_NAME)) {
                    final String name = columnMapping.getName();
                    if (name != null) {
                        value = name;
                    } else {
                        value = "";
                    }
                } else if (property.equals(PROPERTY_TYPE)) {
                    final ColumnType type = columnMapping.getType();
                    if (type != null) {
                        final int index = type.ordinal();
                        value = index;
                    } else {
                        value = 0;
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                return value;
            }

            @Override
            public void modify(final Object element, final String property, final Object value) {
                final ColumnMapping columnMapping = extractColumnMapping(element);
                if (property.equals(PROPERTY_NAME)) {
                    columnMapping.setName((String) value);
                } else if (property.equals(PROPERTY_TYPE)) {
                    if (value instanceof Integer) {
                        final int index = ((Integer) value).intValue();
                        final ColumnType type = ColumnType.values()[index];
                        columnMapping.setType(type);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }

            protected ColumnMapping extractColumnMapping(final Object element) {
                if (!(element instanceof TableItem)) {
                    throw new IllegalArgumentException();
                }
                final TableItem tableItem = (TableItem) element;
                final ColumnMapping columnMapping = (ColumnMapping) tableItem.getData();
                return columnMapping;
            }

        });
        return result;
    }

    /* default */ void setJdbcProfile(final String profileLabel) {
        final String oldValue = getJdbcProfile();
        if (!profileLabel.equals(oldValue)) {
            final String key = SqlComponentConstants.METADATA_JDBC_PROFILE_PROPERTY;
            setProperty(key, oldValue, profileLabel);
        }
    }

    private String getJdbcProfile() {
        final String result = getProperty(SqlComponentConstants.METADATA_JDBC_PROFILE_PROPERTY, String.class);
        return result;
    }

    /* default */ void setTableName(final String tableName) {
        final String oldValue = getTableName();
        if (!tableName.equals(oldValue)) {
            final WorkflowNodeCommand command = new SetConfigurationValueCommand(
                SqlComponentConstants.METADATA_TABLE_NAME_PROPERTY, oldValue, tableName);
            execute(command);
        }
    }

    private String getTableName() {
        final String key = SqlComponentConstants.METADATA_TABLE_NAME_PROPERTY;
        final String result = (String) getReadableConfiguration().getProperty(key);
        return result;
    }

    /* default */ boolean isCreateTableEnabled() {
        final String key = SqlComponentConstants.METADATA_CREATE_TABLE;
        final boolean createTableEnabled = getProperty(key, Boolean.class, Boolean.FALSE);
        return createTableEnabled;
    }

    protected void toggleCreateTableEnabled() {
        final String key = SqlComponentConstants.METADATA_CREATE_TABLE;
        final Boolean value = !isCreateTableEnabled();
        setProperty(key, value);
    }
   
    /* default */ boolean isDropTableEnabled() {
        final String key = SqlComponentConstants.METADATA_DROP_TABLE;
        final Boolean dropTableEnabled = getProperty(key, Boolean.class, Boolean.FALSE);
        return dropTableEnabled;
    }

    protected void toggleDropTableEnabled() {
        final String key = SqlComponentConstants.METADATA_DROP_TABLE;
        final Boolean value = !isDropTableEnabled();
        setProperty(key, value);
    }

    protected void updateDropTableEnabledState() {
        dropTableButton.setEnabled(isCreateTableEnabled());
    }

    protected InputMode getMode() {
        final String key = SqlComponentConstants.METADATA_INPUT_MODE;
        final String value = (String) getReadableConfiguration().getProperty(key);
        final InputMode result;
        if (value == null || value.isEmpty()) {
            result = null;
        } else {
            result = InputMode.valueOf(value);
        }
        return result;
    }
    
    protected void setMode(final InputMode mode) {
        if (mode == getMode()) {
            return;
        }
        final String key = SqlComponentConstants.METADATA_INPUT_MODE;
        final String value;
        if (mode != null) {
            value = mode.name();
        } else {
            value = null;
        }
        setProperty(key, value);
    }
    
    protected InputMapping loadInputMapping() {
        final String key = SqlComponentConstants.METADATA_INPUT_MAPPING;
        final String value = (String) getReadableConfiguration().getProperty(key);
        final InputMapping result;
        if (value != null && !value.isEmpty()) {
            result = InputMapping.deserialize(value);
        } else {
            result = new InputMapping();
        }
        return result;
    }
    
    protected InputMapping getInputMapping() {
        if (inputMapping == null) {
            inputMapping = loadInputMapping();
        }
        return inputMapping;
    }

    protected void setInputMapping(final InputMapping inputMapping) {
        final String key = SqlComponentConstants.METADATA_INPUT_MAPPING;
        final String value;
        if (inputMapping != null) {
            value = inputMapping.serialize();
        } else {
            value = null;
        }
        final String oldValue = (String) getReadableConfiguration().getProperty(key);
        if (!value.equals(oldValue)) {
            setProperty(key, value);
            if (this.inputMapping != inputMapping) {
                refreshInputMapping();
            }
        }
    }
    
    protected void updateAutoSql() {
        if (isCreateTableEnabled()) {
            final String key = SqlComponentConstants.METADATA_SQL_DISPOSE_PROPERTY;
            final String sql = "CREATE TABLE %s";
            setPropertyUnobstrusive(key, sql);
        }
        if (isDropTableEnabled()) {
            final String key = SqlComponentConstants.METADATA_SQL_DISPOSE_PROPERTY;
            final String sql = "DROP TABLE %s";
            setPropertyUnobstrusive(key, sql);
        }
    }

    @Override
    public void refresh() {
        /*
         * clean
         */
        // JDBC Profile
        final SimpleJDBCService jdbcService = SimpleJDBCService.getInstance();
        // table buttons
        createTableButton.setSelection(false);
        dropTableButton.setSelection(false);
        // input mode selection
        modeCombo.select(0);
        /*
         * init
         */
        // JDBC Profile
        jdbcProfileCombo.removeAll();
        jdbcProfileCombo.add(Messages.readerSectionJDBCProfileNullItem);
        final String currentValue = getJdbcProfile();
        boolean containsCurrentValue = currentValue == null || currentValue.isEmpty();
        final List<JDBCProfile> profiles = jdbcService.getProfiles();
        for (int index = 0; index < profiles.size(); ++index) {
            final JDBCProfile jdbcProfile = profiles.get(index);
            final String label = jdbcProfile.getLabel();
            jdbcProfileCombo.add(label);
            final boolean isCurrentValue = label.equals(currentValue);
            containsCurrentValue |= isCurrentValue;
            if (isCurrentValue) {
                final int comboIndex = index + 1;
                jdbcProfileCombo.select(comboIndex);
            }
        }
        if (currentValue != null && !currentValue.isEmpty() && !containsCurrentValue) {
            final String message = Messages.bind(Messages.currentJdbcProfileButMissing, currentValue);
            jdbcProfileCombo.add(message, 0);
            jdbcProfileCombo.select(0);
        }
        // input mode
        final int modeComboIndex;
        final InputMode mode = getMode();
        if (mode != null) {
            modeComboIndex = mode.ordinal() + 1;
        } else {
            modeComboIndex = 0;
        }
        modeCombo.select(modeComboIndex);
        // Table Name
        final String tableName = getTableName();
        if (tableName != null) {
            tableNameText.setText(tableName);
        }
        // table buttons
        final boolean createTableEnabled = isCreateTableEnabled();
        createTableButton.setSelection(createTableEnabled);
        final boolean dropTableEnabled = isDropTableEnabled();
        dropTableButton.setSelection(dropTableEnabled);
        updateDropTableEnabledState();
        // input mapping
        refreshInputMapping();
        super.refresh();
    }

    protected void refreshInputMapping() {
        // cleanup
        if (inputMapping != null) {
            inputMapping.removePropertyChangeListener(inputMappingTableUpdateListener);
            inputMapping = null;
        }
        // init
        final InputMapping currentInputMapping = getInputMapping();
        inputMappingLabelProvider.setInputMapping(currentInputMapping);
        mappingTableViewer.setInput(currentInputMapping);
        currentInputMapping.addPropertyChangeListener(inputMappingTableUpdateListener);
        mappingTableViewer.refresh();
    }

    /**
     * Controller implementation.
     * 
     * @author Christian Weiss
     */
    private class ControllerImpl extends DefaultController {

        @Override
        public void widgetSelected(final SelectionEvent event, final Control source) {
            if (source == jdbcProfileCombo) {
                final int selectionIndex = jdbcProfileCombo.getSelectionIndex();
                final String selectedProfileLabel = jdbcProfileCombo.getItem(selectionIndex);
                setJdbcProfile(selectedProfileLabel);
                if (!jdbcProfileCombo.getItem(0).equals(Messages.writerSectionJDBCProfileNullItem) && selectionIndex > 0) {
                    jdbcProfileCombo.remove(0);
                }
            } else if (source == modeCombo) {
                final int selectionIndex = modeCombo.getSelectionIndex();
                final InputMode mode;
                if (selectionIndex == 0) {
                    mode = null;
                } else {
                    final String item = modeCombo.getItem(selectionIndex);
                    mode = InputMode.valueOfLabel(item);
                }
                setMode(mode);
            } else if (source == moveUpInputMappingButton) {
                final ISelection selection = mappingTableViewer.getSelection();
                final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                final Object firstElement = structuredSelection.getFirstElement();
                if (firstElement != null) {
                    final ColumnMapping columnMapping = (ColumnMapping) firstElement;
                    final InputMapping inputMappingInst = getInputMapping();
                    inputMappingInst.moveUp(columnMapping);
                    setInputMapping(inputMappingInst);
                }
                mappingTableViewer.refresh();
            } else if (source == moveDownInputMappingButton) {
                final ISelection selection = mappingTableViewer.getSelection();
                final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                final Object firstElement = structuredSelection.getFirstElement();
                if (firstElement != null) {
                    final ColumnMapping columnMapping = (ColumnMapping) firstElement;
                    final InputMapping inputMappingInst = getInputMapping();
                    inputMappingInst.moveDown(columnMapping);
                    setInputMapping(inputMappingInst);
                }
                mappingTableViewer.refresh();
            } else if (source == addInputMappingButton) {
                final InputMapping currentInputMapping = getInputMapping();
                currentInputMapping.add();
                setInputMapping(currentInputMapping);
                mappingTableViewer.refresh();
            } else if (source == removeInputMappingButton) {
                final ISelection selection = mappingTableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                    final Object firstElement = structuredSelection.getFirstElement();
                    if (firstElement != null) {
                        final ColumnMapping selectedMapping = (ColumnMapping) firstElement;
                        getInputMapping().remove(selectedMapping);
                    }
                }
                // clear selection to avoid accidental double-deletes
                mappingTableViewer.getTable().deselectAll();
            } else if (source == createTableButton) {
                toggleCreateTableEnabled();
                updateDropTableEnabledState();
            } else if (source == dropTableButton) {
                toggleDropTableEnabled();
            }
        }

    }

    /**
     * Synchronizer implementation.
     * 
     * @author Christian Weiss
     */
    private final class SynchronizerImpl extends DefaultSynchronizer {

        @Override
        public void handlePropertyChange(final String propertyName, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(propertyName, newValue, oldValue);
            if (propertyName.equals(SqlComponentConstants.METADATA_JDBC_PROFILE_PROPERTY)) {
                final String newJdbcProfile = (String) newValue;
                for (int index = 0; index < jdbcProfileCombo.getItemCount(); ++index) {
                    final String label = jdbcProfileCombo.getItem(index);
                    if (label.equals(newJdbcProfile) || (newJdbcProfile == null && label.equals(""))) {
                        jdbcProfileCombo.select(index);
                        break;
                    }
                }
            } else if (propertyName.equals(SqlComponentConstants.METADATA_CREATE_TABLE)) {
                final boolean createTableEnabled = isCreateTableEnabled();
                if (createTableButton.getSelection() != createTableEnabled) {
                    createTableButton.setSelection(createTableEnabled);
                }
                updateDropTableEnabledState();
            } else if (propertyName.equals(SqlComponentConstants.METADATA_DROP_TABLE)) {
                final boolean dropTableEnabled = isDropTableEnabled();
                dropTableButton.setSelection(dropTableEnabled);
            } else if (propertyName.equals(SqlComponentConstants.METADATA_INPUT_MODE)) {
                final String modeName = (String) newValue;
                if (modeName == null || modeName.isEmpty()) {
                    modeCombo.select(0);
                } else {
                    final InputMode mode = InputMode.valueOf(modeName);
                    final InputMode[] modes = InputMode.values();
                    final String[] labels = InputMode.getLabels();
                    boolean selected = false;
                    for (int index = 0; index < labels.length; ++index) {
                        if (modes[index] == mode) {
                            modeCombo.select(index + 1);
                            selected = true;
                        }
                    }
                    if (!selected) {
                        throw new RuntimeException();
                    }
                }
            } else if (propertyName.equals(SqlComponentConstants.METADATA_INPUT_MAPPING)) {
                final InputMapping currentInputMapping = getInputMapping();
                final InputMapping backingInputModel = loadInputMapping();
                /*
                 * Usually changes should be effected on the current model first, then the current
                 * model gets serialized and saved as property value, then this serialized new model
                 * gets deserialized through loadInputModel() and compared against the changed new
                 * model. Overall refreshInputMapping() should only be invoked, if the changes on
                 * the underlying property value have not been made through the GUI.
                 */
                if (!currentInputMapping.equals(backingInputModel)) {
                    refreshInputMapping();
                }
            }
        }

    }

}
