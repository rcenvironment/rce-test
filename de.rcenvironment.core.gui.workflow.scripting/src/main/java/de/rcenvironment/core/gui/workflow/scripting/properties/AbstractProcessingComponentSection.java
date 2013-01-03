/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.scripting.properties;

import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.FACTORY;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.INIT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.RUN;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.commons.scripting.ScriptLanguage;
import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ComponentRunMode;
import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ScriptTime;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * {@link de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection} adding
 * facilities to configure processing instructions to the lifecycle of a component.
 * 
 * @author Christian Weiss
 */
public abstract class AbstractProcessingComponentSection extends ValidatingWorkflowNodePropertySection {

    private static final String PLACEHOLDER_PATTERN = "${%s}";

    private static final int MINIMUM_HEIGHT = 80;

    private static final int MAXIMUM_HEIGHT = 500;

    private static final int WIDTH_HINT = 200;

    private final ScriptTime scriptTime;

    private final Map<String, String> variablesPlaceholders = new HashMap<String, String>();

    private Composite initContainer;

    private Composite runContainer;

    private Button initCommandCheckbox;

    private Text initCommandText;

    private CCombo initCommandLanguageCombo;

    private Text runCommandText;

    private CCombo runCommandLanguageCombo;

    private CCombo initVariablesCombo;

    private Button initVariablesInsertButton;

    private CCombo runVariablesCombo;

    private Button runVariablesInsertButton;

    protected AbstractProcessingComponentSection(final ScriptTime scriptTime) {
        this.scriptTime = scriptTime;
    }

    @Override
    public void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        final Composite content = new LayoutComposite(parent);
        content.setLayout(new GridLayout(2, true));

        initContainer = toolkit.createFlatFormComposite(content);
        initInitCommandSection(toolkit, initContainer);

        runContainer = toolkit.createFlatFormComposite(content);
        initRunCommandSection(toolkit, runContainer);

