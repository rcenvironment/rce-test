/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.converger.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;
import de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection;


/**
 * "Properties" view tab for configuring cells as additional endpoints (i.e. inputs and outputs).
 * 
 * @author Sascha Zur
 */
public class ConvergerInputSection extends WorkflowNodePropertySection {
    
    protected ConvergerDynamicEndpointSelectionPane inputPane;
    
    public ConvergerInputSection() {

        TypeSelectionFactory defaultTypeSelectionFactory = new TypeSelectionFactory() {

            @Override
            public List<TypeSelectionOption> getTypeSelectionOptions() {
                List<TypeSelectionOption> result = new ArrayList<TypeSelectionOption>();
                // mock options for testing
                result.add(new TypeSelectionOption("Double", Double.class.getName()));
                result.add(new TypeSelectionOption("Long", Long.class.getName()));
                result.add(new TypeSelectionOption("Integer", Integer.class.getName()));
                return result;
            }
        };
        inputPane = new ConvergerDynamicEndpointSelectionPane("Input", 
                ComponentDescription.EndpointNature.Input, 
                defaultTypeSelectionFactory, this);
    }

    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        final Composite content = new LayoutComposite(parent);
        content.setLayout(new GridLayout(1, true));
        
        final Composite mainContainer = toolkit.createFlatFormComposite(content);
        mainContainer.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        final FillLayout layout = new FillLayout();
        layout.spacing = 4;
        mainContainer.setLayout(layout);

        inputPane.createControl(mainContainer, "" , toolkit);
    }

    @Override
    public void refresh() {
        final ReadableComponentInstanceConfiguration configuration = getConfiguration();
        inputPane.setConfiguration(configuration);
    }
    
}
