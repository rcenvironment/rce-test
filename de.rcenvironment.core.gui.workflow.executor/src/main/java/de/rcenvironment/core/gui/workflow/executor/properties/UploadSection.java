/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.core.component.executor.FilesToUpload;
import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.AbstractWorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * "Properties" view tab for configuring files to upload before job submission.
 *
 * @author Doreen Seider
 */
public class UploadSection extends ValidatingWorkflowNodePropertySection {
    
    private static Log log = LogFactory.getLog(UploadSection.class);
    
    private Button uploadFilesCheckbox;
    
    private List fileList;
    
    private Button addFilesFromFileSystemButton;
    
    private Button addFilesFromProjectButton;
    
    private Button removeFilesButton;
    
    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        TabbedPropertySheetWidgetFactory factory = aTabbedPropertySheetPage.getWidgetFactory();
        
        Section uploadSection = factory.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        uploadSection.setText(Messages.configureUploadFiles);
        
        Composite uploadParent = factory.createFlatFormComposite(uploadSection);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        uploadParent.setLayout(layout);
        
        createUploadCheckbox(uploadParent, factory);
        
        Composite uploadArea = factory.createComposite(uploadParent);

        layout = new GridLayout();
        layout.numColumns = 2;
        uploadArea.setLayout(layout);
        
        createFilesList(uploadArea, factory);
        
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.BEGINNING;
        
        final Composite buttonComposite = factory.createComposite(uploadArea);
        buttonComposite.setLayoutData(gridData);

        layout = new GridLayout();
        layout.numColumns = 1;
        buttonComposite.setLayout(layout);
        
        createAddFilesFromFileSystemButton(buttonComposite, factory);
        
        createAddFilesFromProjectButton(buttonComposite, factory);
        
        createRemoveFilesButton(buttonComposite, factory);
        
