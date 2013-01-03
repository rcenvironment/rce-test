/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentController;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.DistributedComponentRegistry;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.OutputDescriptor;
import de.rcenvironment.rce.component.workflow.ComponentFailListener;
import de.rcenvironment.rce.component.workflow.ComponentFinishListener;
import de.rcenvironment.rce.component.workflow.Connection;
import de.rcenvironment.rce.component.workflow.Workflow;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.component.workflow.WorkflowState;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Implementation of {@link Workflow}.
 * 
 * @author Roland Gude
 * @author Doreen Seider
 */
public class WorkflowImpl implements Workflow {

    private static final String EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED = "Executing workflow failed. It will be canceled.";

    private static final String BUT_WAS = " but was ";

    private static final long serialVersionUID = 1084316077536368109L;

    private static final Log LOGGER = LogFactory.getLog(WorkflowImpl.class);

    private final WorkflowInformation wfInfo;

    private final User user;

    private volatile WorkflowState state;

    private Map<String, ComponentInstanceDescriptor> nodeIdToCompInstDescMapping = new HashMap<String, ComponentInstanceDescriptor>();

    private Map<String, ComponentController> compInfoIdToCompControllerMapping = new HashMap<String, ComponentController>();

    // needed to hold a reference here, otherwise it is not reachable when it is called backed, if
    // no one holds one anymore
    private ComponentFinishListener cFinishListener;

    // needed to hold a reference here, otherwise it is not reachable when it is called backed, if
    // no one holds one anymore
    private ComponentFailListener cFailListener;

    private DistributedComponentRegistry componentRegistry;

    private CommunicationService communicationService;

    private DistributedNotificationService notificationService;

    private BundleContext bundleCtx;
    
    private ExecutorService executor;

