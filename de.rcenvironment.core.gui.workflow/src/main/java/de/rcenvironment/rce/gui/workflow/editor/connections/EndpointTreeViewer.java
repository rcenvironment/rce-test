/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.connections;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.EndpointHandlingHelper;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.Endpoint;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.EndpointGroup;


/**
 * The TreeViewer to display components, one additional method to find an item by path.
 *
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class EndpointTreeViewer extends TreeViewer {

    /**
     * Constructor.
     * 
     * @param parent See parent.
     * @param style See parent.
     */
    public EndpointTreeViewer(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * Locates the TreeItem associated with a given Endpoint.
     * 
     * @param node The parent WorkflowNode.
     * @param name The Endpoint name.
     * @return The associated TreeItem.
     */
    public TreeItem findEndpoint(WorkflowNode node, String name) {
        TreeItem foundItem = null;

        for (TreeItem item: this.getTree().getItems()) {
            if (item.getData().equals(node)) {
                foundItem = item;
                if (item.getExpanded()) {
                    for (TreeItem child: item.getItems()) {
                        TreeItem endpoint = findEndpoint(child, name);
                        if (endpoint != null) {
                            foundItem = endpoint;
                            break;
                        }
                    }
                }
            }
        }

        return foundItem;
    }
    
    private TreeItem findEndpoint(TreeItem item, String name) {
        
        TreeItem matchingItem = null;
        
        if (item.getData() instanceof EndpointGroup) {
            String[] nameParts = name.split("\\" + EndpointHandlingHelper.DOT);
            if (((EndpointGroup) item.getData()).getName().equals(nameParts[0])) {
                matchingItem = item;
                if (nameParts.length > 0 && item.getExpanded()) {
                    // remove top level part of name and go to one lower level
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 1; i < nameParts.length; i++) {
                        nameBuilder.append(nameParts[i]);
                        if (i < nameParts.length - 1) {
                            nameBuilder.append(EndpointHandlingHelper.DOT);
                        }
                    }

                    for (TreeItem child : item.getItems()) {
                        TreeItem endpoint = findEndpoint(child, nameBuilder.toString());
                        if (endpoint != null) {
                            return endpoint;
                        }
                    }
                }
            }
        } else if (item.getData() instanceof Endpoint) {
            if (((Endpoint) item.getData()).getName().equals(name)) {
                return item;
            }
        }
        
        return matchingItem;

    }
}
