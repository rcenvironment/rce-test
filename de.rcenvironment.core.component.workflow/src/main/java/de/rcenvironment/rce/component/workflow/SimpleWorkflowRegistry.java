/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Class providing access to all workflows within the distributed system. This class does not do a
 * new remote query on each method call but acts on a local cache which has to be manually updated
 * by calling refresh().
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Christian Weiss
 */
public class SimpleWorkflowRegistry {

    private static final Log LOGGER = LogFactory.getLog(SimpleWorkflowRegistry.class);
    
    private static final WorkflowState[] ACTIVE_STATES = new WorkflowState[] { WorkflowState.READY, WorkflowState.PREPARING,
        WorkflowState.RUNNING, WorkflowState.PAUSING, WorkflowState.PAUSED, WorkflowState.RESUMING };

    private static DistributedWorkflowRegistry workflowRegistry = ServiceUtils.createNullService(DistributedWorkflowRegistry.class);;

    private User user;

    /** Only used by OSGi component instantiation. */
    @Deprecated
    public SimpleWorkflowRegistry() {}
    
    public SimpleWorkflowRegistry(User user) {
        this.user = user;
    }

    protected void bindDistributedWorkflowRegistry(DistributedWorkflowRegistry newDistributedWorkflowRegistry) {
        workflowRegistry = newDistributedWorkflowRegistry;
    }

    protected void unbindDistributedWorkflowRegistry(DistributedWorkflowRegistry oldDistributedWorkflowRegistry) {
        workflowRegistry = ServiceUtils.createNullService(DistributedWorkflowRegistry.class);
    }
    
    /**
     * Returns all workflow informations collected from the whole distributed system the last time
     * refresh() was called.
     * 
     * @return A {@link Set} of {@link WorkflowInformationImpl}s.
     */
    public Set<WorkflowInformation> getAllWorkflowInformations() {
        return workflowRegistry.getAllWorkflowInformations(user, false);
    }
    
    /**
     * Returns all workflow informations collected from the whole distributed system the last time
     * refresh() was called.
     * 
     * @param forceRefresh if <code>false</code> cached informations (from last lookup) are returned
     *        otherwise a new lookup is done
     * @deprecated refreshes should be triggered, e.g. via notifications, thus no active refresh
    *             should be called. it needs to be called as long as the trigger mechanism is not
    *             established
     * @return A {@link Set} of {@link WorkflowInformationImpl}s.
     */
    @Deprecated
    public Set<WorkflowInformation> getAllWorkflowInformations(boolean forceRefresh) {
        return workflowRegistry.getAllWorkflowInformations(user, forceRefresh);
    }

    /**
     * Returns the WorkflowInformation for the given instance identifier.
     * 
     * @param instanceIdentifier Instance identifier.
     * @param forceRefresh if <code>false</code> cached informations (from last lookup) are returned
     *        otherwise a new lookup is done
     * @return WorkflowInformation or null.
     */
    public WorkflowInformation getWorkflowInformation(String instanceIdentifier, boolean forceRefresh) {
        for (WorkflowInformation wi: workflowRegistry.getAllWorkflowInformations(user, forceRefresh)) {
            if (wi.getIdentifier().equals(instanceIdentifier)) {
                return wi;
            }
        }
        return null;
    }
    
    /**
     * Creates a new workflow instance.
     * 
     * @param workflowDescription The {@link WorkflowDescription} of the workflow to create a
     *        instance for.
     * @param name The name used for the workflow to create.
     * @param configuration The configuration of the workflow to instantiate.
     * @return A {@link WorkflowInformationImpl} representing the created workflow instance.
     */
    public WorkflowInformation createWorkflowInstance(WorkflowDescription workflowDescription, String name,
        Map<String, Object> configuration) {
        return workflowRegistry.createWorkflowInstance(user, workflowDescription, name,
            configuration, workflowDescription.getTargetPlatform());
    }

