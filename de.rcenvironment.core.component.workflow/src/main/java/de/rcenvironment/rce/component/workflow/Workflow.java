/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import de.rcenvironment.rce.authentication.User;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Class representing a workflow. It contains {@link Component}s and provides an
 * API for its lifecycle.
 * 
 * @author Roland Gude
 * @author Doreen Seider
 */
public interface Workflow extends Serializable {
    
    
    /**
     * Initializes this {@link Workflow}. Sets up all {@link Component}s.
     * Pre: -
     * Post: WorkflowState.READY
     *
     * @param user The user's {@link User}.
     */
    void initialize(User user);
    
    /**
     * Starts this {@link Workflow}. Setups and starts all {@link Component}s.
     * Pre: WorkflowState.READY
     * Post (async): WorkflowState.RUNNING, WorkflowState.FAILED or current state if precondition not satisfied.
     *
     * @param user The user's {@link User}.
     */
    void start(User user);
    
    /**
     * Pauses this {@link Workflow}.
     * Pre: WorkflowState.RUNNING
     * Post (async): WorkflowState.PAUSED, WorkflowState.FAILED or current state of precondition not satisfied.
     * 
     * @param user The user's {@link User}.
     */
    void pause(User user);
    
    /**
     * Resumes this {@link Workflow}..
     * Pre: WorkflowState.READY Post: WorkflowState.RUNNING Error: WorkflowState.ERROR
     * Post (async): WorkflowState.RUNNING, WorkflowState.ERROR
     * 
     * @param user The user's {@link User}.
     */
    void resume(User user);

    /**
     * Cancels all {@link Component}s belonging to this {@link Workflow}.
     * Pre: WorkflowState.RUNNING, WorkflowState.READY
     * Post (async): WorkflowState.CANCELED, WorkflowState.FAILED or current state if precondition not satisfied.
     * 
     * @param user The user's {@link User}.
    */
    void cancel(User user);

    /**
     * Disposes all {@link Component}s.
     * Pre: WorkflowState.FINISHED, WorkflowState.CANCELED, WorkflowState.ERROR Post: WorkflowState.DISPOSED, WorkflowState.ERROR
     * Post (async): WorkflowState.DISPOSED, WorkflowState.FAILED or current state if precondition not satisfied.
     * 
     * @param user The user's {@link User}.
     */
    void dispose(User user);
    
    /**
     * Call done at all {@link Component}s.
     * Pre: WorkflowState.FINISHED,WorkflowState.FAILED Post: WorkflowState.FINISHED, WorkflowState.FAILED
     * Post (async): WorkflowState.FINISHED, WorkflowState.FAILED or current state if precondition not satisfied.

     * @param user The user's {@link User}.
     */
    void finished(User user);

    /**
     * Returns the state of the {@link Workflow}.
     * 
     * @param user The user's {@link User}.
     * 
     * @return the state of the {@link Workflow}.
     */
    WorkflowState getState(User user);
    
    /**
     * Returns the state of a {@link Component} of this {@link Workflow} given by its identifier.
     * 
     * @param user The user's {@link User}.
     * @param componentInstancId Identifier of the instantiated {@link Component}.
     * 
     * @return the state of the {@link Component}.
     */
    ComponentState getStateOfComponent(User user, String componentInstancId);

    /**
     * Returns the {@link ComponentInstanceInformation} objects of the workflow's components.
     * 
     * @param user The user's {@link User}.
     * 
     * @return the instantiated components represented by {@link ComponentInstanceDescriptor} objects.
     */
    Set<ComponentInstanceDescriptor> getComponentInstanceDescriptors(User user);
    
    /**
     * Sets inputs for a component of a workflow.
     * @param user requesting one.
     * @param componentInstancId Identifier of the component.
     * @param inputs new inputs to push to a component.
     */
    void setInputs(User user, String componentInstancId, BlockingQueue<Input> inputs);

    /**
     * Returns inputs of a component of a workflow.
     * @param user requesting one.
     * @param componentInstancId Identifier of the component.
     * @return inputs of a component.
     */
    BlockingQueue<Input> getInputs(User user, String componentInstancId);
    
    /**
     * Returns numbers of currently processed inputs.
     * @param user requesting one.
     * @param componentInstancId Identifier of the component.
     * @return inputs of a component.
     */
    Map<String, Integer> getCurrentInputNumbers(User user, String componentInstancId);
}
