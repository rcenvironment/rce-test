/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import java.io.Serializable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.rcenvironment.rce.components.optimizer.commons.DakotaMethodConstants;
import de.rcenvironment.rce.components.optimizer.commons.DakotaProperties;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;

/**
 * Dialog for configuring the selected algorithmn.
 * 
 * @author Sascha Zur
 */
public class DOEAlgorithmPropertiesDialog extends Dialog{

    private String title;
    private Text textfieldTolerance;
    private Text textfieldIterations;
    private String tolerance;
    private String iterations;
    private String algorithm;
    private Text textfieldConstTol;
    private String constTol;
    private String funcEval;
    private Text textfieldFuncEval;
    private Text textfieldlhsSeed;
    private Button lhsFixedSeed;
    private Text lhsSamplesText;
    private Button lhsMainEffecs;
    private Button lhsQualityMetrics;
    private Button lhsVarianceBasedDecomp;
    private Text textfieldLhsSymbols;
    private int doeLHSSeed;
    private int doeLHSSSamples;
    private int doeLHSSymbols;
    private boolean lhsFixedSeedVal;
    private boolean lhsMainEffecsVal;
    private boolean lhsQualityMetricsVal;
    private boolean lhsVarianceBasedDecompVal;

 

    

    protected DOEAlgorithmPropertiesDialog(Shell parentShell, SelectionAdapter selectionAdapter, String title) {
        super(parentShell);
        this.title = title;
    }

