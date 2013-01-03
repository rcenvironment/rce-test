/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.properties;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.gui.workflow.view.Messages;

/**
 * Class that maps information about a runnin component onto the IPropertySource interface.
 * 
 * @author Doreen Seider
 */
public class ComponentInstancePropertySource implements IPropertySource {

    private static final String PROP_KEY_PLATFORM = "de.rcenvironment.rce.gui.workflow.view.properties.platform";

    private static final String PROP_KEY_STARTTIME = "de.rcenvironment.rce.gui.workflow.view.properties.starttime";
    
    private static final String PROP_KEY_WORKKLOWPLATFORM = "de.rcenvironment.rce.gui.workflow.view.properties.workflowcontrollerplatform";

    private static final String PROP_KEY_NAME = "de.rcenvironment.rce.gui.workflow.view.properties.name";

    private ComponentInstanceDescriptor compInstDescr;
    
    private WorkflowInformation workflowInfo;

    public ComponentInstancePropertySource(WorkflowInformation workflowInfo, ComponentInstanceDescriptor compInstDescr) {
        this.workflowInfo = workflowInfo;
        this.compInstDescr = compInstDescr;
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptors = new IPropertyDescriptor[4];

        descriptors[0] = new TextPropertyDescriptor(PROP_KEY_NAME, Messages.name);
        descriptors[1] = new TextPropertyDescriptor(PROP_KEY_STARTTIME, Messages.starttime);
        descriptors[2] = new TextPropertyDescriptor(PROP_KEY_PLATFORM, Messages.platform);
        descriptors[3] = new TextPropertyDescriptor(PROP_KEY_WORKKLOWPLATFORM, Messages.controllerPlatform);

        return descriptors;
    }

    @Override
    public Object getPropertyValue(Object key) {
        Object value = null;
        if (key.equals(PROP_KEY_NAME)) {
            value = compInstDescr.getName();
        } else if (key.equals(PROP_KEY_PLATFORM)) {
            if (compInstDescr.getPlatform() == null || new SimpleCommunicationService().isLocalPlatform(compInstDescr.getPlatform())) {
                value = Messages.local;
            } else {
                value = compInstDescr.getPlatform();
            }
        } else if (key.equals(PROP_KEY_STARTTIME)) {
            value = workflowInfo.getInstantiationTime();
        } else if (key.equals(PROP_KEY_WORKKLOWPLATFORM)) {
            if (workflowInfo.getControllerPlatform() == null) {
                value = Messages.local;
            } else {
                value = workflowInfo.getControllerPlatform();
            }
        }
        return value;
    }

    @Override
    public boolean isPropertySet(Object key) {
        return true;
    }

    @Override
    public void resetPropertyValue(Object key) {
    }

    @Override
    public void setPropertyValue(Object key, Object value) {
    }

}
