/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * Factory for creating a new palette with all running components.
 * 
 * FIXME: dynamic
 * 
 * @author Heinrich Wendel
 */
public class WorkflowPaletteFactory {

    /**
     * Returns a new (up to date) palette by consuming the ComponentRegistry.
     * @param cds {@link List} of {@link ComponentDescription}s
     * @return A palette.
     */
    public PaletteRoot createPalette(List<ComponentDescription> cds) {
        PaletteRoot palette = new PaletteRoot();
        createToolsGroup(palette);
        createComponentsGroup(palette, cds);
        return palette;
    }

    private void createComponentsGroup(PaletteRoot palette, List<ComponentDescription> cds) {
        
        Map<String, List<PaletteEntry>> groupedComponents = new HashMap<String, List<PaletteEntry>>();

        Collections.sort(cds);
        
        for (ComponentDescription cd : cds) {
            // set the default platform of the ComponendDescription to null
            ComponentDescription componentDescription = cd.clone();
            componentDescription.setPlatform(null);
            // prepare the icon of the component
            ImageDescriptor image = null;
            byte[] icon = componentDescription.getIcon16();
            if (icon != null) {
                image = ImageDescriptor.createFromImage(new Image(Display.getCurrent(), new ByteArrayInputStream(icon)));
            } else {
                image = ImageDescriptor.createFromURL(
                    WorkflowPaletteFactory.class.getResource("/resources/icons/component16.gif")); //$NON-NLS-1$
            }
            // create the palette entry
            CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry(
                componentDescription.getName(), componentDescription.getName(),
                new WorkflowNodeFactory(componentDescription),
                image,
                image
            );
            
            if (!groupedComponents.containsKey(cd.getGroup())) {
                groupedComponents.put(cd.getGroup(), new ArrayList<PaletteEntry>());
            }
            groupedComponents.get(cd.getGroup()).add(component);
        }
        
        List<String> groups = new ArrayList<String>(groupedComponents.keySet());
        Collections.sort(groups);
        for (String group : groups) {
            PaletteDrawer componentsDrawer = new PaletteDrawer(group);
            componentsDrawer.addAll(groupedComponents.get(group));
            if (group.equals(ComponentConstants.COMPONENT_GROUP_TEST)) {
                componentsDrawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
            }
            palette.add(componentsDrawer);
        }

    }

    private void createToolsGroup(PaletteRoot palette) {
        PaletteGroup toolsGroup = new PaletteGroup(Messages.tools);
        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        // Add a selection tool to the group
        ToolEntry tool = new SelectionToolEntry();
        palette.setDefaultEntry(tool);
        entries.add(tool);

        // Add (solid-line) connection tool
        tool = new ConnectionCreationToolEntry(
            Messages.connection,
            Messages.newConnection,
            new SimpleFactory(null),
            ImageDescriptor.createFromURL(WorkflowPaletteFactory.class.getResource("/resources/icons/connection16.gif")), //$NON-NLS-1$
            ImageDescriptor.createFromURL(WorkflowPaletteFactory.class.getResource("/resources/icons/connection24.gif")) //$NON-NLS-1$
        );
        entries.add(tool);

        toolsGroup.addAll(entries);
        palette.add(toolsGroup);
    }
    
    /**
     * Factory to create new WorkflowNode objects.
     *
     * @author Heinrich Wendel
     */
    private class WorkflowNodeFactory implements CreationFactory {

        private ComponentDescription description;
        
        public WorkflowNodeFactory(ComponentDescription description) {
            this.description = description;
        }

        @Override
        public Object getNewObject() {
            WorkflowNode node = new WorkflowNode(description.clone());
            Map<String, ? extends Serializable> defaultValues = node.getComponentDescription().getDefaultConfiguration();
            for (String id : defaultValues.keySet()) {
                node.setProperty(id, defaultValues.get(id));
            }
            return node;
        }

        @Override
        public Object getObjectType() {
            return WorkflowNode.class;
        }

    }
}
