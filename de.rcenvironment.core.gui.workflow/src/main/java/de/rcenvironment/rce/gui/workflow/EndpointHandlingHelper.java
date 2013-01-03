/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.Endpoint;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.EndpointGroup;
import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController.Type;


/**
 * Class helping handling {@link Endpoint}s.
 * 
 * @author Doreen Seider
 */
public class EndpointHandlingHelper {
    
    /** Separator in endpoint named. */
    public static final String DOT = ".";
    
    private Collection<EndpointGroup> parentEndpointGroups;
    
    private Map<String, EndpointGroup> endpointGroups;
    
    /**
     * Extracts {@link EndpointGroup}s for the given properties and returns the parent ones.
     * 
     * @param workflowNode Parent {@link WorkflowNode}.
     * @param type Input oder output?
     * @return parent {@link EndpointGroup}s.
     */
    public Collection<EndpointGroup> getEndpointGroups(WorkflowNode workflowNode, Type type) {
        
        parentEndpointGroups = new HashSet<EndpointGroup>();
        endpointGroups = new HashMap<String, EndpointGroup>();
        
        extractEndpointGroups(workflowNode, getEndpointDefiniitons(workflowNode, type));
        
        for (EndpointGroup endpointGroup : endpointGroups.values()) {
            if (!endpointGroup.getCanonicalName().contains(DOT)) {
                parentEndpointGroups.add(endpointGroup);
            }
        }
        return parentEndpointGroups;
        
    }
    
    /**
     * Extracts {@link Endpoint}s for the given properties and returns them.
     * 
     * @param workflowNode {@link WorkflowNode}.
     * @param type Input oder output?
     * @return {@link Endpoint}s.
     */
    public Collection<Endpoint> getEndpoints(WorkflowNode workflowNode, Type type) {
        
        Collection<EndpointContentProvider.Endpoint> endpoints = new HashSet<EndpointContentProvider.Endpoint>();
        
        for (Entry<String, Class<? extends Serializable>> entry : getEndpointDefiniitons(workflowNode, type).entrySet()) {
            if (!entry.getKey().contains(DOT)) {
                endpoints.add(new Endpoint(workflowNode, entry.getKey(), entry.getValue()));                
            }
        }
        return endpoints;
    }
    
    private Map<String, Class<? extends Serializable>> getEndpointDefiniitons(WorkflowNode workflowNode, Type type) {
        
        Map<String, Class<? extends Serializable>> properties;
        if (type == Type.INPUT) {
            properties = workflowNode.getComponentDescription().getInputDefinitions();
        } else {
            properties = workflowNode.getComponentDescription().getOutputDefinitions();
        }
        
        return properties;
    }
    
    private void extractEndpointGroups(WorkflowNode parent, Map<String, Class<? extends Serializable>> properties) {
        
        for (Entry<String, Class<? extends Serializable>> entry : properties.entrySet()) {
            if (entry.getKey().contains(DOT)) {
                extractEndpointGroups(new Endpoint(parent, entry.getKey(), entry.getValue()));                
            }
        }
    }
    
    private void extractEndpointGroups(Endpoint endpoint) {
        String newEndpointGroupName = endpoint.getCanonicalName().substring(0, endpoint.getCanonicalName().lastIndexOf(DOT));
        
        if (endpointGroups.containsKey(newEndpointGroupName)) {
            endpointGroups.get(newEndpointGroupName).addChildEndpoint(endpoint);
        } else {
            EndpointGroup newEndpointGroup = new EndpointGroup(newEndpointGroupName, endpoint);
            endpointGroups.put(newEndpointGroupName, newEndpointGroup);
            extractEndpointGroups(newEndpointGroup);
        }
        
    }
    
    private void extractEndpointGroups(EndpointGroup endpointGroup) {
        if (endpointGroup.getCanonicalName().contains(DOT)) {
            String newEndpointGroupName = endpointGroup.getCanonicalName()
                .substring(0, endpointGroup.getCanonicalName().lastIndexOf(DOT));
            
            if (endpointGroups.containsKey(newEndpointGroupName)) {
                endpointGroups.get(newEndpointGroupName).addChildEndpointGroup(endpointGroup);
            } else {
                EndpointGroup newEndpointGroup = new EndpointGroup(newEndpointGroupName, endpointGroup);
                endpointGroups.put(newEndpointGroupName, newEndpointGroup);
                extractEndpointGroups(newEndpointGroup);
            }
        }
    }
}

