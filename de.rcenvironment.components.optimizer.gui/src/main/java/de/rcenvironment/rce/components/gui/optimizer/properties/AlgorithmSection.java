/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.optimizer.properties;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.components.optimizer.commons.DakotaMethodConstants;
import de.rcenvironment.rce.components.optimizer.commons.DakotaProperties;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;
/**
 * "Properties" view tab for choosing Algorithm.
 *
 * @author Sascha Zur
 */
public class AlgorithmSection extends ValidatingWorkflowNodePropertySection {

    private static final int WHITE = 255;
    /**
     * Selections.
     */
    public Combo comboAlgorithmSelection;

    private List<String> algorithms;

    private Button buttonProperties;

    private Text dakotaBinaryPath;

    private Button buttonPathselector;

    public AlgorithmSection() {
        algorithms = new LinkedList<String>();
        algorithms.add(OptimizerComponentConstants.ALGORITHM_QUASINEWTON);
        algorithms.add(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH);
        algorithms.add(OptimizerComponentConstants.ALGORITHM_COLINY_EA);
        algorithms.add(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA);
        //        algorithms.add(OptimizerComponentConstants.ALGORITHM_MOGA);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        final Section sectionAlgorithm = PropertyTabGuiHelper.createSingleColumnSectionComposite(parent, getWidgetFactory(),
            Messages.algorithm);
        final Composite algorithmConfigurationParent = getWidgetFactory().createComposite(sectionAlgorithm);
        sectionAlgorithm.setClient(algorithmConfigurationParent);
        algorithmConfigurationParent.setLayout(new GridLayout(2, false));
        comboAlgorithmSelection = new Combo(algorithmConfigurationParent, SWT.BORDER | SWT.READ_ONLY);
        for (final String selectedAlgorithm: algorithms) {
            comboAlgorithmSelection.add(selectedAlgorithm);
        }
        comboAlgorithmSelection.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final String key = comboAlgorithmSelection.getItem(comboAlgorithmSelection.getSelectionIndex());
                setProperty(OptimizerComponentConstants.ALGORITHM, key);
            }
        });
        
        comboAlgorithmSelection.setText(OptimizerComponentConstants.ALGORITHM_QUASINEWTON);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "de.rcenvironment.rce.components.gui.optimizer.OptimizerSection");
        buttonProperties = new Button(algorithmConfigurationParent , SWT.PUSH);
        buttonProperties.setText(Messages.algorithmProperties);
        SelectionAdapter buttonListener = new DakotaSelectionAdapter(parent);
        buttonProperties.addSelectionListener(buttonListener);
        new Label(algorithmConfigurationParent, SWT.NONE).setText(" ");
        new Label(algorithmConfigurationParent, SWT.NONE).setText(" ");
        new Label(algorithmConfigurationParent, SWT.NONE).setText(Messages.dakotaPathOptional);
        new Label(algorithmConfigurationParent, SWT.NONE).setText(" ");

        final Composite pathSelectionComposite = getWidgetFactory().createComposite(algorithmConfigurationParent);
        pathSelectionComposite.setLayout(new GridLayout(2, false));
        dakotaBinaryPath = new Text(pathSelectionComposite, SWT.BORDER | SWT.READ_ONLY | SWT.FILL_EVEN_ODD);
        dakotaBinaryPath.setBackground(new Color(null, WHITE, WHITE, WHITE));
        dakotaBinaryPath.setText("                                                             ");

        buttonPathselector = new Button(pathSelectionComposite , SWT.PUSH);
        buttonPathselector.setText("...");
        buttonPathselector.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
                fd.setText("Select dakota binary");
                if (System.getProperty("os.name").toLowerCase().indexOf("windows") > 0 - 1){
                    String[] filterExt = { "*.exe" };
                    fd.setFilterExtensions(filterExt);
                }
                fd.setFilterPath(System.getProperty("user.dir"));
                String selected = fd.open();
                setProperty(OptimizerComponentConstants.DAKOTAPATH, selected);
                dakotaBinaryPath.setText(selected);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
    }


    @SuppressWarnings("deprecation")
    @Override
    public void refresh() {
        super.refresh();
        final String selectedAlgorithm = getProperty(OptimizerComponentConstants.ALGORITHM, String.class);
        if (selectedAlgorithm != null) {
            boolean ok = false;
            for (String str : comboAlgorithmSelection.getItems()){
                if (str.equals(selectedAlgorithm)){
                    comboAlgorithmSelection.select(comboAlgorithmSelection.indexOf(selectedAlgorithm));
                    ok = true;
                }
            }
            if (!ok) {
                comboAlgorithmSelection.select(1);                  
            }
        } 
    }
    /**
     * Selectionadapter for the dakota algorithms.
     * @author Sascha Zur
     */
    private class DakotaSelectionAdapter extends SelectionAdapter{

        private Composite parent;

        public DakotaSelectionAdapter(Composite parent){
            this.parent = parent;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == buttonProperties) {
                AlgorithmPropertiesDialog dialog = new AlgorithmPropertiesDialog(parent.getShell(), this, Messages.algorithmProperties);
                dialog.setInitialValues(getProperty(OptimizerComponentConstants.TOLERANCE), 
                    getProperty(OptimizerComponentConstants.CONSTRAINTTOLERANCE),
                    getProperty(OptimizerComponentConstants.ITERATIONS),
                    getProperty(OptimizerComponentConstants.FUNCEVAL),
                    comboAlgorithmSelection.getItem(comboAlgorithmSelection.getSelectionIndex()),
                    getMethodDependentProperties());
                if (dialog.open() == Dialog.OK){
                    setProperty(OptimizerComponentConstants.ITERATIONS, dialog.getIterations());
                    setProperty(OptimizerComponentConstants.CONSTRAINTTOLERANCE, dialog.getConstTol());
                    setProperty(OptimizerComponentConstants.FUNCEVAL, dialog.getFuncEval());
                    setProperty(OptimizerComponentConstants.TOLERANCE, dialog.getTolerance());
                    setMethodDependentOptiones(dialog);
                }
            }
        }
        private DakotaProperties getMethodDependentProperties() {
            String alg = comboAlgorithmSelection.getItem(comboAlgorithmSelection.getSelectionIndex());
            DakotaProperties result = new DakotaProperties();
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
                result.qnMaxStep = getProperty(DakotaMethodConstants.QN_MAX_STEPSIZE);
                result.qnGradTolerance = getProperty(DakotaMethodConstants.QN_GRAD_TOLERANCE);
                result.qnCentPar = getProperty(DakotaMethodConstants.QN_CENTERING_PARAMETER);
                result.qnCentPath = getProperty(DakotaMethodConstants.QN_CENTRAL_PATH);
                result.qnMeritFcn = getProperty(DakotaMethodConstants.QN_MERIT_FCN);
                result.qnSearchMet = getProperty(DakotaMethodConstants.QN_SEARCH_METHOD);
                result.qnStepToBound = getProperty(DakotaMethodConstants.QN_STEP_TO_BOUND);
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
                result.appsConstrPenalty = getProperty(DakotaMethodConstants.APPS_CONST_PENALTY);
                result.appsContrFactor = getProperty(DakotaMethodConstants.APPS_CONTR_FACTOR);
                result.appsInitDelta = getProperty(DakotaMethodConstants.APPS_INIT_DELTA);
                result.appsMeritFcn = getProperty(DakotaMethodConstants.APPS_MERIT);
                result.appsSmooth = getProperty(DakotaMethodConstants.APPS_SMOOTH);
                result.appsSolTarget = getProperty(DakotaMethodConstants.APPS_SOL_TARGET);
                result.appsTresDelta = getProperty(DakotaMethodConstants.APPS_TRESDELTA);
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
                result.eaCrossoverType = getProperty(DakotaMethodConstants.EA_CROSSOVER_TYPE);
                result.eaCrossRate = getProperty(DakotaMethodConstants.EA_CROSSOVER_RATE);
                result.eaFitnessType = getProperty(DakotaMethodConstants.EA_FITNESS_TYPE);
                result.eaInitType = getProperty(DakotaMethodConstants.EA_INIT_TYPE);
                result.eaMutRange = getProperty(DakotaMethodConstants.EA_MUT_RANGE);
                result.eaMutRatio = getProperty(DakotaMethodConstants.EA_MUT_RATIO);
                result.eaMutScale = getProperty(DakotaMethodConstants.EA_MUT_SCALE);
                result.eaMutType = getProperty(DakotaMethodConstants.EA_MUT_TYPE);
                result.eaNewSol = getProperty(DakotaMethodConstants.EA_NEW_SOL);
                result.eaPopulation = getProperty(DakotaMethodConstants.EA_POPULATION);
                result.eaReplacementType = getProperty(DakotaMethodConstants.EA_REPLACEMENT_TYPE);
                result.eaReplacementTypeValue = getProperty(DakotaMethodConstants.EA_REPLACEMENT_TYPE_VALUE);
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
                result.ccInitDelta = getProperty(DakotaMethodConstants.CC_INIT_DELTA);
                result.ccThresDelta = getProperty(DakotaMethodConstants.CC_THRES_DELTA);
            }
            return result;
        }
        private void setMethodDependentOptiones(AlgorithmPropertiesDialog dialog) {
            String alg = comboAlgorithmSelection.getItem(comboAlgorithmSelection.getSelectionIndex());

            if (alg.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
                setProperty(DakotaMethodConstants.QN_MAX_STEPSIZE, dialog.getQnMaxStep());
                setProperty(DakotaMethodConstants.QN_GRAD_TOLERANCE, dialog.getQnGradTolerance());
                setProperty(DakotaMethodConstants.QN_CENTERING_PARAMETER, dialog.getQnCentPar());
                setProperty(DakotaMethodConstants.QN_CENTRAL_PATH, dialog.getQnCentPath());
                setProperty(DakotaMethodConstants.QN_MERIT_FCN, dialog.getQnMeritFcn());
                setProperty(DakotaMethodConstants.QN_SEARCH_METHOD, dialog.getQnSearchMet());
                setProperty(DakotaMethodConstants.QN_STEP_TO_BOUND, dialog.getQnStepToBound());
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
                setProperty(DakotaMethodConstants.APPS_CONST_PENALTY, dialog.getAppsConstrPenalty());
                setProperty(DakotaMethodConstants.APPS_CONTR_FACTOR, dialog.getAppsContrFactor());
                setProperty(DakotaMethodConstants.APPS_INIT_DELTA, dialog.getAppsInitDelta());
                setProperty(DakotaMethodConstants.APPS_MERIT, dialog.getAppsMeritFcn());
                setProperty(DakotaMethodConstants.APPS_SMOOTH, dialog.getAppsSmooth());
                setProperty(DakotaMethodConstants.APPS_SOL_TARGET, dialog.getAppsSolTarget());
                setProperty(DakotaMethodConstants.APPS_TRESDELTA, dialog.getAppsTresDelta());
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
                setProperty(DakotaMethodConstants.EA_CROSSOVER_TYPE, dialog.getEaCrossoverType());
                setProperty(DakotaMethodConstants.EA_CROSSOVER_RATE, dialog.getEaCrossRate());
                setProperty(DakotaMethodConstants.EA_FITNESS_TYPE, dialog.getEaFitnessType());
                setProperty(DakotaMethodConstants.EA_INIT_TYPE, dialog.getEaInitType());
                setProperty(DakotaMethodConstants.EA_MUT_RANGE, dialog.getEaMutRange());
                setProperty(DakotaMethodConstants.EA_MUT_RATE, dialog.getEaMutRate());
                //                    setProperty(DakotaMethodConstants.EA_MUT_RATIO, dialog.getEaMutRatio());
                setProperty(DakotaMethodConstants.EA_MUT_SCALE, dialog.getEaMutScale());
                setProperty(DakotaMethodConstants.EA_MUT_TYPE, dialog.getEaMutType());
                setProperty(DakotaMethodConstants.EA_NEW_SOL, dialog.getEaNewSol());
                setProperty(DakotaMethodConstants.EA_POPULATION, dialog.getEaPopulation());
                setProperty(DakotaMethodConstants.EA_REPLACEMENT_TYPE, dialog.getEaReplacementType());
                setProperty(DakotaMethodConstants.EA_REPLACEMENT_TYPE_VALUE, dialog.getEaReplacementTypeValue());
            }
            if (alg.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
                setProperty(DakotaMethodConstants.CC_INIT_DELTA, dialog.getCcInitDelta());
                setProperty(DakotaMethodConstants.CC_THRES_DELTA, dialog.getCcThresDelta());
            }
        }
    }
}
