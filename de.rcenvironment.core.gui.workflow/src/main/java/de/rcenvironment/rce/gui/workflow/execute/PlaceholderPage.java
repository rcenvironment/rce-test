/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.execute;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionPlaceholderHelper;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowPaletteFactory;


/**
 * .
 * @author Sascha Zur
 */
public class PlaceholderPage extends WizardPage{

    private WorkflowDescription workflowDescription;
    private WorkflowExecutionPlaceholderHelper placeholderHelper;
    private Tree componentPlaceholderTree;

    private Map<String, Text> textMap;

    private final String dot = ".";

    private Map<String, String> guiNameToPlaceholder;

    private Map<String, Button> saveButtonMap;

    /**
     * The Constructor.
     */
    public PlaceholderPage(final WorkflowExecutionWizard parentWizard) {
        super(Messages.workflowPageName);
        this.workflowDescription = parentWizard.getWorkflowDescription();
        setTitle(Messages.workflowPageTitle);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        setControl(comp);
        placeholderHelper = WorkflowExecutionPlaceholderHelper.createPlaceholderDescriptionFromWorkflowDescription(workflowDescription);
        addPlaceholderGroup(comp);

        Button clearHistoryButton = new Button(comp, SWT.NONE);
        clearHistoryButton.setText(Messages.clearHistoryButton);
        clearHistoryButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkflowPageClearHistoryDialog chd = new WorkflowPageClearHistoryDialog(getShell(), 
                    Messages.clearHistoryDialogTitle, placeholderHelper, workflowDescription);
                chd.open();
            }

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        if (placeholderHelper.getIdentifiersOfPlaceholderContainingComponents().size() == 0){
            clearHistoryButton.setEnabled(false);
        }

    }
    private void addPlaceholderGroup(Composite container) {
        textMap = new HashMap<String, Text>();
        saveButtonMap = new HashMap<String, Button>();
        guiNameToPlaceholder = new HashMap<String, String>();

        Group placeholderInformationGroup = new Group(container, SWT.NONE);
        placeholderInformationGroup.setText(Messages.placeholderInformationHeader);
        placeholderInformationGroup.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        placeholderInformationGroup.setLayoutData(gridData);

        componentPlaceholderTree =  new Tree(placeholderInformationGroup, SWT.MULTI);

        componentPlaceholderTree.setLayoutData(gridData);
        componentPlaceholderTree.setHeaderVisible(false);
        componentPlaceholderTree.setLinesVisible(true);

        // resize the row height using a MeasureItem listener
        componentPlaceholderTree.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                event.height = 2 * 10;
            }
        });

        fillTree();

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                final TreeItem treeItem = (TreeItem) e.item;
                getShell().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        treeItem.getParent().getColumn(0).pack();
                        treeItem.getParent().getColumn(0).setWidth(treeItem.getParent().getColumn(0).getWidth() + 10);
                    }
                });
            }
        };
        componentPlaceholderTree.addListener(SWT.Collapse, listener);
        componentPlaceholderTree.addListener(SWT.Expand, listener);

        openItems();
        componentPlaceholderTree.getColumn(0).pack();
        componentPlaceholderTree.getColumn(0).setWidth(componentPlaceholderTree.getColumn(0).getWidth() + 10);

    }

    private void openItems() {
        Display display = Display.getCurrent();

        for (TreeItem parent : componentPlaceholderTree.getItems()){
            for (TreeItem secondLevel : parent.getItems()){
                if (secondLevel.getItemCount() == 0){
                    Text current = textMap.get(parent.getText() + dot + guiNameToPlaceholder.get(secondLevel.getText()));
                    parent.setExpanded(true); // Always expand. Copy into if branch, if it should only open when nothing is in it
                    if (current.getText().equals("")
                        || current.getText() == null){
                        current.setBackground(display.getSystemColor(SWT.COLOR_RED));
                    }
                } else {
                    for (TreeItem thirdLevel : secondLevel.getItems()){
                        Text current = textMap.get(secondLevel.getText() + dot 
                            + guiNameToPlaceholder.get(thirdLevel.getText()));
                        parent.setExpanded(true); // Always expand. Copy into if branch, if it should only open when nothing is in it
                        secondLevel.setExpanded(true);
                        if (current.getText().equals("")
                            || current.getText() == null){
                            current.setBackground(display.getSystemColor(SWT.COLOR_RED));
                        }
                    }
                }
            }
        }
    }

    private void fillTree() {
        TreeColumn column1 = new TreeColumn(componentPlaceholderTree, SWT.LEFT);
        column1.setText("");
        TreeColumn column2 = new TreeColumn(componentPlaceholderTree, SWT.CENTER);
        column2.setText("");
        column2.setWidth(10 * 10 + 5);
        TreeColumn column3 = new TreeColumn(componentPlaceholderTree, SWT.CENTER);
        column3.setText("");
        column3.setWidth(5 * 10);
        TreeColumn column4 = new TreeColumn(componentPlaceholderTree, SWT.CENTER);
        column4.setText("");
        column4.setWidth(10 * 10);
        Set<String> componentTypesWithPlaceholder = placeholderHelper.getIdentifiersOfPlaceholderContainingComponents();
        String[] componentTypesWithPlaceholderArray = 
            componentTypesWithPlaceholder.toArray(new String[componentTypesWithPlaceholder.size()]);
        Arrays.sort(componentTypesWithPlaceholderArray);
        for (String componentID : componentTypesWithPlaceholderArray){
            TreeItem componentIDTreeItem = new TreeItem(componentPlaceholderTree, 0);
            String componentName = workflowDescription.getWorkflowNode(placeholderHelper.getComponentInstances(componentID).get(0))
                .getComponentDescription().getName();
            componentIDTreeItem.setText(0, componentName);
            componentIDTreeItem.setImage(getImage(
                workflowDescription.getWorkflowNode(placeholderHelper.getComponentInstances(componentID).get(0))));
            Map<String, Map<String, String>> placeholderAttributes = getPlaceholderAttributes(componentName);
            List <String> globalPlaceholderOrder = 
                PlaceholderSortUtils.getPlaceholderOrder(
                    placeholderHelper.getPlaceholderNameSetOfComponentID(componentID), placeholderAttributes);
            if (globalPlaceholderOrder == null){
                globalPlaceholderOrder = new LinkedList <String>();
            }
            for (String componentPlaceholder : globalPlaceholderOrder){
                TreeItem compPHTreeItem = new TreeItem(componentIDTreeItem, 0);
                String guiName = componentPlaceholder;
                if (placeholderAttributes.get(componentPlaceholder) != null 
                    && placeholderAttributes.get(componentPlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME) != null){
                    guiName = placeholderAttributes.get(componentPlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME);
                }
                guiNameToPlaceholder.put(guiName, componentPlaceholder);

                compPHTreeItem.setText(0, guiName);
                String currentPlaceholder = componentID + dot + componentPlaceholder;
                boolean isPath = false;
                if ((placeholderAttributes.get(componentPlaceholder) != null 
                    && placeholderAttributes.get(componentPlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_ISPATH) != null)){
                    isPath = placeholderAttributes.get(componentPlaceholder).get(
                        ComponentConstants.PLACEHOLDER_ATTRIBUTE_ISPATH).equalsIgnoreCase("true");
                }
                textMap.put(componentName + dot + componentPlaceholder, 
                    addSWTHandler(compPHTreeItem, componentName + dot + componentPlaceholder, 
                        placeholderHelper.isEncryptedPlaceholder(currentPlaceholder), true, isPath));

            }
            List<String> instancesWithPlaceholder = placeholderHelper.getComponentInstances(componentID);
            instancesWithPlaceholder = 
                PlaceholderSortUtils.sortInstancesWithPlaceholderByName(instancesWithPlaceholder, workflowDescription);
            for (String compInstances : instancesWithPlaceholder){
                TreeItem instanceTreeItem = new TreeItem(componentIDTreeItem, 0);
                String instanceName = workflowDescription.getWorkflowNode(compInstances).getName();

                instanceTreeItem.setText(0, instanceName);
                instanceTreeItem.setImage(getImage(
                    workflowDescription.getWorkflowNode(placeholderHelper.getComponentInstances(componentID).get(0))));
                List <String> instancePlaceholderOrder = 
                    PlaceholderSortUtils.getPlaceholderOrder(
                        placeholderHelper.getPlaceholderNameSetOfComponentInstance(compInstances), placeholderAttributes);

                for (String instancePlaceholder : instancePlaceholderOrder){
                    TreeItem instancePHTreeItem = new TreeItem(instanceTreeItem, 0);
                    String guiName = instancePlaceholder;
                    if (placeholderAttributes.get(instancePlaceholder) != null 
                        && placeholderAttributes.get(instancePlaceholder).get(
                            ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME) != null){
                        guiName = placeholderAttributes.get(instancePlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME);
                    }
                    guiNameToPlaceholder.put(guiName, instancePlaceholder);

                    instancePHTreeItem.setText(0, guiName);
                    String currentPlaceholder = componentID + dot + instancePlaceholder;
                    boolean isPath = false;
                    if (placeholderAttributes.get(instancePlaceholder) != null 
                        && placeholderAttributes.get(instancePlaceholder).get(
                            ComponentConstants.PLACEHOLDER_ATTRIBUTE_ISPATH) != null){
                        isPath = placeholderAttributes.get(instancePlaceholder).get(
                            ComponentConstants.PLACEHOLDER_ATTRIBUTE_ISPATH).equalsIgnoreCase("true");
                    }
                    textMap.put(instanceName + dot + instancePlaceholder, 
                        addSWTHandler(instancePHTreeItem, instanceName + dot + instancePlaceholder, 
                            placeholderHelper.isEncryptedPlaceholder(currentPlaceholder), false, isPath));
                }
            }
        }
        column1.pack();
    }


  

    private Text addSWTHandler(TreeItem item, String placeholderName, boolean isEncrypted, boolean isGlobal, boolean isPathField){
        TreeEditor textEditor = new TreeEditor(item.getParent());
        textEditor.horizontalAlignment = SWT.LEFT;
        textEditor.grabHorizontal = true;
        int style =  SWT.BORDER;
        if (isEncrypted){
            style |= SWT.PASSWORD;
        }
        final Text placeholderText = new Text(item.getParent(), style);

        placeholderText.setMessage("No value entered.");
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateInput((Text) e.getSource());
            }

        };
        placeholderText.addModifyListener(modifyListener);
        String[] allProposals = { };
        if (!isGlobal){
            allProposals = placeholderHelper.getInstancePlaceholderHistory(guiNameToPlaceholder.get(item.getText()),  
                getComponentUUIDByName(item.getParentItem().getText()));
        } else {
            allProposals = placeholderHelper.getComponentPlaceholderHistory(guiNameToPlaceholder.get(item.getText()),  
                getComponentIDByName(item.getParentItem().getText()), workflowDescription.getIdentifier());
        }
        if (allProposals.length > 0){
            placeholderText.setText(allProposals[allProposals.length - 1]); // set default value to recent one
        }
        String[] additionalProposals = placeholderHelper.getOtherPlaceholderHistoryValues(guiNameToPlaceholder.get(item.getText()));
        if (allProposals.length == 0){
            allProposals = additionalProposals;
            if (!isEncrypted && allProposals.length > 0){
                String valueFromOtherComponentInWorkflow = 
                    placeholderHelper.getValueFromOtherComponentInWorkflow(
                        guiNameToPlaceholder.get(item.getText()), workflowDescription.getIdentifier());
                if (valueFromOtherComponentInWorkflow != null){
                    placeholderText.setText(valueFromOtherComponentInWorkflow);
                } else {
                    placeholderText.setText(allProposals[allProposals.length - 1]);
                }
            }
        } else {
            allProposals = additionalProposals;
        }

        if (isEncrypted){
            allProposals = new String[0]; // Passwords should not be visible
        }
        SimpleContentProposalProvider scp = new SimpleContentProposalProvider(allProposals);
        scp.setFiltering(true);

        ContentProposalAdapter adapter = null;
        adapter = new ContentProposalAdapter(placeholderText, new TextContentAdapter(), scp, 
            KeyStroke.getInstance(SWT.ARROW_DOWN), null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        adapter.setAutoActivationDelay(1);
        adapter.setPropagateKeys(true);

        textEditor.setEditor(placeholderText, item, 1);
        if (!isGlobal) {
            TreeEditor buttonEditor = new TreeEditor(item.getParent());
            buttonEditor.horizontalAlignment = SWT.LEFT;
            buttonEditor.grabHorizontal = true;
            Button placeholderButton =  new Button(item.getParent(), SWT.PUSH);
            placeholderButton.setText(Messages.applyToAll);
            placeholderButton.setSize(placeholderButton.getText().length() * 6, 0);
            placeholderButton.computeSize(SWT.DEFAULT, item.getParent().getItemHeight());
            placeholderButton.addSelectionListener(new ButtonListener(item));
            buttonEditor.minimumWidth = placeholderButton.getSize().x;
            buttonEditor.setEditor(placeholderButton, item, 3);
        }
        if (isEncrypted){
            TreeEditor checkButton = new TreeEditor(item.getParent());
            checkButton.horizontalAlignment = SWT.LEFT;
            checkButton.grabHorizontal = true;
            Button checkForSaveButton = new Button(item.getParent(), SWT.CHECK);
            checkForSaveButton.setText("Save");
            checkButton.minimumWidth = checkForSaveButton.getSize().x;
            checkButton.setEditor(checkForSaveButton, item, 2);
            saveButtonMap.put(placeholderName, checkForSaveButton);
            if (placeholderText.getText() != null && !placeholderText.getText().equals("")){
                checkForSaveButton.setSelection(true);
            }
        }
        if (isPathField){
            TreeEditor pathButtonEditor = new TreeEditor(item.getParent());
            pathButtonEditor.horizontalAlignment = SWT.LEFT;
            pathButtonEditor.grabHorizontal = true;
            Button pathChooserButton =  new Button(item.getParent(), SWT.PUSH);
            pathChooserButton.setText("...");
            pathChooserButton.setSize(pathChooserButton.getText().length() * 6, 0);
            pathChooserButton.computeSize(SWT.DEFAULT, item.getParent().getItemHeight());
            pathButtonEditor.minimumWidth = pathChooserButton.getSize().x;
            pathChooserButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    final String selectedPath = PropertyTabGuiHelper.openFilesystemFileDialog(getShell(),
                        new String[] { }, "Open path...");   
                    if (selectedPath != null){
                        placeholderText.setText(selectedPath);
                    }
                }
                @Override
                public void widgetDefaultSelected(SelectionEvent arg0) {
                }
            });
            pathButtonEditor.setEditor(pathChooserButton, item, 2);
        }
        return placeholderText;
    }
    private Image getImage(WorkflowNode element){
        byte[] icon = element.getComponentDescription().getIcon16();
        Image image;
        if (icon != null) {
            image = new Image(Display.getCurrent(), new ByteArrayInputStream(icon));
        } else {
            image = ImageDescriptor.createFromURL(
                WorkflowPaletteFactory.class.getResource("/resources/icons/component16.gif")).createImage(); //$NON-NLS-1$            }
        }
        return image;
    }

    private void validateInput(Text source) {
        if (source.getText() != null && !source.getText().equals("")){
            source.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        } else {
            source.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        }
    }
    /**
     * Performs all actions to be done when 'Finish' is clicked.
     * @param wd  : WorkflowDescription of this page
     */
    public void performFinish(WorkflowDescription wd){
        for (TreeItem componentItems : getComponentPlaceholderTree().getItems()){
            for (TreeItem componentIDItems : componentItems.getItems()){
                if (componentIDItems.getItemCount() == 0){
                    // componentPlaceholder
                    String placeholder = componentItems.getText() +  dot + guiNameToPlaceholder.get(componentIDItems.getText());
                    for (String fullPlaceholder : placeholderHelper.getPlaceholderOfComponent(getComponentIDByName(
                        componentItems.getText()))){
                        if (WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(fullPlaceholder).equals(
                            guiNameToPlaceholder.get(componentIDItems.getText()))){
                            boolean addToHistory = true;
                            if (saveButtonMap.get(placeholder) != null){
                                addToHistory = saveButtonMap.get(placeholder).getSelection();
                            }
                            placeholderHelper.setGlobalPlaceholderValue(fullPlaceholder, 
                                getComponentIDByName(componentItems.getText()), textMap.get(placeholder).getText(), 
                                workflowDescription.getIdentifier(), addToHistory);
                        }
                    }
                } else {
                    for (TreeItem instancePlaceholderItems : componentIDItems.getItems()){
                        // instancePlaceholder
                        String placeholder = componentIDItems.getText()
                            +  dot + guiNameToPlaceholder.get(instancePlaceholderItems.getText());
                        for (String fullPlaceholder : placeholderHelper.getPlaceholderNamesOfComponentInstance(
                            getComponentUUIDByName(componentIDItems.getText()))){
                            if (WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(fullPlaceholder).equals(
                                guiNameToPlaceholder.get(instancePlaceholderItems.getText()))){
                                boolean addToHistory = true;
                                if (saveButtonMap.get(placeholder) != null){
                                    addToHistory = saveButtonMap.get(placeholder).getSelection();
                                }
                                placeholderHelper.setPlaceholderValue(fullPlaceholder, getComponentIDByName(componentIDItems.getText()),
                                    getComponentUUIDByName(componentIDItems.getText()), textMap.get(placeholder).getText(), 
                                    workflowDescription.getIdentifier(), addToHistory);
                            }
                        }
                    }
                }
            }
        }
        /* dispose SWT components */
        for (String key : textMap.keySet()){
            textMap.get(key).dispose();
        }
        getComponentPlaceholderTree().dispose();

        placeholderHelper.saveHistory();
        for (WorkflowNode wn : wd.getWorkflowNodes()){
            wn.getComponentDescription().addPlaceholderMap(
                placeholderHelper.getPlaceholderOfComponentType(wn.getComponentDescription().getIdentifier()));
            wn.getComponentDescription().addPlaceholderMap(
                placeholderHelper.getPlaceholderOfComponentInstance(wn.getIdentifier()));
        }
    }

    private String getComponentUUIDByName(String name){
        for (WorkflowNode wn : workflowDescription.getWorkflowNodes()){
            if (wn.getName().equals(name)){
                return wn.getIdentifier();
            }
        }
        return null;
    }
    private String getComponentIDByName(String name){
        for (WorkflowNode wn : workflowDescription.getWorkflowNodes()){
            if (wn.getName().equals(name)){
                return wn.getComponentDescription().getIdentifier();
            }
        }
        return null;
    }   
    private Map<String, Map<String, String>> getPlaceholderAttributes(String name){
        for (WorkflowNode wn : workflowDescription.getWorkflowNodes()){
            if (wn.getComponentDescription().getName().equals(name)){
                return wn.getComponentDescription().getPlaceholderAttributes();
            }
        }
        return null;
    }  
    public Tree getComponentPlaceholderTree() {
        return componentPlaceholderTree;
    }
    /**
     * ButtonListener to identify TreeItem for every button.
     * 
     * @author Sascha Zur
     */
    private class ButtonListener extends SelectionAdapter{
        private TreeItem parentTreeItem;
        public ButtonListener(TreeItem it){
            parentTreeItem = it;
        }

        @Override
        public void widgetSelected(SelectionEvent arg0) {
            String replaceText = textMap.get(parentTreeItem.getParentItem().getText() 
                + dot + guiNameToPlaceholder.get(parentTreeItem.getText())).getText();
            for (TreeItem componentIDItems : parentTreeItem.getParentItem().getParentItem().getItems()){
                for (TreeItem instanceItems : componentIDItems.getItems()){
                    if (instanceItems.getText().equals(parentTreeItem.getText())){
                        textMap.get(componentIDItems.getText() 
                            + dot + guiNameToPlaceholder.get(instanceItems.getText())).setText(replaceText);
                    }
                }
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0) {
            widgetSelected(arg0);
        }
    }
}
