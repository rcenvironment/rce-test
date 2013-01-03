/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.gui.commons;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileInPlaceEditorInput;


/**
 * Helpers for handling of temporary files and workspace files, including dirty-state observing and editor detection.
 *
 * @author Arne Bachmann
 */
public final class EditorsHelper {

    /**
     * Default editor extension (fallback editor).
     */
    private static final String TXT = "txt";
    
    /**
     * "Index of" constant.
     */
    private static final int NOT_FOUND = -1;


    /**
     * This class has only static methods.
     */
    private EditorsHelper() {
        // only static methods
    }
    
    
    /**
     * Helper to determine the file extension.
     * @param filename The file name to check
     * @return the pure extension without a dot or txt as a fallback
     */
    private static String getExtension(final String filename) {
        if (filename == null) {
            return TXT; // default
        }
        final String f = filename.trim();
        final int lastSlash = f.lastIndexOf(File.separator); // works only on current node (same OS)
        final int lastDot = f.lastIndexOf(".");
        if (((lastSlash != NOT_FOUND) && (lastDot != NOT_FOUND)) && (lastDot > lastSlash)) { // found an extension
            return f.substring(lastDot + 1);
        }
        return TXT; // fallback
    }
    
    /**
     * Helper to get just any matching editor.
     * A message dialog pops up if nothing found (!).
     * @param filename The filename to find an editor for
     * @return The editor descriptor
     */
    private static IEditorDescriptor findEditorForFilename(final String filename) {
        
        IEditorDescriptor editor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
        
        if (editor == null) {
            editor = PlatformUI.getWorkbench().getEditorRegistry().getEditors("*." + TXT)[0];
        }
        
        return editor;
    }

    /**
     * Open an in-place editor within the editing view. Falls back to txt-editor.
     * @param ifile The file to open
     * @param callbacks The action to perform whenever save is activated, or null for none
     * @throws PartInitException for eny error
     */
    public static void openFileInEditor(final IFile ifile, final Runnable... callbacks) throws PartInitException {
        if (ifile == null) {
            return;
        }
        final IEditorDescriptor editorDescriptor = findEditorForFilename(ifile.getName());
        final IEditorInput editorInput = new FileInPlaceEditorInput(ifile);
        final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
                editorDescriptor.getId());
        observeEditedFile(ifile, editor, new Runnable() {
            public void run() {
                if (callbacks != null) {
                    for (final Runnable action: callbacks) {
                        action.run();
                    }
                }
            }
        });
    }
    
    /**
     * Helper to open an external file.
     * Set it to read-only if necessary
     * 
     * @param file The file to open (mostly a temp file)
     * @param callbacks The action to perform whenever save is activated, or null for none
     * @throws PartInitException for any error
     */
    public static void openExternalFileInEditor(final File file, final Runnable... callbacks) throws PartInitException {
        final IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getAbsolutePath()));
        final IEditorInput editorInput = new FileStoreEditorInput(fileStore);
        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editor = activePage.openEditor(editorInput, findEditorForFilename(file.getName()).getId());
        observeEditedFile(file, editor, new Runnable() {
            public void run() {
                if (callbacks != null) {
                    for (final Runnable action: callbacks) {
                        action.run();
                    }
                }
            }
        });
    }
    
    /**
     * Observe a file and call the action upon each safe action.
     * @param observedFile The file within the project to observe
     * @param editor The editor we have opened
     * @param callback The action to perform on each safe (but not on undo that makes the editor clean again)
     */
    private static void observeEditedFile(final IFile observedFile, final IEditorPart editor, final Runnable callback) {
        final AtomicLong timeStamp = new AtomicLong(observedFile.getModificationStamp()); // we simply need a final reference here
        editor.addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(final Object source, final int id) {
                if (id == IEditorPart.PROP_DIRTY) {
                    if (!((IEditorPart) source).isDirty()) { // became clean, thus must have been saved
                        final long ts = observedFile.getModificationStamp();
                        if (ts > timeStamp.longValue()) {
                            timeStamp.set(ts);
                            callback.run();
                        }
                    }
                }
            }
        });
    }

    /**
     * Observe a file and call the action upon each safe action.
     * @param observedFile The file in the local filesystem to observe
     * @param editor The editor we have opened
     * @param callback The action to perform on each safe (but not on undo that makes the editor clean again)
     */
    private static void observeEditedFile(final File observedFile, final IEditorPart editor, final Runnable callback) {
        final AtomicLong timeStamp = new AtomicLong(observedFile.lastModified()); // we simply need a final reference here
        editor.addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(final Object source, final int id) {
                if (id == IEditorPart.PROP_DIRTY) {
                    if (!((IEditorPart) source).isDirty()) { // became clean, thus must have been saved
                        final long ts = observedFile.lastModified();
                        if (ts > timeStamp.longValue()) {
                            timeStamp.set(ts);
                            callback.run();
                        }
                    }
                }
            }
        });
    }
    
}
