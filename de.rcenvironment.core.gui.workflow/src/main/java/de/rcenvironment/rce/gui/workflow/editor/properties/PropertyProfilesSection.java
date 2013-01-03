/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.gui.commons.components.PropertyTabGuiHelper;
import de.rcenvironment.rce.component.ComponentDescription;

/**
 * A "Properties" view tab for selecting and managing property profiles.
 * 
 * @author Doreen Seider
 */
public class PropertyProfilesSection extends WorkflowNodePropertySection {

    private static final int FULL_WIDTH = 100;
    
    private Combo[] configMapIdsCombos = new Combo[3];

    private Text text;

    private Button buttonAdd;

    private Button buttonRemove;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {

        super.createControls(parent, aTabbedPropertySheetPage);

        final Section configMapsSelection = PropertyTabGuiHelper
            .createSingleColumnSectionComposite(parent, getWidgetFactory(), Messages.selectTitle);
        final Composite configMapsSelectionClient = getWidgetFactory().createComposite(configMapsSelection);
        configMapsSelection.setClient(configMapsSelectionClient);
        configMapsSelectionClient.setLayout(new FormLayout());

        configMapIdsCombos[0] = new Combo(configMapsSelectionClient, SWT.BORDER | SWT.READ_ONLY);
        final FormData configMapIdsZeroData = new FormData();
        configMapIdsZeroData.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        configMapIdsZeroData.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
        configMapIdsZeroData.right = new FormAttachment(FULL_WIDTH);
        configMapIdsCombos[0].setLayoutData(configMapIdsZeroData);
        configMapIdsCombos[0].addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final SetPropertyMapCommand command;
                if (configMapIdsCombos[0].getText().equals(Messages.defaultConfigMap)) {
                    command = new SetPropertyMapCommand(ComponentDescription.DEFAULT_CONFIG_ID);
                } else {
                    command = new SetPropertyMapCommand(configMapIdsCombos[0].getText());
                    
                }
                execute(command);
            }
        });

        final Section configMapsEditing = PropertyTabGuiHelper
            .createSingleColumnSectionComposite(parent, getWidgetFactory(), Messages.manageTitle);
        final Composite configMapsEditingClient = getWidgetFactory().createComposite(configMapsEditing);
        configMapsEditing.setClient(configMapsEditingClient);
        configMapsEditingClient.setLayout(new FormLayout());

        CLabel newLabel = getWidgetFactory().createCLabel(configMapsEditingClient, Messages.newProfile);
        FormData newData = new FormData();
        newData.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
        newData.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        newLabel.setLayoutData(newData);

        text = getWidgetFactory().createText(configMapsEditingClient, ""); //$NON-NLS-1$
        final FormData textData = new FormData();
        textData.left = new FormAttachment(newLabel, ITabbedPropertyConstants.HSPACE, SWT.RIGHT);
        textData.right = new FormAttachment(FULL_WIDTH, -ITabbedPropertyConstants.HSPACE);
        textData.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        text.setLayoutData(textData);
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                setAddButtonState();
            }
        });

        CLabel inheritedLabel = getWidgetFactory().createCLabel(configMapsEditingClient,  Messages.inheritedFrom);
        FormData inheritedData = new FormData();
        inheritedData.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
        inheritedData.top = new FormAttachment(newLabel, ITabbedPropertyConstants.VSPACE);
        inheritedLabel.setLayoutData(inheritedData);

        configMapIdsCombos[2] = new Combo(configMapsEditingClient, SWT.BORDER | SWT.READ_ONLY);
        final FormData configMapIdsTwoData = new FormData();
        configMapIdsTwoData.left = new FormAttachment(newLabel, ITabbedPropertyConstants.HSPACE, SWT.RIGHT);
        configMapIdsTwoData.right = new FormAttachment(FULL_WIDTH, -ITabbedPropertyConstants.HSPACE);
        configMapIdsTwoData.top = new FormAttachment(text, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
        configMapIdsCombos[2].setLayoutData(configMapIdsTwoData);

        buttonAdd = getWidgetFactory().createButton(configMapsEditingClient, Messages.add, SWT.NONE | SWT.FLAT);
        final FormData addData = new FormData();
        addData.top = new FormAttachment(inheritedLabel, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
        addData.right = new FormAttachment(FULL_WIDTH, -ITabbedPropertyConstants.HSPACE);
        buttonAdd.setLayoutData(addData);
        buttonAdd.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final AddPropertyMapCommand command;
                
                if (configMapIdsCombos[2].getText().equals(Messages.defaultConfigMap)) {
                    command = new AddPropertyMapCommand(text.getText(), ComponentDescription.DEFAULT_CONFIG_ID);
                } else {
                    command = new AddPropertyMapCommand(text.getText(), configMapIdsCombos[2].getText());
                }
                execute(command);
                text.setText("");
                configMapIdsCombos[2].setText(Messages.defaultConfigMap);
                refresh();
            }
        });

        buttonRemove = getWidgetFactory().createButton(configMapsEditingClient, Messages.remove, SWT.NONE | SWT.FLAT);
        final FormData removeData = new FormData();
        removeData.top = new FormAttachment(buttonAdd, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
        removeData.right = new FormAttachment(FULL_WIDTH, -ITabbedPropertyConstants.HSPACE);
        buttonRemove.setLayoutData(removeData);
        buttonRemove.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                final RemovePropertyMapCommand command = new RemovePropertyMapCommand(configMapIdsCombos[1].getText());
                execute(command);
                configMapIdsCombos[1].setText(Messages.defaultConfigMap);
                refresh();
            }
        });

        configMapIdsCombos[1] = new Combo(configMapsEditingClient, SWT.BORDER | SWT.READ_ONLY);
        final FormData configMapIdsOneData = new FormData();
        configMapIdsOneData.top = new FormAttachment(buttonAdd, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
        configMapIdsOneData.right = new FormAttachment(buttonRemove, -ITabbedPropertyConstants.HSPACE, SWT.LEFT);
        configMapIdsOneData.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
        configMapIdsCombos[1].setLayoutData(configMapIdsOneData);
        configMapIdsCombos[1].addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                setRemoveButtonState();
            }
        });

    }

    @Override
    public void refresh() {
        for (Combo combo : configMapIdsCombos) {
            combo.removeAll();
            for (final String configMapId: getReadableConfiguration().getPropertyMapIds()) {
                if (configMapId.equals(ComponentDescription.DEFAULT_CONFIG_ID)) {
                    combo.add(Messages.defaultConfigMap);
                } else {
                    combo.add(configMapId);
                }
            }
        }

        if (getReadableConfiguration().getPropertyMapId().equals(ComponentDescription.DEFAULT_CONFIG_ID)) {
            configMapIdsCombos[0].setText(Messages.defaultConfigMap);
        } else {
            configMapIdsCombos[0].setText(getReadableConfiguration().getPropertyMapId());
        }

        if (configMapIdsCombos[1].getText().equals("")) {
            configMapIdsCombos[1].setText(Messages.defaultConfigMap);
        }

        if (configMapIdsCombos[2].getText().equals("")) {
            configMapIdsCombos[2].setText(Messages.defaultConfigMap);
        }

        setRemoveButtonState();
        setAddButtonState();

    }
    
    private void setAddButtonState() {
        if (text.getText().isEmpty()) {
            buttonAdd.setEnabled(false);
        } else {
            for (String mapId: getReadableConfiguration().getPropertyMapIds()) {
                if (text.getText().equalsIgnoreCase(mapId)) {
                    buttonAdd.setEnabled(false);
                    return;
                }
            }
            buttonAdd.setEnabled(true);
        }
    }
    
    private void setRemoveButtonState() {
        if (configMapIdsCombos[1].getText().equals(Messages.defaultConfigMap)) {
            buttonRemove.setEnabled(false);
        } else {
            buttonRemove.setEnabled(true);
        }
    }

}
