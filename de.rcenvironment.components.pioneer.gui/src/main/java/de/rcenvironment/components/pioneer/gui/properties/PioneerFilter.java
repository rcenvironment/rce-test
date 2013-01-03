/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.gui.properties;

import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for Pioneer.
 *
 * @author Heinrich Wendel
 */
public class PioneerFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.startsWith("de.rcenvironment.components.pioneer.execute.PioneerComponent");
    }

}