    protected Control createDialogArea(Composite parent) {

        Composite container = (Composite) super.createDialogArea(parent);

        container.setLayout(new GridLayout(2, false));

        textfieldTolerance = createLabelAndTextfield(container, "Convergence tolerance", tolerance);
        textfieldConstTol = createLabelAndTextfield(container, "Constraint tolerance", constTol);
        textfieldIterations = createLabelAndTextfield(container, "Max. iterations", iterations);
        textfieldFuncEval = createLabelAndTextfield(container, "Max. function evaluation", funcEval);

        new Label(container, SWT.NONE).setText("\n\nAlgorithm specified options\n\n");
        new Label(container, SWT.NONE).setText(" ");

        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            createLHSControls(container);
        } 
        return container;
    }


    private void createLHSControls(Composite container) {
        textfieldlhsSeed = createLabelAndTextfield(container, "Random Seed", "" + doeLHSSeed);
        new Label(container, SWT.NONE).setText("Fixed Seed");
        lhsFixedSeed = new Button(container, SWT.CHECK);
        lhsFixedSeed.setSelection(lhsFixedSeedVal);
        lhsSamplesText = createLabelAndTextfield(container, "Samples", "" + doeLHSSSamples);
         
        textfieldLhsSymbols = createLabelAndTextfield(container, "Symbols", "" + doeLHSSymbols);
        new Label(container, SWT.NONE).setText("Main effects");
        lhsMainEffecs = new Button(container, SWT.CHECK);
        lhsMainEffecs.setSelection(lhsMainEffecsVal);
        new Label(container, SWT.NONE).setText("Quality Metrics");
        lhsQualityMetrics = new Button(container, SWT.CHECK);
        lhsQualityMetrics.setSelection(lhsQualityMetricsVal);
        new Label(container, SWT.NONE).setText("Variance Based Decomposition");
        lhsVarianceBasedDecomp = new Button(container, SWT.CHECK);
        lhsVarianceBasedDecomp.setSelection(lhsVarianceBasedDecompVal);
    }

    private Text createLabelAndTextfield(Composite container, String text, String value){
        new Label(container, SWT.NONE).setText(text);
        Text result = new Text(container, SWT.SINGLE | SWT.BORDER);
        result.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        result.setText(value);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        super.create();
        getShell().setText(title);
        validateInput();
        installModifyListeners();
    }

    private void installModifyListeners() {
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        };
        textfieldTolerance.addModifyListener(modifyListener);
        textfieldConstTol.addModifyListener(modifyListener);
        textfieldIterations.addModifyListener(modifyListener);
        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            textfieldlhsSeed.addModifyListener(modifyListener);
            textfieldLhsSymbols.addModifyListener(modifyListener);
            lhsSamplesText.addModifyListener(modifyListener);
        }
    }

    /**
     * Sets all values for the selected algorithm.
     * 
     * @param toleranceIn : Convergence tolerance
     * @param constToleranceIn : Constraint tolerance
     * @param iterationsIn : Maximum iterations
     * @param funcEvalIn : Function Evaluation
     * @param alg : Currently selected algorithm
     * @param dakotaProperties : Properties for dakota
     */
    public void setInitialValues(Serializable toleranceIn, 
        Serializable constToleranceIn, Serializable iterationsIn, Serializable funcEvalIn, String alg, DakotaProperties dakotaProperties){
        if (toleranceIn == null){
            this.tolerance = OptimizerComponentConstants.TOLERANCE_DEFAULT;
        } else {
            this.tolerance = (String) toleranceIn;
        }
        if (constToleranceIn == null){
            this.constTol = OptimizerComponentConstants.CONSTTOLERANCE_DEFAULT;
        } else {
            this.constTol = (String) constToleranceIn;
        }

        if (iterationsIn == null){
            this.iterations = OptimizerComponentConstants.ITERATIONS_DEFAULT;
        } else { 
            this.iterations = (String) iterationsIn;
        }
        if (funcEvalIn == null){
            this.funcEval = OptimizerComponentConstants.MAXFUNC_DEFAULT;
        } else { 
            this.funcEval = (String) funcEvalIn;
        }
        this.algorithm = alg;

        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            if (dakotaProperties.doeLHSSeed == null){
                this.doeLHSSeed = (DakotaMethodConstants.DOE_LHS_SEED_DEF);
            } else { 
                this.doeLHSSeed = ((Integer) dakotaProperties.doeLHSSeed);
            }
            if (dakotaProperties.doeLHSFixedSeed == null){
                this.lhsFixedSeedVal = DakotaMethodConstants.DOE_LHS_FIXED_SEED_DEF;
            } else { 
                this.lhsFixedSeedVal = ((Boolean) dakotaProperties.doeLHSFixedSeed);
            }
            if (dakotaProperties.doeLHSSamples == null){
                this.doeLHSSSamples = (DakotaMethodConstants.DOE_LHS_SAMPLES_DEF);
            } else { 
                this.doeLHSSSamples = ((Integer) dakotaProperties.doeLHSSamples);
            }
            if (dakotaProperties.doeLHSSymbols == null){
                this.doeLHSSymbols = (DakotaMethodConstants.DOE_LHS_SYMBOLS_DEF);
            } else { 
                this.doeLHSSymbols = ((Integer) dakotaProperties.doeLHSSamples);
            }
            if (dakotaProperties.doeLHSMainEffects == null){
                this.lhsMainEffecsVal = (DakotaMethodConstants.DOE_LHS_MAIN_EFFECTS_DEF);
            } else { 
                this.lhsMainEffecsVal = ((Boolean) dakotaProperties.doeLHSMainEffects);
            }
            if (dakotaProperties.doeLHSQualityMetrics == null){
                this.lhsQualityMetricsVal = (DakotaMethodConstants.DOE_LHS_QUALISTY_METRICS_DEF);
            } else { 
                this.lhsQualityMetricsVal = ((Boolean) dakotaProperties.doeLHSQualityMetrics);
            }
            if (dakotaProperties.doeLHSVarianceBasedDecomp == null){
                this.lhsVarianceBasedDecompVal = (DakotaMethodConstants.DOE_LHS_VARIANCE_BASED_DECOMP_DEF);
            } else { 
                this.lhsVarianceBasedDecompVal = ((Boolean) dakotaProperties.doeLHSVarianceBasedDecomp);
            }
        }
      
    }
    
    private void validateInput(){
        boolean isValid = true;

        try {
            Double.parseDouble(textfieldTolerance.getText());

            Double.parseDouble(textfieldConstTol.getText());

            int its = Integer.parseInt(textfieldIterations.getText());
            if (its < 0){
                isValid = false;
            }

            its = Integer.parseInt(textfieldFuncEval.getText());
            if (its < 0){
                isValid = false;
            }
        } catch (NumberFormatException e){
            isValid = false;
        }

        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            isValid = isValid && testLHSProps();
        }
        getButton(IDialogConstants.OK_ID).setEnabled(isValid);

    }

   

    private boolean testLHSProps() {
        boolean isValid = true;

        try {
            Integer.parseInt(textfieldlhsSeed.getText());
            Integer.parseInt(textfieldLhsSymbols.getText());
            Integer.parseInt(lhsSamplesText.getText());
        } catch (NumberFormatException e){
            isValid = false;
        }

        return isValid;
    }


    @Override
    protected void okPressed() {
        this.tolerance = textfieldTolerance.getText();
        this.constTol = textfieldConstTol.getText();
        this.iterations = textfieldIterations.getText();
        this.funcEval = textfieldFuncEval.getText();
        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            setDoeLHSSeed(Integer.parseInt(textfieldlhsSeed.getText()));
            setDoeLHSSSamples(Integer.parseInt(lhsSamplesText.getText()));
            setDoeLHSSymbols(Integer.parseInt(textfieldLhsSymbols.getText()));
            lhsFixedSeedVal = lhsFixedSeed.getSelection();
            lhsMainEffecsVal = lhsMainEffecs.getSelection();
            lhsQualityMetricsVal = lhsQualityMetrics.getSelection();
            lhsVarianceBasedDecompVal = lhsVarianceBasedDecomp.getSelection();
        }
        super.okPressed();
    }

    public Serializable getIterations() {
        return iterations;
    }

    public Serializable getTolerance() {
        return tolerance;
    }


    public String getConstTol() {
        return constTol;
    }


    public void setConstTol(String constTol) {
        this.constTol = constTol;
    }


    public String getFuncEval() {
        return funcEval;
    }


    public void setFuncEval(String funcEval) {
        this.funcEval = funcEval;
    }

    public int getDoeLHSSeed() {
        return doeLHSSeed;
    }


    public void setDoeLHSSeed(int doeLHSSeed) {
        this.doeLHSSeed = doeLHSSeed;
    }

 
    public int getDoeLHSSSamples() {
        return doeLHSSSamples;
    }

  
    public void setDoeLHSSSamples(int doeLHSSSamples) {
        this.doeLHSSSamples = doeLHSSSamples;
    }

 
    public int getDoeLHSSymbols() {
        return doeLHSSymbols;
    }

 
    public void setDoeLHSSymbols(int doeLHSSymbols) {
        this.doeLHSSymbols = doeLHSSymbols;
    }

    public boolean getLHSFixedSeed() {
        return lhsFixedSeedVal;
    }

    public Serializable getLHSMainEffects() {
        return lhsMainEffecsVal;
    }

    public Serializable getLHSQualityMetrics() {
        return lhsQualityMetricsVal;
    }

    public Serializable getLHSVarianceBasedDecomp() {
        return lhsVarianceBasedDecompVal;
    }
}
