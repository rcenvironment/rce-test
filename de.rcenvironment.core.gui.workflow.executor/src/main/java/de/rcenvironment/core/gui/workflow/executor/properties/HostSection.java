/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * "Properties" view tab for configuring cluster configuration.
 *
 * @author Doreen Seider
 */
public class HostSection extends ValidatingWorkflowNodePropertySection {

    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        
        TabbedPropertySheetWidgetFactory factory = aTabbedPropertySheetPage.getWidgetFactory();
        
        Section hostSection = factory.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        hostSection.setText(Messages.configureHost);
        
        Composite hostParent = factory.createFlatFormComposite(hostSection);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        hostParent.setLayout(layout);
                
        Label hostLabel = new Label(hostParent, SWT.NONE);
        hostLabel.setText(Messages.hostLabel);

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        Text hostText = factory.createText(hostParent, "");
        hostText.setLayoutData(gridData);
        hostText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_HOST);

        Label portLabel = new Label(hostParent, SWT.NONE);
        portLabel.setText(Messages.portLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        Text portText = factory.createText(hostParent, "");
        portText.setLayoutData(gridData);
        portText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_PORT);
        
        Label sandboxRootLabel = new Label(hostParent, SWT.NONE);
        sandboxRootLabel.setText(Messages.sandboxRootLabel);

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        Text sandboxRootText = factory.createText(hostParent, "");
        sandboxRootText.setLayoutData(gridData);
        sandboxRootText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_SANDBOXROOT);
        
        hostSection.setClient(hostParent);
        
    }

}
