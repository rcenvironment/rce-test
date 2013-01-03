/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.parts.WorkflowNodePart;


/**
 * Class that provides context ID of components.
 *
 * @author Doreen Seider
 */
public class WorkflowEditorHelpContextProvider implements IContextProvider {

    private GraphicalViewer viewer;
    
    public WorkflowEditorHelpContextProvider(GraphicalViewer viewer) {
        this.viewer = viewer;
    }
    
    @Override
    public IContext getContext(Object arg0) {
        Object object = (((IStructuredSelection) viewer.getSelection()).getFirstElement());
        if (object instanceof WorkflowNodePart) {
            WorkflowNodePart nodePart = (WorkflowNodePart) object;
            String componentID = ((WorkflowNode) nodePart.getModel()).getComponentDescription().getIdentifier();
            return HelpSystem.getContext(componentID);
        }
        return HelpSystem.getContext("de.rcenvironment.rce.gui.workflow.editor"); //$NON-NLS-1$
    }

    @Override
    public int getContextChangeMask() {
        return IContextProvider.SELECTION;
    }

    @Override
    public String getSearchExpression(Object arg0) {
        return null;
    }

}
