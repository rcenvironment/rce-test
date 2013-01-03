/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.sql.gui.properties;

import de.rcenvironment.rce.components.sql.commons.SqlComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentFilter;

/**
 * Filter for SqlComponent instances.
 * 
 * @author Christian Weiss
 */
public class SqlComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.equals(SqlComponentConstants.READER_COMPONENT_ID)
                || componentId.equals(SqlComponentConstants.WRITER_COMPONENT_ID)
                || componentId.equals(SqlComponentConstants.COMMAND_COMPONENT_ID);
    }

}
