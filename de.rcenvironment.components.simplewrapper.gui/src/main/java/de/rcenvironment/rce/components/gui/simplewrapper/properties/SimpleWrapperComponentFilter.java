/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for SimpleWrapperComponent instances.
 * 
 * @author Christian Weiss
 */
public class SimpleWrapperComponentFilter extends ComponentFilter {

    private static final String COMPONENT_ID = "de.rcenvironment.rce.components.simplewrapper.SimpleWrapperComponent_Simple Wrapper";

    @Override
    public boolean filterComponentName(final String componentId) {
        return componentId.equals(COMPONENT_ID);
    }

}
