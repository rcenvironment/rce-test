/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.list;

import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;

/**
 * LabelProvider for WorkflowInformation objects.
 * 
 * @author Heinrich Wendel
 */
public class WorkflowInformationLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public String getColumnText(Object element, int column) {
        String text = ""; //$NON-NLS-1$
        if (element instanceof WorkflowInformation) {
            if (column == 0) {
                text = ((WorkflowInformation) element).getName();
            } else if (column == 1) {
                final WorkflowState state = WorkflowStateModel.getInstance().getState(((WorkflowInformation) element).getIdentifier());
                if (state != null) {
                    text = state.toString();
                }
            } else if (column == 2) {
                PlatformIdentifier targetPlatform = ((WorkflowInformation) element).getWorkflowDescription().getTargetPlatform();
                if (targetPlatform != null) {
                    text = targetPlatform.getAssociatedDisplayName();
                } else {
                    text = Messages.localPlatform;
                }
            } else if (column == 3) {
                text = ((WorkflowInformation) element).getUser();
            } else if (column == 4) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss"); //$NON-NLS-1$
                text = df.format(((WorkflowInformation) element).getInstantiationTime());
            } else if (column == 5) {
                text = ((WorkflowInformation) element).getAdditionalInformation();
            }
        }

        return text;
    }

    @Override
    public Image getColumnImage(Object arg0, int arg1) {
        return null;
    }
}
