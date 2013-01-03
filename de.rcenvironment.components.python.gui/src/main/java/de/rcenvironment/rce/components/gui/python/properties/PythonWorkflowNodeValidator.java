/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.python.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * Validator for Python component.
 * @author Sascha Zur
 */
public class PythonWorkflowNodeValidator extends AbstractWorkflowNodeValidator{

    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {

        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
       
        final boolean hasInputs = hasInputs();
        final WorkflowNodeValidationMessage enableInitCommandMessage = new WorkflowNodeValidationMessage(
            WorkflowNodeValidationMessage.Type.WARNING,
            "Has Inputs",
            "HasInputs",
            Messages.bind(Messages.noInput, "HasInputs"));
        if (!hasInputs) {
            messages.add(enableInitCommandMessage);
        }
        return messages;
    }

}
