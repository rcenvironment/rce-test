/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.OutputDescriptor;

/**
 * Controls the lifecycle of one {@link Component}.
 *
 * @author Doreen Seider
 */
public interface ComponentController extends Serializable {

    /**
     * Initializes the controller, i.e., it instantiate the Component and create a appropriate
     * {@link ComponentInstanceInformation}.
     * 
     * @param newCert The user's certificate.
     * @param controllerId The instance id of the component controller.
     * @param compClazz The full qualified name of the Component implementing class.
     * @param compName The name of the {@link Component}.
     * @param compDesc The {@link Component}'s {@link ComponentDescription}.
     * @param compCtx The {@link Component}'s {@link ComponentContext}.
     * @param inputConnected <code>true</code> if at least one {@link Input} of this
     *        {@link Component} to control is connected with at least one {@link Output}.
     * @return The {@link ComponentInstanceDescriptor} representing the initialized {@link Component}.
     * @throws ComponentException if initialization of the {@link Component} failed.
     */
    ComponentInstanceDescriptor initialize(User newCert, String controllerId, String compClazz,
        String compName, ComponentDescription compDesc, ComponentContext compCtx, boolean inputConnected)
        throws ComponentException;

    /**
     * Prepares the {@link Component}.
     * 
     * @param user calling user
     * @param endpoints Mapping of {@link Output}s of the previous {@link Component} to the input names of this {@link Component} mapping.
     */
    void prepare(User user, Map<OutputDescriptor, String> endpoints);

    /**
     * Starts the {@link Component}, i.e., the check method and if required the run method are called.
     * @param user calling user
     */
    void start(User user);
    
    /**
     * Pauses the {@link Component}, i.e., check and run are not called until start is called again.
     * @param user calling user
     */
    void pause(User user);
    
    /**
     * Resumes the {@link Component}, i.e., the check method and if required the run method are called again.
     * @param user calling user
     */
    void resume(User user);
    
    /**
     * Cancels the {@link Component}.
     * @param user calling user
     */
    void cancel(User user);
    
    /**
     * Disposes the {@link Component}.
     * @param user calling user
     */
    void dispose(User user);
    
    /**
    * Call {@link Component}`s finished method.
    * @param user calling user
    */
    void finished(User user);
    
    /**
     * Blocking method called to wait for state READY. All remaining methods are non-blocking
     * methods in order to improve performance when setting up a workflow.
     */
    void waitForLifecyclePhaseFinished();
    
    /**
     * @return the current state of the controlled {@link Component}.
     */
    ComponentState getState();
    
    /**
     * @param inputs new queueds inputs.
     */
    void setInputs(BlockingQueue<Input> inputs);
    
    /**
     * @return queued inputs.
     */
    BlockingQueue<Input> getInputs();
    
    /**
     * @return numbers of currently processed input
     */
    Map<String, Integer> getCurrentInputNumbers();

}
