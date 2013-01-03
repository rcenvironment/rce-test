/*
 * Copyright (C) 2006-2012 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.gui.communication.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.rcenvironment.core.communication.model.NetworkStateNode;
import de.rcenvironment.core.gui.communication.views.model.TopologyLink;

/**
 * The {@link LabelProvider} for the network view.
 * 
 * @author Robert Mischke
 */
public class NetworkViewLabelProvider extends LabelProvider {

    private Image nodeImage;

    private Image networkImage;

    private Image connectImage;

    private Image disconnectImage;

    private boolean optionNodeIdsVisible;

    public NetworkViewLabelProvider() {
        createImages();
    }

    public void setNodeIdsVisible(boolean value) {
        optionNodeIdsVisible = value;
    }

    @Override
    public String getText(Object element) {
        String result;
        if (element == NetworkViewContentProvider.NETWORK_ROOT_NODE) {
            result = "Network";
        } else if (element instanceof NetworkStateNode) {
            NetworkStateNode node = (NetworkStateNode) element;
            result = node.getDisplayName();
            if (optionNodeIdsVisible) {
                result += "  [" + node.getNodeId() + "] ";
            }
            if (node.isWorkflowHost()) {
                result += " <Workflow Host>";
            }
            if (node.isLocalNode()) {
                result += " <Self>";
            }

        } else if (element instanceof TopologyLink) {
            result = ((TopologyLink) element).getLinkId(); // TODO
        } else {
            result = element.toString();
        }
        return result;
    }

    @Override
    public Image getImage(Object element) {
        Image result;
        if (element == NetworkViewContentProvider.NETWORK_ROOT_NODE) {
            result = networkImage;
        } else if (element instanceof NetworkStateNode) {
            result = nodeImage;
        } else if (element instanceof TopologyLink) {
            result = connectImage;
        } else {
            result = super.getImage(element);
        }
        return result;
    }

    @Override
    public void dispose() {
        disposeImages();
    }

    private void createImages() {
        nodeImage = ImageDescriptor.createFromURL(
            getClass().getResource("/icons/node.png")).createImage(); //$NON-NLS-1$
        networkImage = ImageDescriptor.createFromURL(
            getClass().getResource("/icons/network.png")).createImage(); //$NON-NLS-1$
        connectImage = ImageDescriptor.createFromURL(
            getClass().getResource("/icons/connect.png")).createImage(); //$NON-NLS-1$
        disconnectImage = ImageDescriptor.createFromURL(
            getClass().getResource("/icons/disconnect.png")).createImage(); //$NON-NLS-1$   
    }

    private void disposeImages() {
        nodeImage.dispose();
        networkImage.dispose();
        connectImage.dispose();
        disconnectImage.dispose();
    }

}
