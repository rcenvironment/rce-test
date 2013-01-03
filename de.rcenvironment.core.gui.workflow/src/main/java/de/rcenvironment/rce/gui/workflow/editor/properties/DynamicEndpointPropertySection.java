/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;

/**
 * A "Properties" view tab for configuring dynamic endpoints (ie inputs and outputs).
 * 
 * @author Robert Mischke
 * @author Sascha Zur
 */
public class DynamicEndpointPropertySection extends WorkflowNodePropertySection {
    
    private static final int SPACE_ON_RIGHT_SIDE = 20;

    protected DynamicEndpointSelectionPane inputPane;

    protected DynamicEndpointSelectionPane outputPane;

    private Composite mainContainer;

    public DynamicEndpointPropertySection() {

        // TODO example factory; replace by actual mechanism
        TypeSelectionFactory defaultTypeSelectionFactory = new TypeSelectionFactory() {

            @Override
            public List<TypeSelectionOption> getTypeSelectionOptions() {
                List<TypeSelectionOption> result = new ArrayList<TypeSelectionOption>();
                // mock options for testing
                result.add(new TypeSelectionOption("String", String.class.getName()));
                result.add(new TypeSelectionOption("Integer", Integer.class.getName()));
                result.add(new TypeSelectionOption("Double", Double.class.getName()));
                result.add(new TypeSelectionOption("Serializable", Serializable.class.getName()));
                result.add(new TypeSelectionOption("File Reference", DataManagementFileReference.class.getName()));
                result.add(new TypeSelectionOption("Array", VariantArray.class.getName()));
                return result;
            }
        };
        inputPane = new DynamicEndpointSelectionPane("Input",
            ComponentDescription.EndpointNature.Input, defaultTypeSelectionFactory, this);
        outputPane = new DynamicEndpointSelectionPane("Output",
            ComponentDescription.EndpointNature.Output, defaultTypeSelectionFactory, this);
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {

        super.createCompositeContent(parent, aTabbedPropertySheetPage);

        TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        final Composite content = new LayoutComposite(parent);
        mainContainer = toolkit.createFlatFormComposite(content);

        mainContainer.setLayout(new GridLayout(2, true));
        GridData layoutData;

        inputPane.createControl(mainContainer, Messages.additionalInputs, toolkit);
        layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        inputPane.getControl().setLayoutData(layoutData);
        outputPane.createControl(mainContainer, Messages.additionalOutputs, toolkit);
        layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        outputPane.getControl().setLayoutData(layoutData);
    }

    @Override
    public void refresh() {
        final ReadableComponentInstanceConfiguration configuration = getConfiguration();
        inputPane.setConfiguration(configuration);
        outputPane.setConfiguration(configuration);
    }
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection#aboutToBeShown()
     */
    @Override
    public void aboutToBeShown() {
        super.aboutToBeShown();
        if (mainContainer != null){
            mainContainer.pack();
            mainContainer.setSize(mainContainer.getSize().x - SPACE_ON_RIGHT_SIDE, mainContainer.getSize().y);
        }
    }
}
