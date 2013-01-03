/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.properties;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.gui.workflow.SubscriptionManager;
import de.rcenvironment.rce.notification.SimpleNotificationService;


/**
 * Provides central access to input values, processed and pending ones.
 *
 * @author Doreen Seider
 */
public final class InputModel {

    private static InputModel instance;
    
    private static Map<String, Map<String, BlockingQueue<Input>>> allRawInputs; 
    
    private static Map<String, Map<String, Map<String, Deque<Input>>>> allInputs;
    
    private static Map<String, Map<String, Map<String, Integer>>> allCurrentInputNumbers;
   
    private static Map<String, Boolean> workflowChanged;
    
    private static Map<String, Map<String, Boolean>> componentChanged;

    private static Map<String, Map<String, Boolean>> componentModified;
    
    private static CurrentInputSubscriptionEventProcessor eventProcessor;
    
    private static SubscriptionManager currentInputManager;
       
    private InputModel() {}
    
    /**
     * Singleton getter to provide central model access.
     * 
     * @return the singleton instance
     */
    public static synchronized InputModel getInstance() {
        if (null == instance) {
            instance = new InputModel();
            allRawInputs = new ConcurrentHashMap<String, Map<String, BlockingQueue<Input>>>();
            allInputs = new ConcurrentHashMap<String, Map<String, Map<String, Deque<Input>>>>();
            allCurrentInputNumbers = new ConcurrentHashMap<String, Map<String, Map<String, Integer>>>();
            workflowChanged = new ConcurrentHashMap<String, Boolean>();
            componentChanged = new ConcurrentHashMap<String, Map<String, Boolean>>();
            componentModified = new ConcurrentHashMap<String, Map<String, Boolean>>();
            eventProcessor = new CurrentInputSubscriptionEventProcessor(instance);
            currentInputManager = new SubscriptionManager(eventProcessor);
            currentInputManager.initialize(new String[] { ComponentConstants.INPUT_NOTIFICATION_ID,
                ComponentConstants.CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID });

            new SimpleNotificationService().subscribeToAllPlatforms(WorkflowConstants.STATE_DISPOSED_NOTIFICATION_ID,
                new WorkflowDisposeListener());
        }
        return instance;
    }
    
    /**
     * Updates subscriptions to known server instances.
     */
    public synchronized void updateSubscriptions() {
        currentInputManager.updateSubscriptions(new String[] { ComponentConstants.INPUT_NOTIFICATION_ID,
            ComponentConstants.CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID });
    }
    
    /**
     * Batch version of {@link #addConsoleRow(Input)} to reduce synchronization overhead.
     * 
     * @param inputs the list of {@link Inputs}s to add
     */
    public void addInputs(List<Input> inputs) {
        for (Input input : inputs) {
            if (isValue(input)) {
                String workflowId = input.getWorkflowIdentifier();
                String componentId = input.getComponentIdentifier();
                String inputName = input.getName();
                if (!allInputs.containsKey(workflowId)) {
                    allInputs.put(workflowId, new HashMap<String, Map<String, Deque<Input>>>());
                }
                if (!allInputs.get(workflowId).containsKey(componentId)) {
                    allInputs.get(workflowId).put(componentId, new HashMap<String, Deque<Input>>());
                }
                if (!allInputs.get(workflowId).get(componentId).containsKey(inputName)) {
                    allInputs.get(workflowId).get(componentId).put(inputName, new LinkedList<Input>());
                }
                allInputs.get(workflowId).get(componentId).get(inputName).addLast(input);
                setWorkflowChanged(workflowId, true);
                setComponentChanged(workflowId, componentId, true);
            }
        }
    }
    
    /**
     * Sets the currently processed input.
     * @param input currently processed input
     */
    public void setCurrentInput(Input input) {
        if (isValue(input)) {
            String workflowId = input.getWorkflowIdentifier();
            String componentId = input.getComponentIdentifier();
            String inputName = input.getName();
            Map<String, Integer> number = new HashMap<String, Integer>();
            number.put(inputName, input.getNumber());
            setCurrentInputNumbers(workflowId, componentId, number);
        }
    }
    
