/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * Factory responsible for creating the EditParts.
 *
 * @author Heinrich Wendel
 */
public class ReadonlyEditPartFactory implements EditPartFactory {

    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        if (model instanceof WorkflowDescription) {
            part = new ReadonlyWorkflowPart();
        } else if (model instanceof WorkflowNode){
            part = new ReadonlyWorkflowNodePart();
        } else if (model instanceof ConnectionWrapper) {
            part = new ConnectionPart();
        } else if (model instanceof WorkflowInformation) {
            part = new WorkflowInformationPart();
        }
        part.setModel(model);
        return part;
    }

}
