/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.python.properties;

import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;


/**
 * Filter for PythonComponent instances.
 *
 * @author Markus Litz
 */
public class PythonComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith("de.rcenvironment.rce.components.python");
    }

}
