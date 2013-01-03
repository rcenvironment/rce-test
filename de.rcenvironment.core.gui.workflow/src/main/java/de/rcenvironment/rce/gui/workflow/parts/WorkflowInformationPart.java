/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;


/**
 * Root edit part holding a WorkflowInformation.
 *
 * @author Heinrich Wendel
 */
public class WorkflowInformationPart extends AbstractGraphicalEditPart {
    

    @Override
    protected List<WorkflowDescription> getModelChildren() {
        List<WorkflowDescription> child = new ArrayList<WorkflowDescription>();
        child.add(((WorkflowInformation) getModel()).getWorkflowDescription());
        return child;
    }

    @Override
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setLayoutManager(new FreeformLayout());
        return f;
    }
    
    @Override
    protected void createEditPolicies() {
    }

}