    /**
     * Disposes a {@link Workflow} instance.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow instance
     *        to dispose.
     */
    public void disposeWorkflowInstance(WorkflowInformation workflowInformation) {
        workflowRegistry.disposeWorkflowInstance(user,
            workflowInformation.getIdentifier(),
            workflowInformation.getWorkflowDescription().getTargetPlatform());
    }

    /**
     * Returns a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the requested
     *        {@link Workflow}.
     * @return The {@link Workflow} instance.
     */
    public Workflow getWorkflow(WorkflowInformation workflowInformation) {
        return workflowRegistry.getWorkflow(workflowInformation);
    }

    /**
     * Starts a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        start.
     */
    public void startWorkflow(WorkflowInformation workflowInformation) {
        try {
            Workflow w = getWorkflow(workflowInformation);
            w.start(user);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to start remote workflow", e);
        }
        
    }
    
    /**
     * Starts a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        start.
     */
    public void pauseWorkflow(WorkflowInformation workflowInformation) {
        try {
            getWorkflow(workflowInformation).pause(user);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to pause remote workflow", e);
        }
        
    }
    
    /**
     * Starts a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        start.
     */
    public void resumeWorkflow(WorkflowInformation workflowInformation) {
        try {
            getWorkflow(workflowInformation).resume(user);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to resume remote workflow", e);
        }
        
    }

    /**
     * Cancels a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        cancel.
     */
    public void cancelWorkflow(WorkflowInformation workflowInformation) {
        try {
            getWorkflow(workflowInformation).cancel(user);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to cancel remote workflow", e);
        }
    }

    /**
     * Disposes a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        dispose.
     */
    public void disposeWorkflow(WorkflowInformation workflowInformation) {
        try {
            getWorkflow(workflowInformation).dispose(user);
            workflowRegistry.disposeWorkflowInstance(user,
                workflowInformation.getIdentifier(),
                workflowInformation.getControllerPlatform());
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to dispose remote workflow", e);
        }
    }

    /**
     * Cancels all active {@link Workflow} instances.
     */
    public void cancelActiveWorkflows() {
        
        for (WorkflowInformation workflowInformation : workflowRegistry.getWorkflowInformations(user)) {
            if (isWorkflowsInState(getWorkflow(workflowInformation), ACTIVE_STATES)) {
                cancelWorkflow(workflowInformation);
            }
        }

    }

    /**
     * Disposes all {@link Workflow} instances.
     */
    public void disposeWorkflows() {
        
        for (WorkflowInformation workflowInformation : workflowRegistry.getWorkflowInformations(user)) {
            disposeWorkflow(workflowInformation);
        }
    }
    
    /**
     * Sets inputs for a component of a workflow.
     * @param workflowInformation The {@link WorkflowInformation} representing the workflow.
     * @param componentInstancId Identifier of the component.
     * @param inputs new inputs to push to a component.
     */
    public void setInputs(WorkflowInformation workflowInformation, String componentInstancId, BlockingQueue<Input> inputs) {
        try {
            getWorkflow(workflowInformation).setInputs(user, componentInstancId, inputs);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to push inputs to remote workflow component", e);
        }
    }
    
    /**
     * Returns inputs of a component of a workflow.
     * @param workflowInformation The {@link WorkflowInformation} representing the workflow.
     * @param componentInstancId Identifier of the component.
     * @return inputs of a component.
     */
    public BlockingQueue<Input> getInputs(WorkflowInformation workflowInformation, String componentInstancId) {
        try {
            return getWorkflow(workflowInformation).getInputs(user, componentInstancId);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to get inputs from remote workflow component", e);
        }
    }
    
    /**
     * Returns inputs of a component of a workflow.
     * @param workflowInformation The {@link WorkflowInformation} representing the workflow.
     * @param componentInstancId Identifier of the component.
     * @return inputs of a component.
     */
    public Map<String, Integer> getCurrentInputNumbers(WorkflowInformation workflowInformation, String componentInstancId) {
        try {
            return getWorkflow(workflowInformation).getCurrentInputNumbers(user, componentInstancId);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to get current input numbers of remote workflow component", e);
        }
    }
    
