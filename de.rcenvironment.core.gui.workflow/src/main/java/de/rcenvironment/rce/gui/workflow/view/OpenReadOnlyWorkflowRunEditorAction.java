/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;


/**
 * Opens a workflow instance in an editor.
 *
 * @author Christian Weiss
 */
public class OpenReadOnlyWorkflowRunEditorAction extends Action {
    
    private static File folder;
    
    private final WorkflowInformation workflowInformation;
    
    public OpenReadOnlyWorkflowRunEditorAction(final WorkflowInformation workflowInformation) {
        this.workflowInformation = workflowInformation;
    }
    
    private static File getFolder() {
        if (folder == null) {
            try {
                folder = new TempFileUtils().createTempFileWithFixedFilename("workflow_instances");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            folder.mkdir();
        }
        return folder;
    }
    
    @Override
    public void run() {
        final String filename = String.format("%s.wfr", workflowInformation.getIdentifier());
        final File tempFile = new File(getFolder(), filename);
        final IPath location = new Path(tempFile.getAbsolutePath());
        final IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject("External Files").getFile(location);
        final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        final IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
        IEditorPart editorPart;
        try {
            editorPart = activePage.openEditor(new FileEditorInput(file), editorDescriptor.getId());
        } catch (PartInitException e) {
            throw new RuntimeException(e);
        }
        ((ReadOnlyWorkflowRunEditor) editorPart).setWorkflowInformation(workflowInformation);
    }

}
