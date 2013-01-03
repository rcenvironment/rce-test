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
public class DOEAlgorithmSection extends ValidatingWorkflowNodePropertySection {

    private static final int WHITE = 255;
    /**
     * Selections.
     */
    public Combo comboAlgorithmSelection;

    private List<String> algorithms;

    private Button buttonProperties;

    private Text dakotaBinaryPath;
    private Button buttonPathselector;
    /**
     * Constructor.
     */
    public DOEAlgorithmSection() {
        algorithms = new LinkedList<String>();
        algorithms.add(OptimizerComponentConstants.DOE_LHS);
//        algorithms.add(OptimizerComponentConstants.DOE_MONTE);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        final Section sectionAlgorithm = PropertyTabGuiHelper.createSingleColumnSectionComposite(parent, getWidgetFactory(),
            Messages.algorithm);
        final Composite sectionInstallationClient = getWidgetFactory().createComposite(sectionAlgorithm);
        sectionAlgorithm.setClient(sectionInstallationClient);
        sectionInstallationClient.setLayout(new GridLayout(2, false));
        comboAlgorithmSelection = new Combo(sectionInstallationClient, SWT.BORDER | SWT.READ_ONLY);
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
        comboAlgorithmSelection.setText(OptimizerComponentConstants.DOE_LHS);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "de.rcenvironment.rce.components.gui.optimizer.OptimizerSection");
        buttonProperties = new Button(sectionInstallationClient , SWT.PUSH);
        buttonProperties.setText(Messages.algorithmProperties);
        SelectionAdapter buttonListener = new DakotaSelectionAdapter(parent);
        buttonProperties.addSelectionListener(buttonListener);
        new Label(sectionInstallationClient, SWT.NONE).setText(" ");
        new Label(sectionInstallationClient, SWT.NONE).setText(" ");
        new Label(sectionInstallationClient, SWT.NONE).setText(Messages.dakotaPathOptional);
        new Label(sectionInstallationClient, SWT.NONE).setText(" ");
        dakotaBinaryPath = new Text(sectionInstallationClient, SWT.BORDER | SWT.READ_ONLY | SWT.FILL_EVEN_ODD);
        dakotaBinaryPath.setBackground(new Color(null, WHITE, WHITE, WHITE));
        dakotaBinaryPath.setText("                                                             ");
        buttonPathselector = new Button(sectionInstallationClient , SWT.PUSH);
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
                DOEAlgorithmPropertiesDialog dialog = new DOEAlgorithmPropertiesDialog(
                    parent.getShell(), this, Messages.algorithmProperties);
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
            if (alg.equals(OptimizerComponentConstants.DOE_LHS)){
                result.doeLHSSeed = getProperty(DakotaMethodConstants.DOE_LHS_SEED);
                result.doeLHSFixedSeed = getProperty(DakotaMethodConstants.DOE_LHS_FIXED_SEED);
                result.doeLHSSamples = getProperty(DakotaMethodConstants.DOE_LHS_SAMPLES);
                result.doeLHSSymbols = getProperty(DakotaMethodConstants.DOE_LHS_SYMBOLS);
                result.doeLHSMainEffects = getProperty(DakotaMethodConstants.DOE_LHS_MAIN_EFFECTS);
                result.doeLHSQualityMetrics =  getProperty(DakotaMethodConstants.DOE_LHS_QUALITY_METRICS);
                result.doeLHSVarianceBasedDecomp = getProperty(DakotaMethodConstants.DOE_LHS_VARIANCE_BASED_DECOMP);
            }
            
            return result;
        }
        private void setMethodDependentOptiones(DOEAlgorithmPropertiesDialog dialog) {
            String alg = comboAlgorithmSelection.getItem(comboAlgorithmSelection.getSelectionIndex());

            if (alg.equals(OptimizerComponentConstants.DOE_LHS)){
                setProperty(DakotaMethodConstants.DOE_LHS_SEED, dialog.getDoeLHSSeed());
                setProperty(DakotaMethodConstants.DOE_LHS_FIXED_SEED, dialog.getLHSFixedSeed());
                setProperty(DakotaMethodConstants.DOE_LHS_SAMPLES, dialog.getDoeLHSSSamples());
                setProperty(DakotaMethodConstants.DOE_LHS_SYMBOLS, dialog.getDoeLHSSymbols());
                setProperty(DakotaMethodConstants.DOE_LHS_MAIN_EFFECTS, dialog.getLHSMainEffects());
                setProperty(DakotaMethodConstants.DOE_LHS_QUALITY_METRICS, dialog.getLHSQualityMetrics());
                setProperty(DakotaMethodConstants.DOE_LHS_VARIANCE_BASED_DECOMP, dialog.getLHSVarianceBasedDecomp());
            }
        }
    }
}
