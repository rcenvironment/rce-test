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
import de.rcenvironment.rce.authorization.AuthorizationException;

/**
 * Registry for {@link Workflow}s.
 * 
 * @author Roland Gude
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 */
public interface WorkflowRegistry {

    /**
     * Creates a new instance.
     * 
     * @param user user's representation.
     * @param wfDesc {@link WorkflowDescription} describing the {@link Workflow} to create..
     * @param wfName Name of the {@link Workflow} to create.
     * @param configuration Configuration of the {@link Workflow} to create.
     * @return {@link WorkflowInformation} representing the created {@link Workflow}.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    WorkflowInformation createWorkflowInstance(User user, WorkflowDescription wfDesc,
        String wfName, Map<String, Object> configuration) throws AuthorizationException;
    
    /**
     * Removes the instantiated {@link Workflow} from the registry and unregister it from the OSGi service
     * registry. The {@link Workflow} is not accessible from then on.
     * 
     * @param user user's representation.
     * @param instanceId String identifying the {@link Workflow} as the one encapsulated in the {@link WorkflowInformation}.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    void disposeWorkflowInstance(User user, String instanceId) throws AuthorizationException;
    
    /**
     * Returns the {@link WorkflowInformation} that is identified b the given {@link Workflow} identifier.
     * 
     * @param user user's representation.
     * @param instanceId String identifying the {@link Workflow} as the one encapsulated in the {@link WorkflowInformation}.
     * @return {@link WorkflowInformation} representing the created {@link Workflow}.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    WorkflowInformation getWorkflowInformation(User user, String instanceId) throws AuthorizationException;
    
    /**
     * Returns all accessible {@link WorkflowInformation}s.
     * 
     * @param user user's representation.
     * @return {@link WorkflowInformation}s representing the {@link Workflow} accessible by the given user.
     */
    Set<WorkflowInformation> getWorkflowInformations(User user);
    
    /**
     * Checks if a {@link Workflow} was created by the given {@link User}s owner.
     * 
     * @param instanceId String identifying the {@link Workflow} as the one encapsulated in the {@link WorkflowInformation}.
     * @param user user's representation.
     * @return <code>true</code> if the instance exists and it is the creator, else <code>false</code>.
     * @throws AuthorizationException if needed authorizations are not satisfied.
     */
    boolean isCreator(String instanceId, User user) throws AuthorizationException;

}
