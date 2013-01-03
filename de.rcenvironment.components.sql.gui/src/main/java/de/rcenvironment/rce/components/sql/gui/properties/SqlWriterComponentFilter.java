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
 * Filter for SqlWriterComponent instances.
 * 
 * @author Christian Weiss
 */
public class SqlWriterComponentFilter extends ComponentFilter {

    @Override
    public boolean filterComponentName(String componentId) {
        return componentId.equals(SqlComponentConstants.WRITER_COMPONENT_ID);
    }

}
