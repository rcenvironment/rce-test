/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.converger.gui;

import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for merger component.
 * 
 * @author Sascha Zur
 */
public class ConvergerComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith("de.rcenvironment.components.converger.execution.ConvergerComponent_Converger");
    }

}
