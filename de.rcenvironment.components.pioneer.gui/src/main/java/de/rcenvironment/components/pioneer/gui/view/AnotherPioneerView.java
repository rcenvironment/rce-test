/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.gui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.gui.workflow.view.ComponentRuntimeView;

/**
 * Test for IComponentMonitor extension point.
 *
 * @author Heinrich Wendel
 */
public class AnotherPioneerView extends ViewPart implements ComponentRuntimeView {
    
    private Text text;
    
    @Override
    public void createPartControl(Composite parent) {
        text = new Text(parent, SWT.NONE);
        text.setText("No component set");
    }
    
    @Override
    public void setFocus() {

    }

    @Override
    public void setComponentInstanceDescriptor(ComponentInstanceDescriptor compInstDescr) {
        text.setText(compInstDescr.getIdentifier());
    }
}
