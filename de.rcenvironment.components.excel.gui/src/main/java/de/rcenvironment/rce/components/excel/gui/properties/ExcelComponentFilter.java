/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.excel.gui.properties;

import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for ExcelComponent instances.
 * 
 * @author Patrick Schaefer
 */
public class ExcelComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith(ExcelComponentConstants.COMPONENT_ID);
    }

}
