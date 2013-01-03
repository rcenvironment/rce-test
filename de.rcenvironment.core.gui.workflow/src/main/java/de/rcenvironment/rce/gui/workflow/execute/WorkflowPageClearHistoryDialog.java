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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionPlaceholderHelper;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowPaletteFactory;


/**
 * Dialog for clearing placeholder history.
 * 
 * @author Sascha Zur
 */
public class WorkflowPageClearHistoryDialog extends Dialog{

    private String title;

    private WorkflowExecutionPlaceholderHelper weph;

    private WorkflowDescription wd;

    private Map<String, String> guiNameToPlaceholder;

    private Tree componentPlaceholderTree;

    private Composite container;

    protected WorkflowPageClearHistoryDialog(Shell parentShell, String title, WorkflowExecutionPlaceholderHelper pd, 
        WorkflowDescription workflowDescription) {
        super(parentShell);
        this.title = title;
        this.weph = pd;
        this.wd = workflowDescription;
        guiNameToPlaceholder = new HashMap<String, String>();
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
            | SWT.APPLICATION_MODAL); 
    }

    protected Control createDialogArea(final Composite parent) {

        container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        GridData containergridData = new GridData(GridData.FILL, GridData.FILL,
            true, true); 
        container.setLayoutData(containergridData);


        componentPlaceholderTree = new Tree(container, SWT.MULTI | SWT.CHECK);

        componentPlaceholderTree.setLayoutData(containergridData);
        componentPlaceholderTree.setHeaderVisible(false);
        componentPlaceholderTree.setLinesVisible(true);

        fillTree();

        // resize the row height using a MeasureItem listener
        componentPlaceholderTree.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                event.height = 2 * 10;
            }
        });
        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                final TreeItem treeItem = (TreeItem) e.item;
                parent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        treeItem.getParent().getColumn(0).pack();

                    }
                });
            }
        };
        componentPlaceholderTree.addListener(SWT.Collapse, listener);
        componentPlaceholderTree.addListener(SWT.Expand, listener);
        componentPlaceholderTree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                checkItems((TreeItem) event.item, ((TreeItem) event.item).getChecked());
            }

            private void checkItems(TreeItem item, boolean checked) {
                item.setChecked(checked);
                if (item.getItemCount() > 0){
                    for (TreeItem it : item.getItems()){
                        checkItems(it, checked);
                    }
                }
            }
        });

        return container;
    }

    private void clearHistory(TreeItem itComp, String parent, boolean isGlobal) {
        for (WorkflowNode wn : wd.getWorkflowNodes()){
            if (wn.getComponentDescription().getName().equals(itComp.getParentItem().getText())){
                weph.deletePlaceholderHistory(wn.getComponentDescription().getIdentifier(), guiNameToPlaceholder.get(itComp.getText()));
            }
        }
    }

    private void fillTree() {
        TreeColumn column1 = new TreeColumn(componentPlaceholderTree, SWT.LEFT);
        column1.setText("");
        Set<String> componentTypesWithPlaceholder = weph.getIdentifiersOfPlaceholderContainingComponents();
        String[] componentTypesWithPlaceholderArray = 
            componentTypesWithPlaceholder.toArray(new String[componentTypesWithPlaceholder.size()]);
        Arrays.sort(componentTypesWithPlaceholderArray);
        for (String componentID : componentTypesWithPlaceholderArray){
            TreeItem componentIDTreeItem = new TreeItem(componentPlaceholderTree, 0);
            String componentName = wd.getWorkflowNode(weph.getComponentInstances(componentID).get(0))
                .getComponentDescription().getName();
            componentIDTreeItem.setText(0, componentName);
            componentIDTreeItem.setImage(getImage(
                wd.getWorkflowNode(weph.getComponentInstances(componentID).get(0))));
            Map<String, Map<String, String>> placeholderAttributes = getPlaceholderAttributes(componentName);
            componentIDTreeItem.setExpanded(true);

            if (weph.getPlaceholderNameSetOfComponentID(componentID) != null){  
                List <String> globalPlaceholderOrder = 
                    PlaceholderSortUtils.getPlaceholderOrder(weph.getPlaceholderNameSetOfComponentID(componentID), placeholderAttributes);
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
                    compPHTreeItem.setExpanded(true);
                }
            }
            if (weph.getComponentInstances(componentID) != null){
                List<String> instancesWithPlaceholder = weph.getComponentInstances(componentID);
                instancesWithPlaceholder = PlaceholderSortUtils.sortInstancesWithPlaceholderByName(instancesWithPlaceholder, wd);
                if (instancesWithPlaceholder != null){
                    String compInstances = instancesWithPlaceholder.get(0);
                    Set<String> unsortedInstancePlaceholder = weph.getPlaceholderNameSetOfComponentInstance(compInstances);
                    List<String> sortedInstancePlaceholder = 
                        PlaceholderSortUtils.getPlaceholderOrder(unsortedInstancePlaceholder, placeholderAttributes);
                    for (String instancePlaceholder : sortedInstancePlaceholder){
                        TreeItem instancePHTreeItem = new TreeItem(componentIDTreeItem, 0);
                        String guiName = instancePlaceholder;
                        if (placeholderAttributes.get(instancePlaceholder) != null 
                            && placeholderAttributes.get(instancePlaceholder).get(
                                ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME) != null){
                            guiName = placeholderAttributes.get(instancePlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME);
                        }
                        guiNameToPlaceholder.put(guiName, instancePlaceholder);
                        instancePHTreeItem.setText(0, guiName);
                        instancePHTreeItem.setExpanded(true);
                    }
                }
            }

        }
        column1.pack();
    }

    private Map<String, Map<String, String>> getPlaceholderAttributes(String name){
        for (WorkflowNode wn : wd.getWorkflowNodes()){
            if (wn.getComponentDescription().getName().equals(name)){
                return wn.getComponentDescription().getPlaceholderAttributes();
            }
        }
        return null;
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

    @Override
    public void create() {
        super.create();
        // dialog title
        getShell().setText(title);
        for (TreeItem it1 : componentPlaceholderTree.getItems()){
            expandItem(it1);
        }

        this.getShell().pack();
        this.getShell().setSize(this.getShell().getSize().x, this.getShell().getSize().y + 3 * 10);
        componentPlaceholderTree.getColumn(0).setWidth(container.getSize().x - 5 * 2);
    } 

    private void expandItem(TreeItem it1) {
        it1.setExpanded(true);
        if (it1.getItems().length > 0){
            for (TreeItem it2 : it1.getItems()){
                expandItem(it2);
            }
        }

    }

    @Override
    protected void okPressed() {

        for (TreeItem it : componentPlaceholderTree.getItems()){
            for (TreeItem itComp : it.getItems()){
                if (itComp.getChecked()){
                    clearHistory(itComp, it.getText(), true);
                }
            }
        }
        super.okPressed();
    }

    protected Button createButton(Composite parent, int id,
        String label, boolean defaultButton) {
        if (id == IDialogConstants.OK_ID){
            label = Messages.clear;
        }
        return super.createButton(parent, id, label, defaultButton);
    }


}
