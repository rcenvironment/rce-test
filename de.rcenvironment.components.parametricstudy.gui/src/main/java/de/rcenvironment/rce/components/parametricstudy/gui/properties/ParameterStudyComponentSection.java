/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.parametricstudy.gui.properties;


import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.components.parametricstudy.commons.ParametricStudyComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * Provides a GUI for the parametric study component.
 *
 * @author Sascha Zur
 */
public class ParameterStudyComponentSection extends ValidatingWorkflowNodePropertySection {

    private Text textfieldFrom;

    private Text textfieldTo;

    private Text textfieldStepsize;

    private Text textfieldIterations;

    private boolean testing = false;

    private boolean refresh;

    private boolean guessing;


    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {

        final Section sectionProperties = PropertyTabGuiHelper.createSingleColumnSectionComposite(
            parent, getWidgetFactory(), Messages.rangeMsg);
        final Composite parmeterConfigurationParent = getWidgetFactory().createComposite(sectionProperties);
        sectionProperties.setClient(parmeterConfigurationParent);
        parmeterConfigurationParent.setLayout(new RowLayout());

        Label firstLabel = new Label(parmeterConfigurationParent, SWT.NONE);
        firstLabel.setText(Messages.fromMsg);
        textfieldFrom = new Text(parmeterConfigurationParent, SWT.CENTER | SWT.BORDER);

        Label secondLabel = new Label(parmeterConfigurationParent, SWT.NONE);
        secondLabel.setText(Messages.toMsg);
        textfieldTo = new Text(parmeterConfigurationParent, SWT.CENTER | SWT.BORDER);

        Label thirdLabel = new Label(parmeterConfigurationParent, SWT.NONE);
        thirdLabel.setText(Messages.stepSizeMsg);
        textfieldStepsize = new Text(parmeterConfigurationParent, SWT.CENTER | SWT.BORDER);

        Label fourthLabel = new Label(parmeterConfigurationParent, SWT.NONE);
        fourthLabel.setText(Messages.inStepMsg);
        textfieldIterations = new Text(parmeterConfigurationParent, SWT.CENTER | SWT.BORDER);

        Label fifthLabel = new Label(parmeterConfigurationParent, SWT.NONE);
        fifthLabel.setText(Messages.stepsMsg);

        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (e.getSource() != textfieldIterations && !((Text) e.getSource()).getText().equals("")){
                    saveNewValue((Text) e.getSource());
                } else { 
                    if (!refresh && !guessing && !textfieldIterations.getText().equals("")){
                        try {
                            textfieldStepsize.setText("" + (Double.parseDouble(textfieldTo.getText())
                                - Double.parseDouble(textfieldFrom.getText()))/Double.parseDouble(textfieldIterations.getText()));
                        } catch (NumberFormatException ex){
                            ex.getLocalizedMessage();
                        }
                    }
                }
            }
        };
        textfieldFrom.addModifyListener(modifyListener);
        textfieldIterations.addModifyListener(modifyListener);
        textfieldStepsize.addModifyListener(modifyListener);
        textfieldTo.addModifyListener(modifyListener);
        validateInput();
    }

    private void saveNewValue(Text source) {
        if (source == textfieldFrom){
            ParameterStudyComponentSection.this.setProperty(
                ParametricStudyComponentConstants.CV_FROMVALUE, textfieldFrom.getText());
        }
        if (source == textfieldTo){
            ParameterStudyComponentSection.this.setProperty(
                ParametricStudyComponentConstants.CV_TOVALUE, textfieldTo.getText());
        }
        if (source == textfieldStepsize){
            ParameterStudyComponentSection.this.setProperty(
                ParametricStudyComponentConstants.CV_STEPSIZE, textfieldStepsize.getText());
        }
    }
    private void saveData() {
        ParameterStudyComponentSection.this.setProperty(
            ParametricStudyComponentConstants.CV_FROMVALUE, Double.parseDouble(textfieldFrom.getText()));
        ParameterStudyComponentSection.this.setProperty(
            ParametricStudyComponentConstants.CV_TOVALUE, Double.parseDouble(textfieldTo.getText()));
        ParameterStudyComponentSection.this.setProperty(
            ParametricStudyComponentConstants.CV_STEPSIZE, Double.parseDouble(textfieldStepsize.getText()));
    }

    private void getData() {
        if (ParameterStudyComponentSection.this.getProperty(ParametricStudyComponentConstants.CV_FROMVALUE) != null){
            textfieldFrom.setText("" + ParameterStudyComponentSection.this.getProperty(ParametricStudyComponentConstants.CV_FROMVALUE));
        } else {
            textfieldFrom.setText("");
        }

        if (ParameterStudyComponentSection.this.getProperty(ParametricStudyComponentConstants.CV_TOVALUE) != null){
            textfieldTo.setText("" + ParameterStudyComponentSection.this.getProperty(ParametricStudyComponentConstants.CV_TOVALUE));
        } else {
            textfieldTo.setText("");
        }
        if (ParameterStudyComponentSection.this.getProperty(ParametricStudyComponentConstants.CV_STEPSIZE) != null){
            textfieldStepsize.setText("" + ParameterStudyComponentSection.this.getProperty(
                ParametricStudyComponentConstants.CV_STEPSIZE));
        } else {
            textfieldIterations.setText("");
        }
    }

    private void guessLastInput() {
        guessing = true;
        try {
            textfieldIterations.setText("" + ((int) ((Double.parseDouble(textfieldTo.getText())
                - Double.parseDouble(textfieldFrom.getText()))/Double.parseDouble(textfieldStepsize.getText()))));
        } catch (NumberFormatException e){
            textfieldIterations.setText("");
        }
        guessing = false;
    }

    private int validateInput() {

        int validInput = 0;
        if (testField(textfieldFrom, true)){
            validInput++;   
        }
        if (testField(textfieldIterations, false)){
            validInput++;
        }
        if (testField(textfieldTo, true)) {
            validInput++;
        }
        if (testField(textfieldStepsize, true)){
            validInput++;
        }

        return validInput;
    }

    private boolean testField(Text toTest, boolean testDouble) {
        boolean isValid = true;
        final int max = 255;

        try {
            if (testDouble){
                Double.parseDouble(toTest.getText());
            } else {
                Integer.parseInt(toTest.getText());
            }
        } catch (NumberFormatException e){
            isValid = false;
            toTest.setBackground(new Color(null, new RGB(max, 0, 0)));
        }
        if (isValid){
            toTest.setBackground(new Color(null, new RGB(max, max, max)));

        }
        return isValid;
    }

    @Override
    public void refresh() {
        refresh = true;
        textfieldFrom.setText("");
        textfieldTo.setText("");
        textfieldIterations.setText("");
        textfieldStepsize.setText("");
        getData();
        refresh = false;
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new SynchronizerImpl();
    }
    /**
     * Listener to keep the GUI in sync with the model.
     * 
     * @author Christian Weiss
     */
    private class SynchronizerImpl extends DefaultSynchronizer {

        @Override
        public void handlePropertyChange(final String key, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(key, newValue, oldValue);

            int count = validateInput();
            if (count > 2 && !testing){
                testing = true;
                guessLastInput();
                count = validateInput();
                testing = false;
            }
            if (count == 4){
                saveData();
            }
        }

        @Override
        public void handleChannelEvent(final ChannelEvent event) {
            super.handleChannelEvent(event);
        }

    }

}