    /**
     * Returns whether active {@link Workflow} instances exist.
     * 
     * @return True, if active {@link Workflow} instances exist.
     */
    public boolean hasActiveWorkflows() {
        return hasWorkflowsInState(ACTIVE_STATES);
    }

    /**
     * Returns whether {@link Workflow} instances in the given {@link State}s exist.
     * @param states states to check.
     * @return True, if {@link Workflow} instances in the given {@link State}s exist.
     */
    private boolean hasWorkflowsInState(final WorkflowState[] states) {
        for (Workflow workflow : new WorkflowIterator()) {
            if (isWorkflowsInState(workflow, states)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns whether {@link Workflow} instances in the given {@link State}s exist.
     * @param states states to check.
     * @return True, if {@link Workflow} instances in the given {@link State}s exist.
     */
    private boolean isWorkflowsInState(final Workflow workflow, final WorkflowState[] states) {
        final WorkflowState workflowState = workflow.getState(user);
        for (final WorkflowState state : states) {
            if (workflowState == state) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the state of a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        get the state for.
     * @return The state of the {@link Workflow}.
     */
    public WorkflowState getStateOfWorkflow(WorkflowInformation workflowInformation) {
        try {
            return getWorkflow(workflowInformation).getState(user);
        } catch (UndeclaredThrowableException e) {
            throw new IllegalStateException("Failed to get state of remote workflow", e);
        }
    }
    
    /**
     * Returns all {@link ComponentInstanceDescriptor}s of a {@link Workflow}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow to
     *        get the {@link ComponentInstanceDescriptor} for.
     * @return All {@link ComponentInstanceDescriptor}s of the {@link Workflow}.
     */
    public Set<ComponentInstanceDescriptor> getComponentInstanceDescriptors(WorkflowInformation workflowInformation) {
        try {
            return getWorkflow(workflowInformation).getComponentInstanceDescriptors(user);
        } catch (UndeclaredThrowableException e) {
            LOGGER.warn("Failed to get component informations of remote workflow: ", e);
            return new TreeSet<ComponentInstanceDescriptor>();
        }
    }
    
    /**
     * Returns {@link ComponentInstanceDescriptor} of a {@link WorkflowNode}.
     * 
     * @param workflowInformation The {@link WorkflowInformationImpl} representing the workflow the {@link WorkflowNode} belongs to.
     * @param workflowNode The {@link WorkflowNode} to get the {@link ComponentInstanceDescriptor} for.
     * @return All {@link ComponentInstanceDescriptor}s of the {@link WorkflowNode}.
     */
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(WorkflowNode workflowNode, WorkflowInformation workflowInformation) {
        ComponentInstanceDescriptor desc = null;
        final Set<ComponentInstanceDescriptor> compInstDescs = getComponentInstanceDescriptors(workflowInformation);
        for (final ComponentInstanceDescriptor compInstDesc : compInstDescs) {
            if (compInstDesc.getName().equals(workflowNode.getName())
                        && compInstDesc.getComponentIdentifier().equals(workflowNode.getComponentDescription().getIdentifier())) {
                desc = compInstDesc;
                break;
            }
        }
        return desc;
    }

    /**
     * An {@link Iterator} to iterator over all local workflows.
     * 
     * @author Christian Weiss
     */
    private class WorkflowIterator implements Iterator<Workflow>, Iterable<Workflow> {

        private final Iterator<WorkflowInformation> workflowInformationsIterator;

        public WorkflowIterator() {
            workflowInformationsIterator = workflowRegistry.getWorkflowInformations(user).iterator();
        }

        @Override
        public Workflow next() {
            return workflowRegistry.getWorkflow(workflowInformationsIterator.next());
        }

        @Override
        public boolean hasNext() {
            return workflowInformationsIterator.hasNext();
        }

        @Override
        public void remove() {
            workflowInformationsIterator.remove();
        }

        @Override
        public Iterator<Workflow> iterator() {
            return this;
        }

    }

}
