/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.parametricstudy.gui.properties;

import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for PSComponent instances.
 * 
 * @author Markus Kunde
 */
public class ParametricStudyComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith(ParametricStudyComponentConstants.COMPONENT_ID);
    }

}
