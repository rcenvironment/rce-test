/*
 * Copyright (C) 2010-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.converger.execution;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.components.converger.common.ConvergerComponentConstants;
/**
 * Component to get data to convergence.
 * @author Sascha Zur
 *
 */
public class ConvergerComponent extends DefaultComponent{

    private Map<String, Serializable> oldInputs;

    @Override
    public void onPrepare(ComponentInstanceInformation incCompInstanceInformation) throws ComponentException {
        super.onPrepare(incCompInstanceInformation);
        oldInputs = new HashMap<String, Serializable>();
    }

    @Override
    public boolean runInitial(boolean inputsConnected) throws ComponentException {

        for (String key : instInformation.getInputDefinitions().keySet()){
            if (instInformation.getInputMetaData(key) != null
                && instInformation.getInputMetaData(key).get(ConvergerComponentConstants.META_HAS_STARTVALUE) != null
                && (Boolean) instInformation.getInputMetaData(key).get(ConvergerComponentConstants.META_HAS_STARTVALUE)) {
                oldInputs.put(key, instInformation.getInputMetaData(key).get(ConvergerComponentConstants.META_STARTVALUE));
                instInformation.getOutput(key).write((instInformation.getInputMetaData(key)
                    .get(ConvergerComponentConstants.META_STARTVALUE)));
            }
        }
        return inputsConnected;
    }

    @Override
    public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        if (instInformation.getInputMetaData(newInput.getName()) != null
            && instInformation.getInputMetaData(newInput.getName()).get(ConvergerComponentConstants.META_HAS_STARTVALUE) != null
            && !(Boolean) instInformation.getInputMetaData(newInput.getName()).get(ConvergerComponentConstants.META_HAS_STARTVALUE)) {
            if (oldInputs.get(newInput.getName()) == null) {
                oldInputs.put(newInput.getName(), newInput.getValue());
                instInformation.getOutput(newInput.getName()).write(newInput.getValue());
                inputValues.get(newInput.getName()).removeFirst();
            }
        }

        boolean canRun = super.canRunAfterNewInput(newInput, inputValues);
        return canRun;
    }

    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {


        boolean convergedAbs = true;
        boolean convergedRel = true;

        for (String key : oldInputs.keySet()){
            Serializable newInputValue = inputValues.get(key).removeFirst().getValue();
            if (Math.abs((Double) oldInputs.get(key) - (Double) newInputValue) 
                > (Double) instInformation.getConfigurationValue("epsA")){
                convergedAbs = false;
            }
            if (Math.abs((Double) oldInputs.get(key) - (Double) newInputValue) / (Double) oldInputs.get(key)  
                > (Double) instInformation.getConfigurationValue("epsR")){
                convergedRel = false;
            }
            oldInputs.put(key, newInputValue);
            instInformation.getOutput(key).write(oldInputs.get(key));
        }


        instInformation.getOutput("Converged absolute").write(convergedAbs);
        instInformation.getOutput("Converged relative").write(convergedRel);

        if (convergedAbs || convergedRel){
            for (String key : instInformation.getOutputDefinitions().keySet()){
                instInformation.getOutput(key).write(ComponentState.FINISHED.name());
            }
        }


        return true;

    }
    
    @Override
    public boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        
        return super.canRunAfterNewInput(lastInput, inputValues);
    }
}
