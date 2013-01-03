/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowInformationPart;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowNodePart;


/**
 * Returns the label displayed in the head of the properties tab.
 *
 * @author Heinrich Wendel
 */
public final class WorkflowLabelProvider extends LabelProvider {

    @Override
    public String getText(Object objects) {
        String value = ""; //$NON-NLS-1$
        if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
            value = Messages.noItemSelected;
        } else if (((IStructuredSelection) objects).size() > 1) {
            value = ((IStructuredSelection) objects).size() + Messages.itemSelected;
        } else {
            Object object = ((IStructuredSelection) objects).getFirstElement();
            if (object instanceof WorkflowNodePart) {
                value = ((WorkflowNode) ((WorkflowNodePart) object).getModel()).getName();
            } else if (object instanceof WorkflowInformationPart) {
                value = ((WorkflowInformation) ((WorkflowInformationPart) object).getModel()).getName();
            }
        }
        return value;
    }
}
