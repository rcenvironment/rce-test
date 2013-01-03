/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.Deque;
import java.util.Map;

import de.rcenvironment.rce.component.endpoint.Input;

/**
 * A {@link Component} in the representation of an application that has been integrated into RCE and
 * can participate in workflows.
 * 
 * Its lifecycle is as follows:
 * <p>
 * <img src="doc-files/componentLifecycle.png">
 * 
 * TODO adapt image to return values
 * 
 * @author Roland Gude
 * @author Doreen Seider
 * @author Robert Mischke
 */
public interface Component extends Registerable {

    /**
     * Called if the workflow is prepared. Things that usually should go into the
     * constructor (like obtaining resources) need to go here.
     * 
     * This method is called immediately after the {@link Component}'s construction.
     * 
     * @param compInstanceInformation The {@link ComponentInstanceInformation} of the instantiated
     *        {@link Component}.
     * 
     * @throws ComponentException to indicate a fatal error
     */
    void onPrepare(ComponentInstanceInformation compInstanceInformation) throws ComponentException;

    /**
     * Runs the {@link Component} initially. This directly done once after workflow started.
     * @param inputsConnected <code>true</code> if there are {@link Input}s connected, else
     *        <code>false</code>, i,e. checkAfterNewInput() will never be called.
     * @throws ComponentException to indicate a fatal error
     * @return <code>false</code> if {@link Component} is FINISHED, else <code>true</code>
     */
    boolean runInitial(boolean inputsConnected) throws ComponentException;

    /**
     * Called after run method terminated. The {@link Component} needs to check here if it can run
     * again without having new inputs.
     *  @param lastInput The last {@link Input} received. If runStep was called, without having a new
     *        input (i.e. if canRunAfterRun returns <code>true</code>), this parameter is
     *        <code>null</code>
     * @param inputValues pending inputs values map. input names are the keys.
     * @return <code>true</code> if the {@link Component}s run() method can be called, else
     *         <code>false</code>.
     * @throws ComponentException to indicate a fatal error
     */
    boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException;

    /**
     * Runs the {@link Component}. It is meant as one computing step.
     * @param newInput The new {@link Input} received. If runStep is called, without having a new
     *        input (i.e. if canRunAfterRun returns <code>true</code>), this parameter is
     *        <code>null</code>
     * @param inputValues pending inputs values map. input names are the keys.
     * @throws ComponentException to indicate a fatal error
     * @return <code>false</code> if {@link Component} is FINISHED, else <code>true</code>
     */
    boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException;

    /**
     * Called if a new {@link Input} value was received for this {@link Component}. The
     * {@link Component} needs to check here if it can run now.
     * @param newInput The new {@link Input} received.
     * @param inputValues pending inputs values map. input names are the keys.
     * @return <code>true</code> if the {@link Component}s run() method can be called, else
     *         <code>false</code>.
     * @throws ComponentException to indicate a fatal error
     */
    boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException;

    /**
     * Called if the workflow is disposed, i.e. it is finally terminated. All resources
     * acquired and not yet released in onFinish() or onCancel() should be released here.
     */
    void onDispose();

    /**
     * Called if the workflow is canceled. All resources acquired for running should be released
     * here. The component will never run again.
     */
    void onCancel();

    /**
     * Called if the workflow is finished. All resources acquired for running should be released
     * here. The component will never run again.
     */
    void onFinish();

}