    /**
     * Constructor.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} of this {@link Workflow}.
     * 
     * @param proxyCertificate The {@link User} of the user running this {@link Workflow}.
     */
    public WorkflowImpl(WorkflowInformation workflowInformation, User user) {
        wfInfo = workflowInformation;
        this.user = user;

        nodeIdToCompInstDescMapping = new HashMap<String, ComponentInstanceDescriptor>();
        compInfoIdToCompControllerMapping = new HashMap<String, ComponentController>();
        
        // initialize Executor service used for component lifecycle phase execution
        ThreadFactory compTaskThreadFac = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Workflow_" + wfInfo.getName());
            }
        };
        executor = Executors.newSingleThreadExecutor(compTaskThreadFac);
    }

    protected void setCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void setDistributedNotificationService(DistributedNotificationService newDistributedNotificationService) {
        notificationService = newDistributedNotificationService;
    }

    protected void setDistributedComponentRegistry(DistributedComponentRegistry newDistributedComponentRegistry) {
        componentRegistry = newDistributedComponentRegistry;
    }

    protected void setBundleContext(BundleContext bundleContext) {
        bundleCtx = bundleContext;
    }
    
    @Override
    public void initialize(User aUser) {
        checkUser(aUser);
        try {
            instantiateComponents();
            setState(WorkflowState.READY);
            notificationService.send(WorkflowConstants.NEW_WORKFLOW_NOTIFICATION_ID, wfInfo.getIdentifier());
        } catch (RuntimeException e) {
            LOGGER.error(EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED, e);
            cancel(aUser);
            setState(WorkflowState.FAILED);
        }
    }
    
    @Override
    @AllowRemoteAccess
    public void start(final User aUser) {
        
        Callable<WorkflowState> startTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                if (state != WorkflowState.READY) {
                    throw new IllegalStateException("Workflow lifecycle issue when start was requested: needs to be READY"
                        + BUT_WAS + state);
                }
        
                checkUser(aUser);
                try {
                    setState(WorkflowState.PREPARING);
                    prepareComponents();
                    setState(WorkflowState.RUNNING);
                    startComponents();
                } catch (RuntimeException e) {
                    LOGGER.error(EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED, e);
                    cancel(user);
                    setState(WorkflowState.FAILED);
                }
                
                return state;
            }
        };
        
        executor.submit(startTask);
    }

    @Override
    @AllowRemoteAccess
    public void pause(final User newUser) {

        Callable<WorkflowState> pauseTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                
                if (state != WorkflowState.RUNNING) {
                    throw new IllegalStateException("Workflow lifecycle issue when pause was requested: needs to be RUNNING"
                        + BUT_WAS + state);
                }
        
                checkUser(newUser);
                try {
                    setState(WorkflowState.PAUSING);
        
                    for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
                        compController.pause(user);
                    }
                    waitForComponents();
                    setState(WorkflowState.PAUSED);
                    
                } catch (RuntimeException e) {
                    LOGGER.error(EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED, e);
                    cancel(user);
                    setState(WorkflowState.FAILED);
                }
                
                return state;
            }
        };
        
        executor.submit(pauseTask);
        
    }

    @Override
    @AllowRemoteAccess
    public void resume(final User newUser) {

        Callable<WorkflowState> resumeTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                if (state != WorkflowState.PAUSED) {
                    throw new IllegalStateException("Workflow lifecycle issue when resume was requested: needs to be PAUSED"
                        + BUT_WAS + state);
                }
        
                checkUser(newUser);
                try {
                    setState(WorkflowState.RESUMING);
        
                    for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
                        compController.resume(user);
                    }
                    waitForComponents();
                    setState(WorkflowState.RUNNING);
        
                } catch (RuntimeException e) {
                    LOGGER.error(EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED, e);
                    cancel(user);
                    setState(WorkflowState.FAILED);
                }
                
                return state;
            }
        };
        
        executor.submit(resumeTask);

    }

    @Override
    @AllowRemoteAccess
    public void finished(final User aUser) {

        Callable<WorkflowState> finishTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                if (state != WorkflowState.FINISHED) {
                    throw new IllegalStateException("Workflow lifecycle issue when finished was requested: needs to be FINISHED or FAILED"
                        + BUT_WAS + state);
                }
        
                checkUser(aUser);
                try {
                    for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
                        compController.finished(user);
                    }
                } catch (RuntimeException e) {
                    LOGGER.error(EXECUTING_WORKFLOW_FAILED_IT_WILL_BE_CANCELED, e);
                    cancel(user);
                    setState(WorkflowState.FAILED);
                }
                
                return state;
            }
        };
        
        executor.submit(finishTask);

                
    }

    @Override
    @AllowRemoteAccess
    public void cancel(final User newUser) {

        Callable<WorkflowState> cancelTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                checkUser(newUser);
                try {
                    setState(WorkflowState.CANCELING);
                    for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
                        compController.cancel(newUser);
                    }
                    waitForComponents();
                    setState(WorkflowState.CANCELED);
                } catch (RuntimeException e) {
                    LOGGER.error("Cancelling workflow failed.", e);
                    setState(WorkflowState.FAILED);
                }
                return state;
            }
        };
        
        executor.submit(cancelTask);

    }

    @Override
    @AllowRemoteAccess
    public void dispose(final User newUser) throws AuthorizationException {

        Callable<WorkflowState> disposeTask = new Callable<WorkflowState>() {

            @Override
            public WorkflowState call() {
                
                if (state != WorkflowState.FINISHED & state != WorkflowState.CANCELED & state != WorkflowState.FAILED) {
                    throw new IllegalStateException(
                        "Workflow lifecycle issue when dispose was requested: needs to be FINISHED, CANCELED, or FAILED"
                            + BUT_WAS + state);
                }
        
                checkUser(newUser);
                setState(WorkflowState.DISPOSING);
                for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
                    compController.dispose(user);
                }
                waitForComponents();
        
                for (ComponentInstanceDescriptor cid : nodeIdToCompInstDescMapping.values()) {
                    componentRegistry.disposeComponentInstance(user, cid.getIdentifier(), cid.getPlatform());
                }
                notificationService.removePublisher(WorkflowConstants.STATE_NOTIFICATION_ID + wfInfo.getIdentifier());
                setState(WorkflowState.DISPOSED);
                
                return state;
            }
        };
        
        executor.submit(disposeTask);

    }

    @Override
    @AllowRemoteAccess
    public WorkflowState getState(User newUser) {
        checkUser(newUser);
        return state;
    }

    @Override
    public ComponentState getStateOfComponent(User newUser, String componentInstancId) {
        checkUser(newUser);
        if (compInfoIdToCompControllerMapping.containsKey(componentInstancId)) {
            return compInfoIdToCompControllerMapping.get(componentInstancId).getState();
        } else {
            LOGGER.warn("Component state not accessible. Component does not belong to requested workflow.");
            return null;
        }
    }

    @Override
    @AllowRemoteAccess
    public Set<ComponentInstanceDescriptor> getComponentInstanceDescriptors(User aUser) {
        checkUser(aUser);

        return new HashSet<ComponentInstanceDescriptor>(nodeIdToCompInstDescMapping.values());
    }
    
    @Override
    public void setInputs(User newUser, String componentInstancId, BlockingQueue<Input> inputs) {
        checkUser(newUser);
        if (compInfoIdToCompControllerMapping.containsKey(componentInstancId)) {
            compInfoIdToCompControllerMapping.get(componentInstancId).setInputs(inputs);
        } else {
            LOGGER.warn("Input values could not be pushed to Component. Component does not belong to requested workflow.");
        }
    }
    
    @Override
    public BlockingQueue<Input> getInputs(User newUser, String componentInstancId) {
        checkUser(newUser);
        if (compInfoIdToCompControllerMapping.containsKey(componentInstancId)) {
            return compInfoIdToCompControllerMapping.get(componentInstancId).getInputs();
        } else {
            LOGGER.warn("Inputs could not be retrieved from Component. Component does not belong to requested workflow.");
            return new LinkedBlockingQueue<Input>();
        }
    }
    
    @Override
    public Map<String, Integer> getCurrentInputNumbers(User newUser, String componentInstancId) {
        checkUser(newUser);
        if (compInfoIdToCompControllerMapping.containsKey(componentInstancId)) {
            return compInfoIdToCompControllerMapping.get(componentInstancId).getCurrentInputNumbers();
        } else {
            LOGGER.warn("Inputs could not be retrieved from Component. Component does not belong to requested workflow.");
            return new HashMap<String, Integer>();
        }
    }


    /**
     * Sets the state of the {@link Workflow}.
     * 
     * @param state new {@link WorkflowState} to set.
     */
    public void setState(WorkflowState state) {
        // after failure workflow will be canceled but the overall workflow's failure state should be
        // remain even if the workflow is canceled
        if (this.state != WorkflowState.FAILED) {
            this.state = state;
            notificationService.send(WorkflowConstants.STATE_NOTIFICATION_ID + wfInfo.getIdentifier(), state.name());
            if (state == WorkflowState.DISPOSED) {
                notificationService.send(WorkflowConstants.STATE_DISPOSED_NOTIFICATION_ID, wfInfo.getIdentifier());
            }
        }
    }

    public Collection<ComponentInstanceDescriptor> getComponentInstanceDescriptors() {
        return Collections.unmodifiableCollection(nodeIdToCompInstDescMapping.values());
    }

    private void instantiateComponents() {

        Set<String> connectedComponentInstances = new HashSet<String>();

        for (Connection connection : wfInfo.getWorkflowDescription().getConnections()) {
            connectedComponentInstances.add(connection.getTarget().getIdentifier());
        }

        cFinishListener = new ComponentFinishListener(this, user, notificationService);
        cFailListener = new ComponentFailListener(this, user);
        
        for (WorkflowNode wfNode : wfInfo.getWorkflowDescription().getWorkflowNodes()) {
            ComponentInstanceDescriptor compInstanceDesc;
            try {
                compInstanceDesc = componentRegistry.createComponentInstance(user,
                    wfNode.getComponentDescription(),
                    wfNode.getName(),
                    wfInfo,
                    connectedComponentInstances.contains(wfNode.getIdentifier()),
                    wfNode.getComponentDescription().getPlatform());
            } catch (ComponentException e) {
                throw new RuntimeException(e);
            }

            // retrieve the instantiated component controller as a service
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(ComponentConstants.COMP_INSTANCE_ID_KEY, compInstanceDesc.getIdentifier());
            ComponentController compController = (ComponentController) communicationService.getService(ComponentController.class,
                properties, compInstanceDesc.getPlatform(), bundleCtx);

            nodeIdToCompInstDescMapping.put(wfNode.getIdentifier(), compInstanceDesc);
            compInfoIdToCompControllerMapping.put(compInstanceDesc.getIdentifier(), compController);

            String notificationID = ComponentConstants.FINISHED_STATE_NOTIFICATION_ID_PREFIX + wfInfo.getIdentifier();
            notificationService.subscribe(notificationID, cFinishListener, compInstanceDesc.getPlatform());
            
            notificationID = ComponentConstants.FAILED_STATE_NOTIFICATION_ID_PREFIX + wfInfo.getIdentifier();
            notificationService.subscribe(notificationID, cFailListener, compInstanceDesc.getPlatform());
        }
    }

    private void prepareComponents() {

        Map<ComponentInstanceDescriptor, Map<OutputDescriptor, String>> connectedOutputs = new HashMap<ComponentInstanceDescriptor,
            Map<OutputDescriptor, String>>();

        for (Connection connection : wfInfo.getWorkflowDescription().getConnections()) {

            ComponentInstanceDescriptor sourceCompInstDesc = nodeIdToCompInstDescMapping.get(connection.getSource().getIdentifier());
            OutputDescriptor outputDesc = new OutputDescriptor(sourceCompInstDesc, connection.getOutput());

            ComponentInstanceDescriptor targetCompInstDesc = nodeIdToCompInstDescMapping.get(connection.getTarget().getIdentifier());

            if (connectedOutputs.containsKey(targetCompInstDesc)) {
                connectedOutputs.get(targetCompInstDesc).put(outputDesc, connection.getInput());
            } else {
                Map<OutputDescriptor, String> endpoints = new HashMap<OutputDescriptor, String>();
                endpoints.put(outputDesc, connection.getInput());
                connectedOutputs.put(targetCompInstDesc, endpoints);
            }

        }
        for (ComponentInstanceDescriptor compInstDesc : nodeIdToCompInstDescMapping.values()) {

            Map<OutputDescriptor, String> connectedOutputsPerInstance;

            if (connectedOutputs.containsKey(compInstDesc)) {
                connectedOutputsPerInstance = connectedOutputs.get(compInstDesc);
            } else {
                // needed because null values are not supported as parameters if method is called
                // remotely
                connectedOutputsPerInstance = new HashMap<OutputDescriptor, String>();
            }
            compInfoIdToCompControllerMapping.get(compInstDesc.getIdentifier()).prepare(user,
                connectedOutputsPerInstance);
        }

        waitForComponents();
    }

    private void startComponents() {
        for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
            compController.start(user);
        }
    }

    private void waitForComponents() {
        boolean exceptionThrown = false;
        for (ComponentController compController : compInfoIdToCompControllerMapping.values()) {
            try {
                compController.waitForLifecyclePhaseFinished();                
            } catch (RuntimeException e) {
                exceptionThrown = true;
            }
        }
        if (exceptionThrown) {
            throw new RuntimeException("At least one component run failed");
        }
    }

    private void checkUser(final User userCert) {
        if (!userCert.isValid()) {
            throw new IllegalArgumentException("User certificate must not be invalid!");
        }
        if (!user.equals(userCert)) {
            throw new AuthorizationException("A Component information can only be accessed "
                + "by the user who created the associated Component.");
        }
    }

}
