/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.parametricstudy;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.components.parametricstudy.commons.Dimension;
import de.rcenvironment.rce.components.parametricstudy.commons.Measure;
import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyComponentConstants;
import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyService;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyDataset;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyPublisher;
import de.rcenvironment.rce.components.parametricstudy.commons.StudyStructure;

/**
 * Parametric Study implementation of {@link Component}.
 * 
 * @author Markus Kunde
 * @author Arne Bachmann
 * @author Doreen Seider
 */
public class ParametricStudyComponent extends DefaultComponent {
    
    private static final Log LOGGER = LogFactory.getLog(ParametricStudyComponent.class);
    
    private static final String OUTPUT_CHANNELNAME = "DesignVariable";
    
    private static ParametricStudyService parametricStudyService;

    private StudyPublisher study;
    
    private double from;

    private double to;

    private double stepSize;

    private double designVariable;

    private int count;

    private int steps;
    
    private Output designVariableOutput;
    
    @Override
    public void onPrepare(ComponentInstanceInformation information) throws ComponentException {
        super.onPrepare(information);

        designVariableOutput = instInformation.getOutput(OUTPUT_CHANNELNAME);

        from = ((Double) instInformation.getConfigurationValue(ParametricStudyComponentConstants.CV_FROMVALUE)).doubleValue();       
        to = ((Double) instInformation.getConfigurationValue(ParametricStudyComponentConstants.CV_TOVALUE)).doubleValue();      
        stepSize = ((Double) instInformation.getConfigurationValue(ParametricStudyComponentConstants.CV_STEPSIZE)).doubleValue(); 
        study = parametricStudyService.createPublisher(
            instInformation.getIdentifier(),
            information.getComponentContextName(),
            createStructure(instInformation));
        count = 0; 
        steps = ((Double) Math.floor((to - from) / stepSize)).intValue() + 1; // including first AND last
        
        LOGGER.debug("Parametric Study component prepared");
    }
    
    protected void bindParametricStudyService(final ParametricStudyService parametricService) {
        parametricStudyService = parametricService;
    }
    
    protected void unbindParametricStudyService(final ParametricStudyService parametricService) {
    }

    private static StudyStructure createStructure(
        final ComponentInstanceInformation componentInformation) {
        final StudyStructure structure = new StudyStructure();
        // outputs are dimensions
        for (Map.Entry<String, Class<? extends Serializable>> outputDefinition : componentInformation
            .getOutputDefinitions().entrySet()) {
            final Dimension dimension = new Dimension(
                outputDefinition.getKey(), //
                outputDefinition.getValue().getName(), //
                true);
            structure.addDimension(dimension);
        }
        // inputs are measures
        for (Map.Entry<String, Class<? extends Serializable>> inputDefinition : componentInformation
            .getInputDefinitions().entrySet()) {
            final Measure measure = new Measure(
                inputDefinition.getKey(), //
                inputDefinition.getValue().getName());
            structure.addMeasure(measure);
        }
        return structure;
    }

    @Override
    public boolean runInitial(final boolean inputsConnected) {
        designVariableOutput.write(calculateInitialDesignVariable());
        study.add(new StudyDataset(new HashMap<String, Serializable>()));
        if (!inputsConnected) {
            while (count < steps) {
                designVariableOutput.write(calculateDesignVariable());
            }
        }
        return inputsConnected;
    }
    
    @Override
    public boolean runStep(final Input newInput, final Map<String, Deque<Input>> pendingInputValues) {

        // send input parameters to study service for monitoring purposes
        final Map<String, Serializable> values = new HashMap<String, Serializable>();
        // input parameters are response to previous iteration
        values.put(designVariableOutput.getName(), geLastDesignVariable());
        for (final Entry<String, Deque<Input>> input: pendingInputValues.entrySet()) {
            final Double response;
            final Serializable value = input.getValue().pollFirst().getValue();
            if (value instanceof Double) {
                response = ((Double) value);
            } else if (value instanceof Long)  {
                response = Double.valueOf((Long) value); 
            } else if (value instanceof Integer) {
                response = Double.valueOf((Integer) value);
            } else {
                response = Double.NaN;
            }
            values.put(input.getKey(), response);
        }
        
        study.add(new StudyDataset(values));
        
        if (count < steps) {
            designVariableOutput.write(calculateDesignVariable());
        } else {
            designVariableOutput.write(ComponentState.FINISHED.name());
        }

        return true;
    }
    
    @Override
    public boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        return false;
    }
    private double calculateInitialDesignVariable() {
        count ++;
        designVariable = from;
        return designVariable;
    }
    
    private double geLastDesignVariable() {
        return designVariable;
    }

    private double calculateDesignVariable() {
        designVariable = from + (to - from) * (count ++) / (steps - 1.0);
        return designVariable;
    }

}
