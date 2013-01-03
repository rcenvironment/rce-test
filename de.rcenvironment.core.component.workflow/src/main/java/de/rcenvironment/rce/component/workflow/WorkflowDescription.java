/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ChangeSupport;

/**
 * Describes a {@link Workflow} in a way that can be used by a
 * {@link WorkflowRegistry} to create that {@link Workflow}.
 * 
 * @author Roland Gude
 * @author Heinrich Wendel
 */
public class WorkflowDescription extends ChangeSupport implements Serializable, Cloneable {

    /** Property that is fired when a WorkflowNode was added. */
    public static final String NODES_CHANGED_PROP = "WorkflowDescription.WorkflowNodesChanged";

    /** Property that is fired when a WorkflowNode was removed. */
    public static final String CONNECTIONS_CHANGED_PROP = "WorkflowDescription.ConnectionsChanged";
        
    /**Property that is fired, when the Update changed something. */
    public static final String UPDATER_CHANGED = "WorkflowDescription.Updater";
    
    private static final long serialVersionUID = 339866937554580256L;

    private final String identifier;
    
    private int workflowVersionNumber = WorkflowConstants.CURRENT_WORKFLOW_VERSION_NUMBER;
    
    private String name;

    private String additionalInformation;
    
    private PlatformIdentifier targetPlatform;

    private List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
    
    private List<Connection> connections = new ArrayList<Connection>();
    
  
    /**
     * @param identifier The identifier of the {@link WorkflowDescription}.
     */
    public WorkflowDescription(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public int getWorkflowVersion() {
        return workflowVersionNumber;
    }
    
    public void setWorkflowVersion(Integer workflowVersion) {
        this.workflowVersionNumber = workflowVersion;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(final String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public PlatformIdentifier getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(PlatformIdentifier targetPlatform) {
        this.targetPlatform = targetPlatform;
    }


    public List<WorkflowNode> getWorkflowNodes() {
        return nodes;
    }
   
    /**
     * Returns the {@link WorkflowNode} with the given identifier.
     * 
     * @param nodeIdentifier the identifier of the desired {@link WorkflowNode}
     * @return the {@link WorkflowNode} with the given identifier
     * @throws IllegalArgumentException if no {@link WorkflowNode} with the given identifier exists
     */
    public WorkflowNode getWorkflowNode(final String nodeIdentifier) throws IllegalArgumentException {
        for (WorkflowNode node : nodes) {
            if (node.getIdentifier().equals(nodeIdentifier)) {
                return node;
            }
        }
        throw new IllegalArgumentException(String.format("No node with identifier %s found", nodeIdentifier));
    }

    /**
     * Adds a new {@link WorkflowNode}.
     * @param node The new Workflow node.
     */
    public void addWorkflowNode(WorkflowNode node) {
        nodes.add(node);
        // add the EndpointEventListener to get informed about dynamic endpoint changes
        node.getComponentDescription().addPropertyChangeListener(new EndpointChangeListener(this));
        firePropertyChange(NODES_CHANGED_PROP);
    }

    /**
     * Removes a {@link WorkflowNode}.
     * @param node The {@link WorkflowNode} to remove.
     */
    public void removeWorkflowNode(WorkflowNode node) {
        nodes.remove(node);
        firePropertyChange(NODES_CHANGED_PROP);
    }
    
    /**
     * Returns all {@link Connection}s.
     * @return all {@link Connection}s.
     */
    public List<Connection> getConnections() {
        return new ArrayList<Connection>(connections);
    }
    
    /**
     * Adds a new {@link Connection}.
     * @param connection The {@link Connection} to add.
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
        firePropertyChange(CONNECTIONS_CHANGED_PROP);
    }

    /**
     * Removes a new {@link Connection}.
     * @param connection The {@link Connection} to remove.
     */
    public void removeConnection(Connection connection) {
        connections.remove(connection);
        firePropertyChange(CONNECTIONS_CHANGED_PROP);
    }
    
    /**
     * Clones a given {@link WorkflowDescription}.
     * @param cert The {@link User} to use.
     * @return the cloned {@link WorkflowDescription}.
     */
    public WorkflowDescription clone(User cert) {
        try {
            WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();
            ByteArrayOutputStream outputStream = handler.writeWorkflowDescriptionToStream(this);
            WorkflowDescription clonedWD = handler.readWorkflowDescriptionFromStream(
                new ByteArrayInputStream(outputStream.toByteArray()), cert);
            // Write placeholder to clone 
            for (WorkflowNode wn : clonedWD.getWorkflowNodes()){
                wn.getComponentDescription().addPlaceholderMap(
                    this.getWorkflowNode(wn.getIdentifier()).getComponentDescription().getPlaceholderMap());
            }
           
            return clonedWD; 
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }
    
}
