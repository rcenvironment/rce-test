/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow.internal;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.common.concurrent.AsyncExceptionListener;
import de.rcenvironment.core.utils.common.concurrent.CallablesGroup;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.workflow.DistributedWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.Workflow;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowRegistry;

/**
 * Implementation of {@link DistributedWorkflowRegistry}.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class DistributedWorkflowRegistryImpl implements DistributedWorkflowRegistry {

    private static final Log LOGGER = LogFactory.getLog(DistributedWorkflowRegistryImpl.class);

    private CommunicationService communicationService;

    private WorkflowRegistry workflowRegistry;

    private BundleContext context;

    private Set<WorkflowInformation> allWorkflowInformations;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void bindWorkflowRegistry(WorkflowRegistry newWorkflowRegistry) {
        workflowRegistry = newWorkflowRegistry;
    }

    @Override
    public WorkflowInformation createWorkflowInstance(User proxyCertificate, WorkflowDescription workflowDescription,
        String name, Map<String, Object> configuration, PlatformIdentifier platformIdentifier) {
        try {
            WorkflowRegistry registry = (WorkflowRegistry) communicationService
                .getService(WorkflowRegistry.class, platformIdentifier, context);
            return registry.createWorkflowInstance(proxyCertificate, workflowDescription, name, configuration);
        } catch (UndeclaredThrowableException e) {
            Throwable cause = e.getCause();
            LOGGER.error("Failed to create remote workflow instance: ", cause);
            if (cause instanceof CommunicationException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    @Override
    public void disposeWorkflowInstance(User user, String identifier, PlatformIdentifier platform) {
        try {
            WorkflowRegistry registry = (WorkflowRegistry) communicationService
                .getService(WorkflowRegistry.class, platform, context);
            registry.disposeWorkflowInstance(user, identifier);
        } catch (UndeclaredThrowableException e) {
            LOGGER.error("Failed to dispose remote workflow instance: ", e.getCause());
        }

    }

    @Override
    // TODO for safe generics, create a type for the Callable return value
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set<WorkflowInformation> getAllWorkflowInformations(final User user, boolean forceRefresh) {
        if (forceRefresh || allWorkflowInformations == null) {
            allWorkflowInformations = new HashSet<WorkflowInformation>();

            CallablesGroup<Set> callablesGroup = SharedThreadPool.getInstance().createCallablesGroup(Set.class);

            for (PlatformIdentifier pi : communicationService.getAvailableNodes(false)) {
                final PlatformIdentifier pi2 = pi;
                callablesGroup.add(new Callable<Set>() {

                    @Override
                    @TaskDescription("Distributed query: getWorkflowInformations()")
                    public Set call() throws Exception {
                        WorkflowRegistry registry =
                            (WorkflowRegistry) communicationService.getService(WorkflowRegistry.class, pi2, context);
                        try {
                            return registry.getWorkflowInformations(user);
                        } catch (UndeclaredThrowableException e) {
                            LOGGER.warn("Failed to query remote workflows for platform: " + pi2);
                            return null;
                        }
                    }
                });
            }
            List<Set> results = callablesGroup.executeParallel(new AsyncExceptionListener() {

                @Override
                public void onAsyncException(Exception e) {
                    LOGGER.warn("Exception during asynchrous execution", e);
                }
            });
            // merge results
            for (Set singleResult : results) {
                if (singleResult != null) {
                    allWorkflowInformations.addAll(singleResult);
                }
            }
        }
        return allWorkflowInformations;
    }

    @Override
    public Set<WorkflowInformation> getWorkflowInformations(User user) {
        return workflowRegistry.getWorkflowInformations(user);
    }

    @Override
    public Workflow getWorkflow(WorkflowInformation workflowInformation) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WorkflowConstants.WORKFLOW_INSTANCE_ID_KEY, workflowInformation.getIdentifier());

        return (Workflow) communicationService.getService(Workflow.class, properties,
            workflowInformation.getWorkflowDescription().getTargetPlatform(), context);
    }
}
