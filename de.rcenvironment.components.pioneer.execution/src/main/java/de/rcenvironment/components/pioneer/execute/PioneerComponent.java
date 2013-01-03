/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.execute;

import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_EXECUTE_INITIAL;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_ITERATIONS;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_MESSAGE;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_OPERATION_MODE;

import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.ScriptEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.components.pioneer.common.PioneerOperationMode;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentInstanceInformationUtils;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.component.ConsoleRow.Type;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.component.scripting.AbstractScriptingComponent;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Example and test component.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class PioneerComponent extends AbstractScriptingComponent {

    private static final Random RANDOM = new Random();

    /**
     * The default wait time used in several places; can be set to a smaller value to speed up
     * testing.
     */
    private static final long DEFAULT_STEP_DELAY = 1;

    private static final Log LOGGER = LogFactory.getLog(PioneerComponent.class);

    private static DistributedNotificationService notificationService;

    private Output output;

    private PioneerOperationMode operationMode;

    private Integer iterations;

    private String message;

    private final AtomicInteger count = new AtomicInteger(0);

    public PioneerComponent() {
        super(TriggerMode.Manual);
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

    @Override
    public void onPrepare(ComponentInstanceInformation incInstInformation) throws ComponentException {
        super.onPrepare(incInstInformation);
        /*
         * Store the default output for convenient access in the run methods.
         */
        output = instInformation.getOutput("cooler:output");
        /*
         * Extract the configuration values into attributes.
         */
        message = ComponentInstanceInformationUtils.getConfigurationValue(KEY_MESSAGE, String.class, instInformation);
        operationMode = ComponentInstanceInformationUtils.getConfigurationValue(KEY_OPERATION_MODE, PioneerOperationMode.class,
            instInformation);
        iterations = ComponentInstanceInformationUtils.getConfigurationValue(KEY_ITERATIONS, Integer.class, instInformation);
        /*
         * Initialize the NotificationService with reasonable configuration values.
         */
        final int bufferSize = 1000;
        final String componentId = instInformation.getComponentIdentifier();
        notificationService.setBufferSize(componentId + ConsoleRow.NOTIFICATION_SUFFIX, bufferSize);
        /*
         * Log information about the component preparation.
         */
        LOGGER.info(instInformation.getName() + " prepared");
        final String componentName = instInformation.getComponentName();
        sendConsoleNotification(ConsoleRow.Type.META_INFO, componentName + " prepared");
    }

    @Override
    protected void beforeScriptExecution(final ScriptEngine engine) {
        // do something here before script execution
    }

    @Override
    protected void afterScriptExecution(final ScriptEngine engine) {
        // do something here afterscript execution
    }

    @Override
    protected boolean runInitialInScriptingComponent(boolean inputsConnected) throws ComponentException {
        // LOGGER.info(instInformation.getName() + " running initial step");
        sendConsoleNotification(ConsoleRow.Type.STDOUT, "Running initial step");
        // the component has more runs, if iterations have to be executed
        final boolean isActive = operationMode == PioneerOperationMode.ACTIVE;
        final boolean isPassive = !isActive;
        /*
         * If the component is passively answering on incoming inputs, there will be more runs.
         */
        boolean moreRuns = isPassive;
        final boolean executeInitial = ComponentInstanceInformationUtils.getConfigurationValue(KEY_EXECUTE_INITIAL, Boolean.class,
            instInformation);
        // execute all runs in the initial run, if the component is 'active'
        final boolean executeAll = isActive;
        // execute the initial run only if the component is 'passive' and the execute initial
        // property is set
        final boolean executeJustInitial = isPassive && executeInitial;
        /*
         * If the pioneer component shall not wait for inputs to trigger the runs, all iterations
         * are executed in the initial run and the component is finished afterwards.
         * 
         * If the pioneer component shall wait for triggers, but execute the first iteration without
         * waiting, this is done.
         */
        if (executeAll) {
            int iterationNumber;
            while ((iterationNumber = count.incrementAndGet()) <= iterations) {
                executeIteration(iterationNumber);
            }
            moreRuns = false;
        } else if (executeJustInitial) {
            final int iterationNumber = count.incrementAndGet();
            executeIteration(iterationNumber);
        }
        return moreRuns;
    }

    @Override
    protected boolean runStepInScriptingComponent(Map<String, Deque<Input>> inputValues) throws ComponentException {
        // LOGGER.info(instInformation.getName() + " running input-driven step");
        boolean moreRuns = true;
        /*
         * Inform about the input that triggered the component run.
         */
        final String inputName = inputValues.keySet().iterator().next();
        final Input input = inputValues.get(inputName).removeFirst();
        final String receiveMessage = "Running input-driven step after reading input '" + (String) input.getValue() + "'";
        sendConsoleNotification(ConsoleRow.Type.META_INFO, receiveMessage);
        // LOGGER.info(receiveMessage);
        /*
         * Calculate the current iteration number and perform the iteration.
         */
        final int iterationNumber = count.incrementAndGet();
        executeIteration(iterationNumber);
        /*
         * Return the information whether the component is finished after this run.
         */
        return moreRuns;
    }

    private void executeIteration(final int iterationNumber) throws ComponentException {
        waitDefaultTime();
        sendMessage();
    }

    private void sendMessage() throws ComponentException {
        try {
            /*
             * Replace all replacement substrings (example: ${config:iteration}) with their values.
             */
            waitDefaultTime();
            String messageWithCount = message + "-" + count.get();
            output.write(messageWithCount);
            sendConsoleNotification(ConsoleRow.Type.STDOUT, "Sent output '" + messageWithCount + "'");
        } catch (RuntimeException e) {
            /*
             * Throwing a RuntimeException in the component run execution results in a cancellation
             * of the component.
             */
            final String errorMessage = "Component failed and the whole workflow needs to be cancelled.";
            // throw new ComponentException(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public void onDispose() {
        if (instInformation != null) {
            LOGGER.info(instInformation.getName() + " disposed.");
        }
    }

    @Override
    public void onCancel() {
        if (instInformation != null) {
            LOGGER.info(instInformation.getName() + " cancelled.");
        }
    }

    @Override
    public void onFinish() {
        final String notificationMessage = instInformation.getName() + " finished";
        sendConsoleNotification(ConsoleRow.Type.META_INFO, notificationMessage);
        LOGGER.info(instInformation.getName() + "' context finished");
    }

    private void sendConsoleNotification(final Type notificationType, final String notificationMessage) {
        final String componentId = instInformation.getComponentIdentifier();
        final String componentName = instInformation.getComponentIdentifier();
        final String notificationId = componentId + ConsoleRow.NOTIFICATION_SUFFIX;
        notificationService.send(notificationId,
            new ConsoleRow(instInformation.getComponentContextName(),
                componentName,
                notificationType,
                notificationMessage));
    }

    private void waitDefaultTime() throws ComponentException {
        try {
            Thread.sleep(RANDOM.nextInt(new Long(DEFAULT_STEP_DELAY).intValue()));
        } catch (InterruptedException e) {
            throw new ComponentException(e);
        }
    }
}
