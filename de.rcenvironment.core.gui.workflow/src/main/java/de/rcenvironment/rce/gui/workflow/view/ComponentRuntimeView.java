/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;


/**
 * Interface that to get instantiated {@link Component}s of running workflows injected.
 *
 * @author Heinrich Wendel
 */
public interface ComponentRuntimeView {

    /**
     * Called by RCE to set the ComponentInstanceDescriptor of the component to monitor.
     * 
     * @param compInstanceDescr The {@link ComponentInstanceDescriptor} of the component to
     *        monitor.
     */
    void setComponentInstanceDescriptor(ComponentInstanceDescriptor compInstanceDescr);
    
}
