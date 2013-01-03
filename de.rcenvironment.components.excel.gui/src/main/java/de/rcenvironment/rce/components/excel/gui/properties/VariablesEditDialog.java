/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.properties;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.components.excel.commons.ExcelAddress;
import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.AdvancedDynamicEndpointEditDialog;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;

/**
 * A dialog for defining and editing cells as additional endpoints.
 * 
 * @author Patrick Schaefer
 * @author Markus Kunde
 */
public class VariablesEditDialog extends AdvancedDynamicEndpointEditDialog {

    private File xlFile;
    
    private Text textfieldAddress;
    private String rawAddress;
    
    private Button checkboxExpanding;
    private Button checkBoxPruningArray;
    
    private Button selectButton;
    
    private ExcelAddress addr;
    
    private boolean expanding;
    
    private boolean pruning;
    
    private CellSelectionDialog selectionDialog;
    
    
    
    
    /**
     * Constructor.
     * 
     * @param parentShell
     * @param title
     * @param configuration
     * @param direction
     * @param typeSelectionFactory
     */
    public VariablesEditDialog(Shell parentShell, String title, ReadableComponentInstanceConfiguration configuration,
        EndpointNature direction, TypeSelectionFactory typeSelectionFactory, final File xlFile) {
        super(parentShell, title, configuration, direction, typeSelectionFactory);
        pruning = ExcelComponentConstants.DEFAULT_TABLEPRUNING;
        expanding = ExcelComponentConstants.DEFAULT_TABLEEXPANDING;
        setInitialDataType(ExcelComponentConstants.DEFAULT_DATATYPE);
        this.xlFile = xlFile;
    }
    
    @Override
    protected Control createDialogArea(Composite parent){
        Composite control = (Composite) super.createDialogArea(parent);
        
        //Excel specific GUI
        new Label(control, SWT.NONE).setText(Messages.address);
        textfieldAddress = new Text(control, SWT.SINGLE | SWT.BORDER);
        textfieldAddress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (addr != null) {
            textfieldAddress.setText(addr.getFullAddress());
            rawAddress = addr.getFullAddress();
        }
        
        //Add input or output specific things
        if (direction == EndpointNature.Input){
            new Label(control, SWT.NONE).setText(Messages.expand);
            checkboxExpanding = new Button(control, SWT.CHECK);
            checkboxExpanding.setText("");
            checkboxExpanding.setSelection(expanding);
        } else {
            new Label(control, SWT.NONE).setText(Messages.prune);
            checkBoxPruningArray = new Button(control, SWT.CHECK);
            checkBoxPruningArray.setText("");
            checkBoxPruningArray.setSelection(pruning);
        }
        
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                rawAddress = textfieldAddress.getText();
            }
        };
        textfieldAddress.addModifyListener(modifyListener);
        
        
        selectButton = new Button(control, SWT.PUSH);
        selectButton.setText(Messages.selectButton);
        selectButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        selectButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                createExcelDialog();
            }
        });
        
        
        return control;
    }
        
    
    @Override
    protected void okPressed() {
        addr = new ExcelAddress(xlFile, rawAddress);
        
        if (direction == EndpointNature.Input){
            expanding = checkboxExpanding.getSelection();
        } else {
            pruning = checkBoxPruningArray.getSelection();
        }
        
        super.okPressed();
    }
    
      
    /**
     * Returns selected Excel address.
     * 
     * @return Excel address
     */
    public ExcelAddress getExcelAddress() {
        return addr;
    }
    
    /**
     * Sets Excel address which should be selected.
     * 
     * @param address Excel address
     */
    public void setExcelAddress(final ExcelAddress address) {
        this.addr = address;
    }
    
    /**
     * Returns if input range should be extended in its size.
     * 
     * @return true if input range should be extended in its size
     */
    public boolean getExpanding() {
        return expanding;
    }
    
    /**
     * Sets if input range should be extended in its size.
     * 
     * @param expanding true if input range should be extended in its size.
     */
    public void setExpanding(final boolean expanding) {
        this.expanding = expanding;
    }
    
    /**
     * Returns if output range should be pruned by its empty-values at the end.
     * 
     * @return true if output range should be pruned by its empty-values at the end
     */
    public boolean getPruning() {
        return pruning;
    }
    
    /**
     * Returns Excel file.
     * 
     * @return Excel file
     */
    public File getFile() {
        return xlFile;
    }
    
    /**
     * Sets if output range should be pruned by its empty-values at the end.
     * 
     * @param pruning true if output range should be pruned by its empty-values at the end.
     */
    public void setPruning(final boolean pruning) {
        this.pruning = pruning;
    }
    
    
    
    /**
     * Creates the Excel-cell choosing dialog.
     */
    private void createExcelDialog() {
        final Shell excelDialog = new Shell(SWT.ON_TOP);
        selectionDialog =
            new CellSelectionDialog(excelDialog, SWT.ON_TOP | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM, this);

        excelDialog.setLayout(new FillLayout());

        excelDialog.setText(xlFile.getAbsolutePath());

        excelDialog.pack();
        excelDialog.setSize(0, 0);
        Rectangle parentSize = excelDialog.getBounds();
        Rectangle mySize = excelDialog.getBounds();

        int locationX;
        int locationY;
        locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

        excelDialog.setLocation(new Point(locationX, locationY));
        excelDialog.open();
        selectionDialog.open(textfieldAddress.getText());
    }

    
    /**
     * Cell Selection dialog notifies Variable edit dialog about OK-Button selection.
     * 
     */
    protected void notifyAboutSelection() {
        if (selectionDialog.getAddress() != null && !selectionDialog.getAddress().isEmpty()) {
            addr = new ExcelAddress(xlFile, selectionDialog.getAddress());
            if (addr != null) {
                textfieldAddress.setText(addr.getFullAddress());
                rawAddress = addr.getFullAddress();
            }
        }
        selectionDialog.close();    
    }
}
