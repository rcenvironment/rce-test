/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Service handling local and remote workflows.
 * 
 * @author Doreen Seider
 */
public interface DistributedWorkflowRegistry {
    
    /**
     * Returns all running workflows within the distributed system.
     * 
     * @param user Calling {@link User}
     * @param forceRefresh if <code>false</code> cached informations (from last lookup) are returned
     *        otherwise a new lookup is done
     * @return A {@link Set} of all running workflows represented by a {@link WorkflowInformation}
     */
    Set<WorkflowInformation> getAllWorkflowInformations(User user, boolean forceRefresh);

    /**
     * Returns all running workflows of the local platform.
     * 
     * @param user Calling {@link User}
     * @return A {@link Set} of all locally running workflows represented by a {@link WorkflowInformation}
     */
    Set<WorkflowInformation> getWorkflowInformations(User user);
    
    /**
     * Creates a new workflow instance.
     * 
     * @param user Calling {@link User}
     * @param workflowDescription The {@link WorkflowDescription} used for the workflow creation
     * @param name The name used for the workflow to create.
     * @param configuration The configuration of the workflow to instantiate.
     * @param platform The {@link PlatformIdentifier} of the platform the workflow should run
     * @return The workflow instance represented by a {@link WorkflowInformationImpl}
     */
    WorkflowInformation createWorkflowInstance(User user, WorkflowDescription workflowDescription,
        String name, Map<String, Object> configuration, PlatformIdentifier platform);

    /**
     * Disposes a worklfow instance.
     * 
     * @param user Calling {@link User}
     * @param identifier The identifier of the workflow to dipose
     * @param platform The {@link PlatformIdentifier} of the platform the workflow is instantiated
     */
    void disposeWorkflowInstance(User user, String identifier, PlatformIdentifier platform);

    /**
     * Returns a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the requested
     *        {@link Workflow}
     * @return The {@link Workflow} instance
     */
    Workflow getWorkflow(WorkflowInformation workflowInformation);
}