        uploadSection.setClient(uploadParent);

    }
    
    private void createUploadCheckbox(Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        uploadFilesCheckbox = toolkit.createButton(parent, Messages.uploadFiles, SWT.CHECK);
        uploadFilesCheckbox.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_UPLOAD);
        uploadFilesCheckbox.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                setUploadAreaEnabled(uploadFilesCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }
    
    private void createFilesList(Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        final int listWidth = 200;
        final int listHeight = 250;
        GridData gridData = new GridData();
        gridData.widthHint = listWidth;
        gridData.heightHint = listHeight;

        fileList = toolkit.createList(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        fileList.setLayoutData(gridData);
        fileList.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD);
        fileList.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                setUploadAreaEnabled(uploadFilesCheckbox.getSelection());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }
    
    private void createAddFilesFromFileSystemButton(final Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        addFilesFromFileSystemButton = toolkit.createButton(parent, Messages.addFromFileSystem, SWT.NONE);
        addFilesFromFileSystemButton.setLayoutData(gridData);
    }
    
    private void createAddFilesFromProjectButton(final Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        addFilesFromProjectButton = toolkit.createButton(parent, Messages.addFromProject, SWT.NONE);
        addFilesFromProjectButton.setLayoutData(gridData);
    }
    
    private void createRemoveFilesButton(final Composite parent, TabbedPropertySheetWidgetFactory toolkit) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        removeFilesButton = toolkit.createButton(parent, Messages.remove, SWT.NONE);
        removeFilesButton.setLayoutData(gridData);
    }
    
    @Override
    protected Updater createUpdater() {
        return new UploadUpdater();
    }
    
    @Override
    protected Controller createController() {
        return new UploadController();
    }
    
    @Override
    protected Synchronizer createSynchronizer() {
        return new UploadSynchronizer();
    }
    
    private void setUploadAreaEnabled(boolean enabled) {
        fileList.setEnabled(enabled);
        addFilesFromFileSystemButton.setEnabled(enabled);
        addFilesFromProjectButton.setEnabled(enabled);
        if (!enabled) {
            removeFilesButton.setEnabled(enabled);
        } else {
            setRemoveButtonState();        
        }
    }
    
    private void setRemoveButtonState() {
        removeFilesButton.setEnabled(fileList.getSelectionCount() > 0);
    }
    
    /**
     * Custom {@link DefaultUpdater} extension to synchronize managed GUI elements that need special handling.
     *
     * @author Doreen Seider
     */
    private final class UploadUpdater extends DefaultUpdater {

        @Override
        public void updateControl(final Control control, final String propertyName, final Serializable newValue,
            final Serializable oldValue) {
            super.updateControl(control, propertyName, newValue, oldValue);
            
            if (control == uploadFilesCheckbox) {
                setUploadAreaEnabled(uploadFilesCheckbox.getSelection());
            } else if (control == fileList) {
                updateFileList(getProperty(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, String.class));
            }
        }

    }
    
    /**
     * Custom {@link DefaultSynchronizer} implementation to handle the changes in the model and
     * react accordingly through synchronizing the GUI to reflect those changes.
     * 
     * @author Doreen Seider
     */
    private final class UploadSynchronizer extends DefaultSynchronizer {

        @Override
        public void handlePropertyChange(final String propertyName, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(propertyName, newValue, oldValue);
            
            if (propertyName.equals(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD)) {
                updateFileList((String) newValue);
            }
        }

    }
    
    private void updateFileList(String rawFilesToUpload) {
        if (rawFilesToUpload == null || rawFilesToUpload.isEmpty()) {
            rawFilesToUpload = SshExecutorConstants.EYMPTY_FILE_LIST_IN_JSON;
        }
        FilesToUpload filesToUpload = FilesToUpload.valueAs((String) rawFilesToUpload);
        fileList.removeAll();
        java.util.List<String> filenames = filesToUpload.getFileNames();
        Collections.sort(filenames);
        for (String filename : filenames) {
            fileList.add(filename);
        }
    }
    
    /**
     * Custom {@link DefaultController} implementation to handle the activation of the GUI
     * controls.
     * 
     * @author Doreen Seider
     */
    private final class UploadController extends DefaultController {

        @Override
        protected void widgetSelected(final SelectionEvent event, final Control source) {
            super.widgetSelected(event, source);
            
            if (source == addFilesFromFileSystemButton) {
                selectFilesFromFileSystem(source);
            } else if (source == addFilesFromProjectButton) {
                selectFilesFromProject(source);
            } else if (source == removeFilesButton) {
                removeFiles(source);
            }
        }

    }
    
    private void selectFilesFromFileSystem(Control source) {
        FileDialog selectFilesDialog = new FileDialog(source.getShell(), SWT.OPEN | SWT.MULTI);
        selectFilesDialog.setText(Messages.addFromFileSystem);
        String path = selectFilesDialog.open();
        if (path != null) {
            String[] filenames = selectFilesDialog.getFileNames();
            String filterPath = selectFilesDialog.getFilterPath();
            File[] selectedFiles = new File[filenames.length];

            int i = 0;
            for (String filename : filenames) {
                
                if (filterPath != null && filterPath.trim().length() > 0) {
                    selectedFiles[i++] = new File(filterPath, filename);
                } else {
                    selectedFiles[i++] = new File(filename);
                }
            }
            final AddFilesToUploadCommand command = createAddFilesToUploadCommand(selectedFiles);
            execute(command);
        }
    }
    
    private void selectFilesFromProject(Control source) {
        final ElementTreeSelectionDialog selectionDialog = new ElementTreeSelectionDialog(source.getShell(),
            new WorkbenchLabelProvider(),
            new BaseWorkbenchContentProvider()) {
            @Override
            protected void updateOKStatus() {
                Object[] elements = getResult();
                if (elements.length == 0) {
                    getOkButton().setEnabled(false);
                } else {
                    for (Object element : elements) {
                        if (!(element instanceof IFile)) {
                            getOkButton().setEnabled(false);
                            return;
                        }
                    }
                    getOkButton().setEnabled(true);                    
                }
            }
        };
        selectionDialog.setTitle(Messages.addFromProject);
        selectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if (selectionDialog.open() == ElementTreeSelectionDialog.OK) {
            Object[] result = selectionDialog.getResult();
            File[] selectedFiles = new File[result.length];
            int i = 0;
            for (Object object : result) {
                if (object instanceof IFile) {
                    selectedFiles[i++] = ((IFile) object).getRawLocation().makeAbsolute().toFile();
                } else {
                    MessageDialog.openInformation(source.getShell(), Messages.dialogTitleNotSupported, Messages.dialogMessageNotSupported);
                    return;
                }
            }
            final AddFilesToUploadCommand command = createAddFilesToUploadCommand(selectedFiles);
            execute(command);
        }
    }
    
    private void removeFiles(Control source) {
        String[] selectedFilenames = fileList.getSelection();
        final RemoveFilesToUploadCommand command = createRemoveFilesToUploadCommand(selectedFilenames);
        execute(command);
    }
    
    private AddFilesToUploadCommand createAddFilesToUploadCommand(File[] selectedFiles) {
        return new AddFilesToUploadCommand(getProperty(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, String.class),
            selectedFiles);
    }
    
    private RemoveFilesToUploadCommand createRemoveFilesToUploadCommand(String[] selectedFiles) {
        return new RemoveFilesToUploadCommand(getProperty(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, String.class),
            selectedFiles);
    }
    
    /**
     * Workflow command adding files.
     * @author Doreen Seider
     */
    private final class AddFilesToUploadCommand extends FilesToUploadCommand {

        private final File[] selectedFiles;

        private AddFilesToUploadCommand(String oldFiles, File[] selectedFiles) {
            super(oldFiles);
            this.selectedFiles = selectedFiles;
        }
        
        protected String createRawFileToUpdate() {
            FilesToUpload filesToUpload = FilesToUpload.valueAs(oldFiles);
            try {
                filesToUpload.addFiles(selectedFiles);
            } catch (IOException e) {
                log.error("Adding files failed", e);
                undo2();
            }
            return filesToUpload.toString();
        }

    }
    
    /**
     * Workflow command removing files.
     * @author Doreen Seider
     */
    private final class RemoveFilesToUploadCommand extends FilesToUploadCommand {

        private final String[] selectedFilenames;

        private RemoveFilesToUploadCommand(String oldFiles, String[] selectedFilenames) {
            super(oldFiles);
            this.selectedFilenames = selectedFilenames;
        }
        
        protected String createRawFileToUpdate() {
            FilesToUpload filesToUpload = FilesToUpload.valueAs(oldFiles);
            filesToUpload.removeFiles(selectedFilenames);
            return filesToUpload.toString();
        }

    }
    
    /**
     * Abstract workflow command for adding and removing files.
     * @author Doreen Seider
     */
    private abstract class FilesToUploadCommand extends AbstractWorkflowNodeCommand {

        protected final String oldFiles;

        private FilesToUploadCommand(String oldFiles) {
            if (oldFiles == null || oldFiles.isEmpty()) {
                this.oldFiles = SshExecutorConstants.EYMPTY_FILE_LIST_IN_JSON;
            } else {
                this.oldFiles = oldFiles;                
            }
        }

        @Override
        protected void execute2() {
            setProperty(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, createRawFileToUpdate());
        }
        
        protected abstract String createRawFileToUpdate();

        @Override
        protected void undo2() {
            setProperty(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, oldFiles);
        }

    }
}

