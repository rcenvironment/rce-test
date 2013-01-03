/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.commons.FileSupport;
import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.components.simplewrapper.commons.FileMappings;
import de.rcenvironment.rce.components.simplewrapper.commons.ConfigurationValueConverter;
import de.rcenvironment.rce.components.simplewrapper.commons.SimpleWrapperComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * Configuration section to configure a {@link SimpleWrapperComponent}.
 * 
 * @author Christian Weiss
 */
public class SimpleWrapperComponentSection extends ValidatingWorkflowNodePropertySection {

    private static final String PLACEHOLDER_PATTERN = "${%s}";

    private static final int MINIMUM_HEIGHT = 100;

    private static final int WIDTH_HINT = 200;

    private final Map<String, String> variablesPlaceholders = new HashMap<String, String>();

    private Label toolDirectoryLoadStatusLabel;

    private Button toolDirectoryImportButton;

    private Button toolDirectoryExportButton;
    
    private Button separateExecutionDirectoriesButton;

    private Button initCommandCheckbox;

    private Text initCommandText;

    private Text runCommandText;

    private CCombo initVariablesCombo;

    private Button initVariablesInsertButton;

    private CCombo runVariablesCombo;

    private Button runVariablesInsertButton;

    private TableViewer mappingTableViewer;

    @Override
    public void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        final Composite content = new LayoutComposite(parent);
        content.setLayout(new GridLayout(2, true));

        final Composite executableContainer = toolkit.createFlatFormComposite(content);
        initExecutableSection(toolkit, executableContainer);

        final Composite mappingContainer = toolkit.createFlatFormComposite(content);
        initMappingSection(toolkit, mappingContainer);

        final Composite initInvocationContainer = toolkit.createFlatFormComposite(content);
        initInitCommandSection(toolkit, initInvocationContainer);

