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
import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * Factory responsible for creating the EditParts.
 *
 * @author Heinrich Wendel
 */
public class EditorEditPartFactory implements EditPartFactory {

    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        if (model instanceof WorkflowDescription) {
            part = new WorkflowPart();
        } else if (model instanceof WorkflowNode){
            part = new WorkflowNodePart();
        } else if (model instanceof ConnectionWrapper) {
            part = new ConnectionPart();
        }
        part.setModel(model);
        return part;
    }

}
