/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * Returns the labels for the target platform selection table, first column workflow node name,
 * second column target platform.
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
final class WorkflowNodeLabelProvider extends LabelProvider implements ITableLabelProvider {

    private final PlatformIdentifier localPlatform;
    
    public WorkflowNodeLabelProvider() {
        this.localPlatform = null;
    }

    public WorkflowNodeLabelProvider(final PlatformIdentifier localPlatform) {
        this.localPlatform = localPlatform;
    }
    
    @Override
    public String getColumnText(Object element, int column) {
        String value = ""; //$NON-NLS-1$
        if (element instanceof WorkflowNode) {   
            switch (column) {
            case 0:
                value = ((WorkflowNode) element).getName();
                break;
            case 1:
                ComponentDescription cd = ((WorkflowNode) element).getComponentDescription();
                PlatformIdentifier pi = cd.getPlatform();
                if (pi == null) {
                    value = Messages.localPlatformSelectionTitle;
                } else {
                    value = pi.getAssociatedDisplayName();
                    if (localPlatform != null && localPlatform.equals(pi)) {
                        value = Messages.bind(Messages.localPlatformExplicitSelectionTitle, value);
                    }
                }
                break;
            default:
                throw new AssertionError();
            }
        }
        return value;
    }

    @Override
    public Image getColumnImage(Object element, int column) {
        // TODO add component image
        return null;
    }
}
