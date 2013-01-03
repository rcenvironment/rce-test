/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.endpoint.EndpointChange;
import de.rcenvironment.rce.component.endpoint.EndpointChange.Type;


/**
 * {@link PropertyChangeListener} for {@link EndpointChange}s.
 *
 * @author Christian Weiss
 * @author Doreen Seider
 */
public class EndpointChangeListener implements PropertyChangeListener {

    private final WorkflowDescription workflowDesc;
    
    protected EndpointChangeListener(WorkflowDescription newWorkflowDesc) {
        workflowDesc = newWorkflowDesc;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        workflowDesc.firePropertyChange(ComponentDescription.ENDPOINT_CHANGED_PROP);
        EndpointChange epChange = (EndpointChange) evt.getNewValue();
        if (epChange.getType() == Type.Remove) {
            // if an endpoint was removed, automatically remove the associated connections
            final List<Connection> superfluousConnections = new LinkedList<Connection>();
            final ComponentDescription componentDescription = epChange.getComponentDescription();
            final String endpointName = epChange.getEndpointName();
            // check for each connection if the removed endpoint is one of the connection
            // endpoints
            for (Connection connection : workflowDesc.getConnections()) {
                final boolean sourceRemoved = (connection.getSource().getComponentDescription().equals(componentDescription)
                    && connection.getOutput().equals(endpointName));
                final boolean targetRemoved = connection.getTarget().getComponentDescription().equals(componentDescription)
                    && connection.getInput().equals(endpointName);
                // if one of the endpoints of the connection was removed, the connection is
                // superfluous
                if (sourceRemoved || targetRemoved) {
                    superfluousConnections.add(connection);
                }
            }
            // remove all connections that have been identified as superfluous
            for (Connection superfluousConnection : superfluousConnections) {
                workflowDesc.removeConnection(superfluousConnection);
            }
        } else if (epChange.getType() == Type.Change) {
            // if the name of an endpoint changed, the according connections have to be replaced
            final ComponentDescription componentDescription = epChange.getComponentDescription();
            final String endpointName = epChange.getFormerEndpointName();
            // check for each connection if the changed endpoint is one of the connection
            // endpoints
            
            for (Connection connection : workflowDesc.getConnections()) {
                final boolean sourceChanged = (connection.getSource().getComponentDescription().equals(componentDescription)
                    && connection.getOutput().equals(endpointName));
                if (sourceChanged) {
                    workflowDesc.removeConnection(connection);
                    final Connection replacementConnection = new Connection(connection.getSource(),
                        epChange.getEndpointName(), connection.getTarget(), connection.getInput());
                    workflowDesc.addConnection(replacementConnection);
                }
                final boolean targetChanged = connection.getTarget().getComponentDescription().equals(componentDescription)
                    && connection.getInput().equals(endpointName);
                if (targetChanged) {
                    workflowDesc.removeConnection(connection);
                    final Connection replacementConnection = new Connection(connection.getSource(), connection.getOutput(),
                        connection.getTarget(), epChange.getEndpointName());
                    workflowDesc.addConnection(replacementConnection);
                }
            }
            
        }
    }
}
