/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.optimizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.Component;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.components.optimizer.algorithms.DakotaAlgorithm;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerPublisher;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerResultService;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerResultSet;
import de.rcenvironment.rce.components.optimizer.commons.ResultStructure;
import de.rcenvironment.rce.notification.DistributedNotificationService;


/**
 * Optimizer implementation of {@link Component}.
 * 
 * @author Sascha Zur
 */
public class OptimizerComponent implements Component {



    private static OptimizerResultService optimizerResultService;
    private static DistributedNotificationService notificationService;

    private static final Log LOGGER = LogFactory.getLog(OptimizerComponent.class);
    /** */
    public boolean dakotaThreadInterrupted;

    /** Socket for dakota client. */
    public Socket client;

    /** Socket for dakota.*/
    public ServerSocket serverSocket;

    /**
     * Portnumber for communication with dakota.
     */
    public int port;

    private OptimizerPublisher study;

    private Map<String, Class<? extends Serializable>> output;
    private Map<String, Class<? extends Serializable>> input;

    private Map<String, Double> outputValues;
    private Map<String, Serializable> values = new HashMap<String, Serializable>();

    private OptimizerResultSet dataset = null;
    private String algorithm;

    private ComponentInstanceInformation ci;

    private DakotaAlgorithm myDakotaProblem;
    private Thread dakotaThread;

    private String[] messageFromDakotaClient;
    private boolean stop;


    @Override
    public void onPrepare(ComponentInstanceInformation compInstanceInformation) throws ComponentException {
        stop = false;
        this.ci = compInstanceInformation;
        output = compInstanceInformation.getOutputDefinitions();
        input = compInstanceInformation.getInputDefinitions();
        algorithm = (String) compInstanceInformation.getConfigurationValue(OptimizerComponentConstants.ALGORITHM);
        if (algorithm == null && !compInstanceInformation.getComponentName().equals("Design of Experiments")){
            algorithm = OptimizerComponentConstants.ALGORITHM_QUASINEWTON;
        } else if (algorithm == null && compInstanceInformation.getComponentName().equals("Design of Experiments")) {
            algorithm = OptimizerComponentConstants.DOE_LHS;

        }
        final int bufferSize = 1000;
        notificationService.setBufferSize(ci.getComponentIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX, bufferSize);
        
        study = optimizerResultService.createPublisher(
            ci.getIdentifier(),
            ci.getComponentContextName(),
            createStructure(ci));
        LOGGER.debug("Optimizer Component prepared");
    }

    @Override
    public boolean runInitial(boolean inputsConnected) throws ComponentException {

        stop = false;

        outputValues = new HashMap<String, Double>();
        
        for (String key : output.keySet()){
            ci.getOutput(key).write(ci.getOutputMetaData(key).get(OptimizerComponentConstants.META_STARTVALUE));
            outputValues.put(key, (Double) ci.getOutputMetaData(key).get(OptimizerComponentConstants.META_STARTVALUE));
        }
        // Add first empty dataset to initialize datastore
        dataset = new OptimizerResultSet(values, ci.getComponentIdentifier());
        study.add(dataset);
        for (String key : outputValues.keySet()){
            values.put(key, outputValues.get(key));    
        }
        if (isFromDakota()){
            try {
                prepareDakotaProblem();
            } catch (IOException e){
                LOGGER.error(e.getMessage());
            }
        }
        return true;
    }

    private void prepareDakotaProblem() throws IOException, ComponentException {
        LOGGER.debug("Preparing Dakotaproblem");
        try {
            serverSocket = new ServerSocket(0);  // search for free port
            port = serverSocket.getLocalPort();

        } catch (IOException e1) {
            LOGGER.debug(e1.getStackTrace());
        }
        myDakotaProblem = new DakotaAlgorithm(algorithm, outputValues, input, ci, notificationService, this);
        dakotaThreadInterrupted = false;
        dakotaThread = new Thread(myDakotaProblem);
        dakotaThread.start();
        BufferedReader bufferedReader;
        try {
            client = serverSocket.accept();
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            char[] buffer = new char[DakotaAlgorithm.BUFFERLENGTH];
            int anzahlZeichen = bufferedReader.read(buffer, 0, DakotaAlgorithm.BUFFERLENGTH);
            String nachricht = new String(buffer, 0, anzahlZeichen);
            messageFromDakotaClient = nachricht.split("&&");
        } catch (IOException e) {
            LOGGER.debug("Dakotaproblem : Exception " + e.getMessage());
        }
        LOGGER.debug("Dakotaproblem prepared");
    }

