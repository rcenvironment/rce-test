/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.SimpleComponentRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionConfigurationHelper;
import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * ComboBox editing for selecting target platform for each component.
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
final class TargetPlatformEditingSupport extends EditingSupport {

    private final WorkflowExecutionConfigurationHelper helper;
    
    private final int column;

    private final ColumnViewer viewer;

    private final SimpleComponentRegistry scr;

    private final PlatformIdentifier localPlatform;

    /** Mapping between the choices displayed to the user and the backing {@link PlatformIdentifier}s. */
    private final Map<String, PlatformIdentifier> platforms = new HashMap<String, PlatformIdentifier>();
    
    public TargetPlatformEditingSupport(final WorkflowExecutionConfigurationHelper helper, final PlatformIdentifier localPlatform,
        final ColumnViewer viewer, final int column) {
        super(viewer);
        this.helper = helper;
        this.column = column;
        this.scr = helper.getSimpleComponentRegistry();
        this.viewer = viewer;
        this.localPlatform = localPlatform;
    }

    @Override
    protected boolean canEdit(Object arg0) {
        return column == 1;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (!(element instanceof WorkflowNode)) {
            return null;
        }
        ComboBoxCellEditor editor =
            new ComboBoxCellEditor(((TableViewer) viewer).getTable(), getValues((WorkflowNode) element).toArray(new String[] {}));
        return editor;
    }

    /**
     * Returns whether the value retrieved via {@link #getValue(Object)} was a suggestion based on
     * the platforms available for the given {@link WorkflowNode}. A value of false indicates, that
     * the value reflects the value of the {@link ComponentDescription}.
     * 
     * @param The {@link WorkflowNode}.
     * @return True, if the value for the given {@link WorkflowNode} is arbitrarily chosen from the
     *         set of available platforms for this type of node.
     */
    protected boolean isValueSuggestion(final WorkflowNode node) {
        final PlatformIdentifier platform = ((WorkflowNode) node).getComponentDescription().getPlatform();
        final boolean result = !platforms.values().contains(platform);
        return result;
    }

    /**
     * {@inheritDoc}.
     * <p>
     * The returned value is preferably the one specified in the {@link ComponentDescription}, but
     * if this platform is not able to execute this type of node, another valid platform is chosen
     * (which would be indicated by a return value of 'true' through
     * {@link #isValueSuggestion(WorkflowNode)}).
     * </p>
     * 
     * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object element) {
        Integer result = null;
        if (element instanceof WorkflowNode && column == 1) {
            PlatformIdentifier platform = ((WorkflowNode) element).getComponentDescription().getPlatform();
            if (platform == null && getValues((WorkflowNode) element).contains(Messages.localPlatformSelectionTitle)) {
                result = 0;
            } else if (platform != null) {
                String platformIdentifier = platform.toString();
                if (platform.equals(localPlatform)) {
                    platformIdentifier = Messages.bind(Messages.localPlatformExplicitSelectionTitle, platformIdentifier);
                }
                final int preferedIndex = getValues((WorkflowNode) element).indexOf(platformIdentifier);
                if (preferedIndex >= 0) {
                    result = preferedIndex;
                } else {
                    result = 0;
                }
            } else {
                result = 0;
            }
        }
        return result;
    }

    @Override
    protected void setValue(Object element, Object value) {
        int intValue = ((Integer) value).intValue();
        if (intValue >= 0) {
            PlatformIdentifier platform = platforms.get(getValues((WorkflowNode) element).get(intValue));

            String ci = ((WorkflowNode) element).getComponentDescription().getIdentifier();

            ComponentDescription desc = scr.getComponentDescription(ci);
            desc.setPlatform(platform);

            ((WorkflowNode) element).setComponentDescription(desc);
        }
    }

    /**
     * Builds and returns the list of available platforms for the given {@link WorkflowNode}.
     * 
     * @param node The {@link WorkflowNode}.
     * @return The list of strings representing the available platforms for the given
     *         {@link WorkflowNode}.
     */
    protected List<String> getValues(WorkflowNode node) {
        platforms.clear();
        final List<PlatformIdentifier> targetPlatforms = helper.getTargetPlatformsForComponentSortedByName(node.getComponentDescription());
        List<String> targetPlatformsValueList = new ArrayList<String>(platforms.size() + 1);
        // add the *local* option as the topmost one, if the local platform supports the component
        if (targetPlatforms.contains(localPlatform)) {
            targetPlatformsValueList.add(Messages.localPlatformSelectionTitle);
            platforms.put(Messages.localPlatformSelectionTitle, null);
        }
        // add all supporting platforms to the list of choices
        for (PlatformIdentifier platformIdentifier : targetPlatforms) {
            if (platformIdentifier == null) {
                LogFactory.getLog(getClass()).error("NULL platformIdentifier!");
                continue;
            }
            String value = platformIdentifier.getAssociatedDisplayName();
            if (platformIdentifier.equals(localPlatform)) {
                value = Messages.bind(Messages.localPlatformExplicitSelectionTitle, value);
            }
            targetPlatformsValueList.add(value);
            platforms.put(value, platformIdentifier);
        }
        return targetPlatformsValueList;
    }

}
