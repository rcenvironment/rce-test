/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.core.component.executor.ScriptUsage;
import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * A {@link AbstractWorkflowNodeValidator} implementation to validate cluster component configuration.
 *  
 * @author Doreen Seider
 */
public class SshExecutorWorkflowNodeValidator extends AbstractWorkflowNodeValidator {

    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        
        List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        
        messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_HOST);
        messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_PORT);
        messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_SANDBOXROOT);
        messages = checkIfFilesAreConfiguredIfEnabled(messages, SshExecutorConstants.CONFIG_KEY_UPLOAD,
            SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD);
        
        if (getProperty(SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT, String.class)
            .equals(ScriptUsage.LOCAL.toString())) {
            messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT);
        } else if (getProperty(SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT, String.class)
                .equals(ScriptUsage.REMOTE.toString())) {
            messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_REMOTEPATHOFSCRIPT);
        } else {
            /*
             * If the name of the script is optional, the validation for this has to be done in the components validation
             * messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_NAMEOFNEWJOBSCRIPT);
             */
            messages = checkIfStringIsConfigured(messages, SshExecutorConstants.CONFIG_KEY_SCRIPT);            
        }
        
        boolean download = getProperty(SshExecutorConstants.CONFIG_KEY_DOWNLOAD, Boolean.class);
        boolean targetRce = getProperty(SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISRCE, Boolean.class);
        boolean targetFileSystem = getProperty(SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISFILESYSTEM, Boolean.class);
        
        if (download && !targetRce && !targetFileSystem) {
            messages = addMessage(messages, SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISRCE);
            messages = addMessage(messages, SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISFILESYSTEM);
        }
        
        String targetFileSystemPath = getProperty(SshExecutorConstants.CONFIG_KEY_FILESYSTEMPATH, String.class);
        
        if (targetFileSystem && (targetFileSystemPath == null || targetFileSystemPath.isEmpty())) {
            messages = addMessage(messages, SshExecutorConstants.CONFIG_KEY_FILESYSTEMPATH);
        }
        
        return messages;
    }
    
    protected List<WorkflowNodeValidationMessage> addMessage(List<WorkflowNodeValidationMessage> messages, String configurationKey) {
        WorkflowNodeValidationMessage validationMessage =
            new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                configurationKey,
                String.format(Messages.errorMissing, configurationKey),
                Messages.bind(String.format(Messages.errorMissing, configurationKey),
                    configurationKey));
        messages.add(validationMessage);
        return messages;
    }
    
    protected List<WorkflowNodeValidationMessage> checkIfStringIsConfigured(List<WorkflowNodeValidationMessage> messages,
        String configurationKey) {
        final String value = getProperty(configurationKey, String.class);
        if (value == null || value.isEmpty()) {
            WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    configurationKey,
                    String.format(Messages.errorMissing, configurationKey),
                    Messages.bind(String.format(Messages.errorMissing, configurationKey),
                        configurationKey));
            messages.add(validationMessage);
        }
        
        return messages;
    }
    
    protected List<WorkflowNodeValidationMessage> checkIfFilesAreConfiguredIfEnabled(List<WorkflowNodeValidationMessage> messages,
        String enabledConfigurationKey, String fileConfigurationKey) {
        if (getProperty(enabledConfigurationKey, Boolean.class)) {
            
            final String value = getProperty(fileConfigurationKey, String.class);
            if (value == null || value.isEmpty() || value.equals(SshExecutorConstants.EYMPTY_FILE_LIST_IN_JSON)) {
                final WorkflowNodeValidationMessage validationMessage =
                    new WorkflowNodeValidationMessage(
                        WorkflowNodeValidationMessage.Type.WARNING,
                        fileConfigurationKey,
                        String.format(Messages.warningFileListEmpty, fileConfigurationKey),
                        Messages.bind(String.format(Messages.warningFileListEmpty, fileConfigurationKey),
                            fileConfigurationKey));
                messages.add(validationMessage);
            }
        }        
        return messages;
    }
    
}
