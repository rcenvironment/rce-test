/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.DistributedComponentRegistry;
import de.rcenvironment.rce.component.workflow.Workflow;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowInformationImpl;
import de.rcenvironment.rce.component.workflow.WorkflowRegistry;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Implementation of the {@link WorkflowRegistry}.
 * 
 * @author Roland Gude
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 */
public class WorkflowRegistryImpl implements WorkflowRegistry {

    private static final Log LOGGER = LogFactory.getLog(WorkflowRegistryImpl.class);

    /** Collection of all registered instantiated {@link Workflow}s and their identifying strings. */
    private Map<String, Workflow> workflows = Collections.synchronizedMap(new HashMap<String, Workflow>());
    
    /** Collection of all {@link WorkflowInformationImpl}s created a workflow. */
    private Map<String, WorkflowInformation> workflowInformations = Collections.synchronizedMap(new HashMap<String, WorkflowInformation>());

    /** Collection of all {@link User}s created a workflow. */
    private Map<String, User> workflowCertificates = Collections.synchronizedMap(new HashMap<String, User>());

    private BundleContext context;

    private DistributedComponentRegistry componentRegistry;
    
    private DistributedNotificationService notificationService;
    
    private CommunicationService communicationService;
    
    private PlatformIdentifier localPlatform;
    
    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindDistributedComponentRegistry(DistributedComponentRegistry newComponentRegistry) {
        componentRegistry = newComponentRegistry;
    }

    protected void bindDistributedNotificationService(DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }
    
    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    @Override
    @AllowRemoteAccess
    public WorkflowInformation createWorkflowInstance(User user, WorkflowDescription wfDesc, String name,
        Map<String, Object> wfConfiguration) {
        checkUser(user);
        
        Dictionary<String, String> properties = new Hashtable<String, String>();
        String identifier = UUID.randomUUID().toString();
        properties.put(WorkflowConstants.WORKFLOW_INSTANCE_ID_KEY, identifier);

        WorkflowInformation wfInfo = new WorkflowInformationImpl(identifier, name, wfDesc.clone(user), user);
        
        Workflow workflow = new WorkflowImpl(wfInfo, user);
        ((WorkflowImpl) workflow).setDistributedComponentRegistry(componentRegistry);
        ((WorkflowImpl) workflow).setDistributedNotificationService(notificationService);
        ((WorkflowImpl) workflow).setCommunicationService(communicationService);
        ((WorkflowImpl) workflow).setBundleContext(context);
        workflow.initialize(user);
        context.registerService(Workflow.class.getName(), workflow, properties);
        
        ((WorkflowInformationImpl) wfInfo).setComponentInstanceDescriptors(workflow.getComponentInstanceDescriptors(user));

        workflows.put(identifier, workflow);
        workflowCertificates.put(identifier, user);
        workflowInformations.put(identifier, wfInfo);
        
        return wfInfo;
    }

    @Override
    public void disposeWorkflowInstance(User cert, String instanceIdentifier) {
        checkUser(cert);
        
        Workflow workflow = workflows.get(instanceIdentifier);
        if (workflow == null) {
            LOGGER.warn("Disposing workflow failed, because it could not be found: " + instanceIdentifier);
            return;
        }

        if (!workflowCertificates.containsKey(instanceIdentifier) || !workflowCertificates.get(instanceIdentifier).same(cert)) {
            throw new AuthorizationException("Disposing a workflow is only allowed for the user created it.");
        }
        
        workflows.remove(instanceIdentifier);
        workflowCertificates.remove(instanceIdentifier);
        workflowInformations.remove(instanceIdentifier);
        
        // unregister the workflow
        ServiceReference[] refs;
        try {
            refs = context.getServiceReferences(Workflow.class.getName(),
                "(" + WorkflowConstants.WORKFLOW_INSTANCE_ID_KEY  + "=" + instanceIdentifier  + ")");
            for (ServiceReference ref : refs) {
                context.ungetService(ref);
            }
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Disposing workflow failed. This is a bug!");
        }
    }

    @Override
    @AllowRemoteAccess
    public WorkflowInformation getWorkflowInformation(User cert, String instanceIdentifier) {
        checkUser(cert);
        checkCertificate(cert, instanceIdentifier);
        
        return workflowInformations.get(instanceIdentifier);
    }
    
    @Override
    @AllowRemoteAccess
    public Set<WorkflowInformation> getWorkflowInformations(User user) {
        checkUser(user);

        final Set<WorkflowInformation> informations = new HashSet<WorkflowInformation>();

        for (String identifier : workflows.keySet()) {
            if (isCreator(identifier, user)) {
                informations.add(workflowInformations.get(identifier));
            }
        }
        
        return informations;
    }
    
    @Override
    public boolean isCreator(String instanceIdentifier, User cert) {
        checkUser(cert);

        if (workflowCertificates.containsKey(instanceIdentifier)) {
            return workflowCertificates.get(instanceIdentifier).same(cert);
        } else {
            LOGGER.warn("Could not find a workflow with the given identifier: " + instanceIdentifier);
            return false;
        }
        
    }
    
    private void checkUser(final User userCert) {
        if (!userCert.isValid()) {
            throw new IllegalArgumentException("User certificate must not be invalid!");
        }
    }
    
    private void checkCertificate(final User userCert, final String instanceIdentifier) {
        if (!isCreator(instanceIdentifier, userCert)) {
            throw new AuthorizationException("A Workflow information can only ba accessed "
                + "by the user who created the associated Workflow.");
        }
    }

}
