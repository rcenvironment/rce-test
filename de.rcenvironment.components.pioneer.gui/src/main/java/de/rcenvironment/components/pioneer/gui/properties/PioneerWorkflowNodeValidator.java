/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.gui.properties;

import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_ITERATIONS;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_MESSAGE;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_OPERATION_MODE;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_WAIT;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.components.pioneer.common.PioneerOperationMode;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * A {@link AbstractWorkflowNodeValidator} implementation to validate the GUI of the pioneer
 * component.
 * 
 * <p>
 * To be incorporated in the validation process, this validator is registered as an extension to the
 * extension point "de.rcenvironment.rce.gui.workflow.nodeValidators" in the plugin.xml.
 * </p>
 * 
 * @author Christian Weiss
 */
public class PioneerWorkflowNodeValidator extends AbstractWorkflowNodeValidator implements Serializable {

    private static final long serialVersionUID = -8804632592198572260L;

    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        final String message = getProperty(KEY_MESSAGE, String.class);
        final Integer iterations = getProperty(KEY_ITERATIONS, Integer.class);
        final Boolean wait = getProperty(KEY_WAIT, Boolean.class);
        final PioneerOperationMode operationMode = getProperty(KEY_OPERATION_MODE, PioneerOperationMode.class);
        if (operationMode == null) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    KEY_OPERATION_MODE,
                    Messages.errorOpertionModeNotSetRelative,
                    Messages.bind(Messages.errorOpertionModeNotSetAbsolute,
                        KEY_OPERATION_MODE));
            messages.add(validationMessage);
        }
        if (message == null || message.isEmpty()) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.WARNING,
                    KEY_MESSAGE,
                    Messages.errorEmptyMessageRelative,
                    Messages.bind(Messages.errorEmptyMessageAbsolute,
                        KEY_MESSAGE));
            messages.add(validationMessage);
        }
        if (operationMode == PioneerOperationMode.ACTIVE && iterations == 0) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    KEY_ITERATIONS,
                    Messages.errorZeroIterationsRelative,
                    Messages.bind(Messages.errorZeroIterationsAbsolute,
                        KEY_ITERATIONS));
            messages.add(validationMessage);
        }
        return messages;
    }

}
