/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;


/**
 * Filter for OptimizerComponent instances.
 *
 * @author Sascha Zur
 */
public class DOEComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith("de.rcenvironment.rce.components.optimizer.OptimizerComponent_Design");
    }

}
