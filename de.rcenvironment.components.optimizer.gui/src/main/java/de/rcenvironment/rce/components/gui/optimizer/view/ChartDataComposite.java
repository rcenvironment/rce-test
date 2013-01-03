/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.view;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.rcenvironment.commons.excel.ExcelFileExporter;
import de.rcenvironment.commons.variables.TypedValue;
import de.rcenvironment.commons.variables.VariableType;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.components.gui.optimizer.properties.Messages;
import de.rcenvironment.rce.components.optimizer.commons.Dimension;
import de.rcenvironment.rce.components.optimizer.commons.Measure;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerResultSet;
import de.rcenvironment.rce.components.optimizer.commons.ResultStructure;

/**
 * The {@link Composite} displaying the data backing a chart.
 * 
 * @author Sascha Zur
 */
public class ChartDataComposite extends Composite implements ISelectionProvider {

    private static final int DEFAULT_COLUMN_WIDTH = 100;

    /** The table viewer. */
    private TableViewer tableViewer;

    /** The table. */
    private Table table;

    /** The study datastore. */
    private OptimizerDatastore resultDatastore;

    private ComponentInstanceDescriptor ci;

    /** The dataset add listener. */
    private final OptimizerDatastore.OptimizerResultSetAddListener datasetAddListener 
        = new OptimizerDatastore.OptimizerResultSetAddListener() {

            public void handleStudyDatasetAdd(final OptimizerResultSet dataset) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (ChartDataComposite.this.isDisposed()) {
                            return;
                        }
                        ChartDataComposite.this.tableViewer.add(dataset);
                        ChartDataComposite.this.update();
                    }
                });
            }

        };


    /**
     * Instantiates a new chart data composite.
     * 
     * @param parent the parent
     * @param style the style
     */
    public ChartDataComposite(final Composite parent, final int style) {
        super(parent, style);
    }
    
    /**
     * Giveover component instance information.
     * 
     * @param componentInstanceInformation ci
     */
    public void setComponentInstanceDescriptor(ComponentInstanceDescriptor componentInstanceInformation) {
        this.ci = componentInstanceInformation;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        if (resultDatastore != null) {
            resultDatastore.removeDatasetAddListener(datasetAddListener);
        }
        super.dispose();
    }

    /**
     * Creates the controls.
     */
    public void createControls() {
        // layout
        final GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        GridData layoutData;
        // table viewer
        tableViewer = new TableViewer(this, SWT.MULTI | SWT.FULL_SELECTION);
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.horizontalIndent = 0;
        layoutData.verticalIndent = 0;
        table.setLayoutData(layoutData);
        // copy to clipboard
        final Button copyToClipboardButton = new Button(this, SWT.PUSH);
        copyToClipboardButton.setText(Messages.copyToClipboardLabel);




        layoutData = new GridData();
        copyToClipboardButton.setLayoutData(layoutData);
        // copy to clipboard button is only enabled, if a selection is made
        copyToClipboardButton.setEnabled(false);
        addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                copyToClipboardButton.setEnabled(!event.getSelection()
                    .isEmpty());
            }
        });
        copyToClipboardButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                final ISelection selection = getSelection();
                final List<String> keys = new LinkedList<String>();
                for (final TableColumn column : table.getColumns()) {
                    keys.add(column.getText());
                }
                final int keysCount = keys.size();
                final StringBuilder builder = new StringBuilder();
                if (selection != null
                    && selection instanceof IStructuredSelection) {
                    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                    @SuppressWarnings("unchecked") final Iterator<OptimizerResultSet> iterator = structuredSelection
                        .iterator();
                    while (iterator.hasNext()) {
                        final OptimizerResultSet o = iterator.next();
                        for (int index = 0; index < keysCount; index++) {
                            final String key = keys.get(index);
                            final Serializable value = o.getValue(key);

                            if (value != null && ci != null && o.getComponent().equals(ci.getComponentIdentifier())) {
                                builder.append(value);
                            }
                            if (index < (keysCount - 1)) {
                                builder.append("\t");
                            }
                        }
                        if (iterator.hasNext()) {
                            builder.append("\n");
                        }
                    }
                    final String content = builder.toString();
                    final Clipboard clipboard = new Clipboard(Display
                        .getDefault());
                    final TextTransfer textTransfer = TextTransfer
                        .getInstance();
                    clipboard.setContents(new Object[] { content },
                        new Transfer[] { textTransfer });
                }
            }
        });
        final Button saveDataButton = new Button(this, SWT.PUSH);
        saveDataButton.setText(Messages.excelExport);
        saveDataButton.setEnabled(true);
        saveDataButton.addSelectionListener(new MySelectionListener(this));
    }
    /**
     * 
     * 
     * @author zur_sa
     */
    private class MySelectionListener implements SelectionListener{

        private Shell cdc;

        public MySelectionListener(ChartDataComposite cd){
            cdc = cd.getShell();
        }
        @Override
        public void widgetDefaultSelected(SelectionEvent arg0) {

        }

        @Override
        public void widgetSelected(SelectionEvent arg0) {
            FileDialog fd = new FileDialog(cdc, SWT.SAVE);
            fd.setText("Save");
            fd.setFilterPath(System.getProperty("user.dir"));
            String[] filterExt = { "*.xls" };
            fd.setFilterExtensions(filterExt);
            String selected = fd.open();
            if (!selected.substring(selected.lastIndexOf('.') + 1).toLowerCase().equals("xls")){
                selected += ".xls";
            }
            File excelFile = new File(selected); //or "e.xls"
            TypedValue[][] values = new TypedValue[resultDatastore.getDatasetCount() + 1][];
            
            Iterator<OptimizerResultSet> it = resultDatastore.getDatasets().iterator();
            int i = 0;
            while (it.hasNext()){
                
                OptimizerResultSet next = it.next();
               
                if (i == 0){
                    values[i] = new TypedValue[next.getValues().size()];
                    int j = next.getValues().size() - 1;
                    for (String str : next.getValues().keySet()){
                        values[0][j] = new TypedValue(VariableType.String, str);
                        j--;
                    }
                    i = 1;
                }
              
                values[i] = new TypedValue[next.getValues().size()];
                int j = next.getValues().size() - 1;
                for (String key : next.getValues().keySet()){
                    values[i][j] = new TypedValue(VariableType.Real, "" + (Double) next.getValue(key)); 
                    j--;
                }
                i++;
            }
            
            ExcelFileExporter.exportValuesToExcelFile(excelFile, values);
            
        }

    }
    /*   /**
     * Sets the study datastore.
     * 
     * @param studyDatastore the new study datastore
     */
    public void setStudyDatastore(final OptimizerDatastore studyDatastore) {
        if (this.resultDatastore == studyDatastore) {
            return;
        }
        if (this.resultDatastore != null) {
            throw new IllegalStateException("already connected to study");
        }
        this.resultDatastore = studyDatastore;
        final StudyDatastoreContentProvider contentProvider = new StudyDatastoreContentProvider();
        initializeStructure();
        // content
        studyDatastore.addDatasetAddListener(datasetAddListener);
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(studyDatastore);
        // getParent().update();
    }
    /*
    /**
     * Initialize structure.
     */
    private void initializeStructure() {
        final ResultStructure structure = resultDatastore.getStructure();
        /**
         * A {@link ColumnLabelProvider} displaying the values of a parametric study.
         * 
         * @author Christian Weiss
         */
        class ValueLabelProvider extends ColumnLabelProvider {

            /** The key to use to lookup the values in a dataset (which is a map-like structure). */
            private final String key;

            /**
             * Instantiates a new {@link ValueLabelProvider} providing labels for the values with
             * the given key.
             * 
             * @param key
             */
            public ValueLabelProvider(final String key) {
                this.key = key;
            }

            /**
             * {@inheritDoc}
             * 
             * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
             */
            @Override
            public String getText(Object element) {
                if (!(element instanceof OptimizerResultSet)) {
                    return null;
                }
                final Serializable value = ((OptimizerResultSet) element)
                    .getValue(key);
                String result;
                if (value == null) {
                    result = "";
                } else {
                    result = value.toString();
                }
                return result;
            }

        }
        for (final Dimension dimension : structure.getDimensions()) {
            final TableViewerColumn column = new TableViewerColumn(tableViewer,
                SWT.NONE);
            column.getColumn().setText(dimension.getName());
            column.getColumn().setWidth(DEFAULT_COLUMN_WIDTH);
            column.getColumn().setMoveable(true);
            column.setLabelProvider(new ValueLabelProvider(dimension.getName()));
        }
        for (final Measure measure : structure.getMeasures()) {
            final TableViewerColumn column = new TableViewerColumn(tableViewer,
                SWT.NONE);
            column.getColumn().setText(measure.getName());
            column.getColumn().setWidth(DEFAULT_COLUMN_WIDTH);
            column.getColumn().setMoveable(true);
            column.setLabelProvider(new ValueLabelProvider(measure.getName()));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.swt.widgets.Control#update()
     */
    @Override
    public void update() {
        super.update();
        if (table.getItem(0).getText().equals("")){
            table.remove(0);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        tableViewer.addSelectionChangedListener(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    @Override
    public ISelection getSelection() {
        return tableViewer.getSelection();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void removeSelectionChangedListener(
        ISelectionChangedListener listener) {
        tableViewer.removeSelectionChangedListener(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {
        tableViewer.setSelection(selection);
    }

    /**
     * The {@link IStructuredContentProvider} for the data table.
     */
    private final class StudyDatastoreContentProvider implements
        IStructuredContentProvider {
    
            /**
             * {@inheritDoc}
             * 
             * @see org.eclipse.jface.viewers.IContentProvider#dispose()
             */
        @Override
        public void dispose() {}
    
            /**
             * {@inheritDoc}
             * 
             * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
             *      java.lang.Object, java.lang.Object)
             */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    
            /**
             * {@inheritDoc}
             * 
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
        @Override
        public Object[] getElements(Object inputElement) {
            return resultDatastore.getDatasets().toArray(new OptimizerResultSet[0]);
        }
    
    }

}
