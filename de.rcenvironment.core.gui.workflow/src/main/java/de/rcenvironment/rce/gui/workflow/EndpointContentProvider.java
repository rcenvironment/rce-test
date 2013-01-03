/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController.Type;

/**
 * Content Provider taking a WorkflowDescription as root and displaying all componets with either
 * their inputs or outputs.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class EndpointContentProvider implements ITreeContentProvider {
    
    private Type type;

    /**
     * @param type Display input or output endpoints?
     */
    public EndpointContentProvider(Type type) {
        this.type = type;
    }

    @Override
    public Object[] getChildren(Object element) {
        if (element instanceof WorkflowDescription) {
            List<WorkflowNode> items = ((WorkflowDescription) element).getWorkflowNodes();
            Collections.sort(items);
            return items.toArray();
        } else if (element instanceof WorkflowNode) {
            EndpointHandlingHelper helper = new EndpointHandlingHelper();
            List<EndpointItem> items = new ArrayList<EndpointItem>();
            items.addAll(helper.getEndpointGroups((WorkflowNode) element, type));                
            items.addAll(helper.getEndpoints((WorkflowNode) element, type));
            Collections.sort(items);
            return items.toArray();
        } else {
            List<EndpointItem> items = new ArrayList<EndpointItem>();
            items.addAll(((EndpointGroup) element).getChildEndpointGroups());
            items.addAll(((EndpointGroup) element).getChildEndpoints());
            Collections.sort(items);
            return items.toArray();
        }
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof WorkflowDescription || element instanceof WorkflowNode || element instanceof EndpointGroup;
    }

    @Override
    public Object[] getElements(Object element) {
        return getChildren(element);
    }

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer view, Object object1, Object object2) {

    }

    /**
     * Class representing one item in the endpoint tree.
     * 
     * @author Doreen Seider
     */
    public static class EndpointItem implements Serializable, Comparable<EndpointItem> {
        
        private static final String DOT = ".";
        
        private static final long serialVersionUID = 777733457712592306L;

        private String name;

        private String canonicalName;
        
        public EndpointItem(String canonicalName) {
            this.canonicalName = canonicalName;
            if (canonicalName.contains(DOT)) {
                name = canonicalName.substring(canonicalName.lastIndexOf(DOT) + 1);
            } else {
                name = canonicalName;
            }
        }
        
        public String getName() {
            return name;
        }
        
        public String getCanonicalName() {
            return canonicalName;
        }

        @Override
        public int compareTo(EndpointItem o) {
            return name.compareTo(o.getName());
        }
        
    }
    
    /**
     * Class representing one endpoint.
     * 
     * @author Heinrich Wendel
     */
    public static class Endpoint extends EndpointItem {

        private static final long serialVersionUID = 1633769598091968303L;

        private Class<? extends Serializable> type;

        private WorkflowNode parent;
        
        public Endpoint(WorkflowNode parent, String canonicalName, Class<? extends Serializable> type) {
            super(canonicalName);
            this.parent = parent;
            this.type = type;
        }

        public Class<? extends Serializable> getType() {
            return type;
        }
        
        public WorkflowNode getWorkflowNode() {
            return parent;
        }
    }
    
    /**
     * Class representing one endpoint group.
     * 
     * @author Doreen Seider
     */
    public static class EndpointGroup extends EndpointItem {

        private static final long serialVersionUID = 2469748538487119455L;

        private Map<String, EndpointGroup> childEndpointGroups;
        
        private Collection<Endpoint> childEndpoints;

        private EndpointGroup(String canonicalName) {
            super(canonicalName);
            childEndpointGroups = new HashMap<String, EndpointContentProvider.EndpointGroup>();
            childEndpoints = new HashSet<EndpointContentProvider.Endpoint>();
        }
        
        public EndpointGroup(String canonicalName, Endpoint childEndpoint) {
            this(canonicalName);
            childEndpoints.add(childEndpoint);
        }
        
        public EndpointGroup(String canonicalName, EndpointGroup childEndpointGroup) {
            this(canonicalName);
            childEndpointGroups.put(childEndpointGroup.getCanonicalName(), childEndpointGroup);
        }
        
        public Collection<EndpointGroup> getChildEndpointGroups() {
            return childEndpointGroups.values();
        }

        public Collection<Endpoint> getChildEndpoints() {
            return childEndpoints;
        }
        
        protected void addChildEndpointGroup(EndpointGroup endpointGroup) {
            childEndpointGroups.put(endpointGroup.getCanonicalName(), endpointGroup);
        }

        protected void addChildEndpoint(Endpoint endpoint) {
            childEndpoints.add(endpoint);
        }

    }
    
    
}