    /**
     * Sets the number of the currently processed input.
     * @param workflowId identifier of workflow
     * @param componentId identifier of component
     * @param numbers currently processed input numbers
     */
    public void setCurrentInputNumbers(String workflowId, String componentId, Map<String, Integer> numbers) {
        if (!allCurrentInputNumbers.containsKey(workflowId)) {
            allCurrentInputNumbers.put(workflowId, new HashMap<String, Map<String, Integer>>());
        }
        if (!allCurrentInputNumbers.get(workflowId).containsKey(componentId)) {
            allCurrentInputNumbers.get(workflowId).put(componentId, new HashMap<String, Integer>());
        }
        for (String inputName : numbers.keySet()) {
            allCurrentInputNumbers.get(workflowId).get(componentId).put(inputName, numbers.get(inputName));
        }
        setWorkflowChanged(workflowId, true);
        setComponentChanged(workflowId, componentId, true);            
    }
    
    /**
     * Returns a {@link Deque} of {@link Input}s for a specified input of a specified component and worklfow.
     * Intended to be called by input view.
     * @param workflowId identifier of workflow
     * @param componentId identifier of component
     * @param inputName name of input
     * @return {@link Deque} containing inputs
     */
    public Deque<Input> getInputs(String workflowId, String componentId, String inputName) {
        Deque<Input> inputs = new LinkedList<Input>();
        if (allInputs.containsKey(workflowId) && allInputs.get(workflowId).containsKey(componentId)
            && allInputs.get(workflowId).get(componentId).containsKey(inputName)) {
            inputs = allInputs.get(workflowId).get(componentId).get(inputName);
        }
        setWorkflowChanged(workflowId, false);
        setComponentChanged(workflowId, componentId, false);
        return inputs;
    }
    
    /**
     * Sets {@link Input}s.
     * Intended to be called after suspend was requested to have a current snapshot of the inputs.
     * @param workflowId identifier of workflow
     * @param componentId identifier of workflow
     * @param rawInputs raw inputs to set
     */
    public void setRawInputs(String workflowId, String componentId, BlockingQueue<Input> rawInputs) {
        if (!allRawInputs.containsKey(workflowId)) {
            allRawInputs.put(workflowId, new HashMap<String, BlockingQueue<Input>>());
        }
        allRawInputs.get(workflowId).put(componentId, rawInputs);
        setComponentModified(workflowId, componentId, false);
        
        // remove all not processed inputs
        if (allInputs.containsKey(workflowId) && allInputs.get(workflowId).containsKey(componentId)) {
            for (Deque<Input> inputs : allInputs.get(workflowId).get(componentId).values()) {
                Map<String, Integer> inputNumbers = allCurrentInputNumbers.get(workflowId).get(componentId);
                while (inputs.peekLast() != null && inputs.peekLast().getNumber() > inputNumbers.get(inputs.peekLast().getName())) {
                    inputs.removeLast();
                }
            }            
        }
        
        addInputs(new ArrayList<Input>(rawInputs));
    }
    
    /**
     * Returns a {@link Map} of {@link Deque}s of {@link Input}s for a specified component and worklfow.
     * Intended to be called for pushing them to the component.
     * @param workflowId identifier of workflow
     * @param componentId identifier of workflow
     * @return {@link Map} of {@link Deque}s containing inputs
     */
    public BlockingQueue<Input> getRawInputs(String workflowId, String componentId) {
        BlockingQueue<Input>  inputs = new LinkedBlockingQueue<Input>();
        if (allRawInputs.containsKey(workflowId) && allRawInputs.get(workflowId).containsKey(componentId)) {
            inputs = allRawInputs.get(workflowId).get(componentId);
        }
        setComponentModified(workflowId, componentId, false);
        return inputs;
    }
    
    /**
     * @param workflowId identifier of workflow
     * @param componentId identifier of workflow
     * @param inputName name of input
     * @return next input to process for a specified input of a specified component and worklfow.
     */
    public Input getCurrentInput(String workflowId, String componentId, String inputName) {
        int index = getNumberOfCurrentInput(workflowId, componentId, inputName);
        return getInput(workflowId, componentId, inputName, index);
    }
    
    /**
     * @param workflowId identifier of workflow
     * @param componentId identifier of workflow
     * @param inputName name of input
     * @return next input to process for a specified input of a specified component and worklfow.
     */
    public Input getNextInput(String workflowId, String componentId, String inputName) {
        int index = getNumberOfCurrentInput(workflowId, componentId, inputName) + 1;
        return getInput(workflowId, componentId, inputName, index);
    }
    
