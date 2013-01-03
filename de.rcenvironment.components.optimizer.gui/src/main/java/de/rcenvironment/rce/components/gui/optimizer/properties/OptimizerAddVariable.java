/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import static de.rcenvironment.gui.commons.components.PropertyTabGuiHelper.OFFSET;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.DynamicEndpointEditDialog;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;

/**
 * 
 * A dialog for defining and editing Variables as additional endpoints.
 * 
 * @author Sascha Zur
 */
public class OptimizerAddVariable  extends DynamicEndpointEditDialog{

    private int paneType;

    private Composite container;

    private double lowerBound;

    private String initiallow;

    private double upperBound;

    private String initialup;

    private double startValue;

    private String initialstart;

    private double weight;

    private String initialweight;

    private int goal; 

    private String initialgoal;

    private double solveFor;

    private String initialSolve;

    private Text lbText;

    private Text ubText;

    private Text weightText;

    private Text startText;

    private Text solveText;


    private Combo comboboxGoal;


    public OptimizerAddVariable(Shell parentShell, String title, ReadableComponentInstanceConfiguration configuration,
        EndpointNature direction, TypeSelectionFactory typeSelectionFactory) {
        super(parentShell, title, configuration, direction, typeSelectionFactory);
    }

    public OptimizerAddVariable(Shell shell, String string, ReadableComponentInstanceConfiguration configuration, EndpointNature direction,
        TypeSelectionFactory typeSelectionFactory, FormToolkit toolkit, int type) {
        super(shell, string, configuration, direction, typeSelectionFactory);

        paneType = type;
    }
    @Override
    protected Control createDialogArea(Composite parent) {

        container = (Composite) super.createDialogArea(parent);
        if (currentName != null) {
            textfieldName.setText(currentName);
        }
        if (paneType == OptimizerComponentConstants.PANE_CONSTRAINTS){
            Label lowbound = new Label(container, SWT.NONE);
            lowbound.setText("Lower bound");
            lbText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initiallow != null) {
                lbText.setText(initiallow);
            }
            lbText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            Label upbound = new Label(container, SWT.NONE);
            upbound.setText("Upper bound");

            ubText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initialup != null) {
                ubText.setText(initialup);
            }
            ubText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        } else if (paneType == OptimizerComponentConstants.PANE_INPUT){
            Label weightLabel = new Label(container, SWT.NONE);
            weightLabel.setText("Weight");

            weightText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initialweight != null) {
                weightText.setText(initialweight);
            }
            weightText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            comboboxGoal = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
            comboboxGoal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            final FormData versionData = new FormData();
            versionData.top = new FormAttachment(0, OFFSET);
            versionData.left = new FormAttachment(0, OFFSET);

            comboboxGoal.add("Minimize");

            comboboxGoal.add("Maximize");

            comboboxGoal.add("Solve for");

            solveText = new Text(container, SWT.SINGLE | SWT.BORDER);


         
            solveText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            solveText.setEnabled(false);

            if (initialgoal != null){
                comboboxGoal.select(Integer.parseInt(initialgoal));
                if (comboboxGoal.getSelectionIndex() == 2){
                    solveText.setEnabled(true);
                    if (initialSolve != null) {
                        solveText.setText(initialSolve);
                    }
                }
            }

            //            comboboxGoal.setLayoutData(versionData);
            comboboxGoal.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(final Event event) {
                    final String key = comboboxGoal.getItem(comboboxGoal.getSelectionIndex());
                    if (key.equals("Solve for")){
                        goal = 2;
                        solveText.setEnabled(true);

                    } else {
                        if (key.equals("Minimze")){
                            goal = 0;
                        } else {
                            goal = 1;
                        }
                        solveText.setEnabled(false);
                    }
                    validateInput();
                }
            });

        } else if (paneType == OptimizerComponentConstants.PANE_OUTPUT){
            Label lowbound = new Label(container, SWT.NONE);
            lowbound.setText("Lower bound");


            lbText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initiallow != null) {
                lbText.setText(initiallow);
            }
            lbText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            Label upbound = new Label(container, SWT.NONE);
            upbound.setText("Upper bound");

            ubText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initialup != null) {
                ubText.setText(initialup);
            }
            ubText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            Label startvalueLabel = new Label(container, SWT.NONE);
            startvalueLabel.setText("Startvalue");

            startText = new Text(container, SWT.SINGLE | SWT.BORDER);
            if (initialstart != null) {
                startText.setText(initialstart);
            }
            startText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        }
        return container;
    }
    protected void validateInput() {

        String name = getNameInputFromUI();
        String type = getTypeSelectionFromUI();



        // initialName is null if not set, so it will not be equal when naming a new endpoint
        boolean nameIsValid = name.equals(initialName);
        boolean typeIsValid;
        if (direction == ComponentDescription.EndpointNature.Input) {
            nameIsValid |= configuration.validateInputName(name);
            typeIsValid = configuration.validateInputType(type);
        } else {
            nameIsValid |= configuration.validateOutputName(name);
            typeIsValid = configuration.validateOutputType(type);
        }

        boolean ubValid = true;
        boolean lbValid = true;

        if (paneType == OptimizerComponentConstants.PANE_CONSTRAINTS || paneType == OptimizerComponentConstants.PANE_OUTPUT){
            String lower = getLowerBoundFromUI();
            String upper = getUpperBoundFromUI();
            try {
                Double.parseDouble(lower);
                Double.parseDouble(upper);
                ubValid = true;
                lbValid = true;
            } catch (NumberFormatException e) {
                if (!lower.equals("")){
                    lbValid = false;
                }
                if (!upper.equals("")){
                    ubValid = false;
                }
            }

        }
        boolean startValid = true;
        if (paneType == OptimizerComponentConstants.PANE_OUTPUT){
            String start = getStartFromUI();
            try {
                Double.parseDouble(start);
                startValid = true;
            } catch (NumberFormatException e) {
                startValid = false;
            }

        } 
        boolean weightValid = true;
        if (paneType == OptimizerComponentConstants.PANE_INPUT){
            String weightV = getWeightFromUI();
            try {
                Double.parseDouble(weightV);
                weightValid = true;
            } catch (NumberFormatException e) {
                weightValid = false;
            }
            if (comboboxGoal.getSelectionIndex() == 2){
                try {
                    Double.parseDouble(solveText.getText());
                } catch (NumberFormatException e){
                    weightValid = false;
                }
            }

        }

        // enable/disable "ok"
        getButton(IDialogConstants.OK_ID).setEnabled(weightValid && startValid && ubValid && lbValid && nameIsValid && typeIsValid);
    }

    private String getWeightFromUI() {
        return weightText.getText();
    }

    private String getUpperBoundFromUI() {
        return ubText.getText();
    }
    private String getStartFromUI() {
        return startText.getText();
    }
    private String getLowerBoundFromUI() {
        return lbText.getText();
    }

    private String getNameInputFromUI() {
        return textfieldName.getText();

    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        super.create();
        // dialog title
        getShell().setText(title);
        // initial validation
        validateInput();
        // set listeners here so the ok button is initialized
        installModifyListeners();
    }
    protected String getTypeSelectionFromUI() {
        String type = null;
        IStructuredSelection comboSelection = (IStructuredSelection) comboViewer.getSelection();
        if (!comboSelection.isEmpty()) {
            type = ((TypeSelectionOption) comboSelection.getFirstElement()).getTypeName();
        }
        return type;
    }
    private void installModifyListeners() {
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        };
        textfieldName.addModifyListener(modifyListener);

        comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                validateInput();
            }
        });

        if (paneType == OptimizerComponentConstants.PANE_CONSTRAINTS){
            lbText.addModifyListener(modifyListener);
            ubText.addModifyListener(modifyListener);
        }
        if (paneType == OptimizerComponentConstants.PANE_OUTPUT){
            lbText.addModifyListener(modifyListener);
            ubText.addModifyListener(modifyListener);
            startText.addModifyListener(modifyListener);

        }
        if (paneType == OptimizerComponentConstants.PANE_INPUT){
            weightText.addModifyListener(modifyListener);
            solveText.addModifyListener(modifyListener);
        }
    }
    @Override
    protected void okPressed() {
        if (paneType == OptimizerComponentConstants.PANE_INPUT){
            setWeight(Double.parseDouble(getWeightFromUI()));
            setGoal(comboboxGoal.getSelectionIndex());
            if (getGoal() == 2){
                setSolveFor(Double.parseDouble(solveText.getText()));
            }
        }
        if (paneType == OptimizerComponentConstants.PANE_OUTPUT){
            setStartValue(Double.parseDouble(getStartFromUI()));
            setLowerBound(Double.parseDouble(getLowerBoundFromUI()));
            setUpperBound(Double.parseDouble(getUpperBoundFromUI()));
        }
        if (paneType == OptimizerComponentConstants.PANE_CONSTRAINTS){
            if (!getLowerBoundFromUI().equals("")){
                setLowerBound(Double.parseDouble(getLowerBoundFromUI()));
            } else {
                setLowerBound(Double.NEGATIVE_INFINITY);
            }
            if (!getUpperBoundFromUI().equals("")){
                setUpperBound(Double.parseDouble(getUpperBoundFromUI()));
            } else {
                setUpperBound(Double.POSITIVE_INFINITY);
            }
        }
        currentName = getNameInputFromUI();
        currentDataType = getTypeSelectionFromUI();
        callSuperOkPressed();
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getStartValue() {
        return startValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }


    public String getInitiallow() {
        return initiallow;
    }


    public void setInitiallow(String initiallow) {
        this.initiallow = initiallow;
    }


    public String getInitialup() {
        return initialup;
    }


    public void setInitialup(String initialup) {
        this.initialup = initialup;
    }


    public String getInitialstart() {
        return initialstart;
    }


    public void setInitialstart(String initialstart) {
        this.initialstart = initialstart;
    }


    public String getInitialweight() {
        return initialweight;
    }


    public void setInitialweight(String initialweight) {
        this.initialweight = initialweight;
    }


    public String getInitialgoal() {
        return initialgoal;
    }


    public void setInitialgoal(String initialgoal) {
        this.initialgoal = initialgoal;
    }

    public double getSolveFor() {
        return solveFor;
    }

    public void setSolveFor(double solveFor) {
        this.solveFor = solveFor;
    }

    public String getInitialSolve() {
        return initialSolve;
    }

    public void setInitialSolve(String initialSolve) {
        this.initialSolve = initialSolve;
    }
}
