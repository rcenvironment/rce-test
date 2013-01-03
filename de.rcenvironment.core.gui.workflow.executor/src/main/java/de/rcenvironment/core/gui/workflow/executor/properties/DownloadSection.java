/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * "Properties" view tab for configuring files to download after job submission.
 *
 * @author Doreen Seider
 */
public class DownloadSection extends ValidatingWorkflowNodePropertySection {
    
    private Button downloadFilesCheckbox;
    
    private Button toRceDataManagementCheckbox;
    
    private Button toFileSystemCheckbox;
    
    private Text fileSystemPathText;
    
    private Button selectFileSystemPathButton;
    
    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        TabbedPropertySheetWidgetFactory factory = aTabbedPropertySheetPage.getWidgetFactory();
        
        Section downloadSection = factory.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        downloadSection.setText(Messages.configureDownloadFiles);
        
        Composite downloadParent = factory.createFlatFormComposite(downloadSection);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        downloadParent.setLayout(layout);
        
        createDownloadCheckbox(downloadParent, factory);
        
        Composite downloadArea = factory.createComposite(downloadParent);

        layout = new GridLayout();
        layout.numColumns = 4;
        downloadArea.setLayout(layout);
        
        createTargetCheckboxes(downloadArea, factory);

        downloadSection.setClient(downloadParent);
    }
    
    @Override
    protected Updater createUpdater() {
        return new DownloadUpdater();
    }
    
    private void createDownloadCheckbox(Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        downloadFilesCheckbox = toolkit.createButton(parent, Messages.downloadFiles, SWT.CHECK);
        downloadFilesCheckbox.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_DOWNLOAD);
        downloadFilesCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                setDownloadAreaEnabled(downloadFilesCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }
    
    private void createTargetCheckboxes(Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        toRceDataManagementCheckbox = toolkit.createButton(parent, Messages.toRceDataManagement, SWT.CHECK);
        toRceDataManagementCheckbox.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISRCE);
        
        toFileSystemCheckbox = toolkit.createButton(parent, Messages.toFileSystem, SWT.CHECK);
        toFileSystemCheckbox.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISFILESYSTEM);
        toFileSystemCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                setFileSystemAreaEnabled(toFileSystemCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
        
        GridData gridData = new GridData();
        final int textWidth = 180;
        gridData.widthHint = textWidth;

        fileSystemPathText = toolkit.createText(parent, "");
        fileSystemPathText.setLayoutData(gridData);
        fileSystemPathText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_FILESYSTEMPATH);
        
        selectFileSystemPathButton = toolkit.createButton(parent, Messages.threeDots, SWT.NONE);
        selectFileSystemPathButton.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog selectDirectoryDialog = new DirectoryDialog(DownloadSection.this.getComposite().getShell(), SWT.OPEN);
                selectDirectoryDialog.setText(Messages.toFileSystem);
                String path = selectDirectoryDialog.open();
                if (path != null) {
                    if (path.endsWith("/") || path.endsWith("\\")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    fileSystemPathText.setText(path);
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }
    
    private void setDownloadAreaEnabled(boolean enabled) {
        toRceDataManagementCheckbox.setEnabled(enabled);
        toFileSystemCheckbox.setEnabled(enabled);
        setFileSystemAreaEnabled(toFileSystemCheckbox.getSelection());
    }
    
    private void setFileSystemAreaEnabled(boolean enabled) {
        fileSystemPathText.setEnabled(enabled);
        selectFileSystemPathButton.setEnabled(enabled);
    }
    
    /**
     * Custom {@link DefaultUpdater} extension to synchronize managed GUI elements that need special handling.
     *
     * @author Doreen Seider
     */
    private final class DownloadUpdater extends DefaultUpdater {

        @Override
        public void updateControl(final Control control, final String propertyName, final Serializable newValue,
            final Serializable oldValue) {
            super.updateControl(control, propertyName, newValue, oldValue);
            
            if (control == downloadFilesCheckbox) {
                setDownloadAreaEnabled(downloadFilesCheckbox.getSelection());
            }
        }

    }
    
    
}

