/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.components.simplewrapper.commons.SimpleWrapperComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * Validator for {@link BasicWrapperComponent}s.
 * 
 * @author Christian Weiss
 */
public class SimpleWrapperWorkflowNodeValidator extends AbstractWorkflowNodeValidator {

    @Override
    public Collection<WorkflowNodeValidationMessage> validate() {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        // final String executableDirectoryString =
        // (String)
        // getWorkflowNode().getProperty(BasicWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY);
        // if (executableDirectoryString == null || executableDirectoryString.isEmpty()) {
        // final WorkflowNodeValidationMessage message =
        // new
        // WorkflowNodeValidationMessage(BasicWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY,
        // Messages.errorEmptyValue,
        // Messages
        // .bind(Messages.executableDirectoryErrorEmptyValue,
        // BasicWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY));
        // messages.add(message);
        // } else {
        // final File file = new File(executableDirectoryString);
        // if (!file.exists() || !file.canRead() || !file.isDirectory()) {
        // final WorkflowNodeValidationMessage message =
        // new
        // WorkflowNodeValidationMessage(BasicWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY,
        // Messages.invalidDirectory,
        // Messages
        // .bind(Messages.executableDirectoryNotValid,
        // BasicWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY,
        // file.getAbsolutePath()));
        // messages.add(message);
        // }
        // }
        final String executableDirectoryContentString =
            getProperty(SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT, String.class, "");
        if (executableDirectoryContentString.trim().isEmpty()) {
            final WorkflowNodeValidationMessage message =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT,
                    Messages.invalidDirectory,
                    Messages.bind(Messages.executableDirectoryContentNotSet,
                            SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY));
            messages.add(message);
        }
        final boolean hasInputs = hasInputs();
        final String initCommandString = getProperty(SimpleWrapperComponentConstants.PROPERTY_INIT_COMMAND, String.class, "");
        final Boolean isInitCommandEnabledValue = getProperty(
            SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND, Boolean.class, false);
        final boolean isInitCommandEnabled = isInitCommandEnabledValue;
        final boolean hasMeaningfulInitCommand = !initCommandString.trim().isEmpty();
        final String runCommandString = getProperty(SimpleWrapperComponentConstants.PROPERTY_RUN_COMMAND, String.class, "");
        final boolean hasMeaningfulRunCommand = !runCommandString.trim().isEmpty();
        final WorkflowNodeValidationMessage enableInitCommandMessage = new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND,
                Messages.errorMustBeSet,
                Messages.bind(Messages.doInitCommandNotSetValue, SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND));
        final WorkflowNodeValidationMessage provideInitCommandMessage = new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                SimpleWrapperComponentConstants.PROPERTY_INIT_COMMAND,
                Messages.errorEmptyValue,
                Messages.bind(Messages.initCommandErrorEmptyValue, SimpleWrapperComponentConstants.PROPERTY_INIT_COMMAND));
        final WorkflowNodeValidationMessage provideRunCommandMessage = new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                SimpleWrapperComponentConstants.PROPERTY_RUN_COMMAND,
                Messages.errorEmptyValue,
                Messages.bind(Messages.runCommandErrorEmptyValue, SimpleWrapperComponentConstants.PROPERTY_RUN_COMMAND));
        if (isInitCommandEnabled && !hasMeaningfulInitCommand) {
            messages.add(provideInitCommandMessage);
        }
        if (!hasInputs && !isInitCommandEnabled) {
            messages.add(enableInitCommandMessage);
        }
        if (hasInputs && !hasMeaningfulRunCommand) {
            messages.add(provideRunCommandMessage);
        }
        return messages;
    }

}
