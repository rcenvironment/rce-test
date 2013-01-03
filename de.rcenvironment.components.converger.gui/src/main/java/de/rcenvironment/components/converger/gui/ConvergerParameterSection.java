/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.converger.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;



/**
 * Provides a GUI for the parametric study component.
 *
 * @author Sascha Zur
 */
public class ConvergerParameterSection extends ValidatingWorkflowNodePropertySection {
    
    private Text absConvText;
    private Text relConvText;
    
    private String epsR = "epsR";
    private String epsA = "epsA";
    
    private final int colorValue = 255;
    
    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {

        final Section sectionProperties = PropertyTabGuiHelper.createSingleColumnSectionComposite(
            parent, getWidgetFactory(), "");
        final Composite sectionInstallationClient = getWidgetFactory().createComposite(sectionProperties);
        sectionProperties.setClient(sectionInstallationClient);
        sectionInstallationClient.setLayout(new GridLayout(2, false));

        Label absoluteConvergenceLabel = new Label(sectionInstallationClient, SWT.NONE);
        absoluteConvergenceLabel.setText(Messages.absoluteConvergenceMessage);

        absConvText = new Text(sectionInstallationClient, SWT.BORDER);

        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                double result = 0 - 1;

                try {
                    result = Double.parseDouble(absConvText.getText());
                    if (result <= 0){
                        throw new NumberFormatException();
                    }
                    absConvText.setBackground(new Color(null, colorValue, colorValue, colorValue));
                } catch (NumberFormatException nfe){
                    absConvText.setBackground(new Color(null, colorValue, 0 , 0));
                }

                if (result != 0 - 1){
                    ConvergerParameterSection.this.setProperty(
                        epsA, result);
                }
            }
        };
        absConvText.addModifyListener(modifyListener);

        Label relativeConvergenceLabel = new Label(sectionInstallationClient, SWT.NONE);
        relativeConvergenceLabel.setText(Messages.relativeConvergenceMessage);

        relConvText = new Text(sectionInstallationClient, SWT.BORDER);
        ModifyListener modifyListener2 = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                double result = 0 - 1;

                try {
                    result = Double.parseDouble(relConvText.getText());
                    if (result <= 0){
                        throw new NumberFormatException();
                    }
                    relConvText.setBackground(new Color(null, colorValue, colorValue, colorValue));
                } catch (NumberFormatException nfe){
                    relConvText.setBackground(new Color(null, colorValue, 0 , 0));
                }
                if (result != 0 - 1){
                    ConvergerParameterSection.this.setProperty(epsR, result);
                }
            }
        };
        relConvText.addModifyListener(modifyListener2);
    }
    
    @Override
    public void aboutToBeShown() {
        super.aboutToBeShown();
        absConvText.setText("");
        if (getProperty(epsA) != null){
            absConvText.setText("" + getProperty(epsA));
        } else {
            absConvText.setBackground(new Color(null, colorValue, 0 , 0));
        }
        relConvText.setText("");
        if (getProperty(epsR) != null){
            relConvText.setText("" + getProperty(epsR));
        } else {
            relConvText.setBackground(new Color(null, colorValue, 0 , 0));
        }
    }
    @Override
    public void refresh() {
        aboutToBeShown();
    }
}
