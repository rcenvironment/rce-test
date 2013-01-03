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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
public class AlgorithmPropertiesDialog extends Dialog{

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
    private String qnMaxStep;
    private String qnCentPar;
    private String qnCentPath;
    private String qnGradTolerance;
    private String qnMeritFcn;
    private String qnSearchMet;
    private String qnStepToBound;
    private Text textfieldqnMaxStep;
    private Text textfieldqnGradTolerance;
    private Text textfieldqnStepToBound;
    private Text textfieldqnCentPar;
    private Combo comboSearchMethod;
    private Combo comboMeritFcn;
    private Combo comboCentralPath;
    private Text textfieldAppsInitDelta;
    private String appsInitDelta;
    private String appsTresDelta;
    private Text textfieldAppsThresDelta;
    private String appsSolTarget;
    private String appsConstrPenalty;
    private String appsSmooth;
    private String appsMerit;
    private Text textfieldAppsContrFactor;
    private Text textfieldAppsSolTarget;
    private Text textfieldAppsConstrPenalty;
    private Text textfieldAppsSmooth;
    private String appsContrFactor;
    private Combo comboAppsMeritFcn;
    private String appsMeritFcn;
    private Text textfieldEaPopulation;
    private String eaPopulation;
    private Combo comboEaInitType;
    private String eaInitType;
    private Combo comboEaFitnessType;
    private String eaFitnessType;
    private Combo comboeaReplacementType;
    private String eaReplacementType;
    private Text textfieldEaReplacementTypeValue;
    private String eaReplacementTypeValue;
    private Text textfieldEaNewSol;
    private String eaNewSol;
    private Combo comboeaCrossoverType;
    private String eaCrossoverType;
    private Text textfieldEaCrossRate;
    private String eaCrossRate;
    private Combo comboeaMutType;
    private String eaMutType;
    private Text textfieldEaMutScale;
    private String eaMutScale;
    private Text textfieldEaMutRange;
    private String eaMutRange;
    private Text textfieldEaMutRate;
    private String eaMutRate;
    private Text textfieldCCInitDelta;
    private Text textfieldCCThresDelta;
    private String ccInitDelta;
    private String ccThresDelta;


