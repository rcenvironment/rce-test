/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointPropertySection;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointSelectionPane;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;
/**
 * Overrides {@link DynamicEndpointPropertySection} to give it custom in- and outputs.
 *
 * @author Sascha Zur
 */
public class OptimizerInputsOutputs extends DynamicEndpointPropertySection{

    protected DynamicEndpointSelectionPane constraintPane;

    public OptimizerInputsOutputs(){

        TypeSelectionFactory defaultTypeSelectionFactory = new TypeSelectionFactory() {

            @Override
            public List<TypeSelectionOption> getTypeSelectionOptions() {
                List<TypeSelectionOption> result = new ArrayList<TypeSelectionOption>();
                // mock options for testing
                result.add(new TypeSelectionOption("Double", Double.class.getName()));
                return result;
            }
        };
        inputPane = new OptimizerDynamicEndpointSelectionPane(Messages.targetFunction,
            ComponentDescription.EndpointNature.Input, defaultTypeSelectionFactory, this, OptimizerComponentConstants.PANE_INPUT);
        outputPane = new OptimizerDynamicEndpointSelectionPane(Messages.constraints,
            ComponentDescription.EndpointNature.Input, defaultTypeSelectionFactory, this, OptimizerComponentConstants.PANE_CONSTRAINTS);

        constraintPane = new OptimizerDynamicEndpointSelectionPane(Messages.designVariables,
            ComponentDescription.EndpointNature.Output, defaultTypeSelectionFactory, this, OptimizerComponentConstants.PANE_OUTPUT);
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {

        TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        Composite mainContainer = toolkit.createFlatFormComposite(parent);

        mainContainer.setLayout(new GridLayout(1, true));
        GridData layoutData;

        inputPane.createControl(mainContainer, Messages.targetFunction, toolkit);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        inputPane.getControl().setLayoutData(layoutData);
        outputPane.createControl(mainContainer, Messages.constraints, toolkit);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        outputPane.getControl().setLayoutData(layoutData);
        constraintPane.createControl(mainContainer, Messages.designVariables, toolkit);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        constraintPane.getControl().setLayoutData(layoutData);
    }

    @Override
    public void refresh() {
        final ReadableComponentInstanceConfiguration configuration = getConfiguration();
        inputPane.setConfiguration(configuration);
        outputPane.setConfiguration(configuration);
        constraintPane.setConfiguration(configuration);
    }
}
