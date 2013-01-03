/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;

/**
 * Implementation of {@link WorkflowInformation}.
 * 
 * @author Doreen Seider
 */
public class WorkflowInformationImpl implements WorkflowInformation {

    private static final long serialVersionUID = -6042304334362524077L;

    private final String wfId;

    private final String wfName;

    private final User user;

    private final WorkflowDescription description;

    private final Date wfInstantiationTime;

    private final String wfAdditionalInfo;
    
    private Set<ComponentInstanceDescriptor> compInstDescrs;

    /**
     * Constructor.
     * 
     * @param instanceId The identifier of the instantiated {@link Workflow}.
     * @param desc The {@link WorkflowDescription} of the instantiated {@link Workflow}.
     */
    public WorkflowInformationImpl(String instanceId, String name, WorkflowDescription desc, User user) {

        this.wfId = instanceId;
        if (name == null) {
            this.wfName = "";
        } else {
            this.wfName = name;
        }
        this.description = desc;
        this.user = user;

        if (desc.getAdditionalInformation() != null) {
            this.wfAdditionalInfo = desc.getAdditionalInformation();            
        } else {
            this.wfAdditionalInfo = "";
        }
        this.wfInstantiationTime = new Date();
    }

    @Override
    public String getIdentifier() {
        return wfId;
    }

    @Override
    public String getName() {
        return wfName;
    }

    @Override
    public String getUser() {
        return user.getUserId();
    }

    @Override
    public Date getInstantiationTime() {
        return wfInstantiationTime;
    }

    @Override
    public WorkflowDescription getWorkflowDescription() {
        return description;
    }

    @Override
    public String getAdditionalInformation() {
        return wfAdditionalInfo;
    }

    @Override
    public PlatformIdentifier getControllerPlatform() {
        return description.getTargetPlatform();
    }

    @Override
    public PlatformIdentifier getDefaultStoragePlatform() {
        // alway use the controller platform for now; may be split in the future
        return getControllerPlatform();
    }

    @Override
    public Set<PlatformIdentifier> getInvolvedPlatforms() {
        Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
        for (WorkflowNode node : description.getWorkflowNodes()) {
            pis.add(node.getComponentDescription().getPlatform());
        }
        pis.add(description.getTargetPlatform());
        return pis;
    }
    
    @Override
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(String wfNodeName, String componentId) {
        ComponentInstanceDescriptor desc = null;
        for (final ComponentInstanceDescriptor compInstDesc : compInstDescrs) {
            if (compInstDesc.getName().equals(wfNodeName) && compInstDesc.getComponentIdentifier().equals(componentId)) {
                desc = compInstDesc;
                break;
            }
        }
        return desc;
    }
    
    @Override
    public Set<ComponentInstanceDescriptor> getComponentInstanceDescriptors() {
        return Collections.unmodifiableSet(compInstDescrs);
    }
    
    /**
     * @param componentInstanceDescriptors {@link ComponentInstanceDescriptor}s of instantiated components
     */
    public void setComponentInstanceDescriptors(Set<ComponentInstanceDescriptor> componentInstanceDescriptors) {
        compInstDescrs = componentInstanceDescriptors;
    }
    
    
}