    protected AlgorithmPropertiesDialog(Shell parentShell, SelectionAdapter selectionAdapter, String title) {
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

        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            createQuasiNewtonControls(container);
        } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)) {
            createAsynchPatternControls(container);

        } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)) {
            createColinyEAControls(container);

        } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_MOGA)){
            new Label(container, SWT.NONE).setText(" ");
        } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            createColinyCobylaControls(container);
        }
        return container;
    }

    private void createColinyCobylaControls(Composite container) {
        textfieldCCInitDelta = createLabelAndTextfield(container, "Initial trust region", ccInitDelta);
        textfieldCCThresDelta = createLabelAndTextfield(container, "Minimal trust region stopping criteria", ccThresDelta);
    }

    private void createColinyEAControls(Composite container) {
        textfieldEaPopulation = createLabelAndTextfield(container, "Population Size", eaPopulation);
        new Label(container, SWT.NONE).setText("Initialization type");

        comboEaInitType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboEaInitType.add("simple_random");
        comboEaInitType.add("unique_random");
        for (int i = 0; i < comboEaInitType.getItemCount(); i++){
            if (comboEaInitType.getItem(i).equals(eaInitType)){
                comboEaInitType.select(i);
            }
        }
        comboEaInitType.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboEaInitType.getItem(comboEaInitType.getSelectionIndex());
                eaInitType = key;
            }
        });

        new Label(container, SWT.NONE).setText("Fitness type");

        comboEaFitnessType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboEaFitnessType.add("linear_rank");
        comboEaFitnessType.add("merit_function");
        for (int i = 0; i < comboEaFitnessType.getItemCount(); i++){
            if (comboEaFitnessType.getItem(i).equals(eaFitnessType)){
                comboEaFitnessType.select(i);
            }
        }
        comboEaFitnessType.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboEaFitnessType.getItem(comboEaFitnessType.getSelectionIndex());
                eaFitnessType = key;
            }
        });

        new Label(container, SWT.NONE).setText("Replacement type");

        comboeaReplacementType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboeaReplacementType.add("random");
        comboeaReplacementType.add("chc");
        comboeaReplacementType.add("elitist");
        for (int i = 0; i < comboeaReplacementType.getItemCount(); i++){
            if (comboeaReplacementType.getItem(i).equals(eaReplacementType)){
                comboeaReplacementType.select(i);
            }
        }
        comboeaReplacementType.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboeaReplacementType.getItem(comboeaReplacementType.getSelectionIndex());
                eaReplacementType = key;
            }
        });

        textfieldEaReplacementTypeValue = createLabelAndTextfield(container, "Replacement type value", eaReplacementTypeValue);
        textfieldEaNewSol = createLabelAndTextfield(container, "New solutions generated", eaNewSol);


        new Label(container, SWT.NONE).setText("Crossover type");

        comboeaCrossoverType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboeaCrossoverType.add("two_point");
        comboeaCrossoverType.add("blend");
        comboeaCrossoverType.add("uniform");
        for (int i = 0; i < comboeaCrossoverType.getItemCount(); i++){
            if (comboeaCrossoverType.getItem(i).equals(eaCrossoverType)){
                comboeaCrossoverType.select(i);
            }
        }
        comboeaCrossoverType.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboeaCrossoverType.getItem(comboeaCrossoverType.getSelectionIndex());
                eaCrossoverType = key;
            }
        });
        textfieldEaCrossRate = createLabelAndTextfield(container, "Crossover Rate", eaCrossRate);

        new Label(container, SWT.NONE).setText("Mutation type");

        comboeaMutType = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboeaMutType.add("replace_uniform");
        comboeaMutType.add("offset_normal");
        comboeaMutType.add("offset_cauchy");
        comboeaMutType.add("offset_uniform");
        for (int i = 0; i < comboeaMutType.getItemCount(); i++){
            if (comboeaMutType.getItem(i).equals(eaMutType)){
                comboeaMutType.select(i);
            }
        }
        comboeaMutType.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboeaMutType.getItem(comboeaMutType.getSelectionIndex());
                eaMutType = key;
            }
        });

        textfieldEaMutScale = createLabelAndTextfield(container, "Mutation scale", eaMutScale);

        textfieldEaMutRange = createLabelAndTextfield(container, "Mutation range", eaMutRange);
        //        textfieldEaMutRatio = createLabelAndTextfield(container, "Mutation ratio", eaMutRatio);  
        textfieldEaMutRate = createLabelAndTextfield(container, "Mutation rate", eaMutRate);



    }

    private void createAsynchPatternControls(Composite container) {
        textfieldAppsInitDelta = createLabelAndTextfield(container, "Initial delta", appsInitDelta);
        textfieldAppsThresDelta = createLabelAndTextfield(container, "Treshold delta", appsTresDelta);
        textfieldAppsContrFactor = createLabelAndTextfield(container, "Contractions factor", appsContrFactor);
        textfieldAppsSolTarget = createLabelAndTextfield(container, "Solution target", appsSolTarget);
        textfieldAppsConstrPenalty = createLabelAndTextfield(container, "Contraint penalty", appsConstrPenalty);
        textfieldAppsSmooth = createLabelAndTextfield(container, "Smoothing factor", appsSmooth);
        new Label(container, SWT.NONE).setText("Merit function");

        comboAppsMeritFcn = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboAppsMeritFcn.add("merit_max");
        comboAppsMeritFcn.add("merit_max_smooth");
        comboAppsMeritFcn.add("merit1");
        comboAppsMeritFcn.add("merit1_smooth");
        comboAppsMeritFcn.add("merit2");
        comboAppsMeritFcn.add("merit2_smooth");
        comboAppsMeritFcn.add("merit2_squared");
        for (int i = 0; i < comboAppsMeritFcn.getItemCount(); i++){
            if (comboAppsMeritFcn.getItem(i).equals(appsMeritFcn)){
                comboAppsMeritFcn.select(i);
            }
        }
        comboAppsMeritFcn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final String key = comboAppsMeritFcn.getItem(comboAppsMeritFcn.getSelectionIndex());
                appsMeritFcn = key;
            }
        });

    }

    private void createQuasiNewtonControls(Composite container) {
        textfieldqnMaxStep = createLabelAndTextfield(container, "Maximum step size", qnMaxStep);
        textfieldqnGradTolerance = createLabelAndTextfield(container, "Gradient tolerance", qnGradTolerance);

        new Label(container, SWT.NONE).setText("Search method");

        comboSearchMethod = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboSearchMethod.add("trust_region");

        comboSearchMethod.add("gradient_based_line_search");
        comboSearchMethod.add("value_based_line_search");
        for (int i = 0; i < comboSearchMethod.getItemCount(); i++){
            if (comboSearchMethod.getItem(i).equals(qnSearchMet)){
                comboSearchMethod.select(i);
            }
        }
        comboSearchMethod.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final String key = comboSearchMethod.getItem(comboSearchMethod.getSelectionIndex());
                qnSearchMet = key;
            }
        });

        textfieldqnStepToBound = createLabelAndTextfield(container, "Steplength to boundary", qnStepToBound);
        new Label(container, SWT.NONE).setText("Merit function");

        comboMeritFcn = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboMeritFcn.add("el_bakry");
        comboMeritFcn.add("argaez_tapia");
        comboMeritFcn.add("van_shanno");
        for (int i = 0; i < comboMeritFcn.getItemCount(); i++){
            if (comboMeritFcn.getItem(i).equals(qnMeritFcn)){
                comboMeritFcn.select(i);
            }
        }
        comboMeritFcn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final String key = comboMeritFcn.getItem(comboMeritFcn.getSelectionIndex());
                qnMeritFcn = key;
            }
        });
        new Label(container, SWT.NONE).setText("Central path");

        comboCentralPath = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        comboCentralPath.add("el_bakry");
        comboCentralPath.add("argaez_tapia");
        comboCentralPath.add("van_shanno");
        for (int i = 0; i < comboCentralPath.getItemCount(); i++){
            if (comboCentralPath.getItem(i).equals(qnCentPath)){
                comboCentralPath.select(i);
            }
        }
        comboCentralPath.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final String key = comboCentralPath.getItem(comboCentralPath.getSelectionIndex());
                qnCentPath = key;
            }
        });
        textfieldqnCentPar = createLabelAndTextfield(container, "Centering parameter", qnCentPar);

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
        // dialog title
        getShell().setText(title);
        // initial validation
        validateInput();
        // set listeners here so the ok button is initialized
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
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            textfieldqnCentPar.addModifyListener(modifyListener);
            textfieldqnGradTolerance.addModifyListener(modifyListener);
            textfieldqnMaxStep.addModifyListener(modifyListener);
            textfieldqnStepToBound.addModifyListener(modifyListener);
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
            textfieldAppsConstrPenalty.addModifyListener(modifyListener);
            textfieldAppsContrFactor.addModifyListener(modifyListener);
            textfieldAppsInitDelta.addModifyListener(modifyListener);
            textfieldAppsSmooth.addModifyListener(modifyListener);
            textfieldAppsSolTarget.addModifyListener(modifyListener);
            textfieldAppsThresDelta.addModifyListener(modifyListener);

        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
            textfieldEaCrossRate.addModifyListener(modifyListener);
            textfieldEaMutRange.addModifyListener(modifyListener);
            textfieldEaMutRate.addModifyListener(modifyListener);
            //            textfieldEaMutRatio.addModifyListener(modifyListener);
            textfieldEaMutScale.addModifyListener(modifyListener);
            textfieldEaNewSol.addModifyListener(modifyListener);
            textfieldEaPopulation.addModifyListener(modifyListener);
            textfieldEaReplacementTypeValue.addModifyListener(modifyListener);
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            textfieldCCInitDelta.addModifyListener(modifyListener);
            textfieldCCThresDelta.addModifyListener(modifyListener);
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

        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            if (dakotaProperties.qnMaxStep == null){
                this.qnMaxStep = DakotaMethodConstants.QN_MAX_STEPSIZE_DEF;
            } else { 
                this.qnMaxStep = (String) dakotaProperties.qnMaxStep;
            }
            if (dakotaProperties.qnCentPar == null){
                this.qnCentPar = DakotaMethodConstants.QN_CENTERING_PARAMETER_DEF;
            } else { 
                this.qnCentPar = (String) dakotaProperties.qnCentPar;
            }
            if (dakotaProperties.qnCentPath == null){
                this.qnCentPath = DakotaMethodConstants.QN_CENTRAL_PATH_DEF;
            } else { 
                this.qnCentPath = (String) dakotaProperties.qnCentPath;
            }
            if (dakotaProperties.qnGradTolerance == null){
                this.qnGradTolerance = DakotaMethodConstants.QN_GRAD_TOLERANCE_DEF;
            } else { 
                this.qnGradTolerance = (String) dakotaProperties.qnGradTolerance;
            }
            if (dakotaProperties.qnMeritFcn == null){
                this.qnMeritFcn = DakotaMethodConstants.QN_MERIT_FCN_DEF;
            } else { 
                this.qnMeritFcn = (String) dakotaProperties.qnMeritFcn;
            }
            if (dakotaProperties.qnSearchMet == null){
                this.qnSearchMet = DakotaMethodConstants.QN_SEARCH_METHOD_DEF;
            } else { 
                this.qnSearchMet = (String) dakotaProperties.qnSearchMet;
            }
            if (dakotaProperties.qnStepToBound == null){
                this.qnStepToBound = DakotaMethodConstants.QN_STEP_TO_BOUND_DEF;
            } else { 
                this.qnStepToBound = (String) dakotaProperties.qnStepToBound;
            }
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
            if (dakotaProperties.appsConstrPenalty == null){
                this.appsConstrPenalty = DakotaMethodConstants.APPS_CONST_PENALTY_DEF;
            } else { 
                this.appsConstrPenalty = (String) dakotaProperties.appsConstrPenalty;
            }
            if (dakotaProperties.appsContrFactor == null){
                this.appsContrFactor = DakotaMethodConstants.APPS_CONTR_FACTOR_DEF;
            } else { 
                this.appsContrFactor = (String) dakotaProperties.appsContrFactor;
            }
            if (dakotaProperties.appsInitDelta == null){
                this.appsInitDelta = DakotaMethodConstants.APPS_INIT_DELTA_DEF;
            } else { 
                this.appsInitDelta = (String) dakotaProperties.appsInitDelta;
            }
            if (dakotaProperties.appsSmooth == null){
                this.appsSmooth = DakotaMethodConstants.APPS_SMOOTH_DEF;
            } else { 
                this.appsSmooth = (String) dakotaProperties.appsSmooth;
            }
            if (dakotaProperties.appsSolTarget == null){
                this.appsSolTarget = DakotaMethodConstants.APPS_SOL_TARGET_DEF;
            } else { 
                this.appsSolTarget = (String) dakotaProperties.appsSolTarget;
            }
            if (dakotaProperties.appsTresDelta == null){
                this.appsTresDelta = DakotaMethodConstants.APPS_TRESDELTA_DEF;
            } else { 
                this.appsTresDelta = (String) dakotaProperties.appsTresDelta;
            }
            if (dakotaProperties.appsMeritFcn == null){
                this.appsMeritFcn = DakotaMethodConstants.APPS_MERIT_DEF;
            } else { 
                this.appsMeritFcn = (String) dakotaProperties.appsMeritFcn;
            }
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
            initColinyEA(dakotaProperties);

        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            if (dakotaProperties.ccInitDelta == null){
                this.ccInitDelta = DakotaMethodConstants.CC_INIT_DELTA_DEF;
            } else { 
                this.ccInitDelta = (String) dakotaProperties.ccInitDelta;
            }
            if (dakotaProperties.ccThresDelta == null){
                this.ccThresDelta = DakotaMethodConstants.CC_THRES_DELTA_DEF;
            } else { 
                this.ccThresDelta = (String) dakotaProperties.ccThresDelta;
            }

        }
    }
    private void initColinyEA(DakotaProperties dakotaProperties) {
        if (dakotaProperties.eaCrossoverType == null){
            this.eaCrossoverType = DakotaMethodConstants.EA_CROSSOVER_TYPE_DEF;
        } else { 
            this.eaCrossoverType = (String) dakotaProperties.eaCrossoverType;
        }
        if (dakotaProperties.eaCrossRate == null){
            this.eaCrossRate = DakotaMethodConstants.EA_CROSSOVER_RATE_DEF;
        } else { 
            this.eaCrossRate = (String) dakotaProperties.eaCrossRate;
        }
        if (dakotaProperties.eaFitnessType == null){
            this.eaFitnessType = DakotaMethodConstants.EA_FITNESS_TYPE_DEF;
        } else { 
            this.eaFitnessType = (String) dakotaProperties.eaFitnessType;
        }
        if (dakotaProperties.eaInitType == null){
            this.eaInitType = DakotaMethodConstants.EA_INIT_TYPE_DEF;
        } else { 
            this.eaInitType = (String) dakotaProperties.eaInitType;
        }
        if (dakotaProperties.eaMutRange == null){
            this.eaMutRange = DakotaMethodConstants.EA_MUT_RANGE_DEF;
        } else { 
            this.eaMutRange = (String) dakotaProperties.eaMutRange;
        }
        if (dakotaProperties.eaMutRate == null){
            this.eaMutRate = DakotaMethodConstants.EA_MUT_RATE_DEF;
        } else { 
            this.eaMutRate = (String) dakotaProperties.eaMutRate;
        }
        //        if (dakotaProperties.eaMutRatio == null){
        //            this.eaMutRatio = DakotaMethodConstants.EA_MUT_RATIO_DEF;
        //        } else { 
        //            this.eaMutRatio = (String) dakotaProperties.eaMutRatio;
        //        }
        if (dakotaProperties.eaMutScale == null){
            this.eaMutScale = DakotaMethodConstants.EA_MUT_SCALE_DEF;
        } else { 
            this.eaMutScale = (String) dakotaProperties.eaMutScale;
        }
        if (dakotaProperties.eaMutType == null){
            this.eaMutType = DakotaMethodConstants.EA_MUT_TYPE_DEF;
        } else { 
            this.eaMutType = (String) dakotaProperties.eaMutType;
        }
        if (dakotaProperties.eaNewSol == null){
            this.eaNewSol = DakotaMethodConstants.EA_NEW_SOL_DEF;
        } else { 
            this.eaNewSol = (String) dakotaProperties.eaNewSol;
        }
        if (dakotaProperties.eaPopulation == null){
            this.eaPopulation = DakotaMethodConstants.EA_POPULATION_DEF;
        } else { 
            this.eaPopulation = (String) dakotaProperties.eaPopulation;
        }
        if (dakotaProperties.eaReplacementType == null){
            this.eaReplacementType = DakotaMethodConstants.EA_REPLACEMENT_TYPE_DEF;
        } else { 
            this.eaReplacementType = (String) dakotaProperties.eaReplacementType;
        }
        if (dakotaProperties.eaReplacementTypeValue == null){
            this.eaReplacementTypeValue = DakotaMethodConstants.EA_REPLACEMENT_TYPE_VALUE_DEF;
        } else { 
            this.eaReplacementTypeValue = (String) dakotaProperties.eaReplacementTypeValue;
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

        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            isValid = isValid && testQuasiNewtonProps();
        }

        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
            isValid = isValid && testAppsProps();
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
            isValid = isValid && testEAProps();
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            isValid = isValid && testCCProps();
        }
        getButton(IDialogConstants.OK_ID).setEnabled(isValid);

    }

    private boolean testCCProps() {
        boolean isValid = true;

        try {
            Double.parseDouble(textfieldCCInitDelta.getText());
            Double.parseDouble(textfieldCCThresDelta.getText());
           
        } catch (NumberFormatException e){
            isValid = false;
        }

        return isValid;
    }

    private boolean testEAProps() {
        boolean isValid = true;

        try {
            Double.parseDouble(textfieldEaCrossRate.getText());
            Integer.parseInt(textfieldEaMutRange.getText());
            Double.parseDouble(textfieldEaMutRate.getText());
            //            Double.parseDouble(textfieldEaMutRatio.getText());
            Double.parseDouble(textfieldEaMutScale.getText());
            Integer.parseInt(textfieldEaNewSol.getText());
            Integer.parseInt(textfieldEaPopulation.getText());
            Integer.parseInt(textfieldEaReplacementTypeValue.getText());
        } catch (NumberFormatException e){
            isValid = false;
        }

        return isValid;
    }

    private boolean testAppsProps() {
        boolean isValid = true;

        try {
            double its = Double.parseDouble(textfieldAppsConstrPenalty.getText());
            if (its < 0){
                isValid = false;
            }

            its = Double.parseDouble(textfieldAppsContrFactor.getText());
            if (its <= 0  || its >= 1){
                isValid = false;
            }
            its = Double.parseDouble(textfieldAppsInitDelta.getText());
            if (its <= 0){
                isValid = false;
            }
            its = Double.parseDouble(textfieldAppsSmooth.getText());
            if (its < 0 || its > 1){
                isValid = false;
            }

            if (!textfieldAppsSolTarget.getText().equals("")){
                Double.parseDouble(textfieldAppsSolTarget.getText());
            }

            Double.parseDouble(textfieldAppsThresDelta.getText());

        } catch (NumberFormatException e){
            isValid = false;
        }

        return isValid;
    }

    private boolean testQuasiNewtonProps() {
        boolean isvalid = true;
        try {
            double its = Double.parseDouble(textfieldqnGradTolerance.getText());
            if (its < 0){
                isvalid = false;
            }

            its = Double.parseDouble(textfieldqnCentPar.getText());
            if (its < 0){
                isvalid = false;
            }

            its = Double.parseDouble(textfieldqnMaxStep.getText());
            if (its < 0){
                isvalid = false;
            }
            its = Double.parseDouble(textfieldqnStepToBound.getText());
            if (its < 0){
                isvalid = false;
            }
        } catch (NumberFormatException e){
            isvalid = false;
        }

        return isvalid;
    }

    @Override
    protected void okPressed() {
        this.tolerance = textfieldTolerance.getText();
        this.constTol = textfieldConstTol.getText();
        this.iterations = textfieldIterations.getText();
        this.funcEval = textfieldFuncEval.getText();
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            qnCentPar = textfieldqnCentPar.getText();
            qnGradTolerance = textfieldqnGradTolerance.getText();
            qnMaxStep = textfieldqnMaxStep.getText();
            qnStepToBound = textfieldqnStepToBound.getText();
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
            appsConstrPenalty = textfieldAppsConstrPenalty.getText();
            appsContrFactor = textfieldAppsContrFactor.getText();
            appsInitDelta = textfieldAppsInitDelta.getText();
            appsSmooth = textfieldAppsSmooth.getText();
            appsSolTarget = textfieldAppsSolTarget.getText();
            appsTresDelta = textfieldAppsThresDelta.getText();
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
            eaCrossRate = textfieldEaCrossRate.getText();
            eaMutRange = textfieldEaMutRange.getText();
            eaMutRate = textfieldEaMutRate.getText();
            //            eaMutRatio = textfieldEaMutRatio.getText();
            eaMutScale = textfieldEaMutScale.getText();
            eaNewSol = textfieldEaNewSol.getText();
            eaPopulation = textfieldEaPopulation.getText();
            eaReplacementTypeValue = textfieldEaReplacementTypeValue.getText();

        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            ccInitDelta = textfieldCCInitDelta.getText();
            ccThresDelta = textfieldCCThresDelta.getText();
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
    public String getQnMaxStep() {
        return qnMaxStep;
    }


    public void setQnMaxStep(String qnMaxStep) {
        this.qnMaxStep = qnMaxStep;
    }


    public String getQnCentPar() {
        return qnCentPar;
    }


    public void setQnCentPar(String qnCentPar) {
        this.qnCentPar = qnCentPar;
    }


    public String getQnCentPath() {
        return qnCentPath;
    }


    public void setQnCentPath(String qnCentPath) {
        this.qnCentPath = qnCentPath;
    }


    public String getQnGradTolerance() {
        return qnGradTolerance;
    }


    public void setQnGradTolerance(String qnGradTolerance) {
        this.qnGradTolerance = qnGradTolerance;
    }


    public String getQnMeritFcn() {
        return qnMeritFcn;
    }


    public void setQnMeritFcn(String qnMeritFcn) {
        this.qnMeritFcn = qnMeritFcn;
    }


    public String getQnSearchMet() {
        return qnSearchMet;
    }


    public void setQnSearchMet(String qnSearchMet) {
        this.qnSearchMet = qnSearchMet;
    }


    public String getQnStepToBound() {
        return qnStepToBound;
    }


    public void setQnStepToBound(String qnStepToBound) {
        this.qnStepToBound = qnStepToBound;
    }


    public String getAppsInitDelta() {
        return appsInitDelta;
    }


    public void setAppsInitDelta(String appsInitDelta) {
        this.appsInitDelta = appsInitDelta;
    }


    public String getAppsTresDelta() {
        return appsTresDelta;
    }


    public void setAppsTresDelta(String appsTresDelta) {
        this.appsTresDelta = appsTresDelta;
    }


    public String getAppsSolTarget() {
        return appsSolTarget;
    }


    public void setAppsSolTarget(String appsSolTarget) {
        this.appsSolTarget = appsSolTarget;
    }


    public String getAppsConstrPenalty() {
        return appsConstrPenalty;
    }


    public void setAppsConstrPenalty(String appsConstrPenalty) {
        this.appsConstrPenalty = appsConstrPenalty;
    }


    public String getAppsSmooth() {
        return appsSmooth;
    }


    public void setAppsSmooth(String appsSmooth) {
        this.appsSmooth = appsSmooth;
    }


    public String getAppsMerit() {
        return appsMerit;
    }


    public void setAppsMerit(String appsMerit) {
        this.appsMerit = appsMerit;
    }


    public String getAppsContrFactor() {
        return appsContrFactor;
    }


    public void setAppsContrFactor(String appsContrFactor) {
        this.appsContrFactor = appsContrFactor;
    }


    public String getAppsMeritFcn() {
        return appsMeritFcn;
    }


    public void setAppsMeritFcn(String appsMeritFcn) {
        this.appsMeritFcn = appsMeritFcn;
    }


    public String getEaPopulation() {
        return eaPopulation;
    }


    public void setEaPopulation(String eaPopulation) {
        this.eaPopulation = eaPopulation;
    }


    public String getEaInitType() {
        return eaInitType;
    }


    public void setEaInitType(String eaInitType) {
        this.eaInitType = eaInitType;
    }


    public String getEaFitnessType() {
        return eaFitnessType;
    }


    public void setEaFitnessType(String eaFitnessType) {
        this.eaFitnessType = eaFitnessType;
    }


    public String getEaReplacementType() {
        return eaReplacementType;
    }


    public void setEaReplacementType(String eaReplacementType) {
        this.eaReplacementType = eaReplacementType;
    }


    public String getEaReplacementTypeValue() {
        return eaReplacementTypeValue;
    }


    public void setEaReplacementTypeValue(String eaReplacementTypeValue) {
        this.eaReplacementTypeValue = eaReplacementTypeValue;
    }


    public String getEaNewSol() {
        return eaNewSol;
    }


    public void setEaNewSol(String eaNewSol) {
        this.eaNewSol = eaNewSol;
    }


    public String getEaCrossoverType() {
        return eaCrossoverType;
    }


    public void setEaCrossoverType(String eaCrossoverType) {
        this.eaCrossoverType = eaCrossoverType;
    }


    public String getEaCrossRate() {
        return eaCrossRate;
    }


    public void setEaCrossRate(String eaCrossRate) {
        this.eaCrossRate = eaCrossRate;
    }


    public String getEaMutType() {
        return eaMutType;
    }


    public void setEaMutType(String eaMutType) {
        this.eaMutType = eaMutType;
    }


    public String getEaMutScale() {
        return eaMutScale;
    }


    public void setEaMutScale(String eaMutScale) {
        this.eaMutScale = eaMutScale;
    }


    public String getEaMutRange() {
        return eaMutRange;
    }


    public void setEaMutRange(String eaMutRange) {
        this.eaMutRange = eaMutRange;
    }


    //    public String getEaMutRatio() {
    //        return eaMutRatio;
    //    }
    //
    //
    //    public void setEaMutRatio(String eaMutRatio) {
    //        this.eaMutRatio = eaMutRatio;
    //    }


    public String getEaMutRate() {
        return eaMutRate;
    }


    public void setEaMutRate(String eaMutRate) {
        this.eaMutRate = eaMutRate;
    }

    
    public String getCcInitDelta() {
        return ccInitDelta;
    }

    
    public void setCcInitDelta(String ccInitDelta) {
        this.ccInitDelta = ccInitDelta;
    }

    
    public String getCcThresDelta() {
        return ccThresDelta;
    }

    
    public void setCcThresDelta(String ccThresDelta) {
        this.ccThresDelta = ccThresDelta;
    }
}
