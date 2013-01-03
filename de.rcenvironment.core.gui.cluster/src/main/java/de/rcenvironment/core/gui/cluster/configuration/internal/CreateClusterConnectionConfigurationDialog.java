/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.cluster.configuration.internal;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.rcenvironment.core.utils.cluster.ClusterJobInformation;
import de.rcenvironment.core.utils.cluster.ClusterQueuingSystem;

/**
 * Dialog to create a new cluster connection configuration.
 * 
 * @author Doreen Seider
 */
public class CreateClusterConnectionConfigurationDialog extends TitleAreaDialog {

    protected static final int CREATE = 2;

    protected Combo queuingSystemCombo;
    
    protected Text hostText;

    protected Text portText;

    protected Text usernameText;

    protected Text passwordText;
    
    protected Button savePasswordCheckbox;
    
    protected Text configurationNameText;

    protected Button defaultConfigurationNameCheckbox;
    
    protected Button createButton;
    
    protected List<String> existingConfigurationNames;

    private ClusterQueuingSystem queuingSystem;
    
    private String host;

    private int port;

    private String username;

    private String password;
    
    private String configurationName;
    
    private boolean savePassword;
    
    public CreateClusterConnectionConfigurationDialog(Shell parentShell, List<String> existingConfigurationNames) {
        super(parentShell);
        this.existingConfigurationNames = existingConfigurationNames;
    }
    
    @Override
    public void create() {
        super.create();
        setTitle(Messages.newConfigurationDialogTitle);
        setMessage(Messages.newConfigurationDialogMessage, IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);

        Label queuingSystemLabel = new Label(parent, SWT.NONE);
        queuingSystemLabel.setText(Messages.queueingSystemLabel);

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        queuingSystemCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        queuingSystemCombo.setLayoutData(gridData);
        queuingSystemCombo.setItems(new String[] { ClusterQueuingSystem.TORQUE.toString() });
        queuingSystemCombo.select(0);
        
        Label hostLabel = new Label(parent, SWT.NONE);
        hostLabel.setText(Messages.hostLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        hostText = new Text(parent, SWT.BORDER);
        hostText.setLayoutData(gridData);

        Label portLabel = new Label(parent, SWT.NONE);
        portLabel.setText(Messages.portLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        portText = new Text(parent, SWT.BORDER);
        portText.setLayoutData(gridData);
        portText.setText(String.valueOf(ClusterJobInformation.DEFAULT_SSH_PORT));
        
        Label usernameLabel = new Label(parent, SWT.NONE);
        usernameLabel.setText(Messages.usernameLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;

        usernameText = new Text(parent, SWT.BORDER);
        usernameText.setLayoutData(gridData);

        Label passwordLabel = new Label(parent, SWT.NONE);
        passwordLabel.setText(Messages.passwordLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        passwordText = new Text(parent, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(gridData);
        passwordText.setEnabled(false);

        // placeholder label
        new Label(parent, SWT.NONE);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        savePasswordCheckbox = new Button(parent, SWT.CHECK);
        savePasswordCheckbox.setText(Messages.savePasswordCheckboxLabel);
        savePasswordCheckbox.setLayoutData(gridData);
        savePasswordCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                passwordText.setEnabled(savePasswordCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });

        Label configurationNameLabel = new Label(parent, SWT.NONE);
        configurationNameLabel.setText(Messages.configurationNameLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        configurationNameText = new Text(parent, SWT.BORDER);
        configurationNameText.setLayoutData(gridData);
        configurationNameText.setEnabled(false);
                
        // placeholder label
        new Label(parent, SWT.NONE);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        defaultConfigurationNameCheckbox = new Button(parent, SWT.CHECK);
        defaultConfigurationNameCheckbox.setText(Messages.useDefaultNameCheckboxLabel);
        defaultConfigurationNameCheckbox.setLayoutData(gridData);
        defaultConfigurationNameCheckbox.setSelection(true);
        defaultConfigurationNameCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                configurationNameText.setEnabled(!defaultConfigurationNameCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
        
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        parent.setLayoutData(gridData);
        
        createButton = createButton(parent, CREATE, Messages.createButtonTitle, true);
        createButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (isValidInput()) {
                    saveInput();
                    setReturnCode(CREATE);
                    close();
                }
            }
        });
        Button cancelButton = createButton(parent, CANCEL, Messages.cancel, false);
        cancelButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setReturnCode(CANCEL);
                close();
            }
        });
    }

    private boolean isValidInput() {
        boolean valid = true;
        if (hostText.getText().length() == 0) {
            setErrorMessage(Messages.maintainHostLabel);
            valid = false;
        }
        if (portText.getText().length() == 0) {
            setErrorMessage(Messages.portLabel);
            valid = false;
        }
        try {
            Integer.valueOf(portText.getText());
        } catch (NumberFormatException e) {
            setErrorMessage(Messages.maintainPortNumberLabel);
            valid = false;
        }
        if (usernameText.getText().length() == 0) {
            setErrorMessage(Messages.maintainUsernameLabel);
            valid = false;
        }
        if (savePasswordCheckbox.getSelection()) {
            if (passwordText.getText().length() == 0) {
                setErrorMessage(Messages.maintainPasswordLabel);
                valid = false;
            }
        }
        
        valid = isConfigurationNameValid();
        
        return valid;
    }
    
    protected boolean isConfigurationNameValid() {
        boolean valid = true;
        if (!defaultConfigurationNameCheckbox.getSelection()) {
            if (configurationNameText.getText().length() == 0) {
                setErrorMessage(Messages.configurationNameLabel);
                valid = false;
            }
        } else if (existingConfigurationNames.contains(usernameText.getText() + "@" + hostText.getText())) {
            setErrorMessage(Messages.maintainAnotherConfigurationNameLabel);
            valid = false;            
            
        }
        if (existingConfigurationNames.contains(configurationNameText.getText())) {
            setErrorMessage(Messages.maintainAnotherConfigurationNameLabel);
            valid = false;            
        }
        return valid;
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }

    private void saveInput() {
        queuingSystem = ClusterQueuingSystem.valueOf(queuingSystemCombo.getItem(queuingSystemCombo.getSelectionIndex()));
        host = hostText.getText();
        port = Integer.valueOf(portText.getText());
        username = usernameText.getText();
        if (savePasswordCheckbox.getSelection()) {
            password = passwordText.getText();
        }
        if (defaultConfigurationNameCheckbox.getSelection()) {
            configurationName = username + "@" + host;
        } else {
            configurationName = configurationNameText.getText();            
        }
    }

    public ClusterQueuingSystem getClusterQueuingSystem() {
        return queuingSystem;
    }
    
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getConfigurationName() {
        return configurationName;
    }
    
    /**
     * @return <code>true</code> if password should be saved, otherwise <code>false</code>
     */
    public boolean savePassword() {
        return savePassword;
    }

}