        final Composite runInvocationContainer = toolkit.createFlatFormComposite(content);
        initRunCommandSection(toolkit, runInvocationContainer);
    }

    private void initExecutableSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite executableContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        executableContainer.setLayoutData(layoutData);
        executableContainer.setLayout(new FillLayout());
        final Section executableSection = toolkit.createSection(executableContainer,
                Section.TITLE_BAR | Section.EXPANDED);
        executableSection.setText(Messages.executableSectionTitle);
        final Composite executableClientContainer = toolkit.createComposite(executableSection);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        executableClientContainer.setLayoutData(layoutData);
        executableClientContainer.setLayout(new GridLayout(4, false));
        toolkit.createLabel(executableClientContainer, Messages.directoryLabel);
        toolDirectoryLoadStatusLabel = toolkit.createLabel(executableClientContainer, "");
        toolDirectoryLoadStatusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
        toolDirectoryLoadStatusLabel.setData(CONTROL_PROPERTY_KEY,
                SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        toolDirectoryLoadStatusLabel.setLayoutData(layoutData);
        toolDirectoryImportButton = toolkit.createButton(executableClientContainer,
            Messages.directoryImportButtonLabel, SWT.PUSH);
        toolDirectoryExportButton = toolkit.createButton(executableClientContainer,
                Messages.directoryExportButtonLabel, SWT.PUSH);
        separateExecutionDirectoriesButton = toolkit.createButton(executableClientContainer,
                Messages.separateExecutionDirectoriesLabel, SWT.CHECK);
        separateExecutionDirectoriesButton.setData(CONTROL_PROPERTY_KEY,
                SimpleWrapperComponentConstants.PROPERTY_SEPARATE_EXECUTION_DIRECTORIES);
        // TODO implement separate execution directories
        separateExecutionDirectoriesButton.setToolTipText("CURRENTLY NOT SUPPORTED");
        separateExecutionDirectoriesButton.setEnabled(false);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
        layoutData.horizontalSpan = 4;
        separateExecutionDirectoriesButton.setLayoutData(layoutData);
        executableSection.setClient(executableClientContainer);
    }

    private void initMappingSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite mappingContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
        mappingContainer.setLayoutData(layoutData);
        mappingContainer.setLayout(new FillLayout());
        final Section mappingSectionSection = toolkit.createSection(mappingContainer,
            Section.TITLE_BAR | Section.EXPANDED);
        mappingSectionSection.setText(Messages.mappingSectionTitle);
        mappingSectionSection.setLayout(new GridLayout(1, false));
        final Composite mappingClient = toolkit.createComposite(mappingSectionSection);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.heightHint = MINIMUM_HEIGHT;
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.widthHint = WIDTH_HINT;
        mappingClient.setLayoutData(layoutData);
        mappingClient.setLayout(new TableWrapLayout());
        final LayoutComposite mappingTableComposite = new LayoutComposite(mappingClient);
        mappingTableViewer = createMappingTableViewer(mappingTableComposite,
            SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
        toolkit.adapt(mappingTableViewer.getTable());
        toolkit.paintBordersFor(mappingTableViewer.getTable());
        TableWrapData layoutDataWrap;
        layoutDataWrap = new TableWrapData(TableWrapData.FILL_GRAB);
        layoutDataWrap.heightHint = MINIMUM_HEIGHT;
        mappingTableComposite.setLayoutData(layoutDataWrap);
        mappingSectionSection.setClient(mappingClient);
    }

    private void initInitCommandSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite initInvocationContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
        initInvocationContainer.setLayoutData(layoutData);
        initInvocationContainer.setLayout(new FillLayout());
        final Section initInvocationSectionSection = toolkit.createSection(initInvocationContainer,
            Section.TITLE_BAR | Section.EXPANDED);
        initInvocationSectionSection.setText(Messages.initInvocationSectionTitle);
        final Composite initInvocationClient = toolkit.createComposite(initInvocationSectionSection);
        initInvocationClient.setLayout(new GridLayout(1, false));
        initCommandText = toolkit.createText(initInvocationClient, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        initCommandText.setData(CONTROL_PROPERTY_KEY, SimpleWrapperComponentConstants.PROPERTY_INIT_COMMAND);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.heightHint = MINIMUM_HEIGHT;
        layoutData.widthHint = WIDTH_HINT;
        initCommandText.setLayoutData(layoutData);
        // Variables Insertion
        final Composite initVariablesInsertionComposite = new Composite(initInvocationClient, SWT.NONE);
        initVariablesInsertionComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        initVariablesInsertionComposite.setLayoutData(layoutData);
        toolkit.createLabel(initVariablesInsertionComposite, Messages.variablesLabel);
        initVariablesCombo = toolkit.createCCombo(initVariablesInsertionComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.paintBordersFor(initVariablesCombo);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        initVariablesCombo.setLayoutData(layoutData);
        initVariablesInsertButton = toolkit.createButton(initVariablesInsertionComposite,
                Messages.variablesInsertButtonLabel, SWT.PUSH);
        initCommandCheckbox = toolkit.createButton(initInvocationClient, Messages.doInitCommandLabel, SWT.CHECK);
        initCommandCheckbox.setData(CONTROL_PROPERTY_KEY, SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND);
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        initCommandCheckbox.setLayoutData(layoutData);
        initInvocationSectionSection.setClient(initInvocationClient);
    }

    private void initRunCommandSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite runInvocationContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
        runInvocationContainer.setLayoutData(layoutData);
        runInvocationContainer.setLayout(new FillLayout());
        final Section runInvocationSectionSection = toolkit.createSection(runInvocationContainer,
                Section.TITLE_BAR | Section.EXPANDED);
        runInvocationSectionSection.setText(Messages.runInvocationSectionTitle);
        final Composite runInvocationClient = toolkit.createComposite(runInvocationSectionSection);
        runInvocationClient.setLayout(new GridLayout(2, false));
        runCommandText = toolkit.createText(runInvocationClient, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        runCommandText.setData(CONTROL_PROPERTY_KEY, SimpleWrapperComponentConstants.PROPERTY_RUN_COMMAND);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.heightHint = MINIMUM_HEIGHT;
        layoutData.widthHint = WIDTH_HINT;
        layoutData.horizontalSpan = 2;
        runCommandText.setLayoutData(layoutData);
        // Variables Insertion
        final Composite runVariablesInsertionComposite = new Composite(runInvocationClient, SWT.NONE);
        runVariablesInsertionComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        runVariablesInsertionComposite.setLayoutData(layoutData);
        toolkit.createLabel(runVariablesInsertionComposite, Messages.variablesLabel);
        runVariablesCombo = toolkit.createCCombo(runVariablesInsertionComposite,
                SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.paintBordersFor(runVariablesCombo);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        runVariablesCombo.setLayoutData(layoutData);
        runVariablesInsertButton = toolkit.createButton(runVariablesInsertionComposite,
                Messages.variablesInsertButtonLabel, SWT.PUSH);
        runInvocationSectionSection.setClient(runInvocationClient);
    }

    private TableViewer createMappingTableViewer(final Composite parent, final int style) {
        final Composite composite = new Composite(parent, SWT.TRANSPARENT);
        final TableColumnLayout tableLayout = new TableColumnLayout();
        composite.setLayout(tableLayout);
        final TableViewer result = new TableViewer(composite, style);
        final String[] columnTitles = new String[] { Messages.direction, Messages.name, Messages.path };
        final int[] weights = { 20, 30, 50 };
        final int[] columnAlignments = { SWT.CENTER, SWT.LEFT, SWT.LEFT };
        for (int index = 0; index < columnTitles.length; index++) {
            final TableViewerColumn viewerColumn = new TableViewerColumn(result, SWT.NONE);
            final TableColumn column = viewerColumn.getColumn();
            // set column properties
            column.setText(columnTitles[index]);
            column.setAlignment(columnAlignments[index]);
            final boolean resizable = index < columnTitles.length - 1;
            column.setResizable(resizable);
            tableLayout.setColumnData(column, new ColumnWeightData(weights[index], resizable));
        }
        result.setContentProvider(new InputMappingContentProvider());
        final InputMappingLabelProvider inputMappingLabelProvider = new InputMappingLabelProvider();
        result.setLabelProvider(inputMappingLabelProvider);
        final Table table = result.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        result.setColumnProperties(columnTitles);
        // editing support
        result.setCellEditors(new CellEditor[] {
            null,
            null,
            new TextCellEditor(result.getTable()) });
        result.setCellModifier(new ICellModifier() {

            @Override
            public boolean canModify(final Object element, final String property) {
                return true;
            }

            @Override
            public Object getValue(final Object element, final String property) {
                final int columnIndex = getColumnIndex(property);
                return inputMappingLabelProvider.getColumnText(element, columnIndex);
            }

            private int getColumnIndex(final String property) {
                final int noSuchElementIndex = -1;
                int result = noSuchElementIndex;
                for (int index = 0; index < columnTitles.length; ++index) {
                    if (columnTitles[index] == property) {
                        result = index;
                    }
                }
                return result;
            }

            @Override
            public void modify(final Object element, final String property, final Object value) {
                final int columnIndex = getColumnIndex(property);
                if (columnIndex > 0 && element != null) {
                    final String[] data = ((String[]) ((TableItem) element).getData());
                    final String direction = data[0];
                    final String name = data[1];
                    final String path = (String) value;
                    final EndpointNature endpointNature = EndpointNature.valueOf(direction);
                    setFileMapping(endpointNature, name, path);
                }
            }

        });
        return result;
    }

    @Override
    protected Controller createController() {
        return new ControllerImpl();
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new SynchronizerImpl();
    }

    @Override
    protected void afterInitializingModelBindingWithValidation() {
        final ReadableComponentInstanceConfiguration configuration = getConfiguration();
        mappingTableViewer.setInput(configuration);
    }

    private String getToolDirPreset() {
        final String result;
        final String executableDirectory = getExecutableDirectory();
        if (executableDirectory != null) {
            result = executableDirectory;
        } else {
            final String userHome = System.getProperty("user.home");
            result = userHome;
        }
        return result;
    }

    private String getExecutableDirectory() {
        final String result =
            getProperty(SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY, String.class);
        return result;
    }

    private void setExecutableDirectory(final String executableDirectory) {
        setProperty(SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY, executableDirectory);
    }

    private boolean isExecutableDirectoryValid() {
        final String path = getExecutableDirectory();
        boolean result = false;
        if (path != null && !path.isEmpty() && new File(path).isDirectory() && new File(path).list().length > 0) {
            final File file = new File(path);
            if (file.exists() && file.isDirectory() && file.canRead()) {
                result = true;
            }
        }
        return result;
    }

    private void importExecutableDirectory() {
        if (!isExecutableDirectoryValid()) {
            handleDirectoryLoadingError();
            return;
        }
        final Job job = new Job(Messages.importDir) {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    monitor.beginTask(Messages.loadFiles, 3);
                    final String path = getExecutableDirectory();
                    final File file = new File(path);
                    final byte[] content;
                    IStatus status = Status.OK_STATUS;
                    monitor.worked(1);
                    try {
                        content = FileSupport.zip(file);
                        setExecutableDirectoryContent(content);
                    } catch (IOException e) {
                        handleDirectoryLoadingError();
                        status = Status.CANCEL_STATUS;
                    } catch (InterruptedException e) {
                        handleDirectoryLoadingError();
                        status = Status.CANCEL_STATUS;
                    } catch (RuntimeException e) {
                        handleDirectoryLoadingError();
                        status = Status.CANCEL_STATUS;
                    }
                    return status;
                } finally {
                    monitor.done();
                }
            }
        };
        job.setUser(true);
        job.schedule();
    }

    private void handleDirectoryLoadingError() {
        final IStatus status = new Status(Status.ERROR, "de.rcenvironment.rce.components.gui.simplewrapper", Messages.invalidDir);
        StatusManager.getManager().handle(status, StatusManager.SHOW);
        setExecutableDirectory(null);
        setExecutableDirectoryContent(new byte[0]);
    }

    private void exportExecutableDirectory(final String path) {
        if (!isExecutableDirectoryContentSet()) {
            throw new RuntimeException();
        }
        final Job job = new Job("Export executable directory") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final byte[] content = getExecutableDirectoryContent();
                final File file = new File(path);
                try {
                    FileSupport.unzip(content, file);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to export executable directory:", e);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

    private void setExecutableDirectoryContent(final byte[] content) {
        // use the GUI-Thread to update the property to avoid Invalid thread access exception
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                final String contentHexString = ConfigurationValueConverter.executableDirectoryContent(content);
                SimpleWrapperComponentSection.this.setProperty(
                    SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT,
                    contentHexString);
            }

        });
    }

    private byte[] getExecutableDirectoryContent() {
        final String contentString = getProperty(SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT, String.class);
        final byte[] content = ConfigurationValueConverter.executableDirectoryContent(contentString);
        return content;
    }

    private boolean isExecutableDirectoryContentSet() {
        final String content = getProperty(SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT, String.class, "");
        final boolean result = !content.isEmpty();
        return result;
    }

    private void setFileMapping(final EndpointNature direction, final String name, final String path) {
        final String mappingsString = getProperty(SimpleWrapperComponentConstants.PROPERTY_FILE_MAPPING, String.class);
        final FileMappings mappings = ConfigurationValueConverter.getConfiguredMappings(mappingsString);
        final String directionName = direction.name();
        mappings.put(directionName, name, path);
        final String value = ConfigurationValueConverter.getConfiguredMappings(mappings);
        setProperty(SimpleWrapperComponentConstants.PROPERTY_FILE_MAPPING, value);
    }

    private void updateInitCommand() {
        initCommandText.setEnabled(isInitCommandEnabled());
        updateVariableInsertControls();
    }

    private boolean isInitCommandEnabled() {
        return initCommandCheckbox.getSelection();
    }
    
    private boolean isRunCommandEnabled() {
        return !getConfiguration().getDynamicInputDefinitions().isEmpty();
    }

    private void updateVariableInsertControls() {
        final boolean hasInitVariableReplacements = initVariablesCombo.getItems().length > 0;
        final boolean enableInitVariableInsertControl = hasInitVariableReplacements & isInitCommandEnabled();
        initVariablesCombo.setEnabled(enableInitVariableInsertControl);
        initVariablesInsertButton.setEnabled(enableInitVariableInsertControl);
        if (hasInitVariableReplacements) {
            initVariablesCombo.select(0);
        }
        runCommandText.setEnabled(isRunCommandEnabled());
        final boolean hasRunVariableReplacements = runVariablesCombo.getItems().length > 0;
        final boolean enableRunVariableInsertControl = hasRunVariableReplacements & isRunCommandEnabled();
        runVariablesCombo.setEnabled(enableRunVariableInsertControl);
        runVariablesInsertButton.setEnabled(enableRunVariableInsertControl);
        if (hasRunVariableReplacements) {
            runVariablesCombo.select(0);
        }
    }

    @Override
    public void refreshBeforeValidation() {
        //
        initVariablesCombo.removeAll();
        runVariablesCombo.removeAll();
        variablesPlaceholders.clear();
        //
        toolDirectoryExportButton.setEnabled(isExecutableDirectoryContentSet());
        if (isExecutableDirectoryContentSet()) {
            toolDirectoryLoadStatusLabel.setText(Messages.executableDirectoryContentSetLabel);
        } else {
            toolDirectoryLoadStatusLabel.setText(Messages.executableDirectoryContentNotSetLabel);
        }
        // variable combo
        final ReadableComponentInstanceConfiguration configuration = getReadableConfiguration();
        final List<String> inputNames = new LinkedList<String>(configuration.getDynamicInputDefinitions().keySet());
        Collections.sort(inputNames);
        for (final String inputName : inputNames) {
            final Class<? extends Serializable> type = configuration.getDynamicInputDefinitions().get(inputName);
            final String placeholder = String.format(PLACEHOLDER_PATTERN, inputName);
            if (!DataManagementFileReference.class.isAssignableFrom(type)) {
                final String inputType = configuration.getInputType(inputName);
                final String label = Messages.bind(Messages.variablesInputPattern, inputName, inputType);
                runVariablesCombo.add(label);
                variablesPlaceholders.put(label, placeholder);
            }
        }
        updateVariableInsertControls();
        mappingTableViewer.refresh();
        updateInitCommand();
    }

    /**
     * Controller.
     * 
     * @author Christian Weiss
     */
    private class ControllerImpl extends DefaultController {

        @Override
        public void widgetSelected(final SelectionEvent event, final Control source) {
            if (source == toolDirectoryImportButton) {
                final Shell shell = Display.getCurrent().getActiveShell();
                final DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText(Messages.directoryChooserDialogTitle);
                dialog.setFilterPath(getToolDirPreset());
                final String directory = dialog.open();
                if (directory != null) {
                    setExecutableDirectory(directory);
                    importExecutableDirectory();
                }
            } else if (source == toolDirectoryExportButton) {
                final Shell shell = Display.getCurrent().getActiveShell();
                final DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText(Messages.directoryChooserDialogTitle);
                dialog.setFilterPath(getToolDirPreset());
                final String directory = dialog.open();
                if (directory != null) {
                    exportExecutableDirectory(directory);
                }
            } else if (source == initVariablesInsertButton) {
                final int selectionIndex = initVariablesCombo.getSelectionIndex();
                final String selectedLabel = initVariablesCombo.getItem(selectionIndex);
                if (selectionIndex >= 0 && selectionIndex < variablesPlaceholders.size()) {
                    final String placeholder = variablesPlaceholders.get(selectedLabel);
                    replace(initCommandText, placeholder);
                }
            } else if (source == runVariablesInsertButton) {
                final int selectionIndex = runVariablesCombo.getSelectionIndex();
                final String selectedLabel = runVariablesCombo.getItem(selectionIndex);
                if (selectionIndex >= 0 && selectionIndex < variablesPlaceholders.size()) {
                    final String placeholder = variablesPlaceholders.get(selectedLabel);
                    replace(runCommandText, placeholder);
                }
            }
        }

    }

    /**
     * Listener to keep the GUI in sync with the model.
     * 
     * @author Christian Weiss
     */
    private class SynchronizerImpl extends DefaultSynchronizer {

        @Override
        public void handlePropertyChange(final String key, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(key, newValue, oldValue);
            if (SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT.equals(key)) {
                toolDirectoryExportButton.setEnabled(isExecutableDirectoryContentSet());
                if (isExecutableDirectoryContentSet()) {
                    toolDirectoryLoadStatusLabel.setText(Messages.executableDirectoryContentSetLabel);
                } else {
                    toolDirectoryLoadStatusLabel.setText(Messages.executableDirectoryContentNotSetLabel);
                }
            } else if (SimpleWrapperComponentConstants.PROPERTY_FILE_MAPPING.equals(key)) {
                mappingTableViewer.refresh();
            } else if (SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND.equals(key)) {
                updateInitCommand();
            }
        }

        @Override
        public void handleChannelEvent(final ChannelEvent event) {
            super.handleChannelEvent(event);
            mappingTableViewer.refresh();
            updateVariableInsertControls();
        }

    }

}
