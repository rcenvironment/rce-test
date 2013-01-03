/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.gui.workflow.scripting.properties;

import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ScriptTime;


/**
 * {@link AbstractProcessingComponentSection} for PRE-processing.
 * 
 * @author Christian Weiss
 */
public class PreProcessingComponentSection extends AbstractProcessingComponentSection {

    public PreProcessingComponentSection() {
        super(ScriptTime.PRE);
    }

}
