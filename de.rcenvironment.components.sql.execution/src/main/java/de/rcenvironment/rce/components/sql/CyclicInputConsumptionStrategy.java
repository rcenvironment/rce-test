/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import de.rcenvironment.rce.component.endpoint.Input;

/**
 * {@link InputConsumptionStrategy} considering a run, if certain or all {@link Input}s have a
 * value. If no specific inputs are used for construction (via
 * {@link CyclicInputConsumptionStrategy#CyclicInputConsumptionStrategy(String...)}), all
 * {@link Input}s must have a value.
 * 
 * @author Christian Weiss
 */
public class CyclicInputConsumptionStrategy {

    private Map<String, Class<? extends Serializable>> inputDefinitions;

    private final Map<String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();

    public CyclicInputConsumptionStrategy(final String... inputNames) {
        for (final String inputName : inputNames) {
            inputs.put(inputName, null);
        }
    }

    public CyclicInputConsumptionStrategy(final Collection<String> inputNames) {
        this(inputNames.toArray(new String[0]));
    }

    /**
     * Sets the input definitions.
     * 
     * @param inputDefinitions the input definitions
     */
    public void setInputDefinitions(final Map<String, Class<? extends Serializable>> inputDefinitions) {
        this.inputDefinitions = inputDefinitions;
        final boolean useAll = inputs.isEmpty();
        for (final String inputName : inputDefinitions.keySet()) {
            if (useAll || inputs.containsKey(inputName)) {
                inputs.put(inputName, new LinkedBlockingDeque<Input>());
            }
        }
    }

    /**
     * Adds an {@link Input}.
     * 
     * @param input the {@link Input} to add
     */
    public void addInput(final Input input) {
        checkState();
        final String inputName = input.getName();
        assert inputDefinitions.containsKey(inputName);
        assert inputDefinitions.get(inputName).isAssignableFrom(input.getType());
        if (!inputs.containsKey(inputName)) {
            throw new IllegalArgumentException(String.format("Unknown input (name='%s')"));
        }
        inputs.get(inputName).add(input);
    }

    /**
     * Returns, whether a run is indicated.
     * 
     * @return true, if a run is indicated
     */
    public boolean canRun() {
        checkState();
        boolean result = true;
        for (final String inputName : inputs.keySet()) {
            if (inputs.get(inputName).size() < 1) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the {@link Input}s for the current run.
     * 
     * @return the {@link Input}s for the current run
     */
    public Map<String, List<Input>> peekInputs() {
        checkState();
        final Map<String, List<Input>> result = new HashMap<String, List<Input>>();
        for (final String inputName : inputs.keySet()) {
            final List<Input> inputList = new ArrayList<Input>(1);
            inputList.add(inputs.get(inputName).peek());
            result.put(inputName, inputList);
        }
        return result;
    }

    /**
     * Returns the {@link Input}s for the current run.
     * 
     * @return the {@link Input}s for the current run
     */
    public Map<String, List<Input>> popInputs() {
        checkState();
        final Map<String, List<Input>> result = new HashMap<String, List<Input>>();
        for (final String inputName : inputs.keySet()) {
            final List<Input> inputList = new ArrayList<Input>(1);
            inputList.add(inputs.get(inputName).pop());
            result.put(inputName, inputList);
        }
        return result;
    }

    private void checkState() {
        if (inputDefinitions == null) {
            throw new IllegalStateException();
        }
    }

}
