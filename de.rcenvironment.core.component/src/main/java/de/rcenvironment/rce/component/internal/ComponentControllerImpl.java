/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.component.Component;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentController;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.InputHandler;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.component.endpoint.OutputDescriptor;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Implementation of {@link ComponentController}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class ComponentControllerImpl implements ComponentController {

    private static final String RUNNING_COMPONENT_FAILED = "Component run failed";

    private static final long serialVersionUID = -211259434066472102L;
    
    private static final int INPUT_BUFFER = 10000;

    private static final Log LOGGER = LogFactory.getLog(ComponentControllerImpl.class);

    private Component component;

    private ComponentInstanceInformation compInfo;

    private ComponentState compState;

    private User user;

    private ExecutorService lifecycleExecutor;
    
    private ExecutorService sendNotificationExecutor = Executors.newSingleThreadExecutor();

    private BlockingQueue<Future<?>> lastRunningTasks = new LinkedBlockingQueue<Future<?>>();

    private Runnable processInputsTask;

    private ThreadFactory processInputsThreadFac;

    private volatile ExecutorService processInputsExecutor;

    private volatile boolean interruptInputProcessing = false;
    
    private boolean postRunCheckDone = true;

    private BlockingQueue<Input> queuedInputs = new LinkedBlockingQueue<Input>();
    
    private Queue<Input> queuedInputsDuringPaused = new LinkedList<Input>();

    private Map<String, Deque<Input>> pendingInputs = new HashMap<String, Deque<Input>>();
    
    private Map<String, Deque<Input>> allInputs = new HashMap<String, Deque<Input>>();
    
    private Map<String, Integer> numbersOfCurrentInputs = new HashMap<String, Integer>();
    
    private Set<String> connectedInputNames = new HashSet<String>();

    private Set<String> finishedInputs = new HashSet<String>();

    // needed to hold references here, otherwise they are not reachable when they are called backed,
    // if no one holds them anymore
    private Set<InputHandler> inputHandlers = new HashSet<InputHandler>();

    private DistributedNotificationService notificationService;

    private ConfigurationService configurationService;
    
    // excluding initial run
    private int noOfRuns = 0;

    public void setDistributedNotificationService(DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    public void setConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    @Override
    @AllowRemoteAccess
    public ComponentInstanceDescriptor initialize(User incUser, String controllerId, String compClazz,
        String compName, ComponentDescription compDesc, ComponentContext compCtx, boolean inputConnected) throws ComponentException {

        user = incUser;

        final String message = "Can not instantiate component: " + compClazz;
        try {
            component = (Component) Class.forName(compClazz).getConstructor().newInstance();
        } catch (SecurityException e) {
            throw new ComponentException(message, e);
        } catch (NoSuchMethodException e) {
            throw new ComponentException(message, e);
        } catch (ClassNotFoundException e) {
            throw new ComponentException(message, e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException(message, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException(message, e);
        } catch (InvocationTargetException e) {
            throw new ComponentException(message, e);
        } catch (InstantiationException e) {
            throw new ComponentException(message, e);
        }

        final String compWorkDir = configurationService.getPlatformTempDir() + File.separator + "component-" + controllerId;
        File tmpDir = new File(compWorkDir);
        tmpDir.mkdirs();

        ComponentInstanceDescriptor compInstDesc = new ComponentInstanceDescriptor(controllerId, compName,
            compDesc.getPlatform(), compWorkDir, compDesc.getIdentifier(), compCtx.getName(), compCtx.getInvolvedPlatforms());
        Set<Output> outputs = new HashSet<Output>();
        Map<String, Class<? extends Serializable>> outputsDef = compDesc.getOutputDefinitions();
        for (String outputName : outputsDef.keySet()) {
            outputs.add(new Output(new OutputDescriptor(compInstDesc, outputName), outputsDef.get(outputName), notificationService));
        }

        compInfo = new ComponentInstanceInformation(controllerId, compName, compWorkDir, compDesc, compCtx,
                incUser, inputConnected, outputs);

        // initialize Executor service used for component lifecycle phase execution
        ThreadFactory compTaskThreadFac = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Component_" + compInfo.getName());
            }
        };
        lifecycleExecutor = Executors.newSingleThreadExecutor(compTaskThreadFac);

        // set up the task processing the inputs
        processInputsTask = new Runnable() {

            @Override
            public void run() {
                processQueuedInputs();
            }
        };

        processInputsThreadFac = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ProcessComponentInputs_" + compInfo.getName());
            }
        };
        processInputsExecutor = Executors.newSingleThreadExecutor(processInputsThreadFac);

        notificationService.setBufferSize(ComponentConstants.STATE_NOTIFICATION_ID_PREFIX + compInfo.getIdentifier(), 1);
        notificationService.setBufferSize(ComponentConstants.NO_OF_RUNS_NOTIFICATION_ID_PREFIX + compInfo.getIdentifier(), 1);
        notificationService.setBufferSize(ComponentConstants.INPUT_NOTIFICATION_ID, INPUT_BUFFER);
        notificationService.setBufferSize(ComponentConstants.CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID, 1);
        setState(ComponentState.INSTANTIATED);

        return compInstDesc;
    }

    @Override
    @AllowRemoteAccess
    public void prepare(final User incUser, final Map<OutputDescriptor, String> endpoints) {

        Runnable prepareTask = new Runnable() {

            @Override
            public void run() {
                if (compState != ComponentState.INSTANTIATED) {
                    throw new IllegalStateException("Component lifecycle issue when prepare was requested: needs to be INSTANTIATED"
                        + " but was " + compState);
                }

                setState(ComponentState.PREPARING);
                checkUser(incUser);
                if (endpoints != null) {
                    for (OutputDescriptor outputDesc : endpoints.keySet()) {
                        subscribeForOutputNotifications(outputDesc,
                            endpoints.get(outputDesc),
                            compInfo.getInputDefinitions().get(endpoints.get(outputDesc)));

                        connectedInputNames.add(endpoints.get(outputDesc));
                    }
                    for (String inputName: connectedInputNames) {
                        pendingInputs.put(inputName, new LinkedList<Input>());
                        allInputs.put(inputName, new LinkedList<Input>());
                        final int none = -1;
                        numbersOfCurrentInputs.put(inputName, none);
                    }
                }

                try {
                    component.onPrepare(compInfo);
                } catch (ComponentException e) {
                    throw new RuntimeException(e);
                }
                setState(ComponentState.PREPARED);
            }
        };

        lastRunningTasks.add(lifecycleExecutor.submit(prepareTask));

    }

    @Override
    @AllowRemoteAccess
    public void start(final User incUser) {

        synchronized (lastRunningTasks) {
            
            Runnable initialRunTask = new Runnable() {
    
                @Override
                public void run() {
                    if (compState != ComponentState.PREPARED) {
                        throw new IllegalStateException("Component lifecycle issue when start was requested: needs to be PREPARED but was "
                            + compState);
                    }
                    setState(ComponentState.RUNNING);
                    checkUser(incUser);
                    boolean canRunAgain = false;
                    try {
                        try {
                            canRunAgain = component.runInitial(compInfo.isInputConnected());
                        } catch (RuntimeException e) {
                            // wrap RuntimeExceptions
                            throw new ComponentException(e);
                        }
                    } catch (ComponentException e) {
                        LOGGER.error(RUNNING_COMPONENT_FAILED, e);
                        setState(ComponentState.FAILED);
                        return;
                    }
                    if (!compInfo.isInputConnected() || !canRunAgain) {
                        finish();
                    } else {
                        setState(ComponentState.READY);
                    }
                }
            };
    
            lastRunningTasks.add(lifecycleExecutor.submit(initialRunTask));
    
            Runnable runTask = new Runnable() {
    
                @Override
                public void run() {
                    if (connectedInputNames.size() > 0) {
                        if (compState != ComponentState.READY) {
                            throw new IllegalStateException("Component lifecycle issue when start was requested: needs to be READY but was "
                                    + compState);
                        }
                        checkUser(incUser);
                        processInputsExecutor.submit(processInputsTask);
                    }
                }
            };
            
            lastRunningTasks.add(lifecycleExecutor.submit(runTask));
        }
    }

    @Override
    @AllowRemoteAccess
    public void pause(final User incUser) {

        Runnable pauseTask = new Runnable() {

            @Override
            public void run() {
                // component can fail or finish asynchronously; needs to be checked here if this happened
                if (compState == ComponentState.FINISHED || compState == ComponentState.FAILED) {
                    return;
                }
                
                if (compState != ComponentState.RUNNING && compState != ComponentState.CHECKING && compState != ComponentState.READY) {
                    throw new IllegalStateException("Component lifecycle issue when pause was requested: needs to be RUNNING or CHECKING,"
                        + " or READY but was " + compState);
                }

                setState(ComponentState.PAUSING);
                checkUser(incUser);
                interruptInputProcessing = true;
                processInputsExecutor.shutdown();
                final long fourtyTwo = 42;
                try {
                    processInputsExecutor.awaitTermination(fourtyTwo, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    LOGGER.error("Awaiting the last component's run after pausing request failed.", e);
                }
                setState(ComponentState.PAUSED);
            }
        };

        lastRunningTasks.add(lifecycleExecutor.submit(pauseTask));

    }

    @Override
    @AllowRemoteAccess
    public void resume(final User incUser) {

        Runnable resumeTask = new Runnable() {

            @Override
            public void run() {
                // component can fail or finish asynchronously; needs to be checked here if this happened
                if (compState == ComponentState.FINISHED || compState == ComponentState.FAILED) {
                    return;
                }
                if (compState != ComponentState.PAUSED) {
                    throw new IllegalStateException("Component lifecycle issue when resume was requested: needs to be PAUSED but was "
                        + compState);
                }
                setState(ComponentState.STARTING);
                checkUser(incUser);
                while (queuedInputsDuringPaused.peek() != null) {
                    newInput(queuedInputsDuringPaused.poll());
                }
                processInputsExecutor = Executors.newSingleThreadExecutor(processInputsThreadFac);
                processInputsExecutor.submit(processInputsTask);
            }
        };
        lastRunningTasks.add(lifecycleExecutor.submit(resumeTask));
    }

    @Override
    @AllowRemoteAccess
    public void cancel(final User incUser) {

        Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                // component can fail or finish asynchronously; needs to be checked here if this happened
                if (compState == ComponentState.FINISHED_NO_RUN_STEP
                    || compState == ComponentState.FINISHED
                    || compState == ComponentState.FAILED) {
                    return;
                }
                
                setState(ComponentState.CANCELING);
                checkUser(incUser);
                interruptInputProcessing = true;
                processInputsExecutor.shutdown();
                final long fourtyTwo = 42;
                try {
                    processInputsExecutor.awaitTermination(fourtyTwo, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    LOGGER.error("Awaiting the last component's run after cancelling request failed.", e);
                }
                component.onCancel();
                setState(ComponentState.CANCELED);
            }
        };

        lastRunningTasks.add(lifecycleExecutor.submit(cancelTask));
    }

    @Override
    @AllowRemoteAccess
    public void dispose(final User incUser) {

        Runnable disposeTask = new Runnable() {

            @Override
            public void run() {
                if (compState != ComponentState.FINISHED && compState != ComponentState.FAILED && compState != ComponentState.CANCELED) {
                    throw new IllegalStateException("Component lifecycle issue when 'dispose' was requested: needs to be"
                        + " FINISHED, FAILED, or CANCELED but was " + compState);
                }
                setState(ComponentState.DISPOSING);
                checkUser(incUser);
                component.onDispose();
                setState(ComponentState.DISPOSED);
            }
        };

        lastRunningTasks.add(lifecycleExecutor.submit(disposeTask));
        lifecycleExecutor.shutdown();
    }

    @Override
    @AllowRemoteAccess
    public void waitForLifecyclePhaseFinished() {

        boolean exceptionThrown = false;
        
        final String message = "Error occured in some lifecycle phase.";
        while (!lastRunningTasks.isEmpty()) {
            try {
                lastRunningTasks.poll().get();
            } catch (InterruptedException e) {
                LOGGER.error(RUNNING_COMPONENT_FAILED, e);
                setState(ComponentState.FAILED);
                exceptionThrown = true;
            } catch (ExecutionException e) {
                LOGGER.error(RUNNING_COMPONENT_FAILED, e);
                setState(ComponentState.FAILED);
                exceptionThrown = true;
            } catch (RuntimeException e) {
                LOGGER.error(RUNNING_COMPONENT_FAILED, e);
                setState(ComponentState.FAILED);
                exceptionThrown = true;
            }
        }

        if (exceptionThrown) {
            throw new RuntimeException(message);            
        }
    }

    @Override
    @AllowRemoteAccess
    public ComponentState getState() {
        return compState;
    }
    
    @Override
    public Map<String, Integer> getCurrentInputNumbers() {
        return Collections.unmodifiableMap(numbersOfCurrentInputs);
    }

    /**
     * Called to announce a new {@link Input}.
     * 
     * @param input New {@link Input} to announce.
     */
    public synchronized void newInput(Input input) {
        if (compState == ComponentState.PAUSING || compState == ComponentState.PAUSED) {
            queuedInputsDuringPaused.add(input);
        } else {
            queuedInputs.add(input);
            allInputs.get(input.getName()).addLast(input);
            sendInput(input);            
        }
    }

    private void checkUser(final User aUser) {
        if (!aUser.equals(user)) {
            throw new AuthorizationException("User not allowed to control lifecyle of component: " + aUser.toString());
        }
    }

    private void subscribeForOutputNotifications(OutputDescriptor outputDesc, String inputName, Class<? extends Serializable> inputType) {
        final String notificationId = ComponentConstants.OUTPUT_NOTIFICATION_ID_PREFIX
                + outputDesc.getComponentInstanceDescriptor().getIdentifier()
                + outputDesc.getName();
        InputHandler inputHandler = new InputHandler(this, inputName, inputType,
            compInfo.getComponentContextIdentifier(), compInfo.getIdentifier());
        inputHandlers.add(inputHandler);
        notificationService.subscribe(notificationId, inputHandler, outputDesc.getComponentInstanceDescriptor().getPlatform());
    }

    private void processQueuedInputs() {
        setState(ComponentState.READY);
        
        boolean continueInputProcessing = true;
        while (continueInputProcessing) {
            if (interruptInputProcessing) {
                interruptInputProcessing = false;
                continueInputProcessing = false;
                continue;
            }
            Input input = null;
            if (postRunCheckDone) { // first gather new input and check for running passing this input to component
                try {
                    final long oneSecond = 1000;
                    input = queuedInputs.poll(oneSecond, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    failed(new IllegalStateException("Waiting for inputs failed.", e));
                    continueInputProcessing = false;
                    continue;
                }

                if (input == null) {
                    continue;
                }
                if (input.getValue() instanceof String && input.getValue().equals(ComponentState.FINISHED.name())) {
                    finishedInputs.add(input.getName());
                    if (finishedInputs.size() == connectedInputNames.size()) { // if all inputs are finished
                        finish();
                        continueInputProcessing = false;
                        continue;
                    }
                    continue;
                } else {
                    finishedInputs.remove(input.getName());
                    pendingInputs.get(input.getName()).addLast(input);
                    numbersOfCurrentInputs.put(input.getName(), input.getNumber());
                    sendRecentlyConsumedInput(input);
                    setState(ComponentState.CHECKING);
                    try {
                        try {
                            if (!component.canRunAfterNewInput(input, pendingInputs)) {
                                setState(ComponentState.READY);
                                continue;
                            } else {
                                setState(ComponentState.RUNNING);
                                boolean finished = !component.runStep(input, pendingInputs);
                                increaseNoOfRuns();
                                if (finished) {
                                    finish();
                                    continueInputProcessing = false;
                                    continue;
                                }
                            }
                        } catch (RuntimeException e) {
                            // wrap RuntimeExceptions
                            throw new ComponentException(e);
                        }
                    } catch (ComponentException e) {
                        failed(e);
                        continueInputProcessing = false;
                        continue;
                    }
                }
            }
            try {
                try {
                    while (continueInputProcessing) {
                        postRunCheckDone = false;
                        if (interruptInputProcessing) {
                            break;
                        }
                        
                        setState(ComponentState.CHECKING);
                        if (component.canRunAfterRun(input, pendingInputs)) {
                            input = null;
                            postRunCheckDone = true;
                            setState(ComponentState.RUNNING);
                            boolean finished = !component.runStep(null, pendingInputs);
                            increaseNoOfRuns();
                            if (finished) {
                                finish();
                                continueInputProcessing = false;
                                continue;
                            }
                        } else {
                            postRunCheckDone = true;
                            setState(ComponentState.READY);
                            break;
                        }
                    }
                } catch (RuntimeException e) {
                    // wrap RuntimeExceptions
                    throw new ComponentException(e);
                }
            } catch (ComponentException e) {
                failed(e);
                continueInputProcessing = false;
                continue;
            }
        }
    }
    
    private synchronized void setState(ComponentState state) {
        ComponentState oldState = compState;
        compState = state;
        // after failure workflow will be canceled but the component's failure state should be
        // remain even if the component is canceled
        if (oldState != ComponentState.FAILED) {
            
            notificationService.send(ComponentConstants.STATE_NOTIFICATION_ID_PREFIX + compInfo.getIdentifier(), state.name());
        
            if (state == ComponentState.FINISHED || state == ComponentState.FINISHED_NO_RUN_STEP) {
                notificationService.send(ComponentConstants.FINISHED_STATE_NOTIFICATION_ID_PREFIX
                    + compInfo.getComponentContextIdentifier(), compInfo.getIdentifier());
            }
        
            // if the component fails during running, ComponentFailListener needs to be notified
            // in other lifecycle phases this is handled in another way based on waitingForLifecyclePhaseFinished
            if (state == ComponentState.FAILED && (oldState == ComponentState.RUNNING || oldState == ComponentState.CHECKING)) {
                sendNotificationExecutor.submit(new Runnable() {

                    @Override
                    public void run() {
                        notificationService.send(ComponentConstants.FAILED_STATE_NOTIFICATION_ID_PREFIX
                            + compInfo.getComponentContextIdentifier(), compInfo.getIdentifier());
                    }
                });
            }
            compState = state;
        }
    }
    
    private synchronized void increaseNoOfRuns() {
        notificationService.send(ComponentConstants.NO_OF_RUNS_NOTIFICATION_ID_PREFIX
            + compInfo.getIdentifier(), new Integer(++noOfRuns));
    }

    private void finish() {
        processInputsExecutor.shutdown();
        if (connectedInputNames.size() != 0 && noOfRuns == 0) {
            setState(ComponentState.FINISHED_NO_RUN_STEP);
        } else {
            setState(ComponentState.FINISHED);
        }
        for (String outputName : compInfo.getOutputDefinitions().keySet()) {
            compInfo.getOutput(outputName).write(ComponentState.FINISHED.name());
        }
    }
    
    private void failed(Throwable t) {
        LOGGER.error(RUNNING_COMPONENT_FAILED, t);
        setState(ComponentState.FAILED);
    }

    @Override
    @AllowRemoteAccess
    public void finished(final User aUser) {

        Runnable finishedTask = new Runnable() {

            @Override
            public void run() {
                if (compState != ComponentState.FINISHED && compState != ComponentState.FAILED) {
                    throw new IllegalStateException("Component lifecycle issue when 'finish' was requested:"
                            + " must not be FINISHED, FINISHED_NOT_RUN, or FAILED - it was " + compState);
                }
                checkUser(aUser);
                component.onFinish();
            }
        };

        lastRunningTasks.add(lifecycleExecutor.submit(finishedTask));
    }

    @Override
    public void setInputs(BlockingQueue<Input> inputs) {
        queuedInputs = inputs;
    }
    
    @Override
    public BlockingQueue<Input> getInputs() {
        return queuedInputs;
    }
    
    private void sendInput(final Input input) {
        sendNotificationExecutor.submit(new Runnable() {
            
            @Override
            public void run() {
                notificationService.send(ComponentConstants.INPUT_NOTIFICATION_ID, input);
            }
            
        });
    }
    
    private void sendRecentlyConsumedInput(final Input input) {
        
        sendNotificationExecutor.submit(new Runnable() {
            
            @Override
            public void run() {
                notificationService.send(ComponentConstants.CURRENTLY_PROCESSED_INPUT_NOTIFICATION_ID, input);
            }
            
        });
    }

}
