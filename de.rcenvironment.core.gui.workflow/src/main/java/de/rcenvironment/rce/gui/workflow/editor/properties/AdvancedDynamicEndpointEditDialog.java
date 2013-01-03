/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;


/**
 * Extended version of {@link DynamicEndpointEditDialog} with the opportunity to choose between required and initial variables.
 * 
 * @author Sascha Zur
 */
public class AdvancedDynamicEndpointEditDialog extends DynamicEndpointEditDialog{

    private Combo dataUseCombo;
    private String dataUseSelection;

    private String initialDataUse;

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param title
     * @param configuration
     * @param direction
     * @param typeSelectionFactory
     */
    public AdvancedDynamicEndpointEditDialog(Shell parentShell, String title, ReadableComponentInstanceConfiguration configuration,
        EndpointNature direction, TypeSelectionFactory typeSelectionFactory) {
        super(parentShell, title, configuration, direction, typeSelectionFactory);
        dataUseSelection = ComponentConstants.INPUT_USAGE_TYPES[0];
    }



    @Override
    protected Control createDialogArea(Composite parent){
        Composite control = (Composite) super.createDialogArea(parent);

        if (direction == EndpointNature.Input){
            new Label(control, SWT.NONE).setText(Messages.dataUse);

            dataUseCombo = new Combo(control, SWT.READ_ONLY);
            for (String str : ComponentConstants.INPUT_USAGE_TYPES){
                dataUseCombo.add(getDataUseLokalization(str));
            }
            if (initialDataUse != null){
                dataUseCombo.select(dataUseCombo.indexOf(getDataUseLokalization(initialDataUse)));
            } else {
                dataUseCombo.select(0);
            }
            dataUseCombo.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    dataUseSelection = ComponentConstants.INPUT_USAGE_TYPES[dataUseCombo.getSelectionIndex()];
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent arg0) {
                    widgetSelected(arg0);

                }
            });

        }

        return control;
    }
    
    private String getDataUseLokalization(String in){
        String result = "";
        
        if (in.equals(ComponentConstants.INPUT_USAGE_TYPES[0])){
            result = Messages.dataUseRequired;
        } else if (in.equals(ComponentConstants.INPUT_USAGE_TYPES[1])){
            result = Messages.dataUseInit;
        } else if (in.equals(ComponentConstants.INPUT_USAGE_TYPES[2])){
            result = Messages.dataUseOptional;
        } 
        return result;
    }

    public String getDataUseSelection() {
        return dataUseSelection;
    }


    public void setDataUseSelection(String dataUseSelection) {
        this.dataUseSelection = dataUseSelection;
    }

    public void setInitialDataUse(String oldDataUse) {
        this.initialDataUse = oldDataUse;        
    }


}
