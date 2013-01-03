/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Default base class for {@link Component} implementations. Unless there is a specific reason not
 * to, all implementations should extend this class, instead of implementing {@link Component}
 * directly.
 * 
 * The onPrepare method needs to be called per super by any subclass, providing the information needed for the advanced
 * variables. If the method is not called, an error will show up.
 * The conRunAfterNewInput method provides a default implementation of the advanced variables. There, init variables are filled up
 * every time, after they got a value for the first time. Optional variables will be filled up if there was a value at any time, else 
 * they will be null. The method returns true, if all required variables got a value.
 * 
 * The other methods have default implementations, so subclasses only have to implement the aspects that
 * they actually need.
 * 
 * @author Robert Mischke
 * @author Sascha Zur
 */
public abstract class DefaultComponent implements Component {
    
    protected final Log logger = LogFactory.getLog(getClass());

    protected ComponentInstanceInformation instInformation;

    private Map<String, Input> initValues;

    private boolean subclassCalledSuper = false;
    
    private int beforeRunStepInputCount = 0;

    @Override
    public void onPrepare(ComponentInstanceInformation incCompInstanceInformation) throws ComponentException {
        this.instInformation = incCompInstanceInformation;
        initValues = new HashMap<String, Input>();

        Iterator<String> it = instInformation.getInputDefinitions().keySet().iterator();

        while (it.hasNext()){
            String next = it.next();
            if (instInformation.getInputMetaData(next).get(ComponentConstants.METADATAKEY_INPUT_USAGE) != null){
                if (instInformation.getInputMetaData(next).get(ComponentConstants.METADATAKEY_INPUT_USAGE)
                    .equals(ComponentConstants.INPUT_USAGE_TYPES[1])){
                    initValues.put(next, null);
                } else if (instInformation.getInputMetaData(next).get(ComponentConstants.METADATAKEY_INPUT_USAGE)
                    .equals(ComponentConstants.INPUT_USAGE_TYPES[2])){
                    initValues.put(next, null);
                }
            }
        }
        subclassCalledSuper = true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to do nothing; subclasses can override.
     */
    @Override
    public boolean runInitial(boolean inputsConnected) throws ComponentException {
        // by default, do nothing on initial run and wait for input
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to execute {@link #runStep} for each
     * input event by returning "true" from this method. Subclasses can aggregate input by returning
     * "false" until all necessary data is available.
     */
    @Override
    public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        
        if (!subclassCalledSuper){
            throw new ComponentException("Developer Error: Subclass of DefaultComponent did not call required super.onPrepare()!");
        }
        
        if (newInput != null && initValues.containsKey(newInput.getName())){
            initValues.put(newInput.getName(), newInput.clone());
        }
        
        return canRun(inputValues);
        
    }
    
    private boolean canRun(Map<String, Deque<Input>> inputValues) {
        
        boolean result = !hasNullInit();
        
        if (requiredNotEmpty(inputValues)){
            for (String key : initValues.keySet()){
                if (initValues.get(key) != null){
                    Deque<Input> newQueue = new LinkedList<Input>();
                    newQueue.add(initValues.get(key).clone());
                    inputValues.remove(key);
                    inputValues.put(key, newQueue);
                }
            }
        } else {
            result = false;
        }

        beforeRunStepInputCount = 0;
        for (String key : inputValues.keySet()){
            beforeRunStepInputCount += inputValues.get(key).size();
        }

        return result;
    }

    
    private boolean requiredNotEmpty(Map<String, Deque<Input>> inputValues) {
        Set<String> keys = instInformation.getInputDefinitions().keySet();
        for (String next : keys){
            if (!initValues.containsKey(next)){
                if ((inputValues.get(next) == null || inputValues.get(next).isEmpty())){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasNullInit(){
        Iterator<String> it = instInformation.getInputDefinitions().keySet().iterator();
   
        while (it.hasNext()){
            String next = it.next();
           
            if (instInformation.getInputMetaData(next).get(ComponentConstants.METADATAKEY_INPUT_USAGE) != null){
                if (instInformation.getInputMetaData(next).get(ComponentConstants.METADATAKEY_INPUT_USAGE)
                    .equals(ComponentConstants.INPUT_USAGE_TYPES[1])){
                    if (initValues.get(next) == null){
                        return true;
                    }
                } 
            }
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to do nothing; subclasses can override.
     */
    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        // do nothing; the return value is rather meaningless in a default implementation, so it is
        // arbitrarily set to "false" (which makes the component finish on any input)

        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to run again if canRunAfterNewInput is true; subclasses can override.
     */
    @Override
    public boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        int afterRunStepInputCount = 0;
        
        for (String key : inputValues.keySet()){
            afterRunStepInputCount += inputValues.get(key).size();
        }

        if (beforeRunStepInputCount == afterRunStepInputCount){
            throw new ComponentException("Developer Error: runStep did not consume inputs.");
        }
        return canRun(inputValues);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to do nothing; subclasses can override.
     */
    @Override
    public void onDispose() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to do nothing; subclasses can override.
     */
    @Override
    public void onCancel() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * The standard behaviour of {@link DefaultComponent} is to do nothing; subclasses can override.
     */
    @Override
    public void onFinish() {
        // do nothing
    }

}
