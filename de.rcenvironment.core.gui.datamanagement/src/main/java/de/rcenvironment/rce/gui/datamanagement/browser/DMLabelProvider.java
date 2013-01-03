/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeType;

/**
 * Label provider for the {@link DataManagementBrowser}.
 * 
 * @author Markus Litz
 * @author Robert Mischke
 * 
 */
public class DMLabelProvider extends LabelProvider {

    private static final boolean DEBUG_LABEL_INFO_ENABLED = false;

    @Override
    public Image getImage(Object element) {
        Image result = DMBrowserImages.IMG_DEFAULT;
        DMBrowserNode file = (DMBrowserNode) element;
        if (file.getType() == DMBrowserNodeType.Workflow) {
            result = DMBrowserImages.IMG_WORKFLOW;
        } else if (file.getType() == DMBrowserNodeType.Folder) {
            result = DMBrowserImages.IMG_FOLDER;
        } else if (file.getType() == DMBrowserNodeType.DMFileResource) {
            result = DMBrowserImages.IMG_FILE;
        } else if (file.getType() == DMBrowserNodeType.InformationText) {
            result = DMBrowserImages.IMG_INFO;
        } else if (file.getType() == DMBrowserNodeType.WarningText) {
            result = DMBrowserImages.IMG_WARNING;
        } else if (file.getType() == DMBrowserNodeType.HistoryRoot) {
            result = DMBrowserImages.IMG_DEFAULT;
            // return DMBrowserImages.IMG_DATA_MANAGEMENT;
            // } else if (file.type == DMBrowserNodeType.Resource) {
            // return DMBrowserImages.IMG_FILE;
            // } else if (file.type == DMBrowserNodeType.VersionizedResource) {
            // return DMBrowserImages.imageRevision;
            // } else if (file.type == DMBrowserNodeType.Component) {
            // return DMBrowserImages.imageComponent;
        } else if (file.getType() == DMBrowserNodeType.Loading) {
            result = DMBrowserImages.IMG_REFRESH;
        }
        return result;
    }

    @Override
    public String getText(Object element) {
        DMBrowserNode node = (DMBrowserNode) element;
        if (DEBUG_LABEL_INFO_ENABLED && (node.getDataReferenceId() != null || node.getAssociatedFilename() != null)) {
            return String.format("%s <DM Reference: %s; Filename: %s>", node.getTitle(), node.getDataReferenceId(),
                node.getAssociatedFilename());
        } else {
            return node.getTitle();
        }
    }

}
