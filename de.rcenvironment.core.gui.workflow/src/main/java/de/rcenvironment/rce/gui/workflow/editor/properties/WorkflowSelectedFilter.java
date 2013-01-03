/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import org.eclipse.jface.viewers.IFilter;

import de.rcenvironment.rce.gui.workflow.parts.WorkflowInformationPart;


/**
 * Filter class to display the general property tab for all workflows.
 *
 * @author Doreen Seider
 */
public class WorkflowSelectedFilter implements IFilter {

    @Override
    public boolean select(Object object) {
        return object instanceof WorkflowInformationPart;
    }

}
