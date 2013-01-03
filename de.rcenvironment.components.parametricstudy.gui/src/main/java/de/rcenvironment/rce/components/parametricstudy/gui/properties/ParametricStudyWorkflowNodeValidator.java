/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.gui.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * Validator for parametric study component.
 * @author Sascha Zur
 */
public class ParametricStudyWorkflowNodeValidator extends AbstractWorkflowNodeValidator{

    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();

        if (getProperty(ParametricStudyComponentConstants.CV_FROMVALUE) == null){
            messages.add(makeMessage(ParametricStudyComponentConstants.CV_FROMVALUE));
        }
        if (getProperty(ParametricStudyComponentConstants.CV_TOVALUE) == null){
            messages.add(makeMessage(ParametricStudyComponentConstants.CV_TOVALUE));
        }
        if (getProperty(ParametricStudyComponentConstants.CV_STEPSIZE) == null){
            messages.add(makeMessage(ParametricStudyComponentConstants.CV_STEPSIZE));
        }

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

    private WorkflowNodeValidationMessage makeMessage(String type){
        final WorkflowNodeValidationMessage message =
            new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                type,
                "No value",
                Messages.bind(Messages.noValue + getTextOfType(type),
                    type));
        return message;
    }
    
    private String getTextOfType(String type) {
        String text;
        if (type.equals(ParametricStudyComponentConstants.CV_FROMVALUE)) {
            text = Messages.fromMsg;
        } else if (type.equals(ParametricStudyComponentConstants.CV_TOVALUE)) {
            text = Messages.toMsg;
        } else if (type.equals(ParametricStudyComponentConstants.CV_STEPSIZE)) {
            text = Messages.stepSizeMsg;
        } else {
            text = type;
        }
        return text;
    }

}
