/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.cluster.configuration.internal;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * Dialog to edit an existing cluster connection configuration.
 * 
 * @author Doreen Seider
 */
public class EditClusterConnectionConfigurationDialog extends CreateClusterConnectionConfigurationDialog {

    private ClusterConnectionConfiguration configuration;
    
    public EditClusterConnectionConfigurationDialog(Shell parentShell, List<String> existingConfigurationNames,
        ClusterConnectionConfiguration configuration) {
        super(parentShell, existingConfigurationNames);
        this.configuration = configuration;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        prefillForm();
        return control;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        createButton.setText(Messages.editButtonTitle2);
    }
    @Override
    protected boolean isConfigurationNameValid() {
        boolean valid = true;
        if (!defaultConfigurationNameCheckbox.getSelection()) {
            if (configurationNameText.getText().length() == 0) {
                setErrorMessage(Messages.maintainConfigurationNameLabel);
                valid = false;
            }
        } else if (existingConfigurationNames.contains(usernameText.getText() + "@" + hostText.getText())) {
            setErrorMessage(Messages.maintainAnotherConfigurationNameLabel);
            valid = false;            
            
        }
        if (existingConfigurationNames.contains(configurationNameText.getText())
            && !configuration.getConfigurationName().equals(configurationNameText.getText())) {
            setErrorMessage(Messages.maintainConfigurationNameLabel);
            valid = false;
        }
        return valid;
    }
    
    private void prefillForm() {
        hostText.setText(configuration.getHost());
        portText.setText(String.valueOf(configuration.getPort()));
        usernameText.setText(configuration.getUsername());
        if (configuration.getPassword() != null) {
            passwordText.setText(configuration.getPassword());
            passwordText.setEnabled(true);
            savePasswordCheckbox.setSelection(true);
        }
        configurationNameText.setText(configuration.getConfigurationName());
        configurationNameText.setEnabled(true);
        defaultConfigurationNameCheckbox.setSelection(false);
    }
}