    @Override
    public boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        return false;
    }



    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        Map<String, Double> inputVariables = new HashMap<String, Double>();
        Map<String, Double> constraintVariables = new HashMap<String, Double>();
        // isolate variables and save result
        manageNewInput(inputVariables, constraintVariables, newInput, inputValues);

        // start new algorithm run
        if (!stop && isFromDakota()){
            myDakotaProblem.runStep(this, messageFromDakotaClient, inputVariables, constraintVariables,  outputValues);
            if (myDakotaProblem.isDakotaFinished()){
                stop = true;
                myDakotaProblem.stop();
            }
        }

        // send new outputdata
        if (!stop){
            for (String key: output.keySet()){
                ci.getOutput(key).write(outputValues.get(key));
                values.put(key, outputValues.get(key));
            }
        } else {
            for (String key: output.keySet()){
                ci.getOutput(key).write("FINISHED");
            }
        }

        return !dakotaThreadInterrupted && !stop;
    }

    private boolean isFromDakota() {
  
        return (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON) 
            || algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)
            || algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)
            || algorithm.equals(OptimizerComponentConstants.ALGORITHM_MOGA)
            || algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)
            || algorithm.equals(OptimizerComponentConstants.DOE_LHS));
    }

    private void manageNewInput(Map<String, Double> inputVariables, Map<String, Double> constraintVariables,
        Input newInput, Map<String, Deque<Input>> inputValues) {
        for (String key : input.keySet()){
            double inputField = 0;
            if (inputValues.get(key) != null && !inputValues.get(key).isEmpty()){
                inputField = (Double) inputValues.get(key).getFirst().getValue();
                inputValues.get(key).removeFirst();
            } else {
                inputField = (Double) newInput.getValue();
            }

            if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) == OptimizerComponentConstants.PANE_INPUT) {
                inputVariables.put(key, inputField);
            } else {
                constraintVariables.put(key, inputField);
            }

        }
        for (String key : inputVariables.keySet()){
            values.put(key, inputVariables.get(key));
        }   
        for (String key : constraintVariables.keySet()){
            values.put(key, constraintVariables.get(key));
        }

        dataset = new OptimizerResultSet(values, ci.getComponentIdentifier());
        study.add(dataset);
        values = new HashMap<String, Serializable>();
    }

    @Override
    public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        boolean allInputsValid = true;
        if (allInputsValid){
            for (String key : input.keySet()){
                if (inputValues.get(key) != null && inputValues.get(key).isEmpty()  && newInput != null && !newInput.getName().equals(key)){
                    allInputsValid = false;
                }
            }
        }
        return allInputsValid;
    }

    @Override
    public void onDispose() {
        if (myDakotaProblem != null){
            myDakotaProblem.dispose();
        }
    }
    
    @Override
    public void onCancel() {
        stop = true;
        if (myDakotaProblem != null){
            dakotaThreadInterrupted = true;
            myDakotaProblem.stop();
            dakotaThread.interrupt();
            myDakotaProblem.dispose();
            myDakotaProblem = null;
        }
    }

    protected void bindOptimizerResultService(final OptimizerResultService optimizerService) {
        optimizerResultService = optimizerService;
    }

    protected void unbindOptimizerResultService(final OptimizerResultService optimizerService) {
    }


    private static ResultStructure createStructure(
        final ComponentInstanceInformation componentInformation) {
        final ResultStructure structure = new ResultStructure();
        // outputs are dimensions
        for (Map.Entry<String, Class<? extends Serializable>> outputDefinition : componentInformation
            .getOutputDefinitions().entrySet()) {
            final de.rcenvironment.rce.components.optimizer.commons.Dimension dimension = 
                new de.rcenvironment.rce.components.optimizer.commons.Dimension(
                    outputDefinition.getKey(), //
                    outputDefinition.getValue().getName(), //
                    true);
            structure.addDimension(dimension);
        }
        // inputs are measures
        for (Map.Entry<String, Class<? extends Serializable>> inputDefinition : componentInformation
            .getInputDefinitions().entrySet()) {
            final de.rcenvironment.rce.components.optimizer.commons.Measure measure = 
                new de.rcenvironment.rce.components.optimizer.commons.Measure(
                    inputDefinition.getKey(), //
                    inputDefinition.getValue().getName());
            structure.addMeasure(measure);
        }
        return structure;
    }


    @Override
    public void onFinish() {
        if (myDakotaProblem != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.debug(e.getStackTrace());
            }
            myDakotaProblem.stop();
            dakotaThread.interrupt();
            myDakotaProblem.dispose();
        }
    }
    protected void bindDistributedNotificationService(
        DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void unbindDistributedNotificationService(
        DistributedNotificationService oldNotificationService) {
        // nothing to do here, this unbind method is only needed, because DS is
        // thtowing an
        // exception when disposing otherwise. properly a bug
    }

}
