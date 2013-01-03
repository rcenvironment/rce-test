/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.converger.gui;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointEditDialog;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;

/**
 * 
 * A dialog for defining and editing Variables as additional endpoints.
 * 
 * @author Sascha Zur
 */
public class ConvergerAddVariable  extends DynamicEndpointEditDialog{

    private Composite container;

    private double startValue;

    private String initialstart;

    private Text startText;

    private boolean initialHasStart;

    private boolean hasStartValue;

    private Button buttonHasStartValue;

    public ConvergerAddVariable(Shell parentShell, String title, ReadableComponentInstanceConfiguration configuration, 
        TypeSelectionFactory typeSelectionFactory) {
        super(parentShell, title, configuration, null, typeSelectionFactory);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        container = (Composite) super.createDialogArea(parent);
        if (currentName != null) {
            textfieldName.setText(currentName);
        }

        Label hasStartValueLabel = new Label(container, SWT.NONE);
        hasStartValueLabel.setText(Messages.hasStartValue);
        buttonHasStartValue = new Button(container, SWT.CHECK);
        buttonHasStartValue.setSelection(initialHasStart);

        Label startvalueLabel = new Label(container, SWT.NONE);
        startvalueLabel.setText(Messages.startValue);
        startText = new Text(container, SWT.SINGLE | SWT.BORDER);
        if (initialstart != null && !initialstart.equalsIgnoreCase("null")) {
            startText.setText(initialstart);
        }
        startValue = 0;
        startText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        startText.setEnabled(initialHasStart);
        return container;
    }
    protected void validateInput() {

        String name = textfieldName.getText();
        String type = getTypeSelectionFromUI();



        // initialName is null if not set, so it will not be equal when naming a new endpoint
        boolean nameIsValid = name.equals(initialName);
        boolean typeIsValid;
        nameIsValid |= configuration.validateInputName(name);
        typeIsValid = configuration.validateInputType(type);
        nameIsValid |= configuration.validateOutputName(name);
        typeIsValid = configuration.validateOutputType(type);

        boolean startValid = true;
        if (hasStartValue){
            String start = startText.getText();
            try {
                Double.parseDouble(start);
                startValid = true;
            } catch (NumberFormatException e) {
                startValid = false;
            }
        }
        getButton(IDialogConstants.OK_ID).setEnabled(startValid && nameIsValid && typeIsValid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        super.create();
        // dialog title
        getShell().setText(title);
        // initial validation
        validateInput();
        // set listeners here so the ok button is initialized
        installModifyListeners();
    }
    protected String getTypeSelectionFromUI() {
        String type = null;
        IStructuredSelection comboSelection = (IStructuredSelection) comboViewer.getSelection();
        if (!comboSelection.isEmpty()) {
            type = ((TypeSelectionOption) comboSelection.getFirstElement()).getTypeName();
        }
        return type;
    }
    private void installModifyListeners() {
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        };
        textfieldName.addModifyListener(modifyListener);


        startText.addModifyListener(modifyListener);
        buttonHasStartValue.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                hasStartValue = buttonHasStartValue.getSelection();
                startText.setEnabled(hasStartValue);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }
        });

    }
    @Override
    protected void okPressed() {

        currentName = textfieldName.getText();
        currentDataType = getTypeSelectionFromUI();
        hasStartValue = buttonHasStartValue.getSelection();

        if (hasStartValue){
            startValue = Double.parseDouble(startText.getText());
        } else {
            startValue = Double.NaN;
        }
        callSuperOkPressed();
    }


    public double getStartValue() {
        return startValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }


    public String getInitialstart() {
        return initialstart;
    }


    public void setInitialstart(String initialstart) {
        this.initialstart = initialstart;
    }

    /**
     * modified getter.
     * @return value
     */
    public boolean hasStartValue() {
        return hasStartValue;
    }


    public void setHasStartValue(boolean hasStartValue) {
        this.hasStartValue = hasStartValue;
    }


    public boolean isInitialHasStart() {
        return initialHasStart;
    }

    public void setInitialHasStart(boolean initialHasStart) {
        this.initialHasStart = initialHasStart;
    }
}