        parent.getParent().addListener(SWT.Resize,  new Listener() {
            public void handleEvent(Event e) {

                if (parent.getParent().getSize().y < MINIMUM_HEIGHT){
                    ((GridData) initContainer.getLayoutData()).heightHint = MINIMUM_HEIGHT;
                    ((GridData) runContainer.getLayoutData()).heightHint = MINIMUM_HEIGHT;
                } else if (parent.getParent().getSize().y > MAXIMUM_HEIGHT) {
                    ((GridData) initContainer.getLayoutData()).heightHint = MAXIMUM_HEIGHT;
                    ((GridData) runContainer.getLayoutData()).heightHint = MAXIMUM_HEIGHT;
                } else {
                    ((GridData) initContainer.getLayoutData()).heightHint = parent.getParent().getSize().y - 2 * 10;
                    ((GridData) runContainer.getLayoutData()).heightHint = parent.getParent().getSize().y - 2 * 10;
                }
                if (initContainer.getSize().y == MAXIMUM_HEIGHT && parent.getParent().getSize().y < MAXIMUM_HEIGHT + 3 * 5){
                    ((GridData) initContainer.getLayoutData()).heightHint = parent.getParent().getSize().y - 2 * 10;
                    ((GridData) runContainer.getLayoutData()).heightHint = parent.getParent().getSize().y - 2 * 10;
                }
            }
        });

    }
    
    @Override
    public void aboutToBeShown() {
        super.aboutToBeShown();
        refreshSection();
    }

    private void initInitCommandSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite initInvocationContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
        initInvocationContainer.setLayoutData(layoutData);
        initInvocationContainer.setLayout(new FillLayout());
        final Section initInvocationSectionSection = toolkit.createSection(initInvocationContainer,
            Section.TITLE_BAR | Section.EXPANDED);
        initInvocationSectionSection.setText(Messages.initInvocationSectionTitle);
        final Composite initInvocationClient = toolkit.createComposite(initInvocationSectionSection);
        initInvocationClient.setLayout(new GridLayout(2, false));
        initCommandText = toolkit.createText(initInvocationClient, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        initCommandText.setData(CONTROL_PROPERTY_KEY, FACTORY.script(scriptTime, INIT));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.heightHint = MINIMUM_HEIGHT;
        layoutData.widthHint = WIDTH_HINT;
        layoutData.horizontalSpan = 2;
        initCommandText.setLayoutData(layoutData);
        // language selection
        final Composite initCommandLanguageComposite = new Composite(initInvocationClient, SWT.NONE);
        initCommandLanguageComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        initCommandLanguageComposite.setLayoutData(layoutData);
        toolkit.createLabel(initCommandLanguageComposite, Messages.languagesLabel);
        initCommandLanguageCombo = toolkit.createCCombo(initCommandLanguageComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        addItemsToLanguageCombo(initCommandLanguageCombo);
        initCommandLanguageCombo.setData(CONTROL_PROPERTY_KEY, FACTORY.language(scriptTime, INIT));
        toolkit.paintBordersFor(initCommandLanguageCombo);
        layoutData = new GridData(GridData.GRAB_HORIZONTAL);
        initCommandLanguageCombo.setLayoutData(layoutData);
        // Variables Insertion
        final Composite initVariablesInsertionComposite = new Composite(initInvocationClient, SWT.NONE);
        initVariablesInsertionComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        initVariablesInsertionComposite.setLayoutData(layoutData);
        toolkit.createLabel(initVariablesInsertionComposite, Messages.variablesLabel);
        initVariablesCombo = toolkit.createCCombo(initVariablesInsertionComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.paintBordersFor(initVariablesCombo);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        initVariablesCombo.setLayoutData(layoutData);
        initVariablesInsertButton = toolkit.createButton(initVariablesInsertionComposite,
            Messages.variablesInsertButtonLabel, SWT.PUSH);
        initCommandCheckbox = toolkit.createButton(initInvocationClient, Messages.doInitCommandLabel, SWT.CHECK);
        initCommandCheckbox.setData(CONTROL_PROPERTY_KEY, FACTORY.doScript(scriptTime, INIT));
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        initCommandCheckbox.setLayoutData(layoutData);
        initInvocationSectionSection.setClient(initInvocationClient);
    }

    private void initRunCommandSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite runInvocationContainer) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
        runInvocationContainer.setLayoutData(layoutData);
        runInvocationContainer.setLayout(new FillLayout());
        final Section runInvocationSectionSection = toolkit.createSection(runInvocationContainer,
            Section.TITLE_BAR | Section.EXPANDED);
        runInvocationSectionSection.setText(Messages.runInvocationSectionTitle);
        final Composite runInvocationClient = toolkit.createComposite(runInvocationSectionSection);
        runInvocationClient.setLayout(new GridLayout(2, false));
        // final Label commandLabel = toolkit.createLabel(invocationClient, Messages.commandLabel,
        // SWT.NONE);
        runCommandText = toolkit.createText(runInvocationClient, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        runCommandText.setData(CONTROL_PROPERTY_KEY, FACTORY.script(scriptTime, RUN));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.heightHint = MINIMUM_HEIGHT;
        layoutData.widthHint = WIDTH_HINT;
        layoutData.horizontalSpan = 2;
        runCommandText.setLayoutData(layoutData);
        // language selection
        final Composite runCommandLanguageComposite = new Composite(runInvocationClient, SWT.NONE);
        runCommandLanguageComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        runCommandLanguageComposite.setLayoutData(layoutData);
        toolkit.createLabel(runCommandLanguageComposite, Messages.languagesLabel);
        runCommandLanguageCombo = toolkit.createCCombo(runCommandLanguageComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        addItemsToLanguageCombo(runCommandLanguageCombo);
        runCommandLanguageCombo.setData(CONTROL_PROPERTY_KEY, FACTORY.language(scriptTime, RUN));
        toolkit.paintBordersFor(runCommandLanguageCombo);
        layoutData = new GridData(GridData.GRAB_HORIZONTAL);
        runCommandLanguageCombo.setLayoutData(layoutData);
        // Variables Insertion
        final Composite runVariablesInsertionComposite = new Composite(runInvocationClient, SWT.NONE);
        runVariablesInsertionComposite.setLayout(new GridLayout(3, false));
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        runVariablesInsertionComposite.setLayoutData(layoutData);
        toolkit.createLabel(runVariablesInsertionComposite, Messages.variablesLabel);
        runVariablesCombo = toolkit.createCCombo(runVariablesInsertionComposite,
            SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.paintBordersFor(runVariablesCombo);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        runVariablesCombo.setLayoutData(layoutData);
        runVariablesInsertButton = toolkit.createButton(runVariablesInsertionComposite,
            Messages.variablesInsertButtonLabel, SWT.PUSH);
        runInvocationSectionSection.setClient(runInvocationClient);
    }
    
    private void addItemsToLanguageCombo(final CCombo combo) {
        for (final ScriptLanguage language : ScriptLanguage.values()) {
            final String languageName = language.getName();
            combo.add(languageName);
        }
        combo.add("", 0);
        combo.select(0);
    }

    @Override
    protected Controller createController() {
        return new ControllerImpl();
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new SynchronizerImpl();
    }

    private boolean isInitCommandEnabled() {
        return initCommandCheckbox.getSelection();
    }

    private boolean isRunCommandEnabled() {
        // return !getConfiguration().getDynamicInputDefinitions().isEmpty();
        // the run command is enabled as soon as inputs exist, that could trigger a run
        return hasInputs();
    }

    private void updateControls() {
        updateTextControls();
        updateVariableInsertControls();
        updateScriptLanguageSelectionControls();
    }

    private void updateTextControls() {
        initCommandText.setEnabled(isInitCommandEnabled());
        runCommandText.setEnabled(isRunCommandEnabled());
    }

    private void updateVariableInsertControls() {
        final boolean hasInitVariableReplacements = initVariablesCombo.getItems().length > 0;
        final boolean enableInitVariableInsertControl = hasInitVariableReplacements & isInitCommandEnabled();
        initVariablesCombo.setEnabled(enableInitVariableInsertControl);
        initVariablesInsertButton.setEnabled(enableInitVariableInsertControl);
        if (hasInitVariableReplacements) {
            initVariablesCombo.select(0);
        }
        final boolean hasRunVariableReplacements = runVariablesCombo.getItems().length > 0;
        final boolean enableRunVariableInsertControl = hasRunVariableReplacements & isRunCommandEnabled();
        runVariablesCombo.setEnabled(enableRunVariableInsertControl);
        runVariablesInsertButton.setEnabled(enableRunVariableInsertControl);
        if (hasRunVariableReplacements) {
            runVariablesCombo.select(0);
        }
    }

    private void updateScriptLanguageSelectionControls() {
        initCommandLanguageCombo.setEnabled(isInitCommandEnabled());
        final String initCommandLanguage = getProperty(FACTORY.language(scriptTime, INIT), String.class);
        selectComboItem(initCommandLanguageCombo, initCommandLanguage);
        runCommandLanguageCombo.setEnabled(isRunCommandEnabled());
        final String runCommandLanguage = getProperty(FACTORY.language(scriptTime, RUN), String.class);
        selectComboItem(runCommandLanguageCombo, runCommandLanguage);
    }

    private void selectComboItem(final CCombo combo, final String item) {
        if (item != null) {
            boolean found = false;
            for (int index = 0; index < combo.getItemCount(); ++index) {
                final String comboItem = combo.getItem(index);
                if (item.equals(comboItem)) {
                    combo.select(index);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(String.format("Item '%s' does not exists", item));
            }
        }
    }

    @Override
    public void refreshBeforeValidation() {
        final ReadableComponentInstanceConfiguration configuration = getConfiguration();
        variablesPlaceholders.clear();
        // variable combo
        final Map<ComponentRunMode, List<String>> variables = new HashMap<ComponentRunMode, List<String>>();
        variables.put(ComponentRunMode.INIT, new LinkedList<String>());
        variables.put(ComponentRunMode.RUN, new LinkedList<String>());
        if (scriptTime == ScriptTime.PRE) {
            final List<String> inputNames = new LinkedList<String>(configuration.getDynamicInputDefinitions().keySet());
            Collections.sort(inputNames);
            for (final String inputName : inputNames) {
                final Class<? extends Serializable> type = configuration.getDynamicInputDefinitions().get(inputName);
                if (!DataManagementFileReference.class.isAssignableFrom(type)) {
                    final String inputType = configuration.getInputType(inputName);
                    final String label = Messages.bind(Messages.variablesInputPattern, inputName, inputType);
                    variables.get(ComponentRunMode.RUN).add(label);
                    final String placeholder = String.format(PLACEHOLDER_PATTERN, inputName);
                    variablesPlaceholders.put(label, placeholder);
                }
            }            
        } else if (scriptTime == ScriptTime.POST) {
            final List<String> outputNames = new LinkedList<String>(configuration.getDynamicOutputDefinitions().keySet());
            Collections.sort(outputNames);
            for (final String outputName : outputNames) {
                final Class<? extends Serializable> type = configuration.getDynamicOutputDefinitions().get(outputName);
                if (!DataManagementFileReference.class.isAssignableFrom(type)) {
                    final String label = Messages.bind(Messages.variablesOutputPattern, outputName);
                    variables.get(ComponentRunMode.INIT).add(label);
                    variables.get(ComponentRunMode.RUN).add(label);
                    final String placeholder = String.format(PLACEHOLDER_PATTERN, outputName);
                    variablesPlaceholders.put(label, placeholder);
                }
            }            
        }
        String cwdLabel = Messages.workingDir;
        variablesPlaceholders.put(cwdLabel, String.format(PLACEHOLDER_PATTERN, "cwd"));
        variables.get(ComponentRunMode.INIT).add(cwdLabel);
        variables.get(ComponentRunMode.RUN).add(cwdLabel);
        refreshSection(ComponentRunMode.INIT, variables, initVariablesCombo, initCommandLanguageCombo);
        refreshSection(ComponentRunMode.RUN, variables, runVariablesCombo, runCommandLanguageCombo);
        updateControls();
    }

    protected void refreshSection(final ComponentRunMode componentRunMode, final Map<ComponentRunMode, List<String>> variables,
        final CCombo variablesCombo, final CCombo commandLanguageCombo) {
        refreshSection(variables.get(componentRunMode), variablesCombo, commandLanguageCombo);
    }

    protected void refreshSection(final List<String> labels, final CCombo variablesCombo, final CCombo commandLanguageCombo) {
        variablesCombo.removeAll();
        for (final String label : labels) {
            variablesCombo.add(label);
        }
    }

    /**
     * Controller.
     * 
     * @author Christian Weiss
     */
    private class ControllerImpl extends DefaultController {

        @Override
        public void widgetSelected(final SelectionEvent event, final Control source) {
            final String propertyKey = (String) source.getData(CONTROL_PROPERTY_KEY);
            if (source == initVariablesInsertButton) {
                final int selectionIndex = initVariablesCombo.getSelectionIndex();
                final String selectedLabel = initVariablesCombo.getItem(selectionIndex);
                if (selectionIndex >= 0 && selectionIndex < variablesPlaceholders.size()) {
                    final String placeholder = variablesPlaceholders.get(selectedLabel);
                    replace(initCommandText, placeholder);
                }
            } else if (source == runVariablesInsertButton) {
                final int selectionIndex = runVariablesCombo.getSelectionIndex();
                final String selectedLabel = runVariablesCombo.getItem(selectionIndex);
                if (selectionIndex >= 0 && selectionIndex < variablesPlaceholders.size()) {
                    final String placeholder = variablesPlaceholders.get(selectedLabel);
                    replace(runCommandText, placeholder);
                }
            } else if (source == initCommandCheckbox) {
                updateControls();
            } else if (source == runCommandLanguageCombo || source == initCommandLanguageCombo) {
                final int selectionIndex = ((CCombo) source).getSelectionIndex();
                final String selection = ((CCombo) source).getItem(selectionIndex);
                String language = "";
                if (selection != null && !selection.isEmpty()) {
                    final ScriptLanguage scriptLanguage = ScriptLanguage.getByName(selection);
                    language = scriptLanguage.getName();
                }
                setProperty(propertyKey, language);
            }
        }

        @Override
        public void keyPressed(final KeyEvent event) {
            super.keyPressed(event);
        }

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
            updateScriptLanguageSelectionControls();
        }

        @Override
        public void handleChannelEvent(final ChannelEvent event) {
            updateVariableInsertControls();
        }

    }

}
