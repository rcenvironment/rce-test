/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;


/**
 * Validator for optimizer component.
 * @author Sascha Zur
 */
public class OptimizerWorkflowNodeValidator extends AbstractWorkflowNodeValidator{


    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();

        if (!searchDakotaBinary()){
            final WorkflowNodeValidationMessage noDakotaMessage = new WorkflowNodeValidationMessage(
                WorkflowNodeValidationMessage.Type.ERROR,
                "No Dakota",
                "NoDakota",
                Messages.bind("Optimizer could not find Dakota! Please download optimizer bundle or link local binary.", "NoDakota"));
            messages.add(noDakotaMessage);
        }


        final boolean hasInputs = hasInputs();
        final WorkflowNodeValidationMessage enableInitCommandMessage = new WorkflowNodeValidationMessage(
            WorkflowNodeValidationMessage.Type.ERROR,
            "Has Inputs",
            "HasInputs",
            Messages.bind("No input", "HasInputs"));
        if (!hasInputs) {
            messages.add(enableInitCommandMessage);
        }
        final boolean hasOutputs = hasOutputs();
        final WorkflowNodeValidationMessage noOutputMessage = new WorkflowNodeValidationMessage(
            WorkflowNodeValidationMessage.Type.ERROR,
            "Has Outputs",
            "HasOutputs",
            Messages.bind("No output", "HasOutputs"));
        if (!hasOutputs) {
            messages.add(noOutputMessage);
        }
        return messages;
    }


    private boolean searchDakotaBinary() {
        // TODO Implement!
        //        boolean foundpath = false;
        //        InputStream dakotaInput = this.getClass().getResourceAsStream("/resources/binaries/dakota.exe");
        //        if (dakotaInput != null){
        //            foundpath = true;
        //        } else {
        //            if (getProperty(OptimizerComponentConstants.DAKOTAPATH) != null){
        //                File dakotaBin = new File((String) getProperty(OptimizerComponentConstants.DAKOTAPATH));
        //                if (dakotaBin.exists() && dakotaBin.getAbsolutePath().endsWith("dakota.exe")){
        //                    foundpath = true;
        //                }
        //            }
        //        }            
        return true;
    }

}
