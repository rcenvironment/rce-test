/*
 * Copyright (C) 2010-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.merger.execution;

import java.util.Deque;
import java.util.Map;

import de.rcenvironment.rce.component.Component;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.components.merger.common.MergerComponentConstants;
/**
 * Component to merge n inputs to 1.
 *
 * @author Sascha Zur
 */
public class MergerComponent implements Component {

    private ComponentInstanceInformation compInformation;

    @Override
    public void onPrepare(ComponentInstanceInformation compInstanceInformation)
        throws ComponentException {
        compInformation = compInstanceInformation;
    }

    @Override
    public boolean runInitial(boolean inputsConnected)
        throws ComponentException {
        return inputsConnected;
    }

    @Override
    public boolean canRunAfterRun(Input lastInput,
        Map<String, Deque<Input>> inputValues) throws ComponentException {

        return false;
    }

    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues)
        throws ComponentException {
        compInformation.getOutput(MergerComponentConstants.OUTPUT_NAME).write(newInput.getValue());
        inputValues.get(newInput.getName()).clear();
        return true;
    }

    @Override
    public boolean canRunAfterNewInput(Input newInput,
        Map<String, Deque<Input>> inputValues) throws ComponentException {
        return true;
    }

    @Override
    public void onDispose() {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {

    }

}
