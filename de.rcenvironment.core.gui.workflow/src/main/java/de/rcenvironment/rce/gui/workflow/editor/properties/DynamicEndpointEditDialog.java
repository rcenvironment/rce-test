/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;

/**
 * A dialog for editing a single endpoint configuration.
 * 
 * @author Robert Mischke
 */
public class DynamicEndpointEditDialog extends Dialog {

    protected final ReadableComponentInstanceConfiguration configuration;

    protected final ComponentDescription.EndpointNature direction;

    protected Text textfieldName;

    protected Combo comboboxDataType;

    protected String initialName;

    protected TypeSelectionFactory typeSelectionFactory;

    protected String currentName;

    protected String currentDataType;

    protected String title;
    
    protected ComboViewer comboViewer;

    protected List<TypeSelectionOption> typeSelectionOptions;

    

    /**
     * Dialog for creating or editing an endpoint.
     * 
     * @param parentShell parent Shell
     * @param title
     * @param configuration the containing endpoint manager
     */
    public DynamicEndpointEditDialog(Shell parentShell, String title, ReadableComponentInstanceConfiguration configuration,
        ComponentDescription.EndpointNature direction,
        TypeSelectionFactory typeSelectionFactory) {
        super(parentShell);
        this.configuration = configuration;
        this.direction = direction;
        this.typeSelectionFactory = typeSelectionFactory;
        this.title = title;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite container = (Composite) super.createDialogArea(parent);

        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText(Messages.name);

        textfieldName = new Text(container, SWT.SINGLE | SWT.BORDER);
        textfieldName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        new Label(container, SWT.NONE).setText(Messages.dataType);

        comboboxDataType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboboxDataType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // use combo viewer to avoid managing array mappings manually
        comboViewer = new ComboViewer(comboboxDataType);

        // store initial name to skip validation if unchanged
        initialName = currentName;

        // store options
        typeSelectionOptions = typeSelectionFactory.getTypeSelectionOptions();

        // set default provider because base input is a List
        comboViewer.setContentProvider(new ArrayContentProvider());
        // define label provider
        comboViewer.setLabelProvider(new LabelProvider() {

            /**
             * Return display names for TypeSelectionOptions
             */
            @Override
            public String getText(Object element) {
                return ((TypeSelectionOption) element).getDisplayName();
            }
        });
        // set items
        comboViewer.setInput(typeSelectionOptions);

        // set initial input when editing
        if (currentName != null) {
            textfieldName.setText(currentName);
        }
        if (currentDataType != null) {
            for (TypeSelectionOption option : typeSelectionOptions) {
                if (option.getTypeName().equals(currentDataType)) {
                    comboViewer.setSelection(new StructuredSelection(option));
                    break;
                }
            }
        }
        return container;
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

    private void installModifyListeners() {
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        };
        textfieldName.addModifyListener(modifyListener);
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                validateInput();
            }
        });
    }

    protected void validateInput() {

        String name = getNameInputFromUI();
        String type = getTypeSelectionFromUI();

        // initialName is null if not set, so it will not be equal when naming a new endpoint
        boolean nameIsValid = name.equals(initialName);
        boolean typeIsValid;
        if (direction == ComponentDescription.EndpointNature.Input) {
            nameIsValid |= configuration.validateInputName(name);
            typeIsValid = configuration.validateInputType(type);
        } else {
            nameIsValid |= configuration.validateOutputName(name);
            typeIsValid = configuration.validateOutputType(type);
        }

        // enable/disable "ok"
        getButton(IDialogConstants.OK_ID).setEnabled(nameIsValid && typeIsValid);
    }

    private String getNameInputFromUI() {
        return textfieldName.getText();
    }

    protected String getTypeSelectionFromUI() {
        String type = null;
        IStructuredSelection comboSelection = (IStructuredSelection) comboViewer.getSelection();
        if (!comboSelection.isEmpty()) {
            type = ((TypeSelectionOption) comboSelection.getFirstElement()).getTypeName();
        }
        return type;
    }

    @Override
    protected void okPressed() {
        currentName = getNameInputFromUI();
        currentDataType = getTypeSelectionFromUI();
        callSuperOkPressed();
    }

    protected void callSuperOkPressed() {
        super.okPressed();
    }

    public void setInitialName(String name) {
        currentName = name;
    }

    public void setInitialDataType(String type) {
        currentDataType = type;
    }

    public String getChosenName() {
        return currentName;
    }

    public String getChosenDataType() {
        return currentDataType;
    }

}
