/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.components.simplewrapper.commons.SimpleWrapperComponentConstants;
import de.rcenvironment.rce.components.simplewrapper.commons.FileMappings;
import de.rcenvironment.rce.components.simplewrapper.commons.ConfigurationValueConverter;


/**
 * Content provider for input mapping.
 *
 * @author Christian Weiss
 */
public class InputMappingContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        if (inputElement == null || !(inputElement instanceof WorkflowNode)) {
            throw new IllegalArgumentException();
        }
        final WorkflowNode workflowNode = (WorkflowNode) inputElement;
        final List<String[]> result = new LinkedList<String[]>();
        final FileMappings configuredMappings = getConfiguredMappings(workflowNode);
        // input
        result.addAll(gatherDefinitions(configuredMappings, EndpointNature.Input, workflowNode.getDynamicInputDefinitions().entrySet()));
        // output
        result.addAll(gatherDefinitions(configuredMappings, EndpointNature.Output, workflowNode.getDynamicOutputDefinitions().entrySet()));
        final String[][] resultsArray = result.toArray(new String[0][]);
        return resultsArray;
    }

    private List<String[]> gatherDefinitions(final FileMappings configuredMappings,
        final EndpointNature direction, final Set<Entry<String, Class<? extends Serializable>>> dynamicDefinitions) {
        final List<String[]> result = new LinkedList<String[]>();
        for (final Map.Entry<String, Class<? extends Serializable>> entry : dynamicDefinitions) {
            final String name = entry.getKey();
            final Class<? extends Serializable> type = entry.getValue();
            if (!DataManagementFileReference.class.isAssignableFrom(type)) {
                continue;
            }
            final String[] item = new String[3];
            item[0] = direction.name();
            item[1] = name;
            if (configuredMappings.contains(item[0], item[1])) {
                item[2] = configuredMappings.getPath(item[0], item[1]);
            } else {
                item[2] = "";
            }
            result.add(item);
        }
        return result;
    }

    private FileMappings getConfiguredMappings(final WorkflowNode workflowNode) {
        final String mappingString = (String) workflowNode.getProperty(SimpleWrapperComponentConstants.PROPERTY_FILE_MAPPING);
        final FileMappings result = ConfigurationValueConverter.getConfiguredMappings(mappingString);
        return result;
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // do nothing
    }

    @Override
    public void dispose() {
        // do nothing
    }

}
