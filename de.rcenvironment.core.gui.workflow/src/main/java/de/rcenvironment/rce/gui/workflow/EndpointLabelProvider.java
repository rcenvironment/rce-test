/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

import de.rcenvironment.rce.gui.workflow.editor.connections.ConnectionDialogController.Type;


/**
 * {@link LabelProvider} for the contents of the {@link EndpointTreeViewer}.
 *
 * @author Heinrich Wendel
 */
public class EndpointLabelProvider extends LabelProvider {

    private Image componentImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/component16.gif")).createImage();
    
    private Image inputImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/input16.gif")).createImage();
    
    private Image outputImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/output16.gif")).createImage();
    
    private Image stringImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/string16.png")).createImage();
    
    private Image integerImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/integer16.png")).createImage();
    
    private Image longImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/long16.png")).createImage();
    
    private Image doubleImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/double16.png")).createImage();
    
    private Image booleanImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/boolean16.png")).createImage();
    
    private Image linkImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/link16.gif")).createImage();
    
    private Image tableImage = ImageDescriptor.createFromURL(
        EndpointLabelProvider.class.getResource("/resources/icons/table16.png")).createImage();
    
    private Map<String, Image> componentImages = new HashMap<String, Image>();
    
    private Type type;
    
    public EndpointLabelProvider(Type type) {
        this.type = type;
    }
    
    @Override
    public String getText(Object element) {
        String name;
        if (element instanceof WorkflowNode) {
            name = ((WorkflowNode) element).getName();
        } else if (element instanceof EndpointContentProvider.EndpointGroup) {
            name = ((EndpointContentProvider.EndpointGroup) element).getName();
        } else if (element instanceof EndpointContentProvider.Endpoint) {
            name = ((EndpointContentProvider.Endpoint) element).getName();
        } else {
            name = ""; //$NON-NLS-1$
        }
        return name;
    }

    @Override
    public Image getImage(Object element) {
        
        Image image = null;
        if (element instanceof WorkflowNode) {
            ComponentDescription componentDesc = ((WorkflowNode) element).getComponentDescription();
            if (componentImages.containsKey(componentDesc.getIdentifier())) {
                image = componentImages.get(componentDesc.getIdentifier());
            } else {
                byte[] icon = componentDesc.getIcon16();
                if (icon != null) {
                    image = new Image(Display.getCurrent(), new ByteArrayInputStream(icon));
                    componentImages.put(componentDesc.getIdentifier(), image);
                } else {
                    image = componentImage;
                }
            }            
        } else if (element instanceof EndpointContentProvider.EndpointGroup) {
            if (type == Type.INPUT) {
                image = inputImage;
            } else {
                image = outputImage;
            }
            
        } else if (element instanceof EndpointContentProvider.Endpoint) {
            if (((EndpointContentProvider.Endpoint) element).getType() == String.class) {
                image = stringImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == Integer.class) {
                image = integerImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == Long.class) {
                image = longImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == Double.class) {
                image = doubleImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == Boolean.class) {
                image = booleanImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == DataManagementFileReference.class) {
                image = linkImage;
            } else if (((EndpointContentProvider.Endpoint) element).getType() == VariantArray.class) {
                image = tableImage;
            }
        }
        return image;
    }
    
    @Override
    public void dispose() {
        componentImage.dispose();
        inputImage.dispose();
        outputImage.dispose();
        stringImage.dispose();
        integerImage.dispose();
        longImage.dispose();
        doubleImage.dispose();
        booleanImage.dispose();
        linkImage.dispose();
        tableImage.dispose();
        for (Image image : componentImages.values()) {
            image.dispose();
        }
    }

}