    private Input getInput(String workflowId, String componentId, String inputName, int index) {
        List<Input> inputList = new ArrayList<Input>(getInputs(workflowId, componentId, inputName));
        if (!inputList.isEmpty() && index >= 0 && index < inputList.size()) {
            return inputList.get(index);
        } else {
            return null;
        }
    }
        
    /**
     * Replaces input value of given input.
     * @param newInput affected input
     */
    public void replaceInput(Input newInput) {
        String workflowId = newInput.getWorkflowIdentifier();
        String componentId = newInput.getComponentIdentifier();
        String inputName = newInput.getName();
        if (allInputs.get(workflowId) != null && allInputs.get(workflowId).get(componentId) != null
            && allInputs.get(workflowId).get(componentId).containsKey(inputName)) {
            Deque<Input> inputs = allInputs.get(workflowId).get(componentId).get(inputName);
            for (Input input : inputs) {
                if (input.getNumber() == newInput.getNumber()) {
                    input.setValue(newInput.getValue());
                }
            }
        }
        setComponentModified(workflowId, componentId, true);
    }
    
    /**
     * Returns current index (i.e. next input to process) for a specified input of a specified component and worklfow.
     * @param workflowId identifier of workflow
     * @param componentId identifier of workflow
     * @param inputName name of input
     * @return index
     */
    public int getNumberOfCurrentInput(String workflowId, String componentId, String inputName) {
        if (allCurrentInputNumbers.containsKey(workflowId) && allCurrentInputNumbers.get(workflowId).containsKey(componentId)
            && allCurrentInputNumbers.get(workflowId).get(componentId).containsKey(inputName)) {
            return allCurrentInputNumbers.get(workflowId).get(componentId).get(inputName);
        }
        setWorkflowChanged(workflowId, false);
        setComponentChanged(workflowId, componentId, false);
        return 0;
    }
    
    /**
     * @param workflowId identifier of workflow
     * @param componentId identifier of component
     * @return <code>true</code> if model for given workflow and component changed, else <code>false</code>
     */
    public boolean hasChanged(String workflowId, String componentId) {
        if (componentChanged.containsKey(workflowId) && componentChanged.get(workflowId).containsKey(componentId)) {
            return componentChanged.get(workflowId).get(componentId);
        }
        return false;
    }
    
    /**
     * @param workflowId identifier of workflow
     * @return <code>true</code> if model for given workflow changed, else <code>false</code>
     */
    public boolean hasChanged(String workflowId) {
        if (workflowChanged.containsKey(workflowId)) {
            return workflowChanged.get(workflowId);
        }
        return false;
    }
    
    /**
     * @param workflowId identifier of workflow
     * @param componentId identifier of component
     * @return <code>true</code> if model for given workflow and component has been modified, else <code>false</code>
     */
    public boolean hasBeenModified(String workflowId, String componentId) {
        if (componentModified.containsKey(workflowId) && componentModified.get(workflowId).containsKey(componentId)) {
            return componentModified.get(workflowId).get(componentId);
        }
        return false;
    }
    
    /**
     * Removes all {@link Input}s beloning to given workflow.
     * @param workflowId identifier of workflow
     */
    public void removeInputs(String workflowId) {
        allInputs.remove(workflowId);
    }
    
    private void setWorkflowChanged(String workflowId, Boolean hasChanged) {
        workflowChanged.put(workflowId, hasChanged);
    }
    
    private void setComponentChanged(String workflowId, String componentId, Boolean hasChanged) {
        if (!componentChanged.containsKey(workflowId)) {
            componentChanged.put(workflowId, new HashMap<String, Boolean>());
        }
        componentChanged.get(workflowId).put(componentId, hasChanged);
    }
    
    private void setComponentModified(String workflowId, String componentId, Boolean hasBeenModified) {
        if (!componentModified.containsKey(workflowId)) {
            componentModified.put(workflowId, new HashMap<String, Boolean>());
        }
        componentModified.get(workflowId).put(componentId, hasBeenModified);
    }
    
    private boolean isValue(Input input) {
        return !(input.getValue() instanceof String
            && (((String) input.getValue()).equals(ComponentState.FINISHED.toString())
                || ((String) input.getValue()).equals(ComponentState.FAILED.toString())));
    }
    
    public CurrentInputSubscriptionEventProcessor getEventProcessor() {
        return eventProcessor;
    }
}
