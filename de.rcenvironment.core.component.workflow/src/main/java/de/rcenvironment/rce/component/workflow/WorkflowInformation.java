/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;

/**
 * Holding information of a instantiated {@link Workflow}.
 * 
 * @author Doreen Seider
 */
public interface WorkflowInformation extends ComponentContext, Serializable {

    /**
     * @return the name of the workflow.
     */
    String getName();

    /**
     * @return the id of the user instantiated the workflow.
     */
    String getUser();

    /**
     * @return the time the workflow was instantiated.
     */
    Date getInstantiationTime();

    /**
     * @return the time the workflow was instantiated.
     */
    WorkflowDescription getWorkflowDescription();

    /**
     * @return the additional information of the workflow.
     */
    String getAdditionalInformation();
    
    /**
     * @param wfNodeName name of workflow node
     * @param componentId identifier of underlying component
     * @return {@link ComponentInstanceDescriptor} of running component with given aname.
     */
    ComponentInstanceDescriptor getComponentInstanceDescriptor(String wfNodeName, String componentId);
    
    /**
     * @return {@link ComponentInstanceDescriptor}s of contained running components.
     */
    Set<ComponentInstanceDescriptor> getComponentInstanceDescriptors();

}
