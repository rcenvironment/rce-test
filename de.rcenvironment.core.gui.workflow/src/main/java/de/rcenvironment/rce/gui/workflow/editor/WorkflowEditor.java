/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentUtils;
import de.rcenvironment.rce.component.SimpleComponentRegistry;
import de.rcenvironment.rce.component.workflow.PersistentWorkflowDescriptionUpdater;
import de.rcenvironment.rce.component.workflow.WorkflowConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowDescriptionPersistenceHandler;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.parts.EditorEditPartFactory;

/**
 * Editor window which opens when selecting a workflow file in the project explorer.
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 * @author Sascha Zur
 * @author Doreen Seider
 */
public class WorkflowEditor extends GraphicalEditorWithFlyoutPalette implements ITabbedPropertySheetPageContributor {

    /** pref key. */
    public  static final String PREFS_KEY_UPDATEAUTOMATICALLY = "de.rcenvironment.rce.gui.workflow.editor.updateautomatically";

    private TabbedPropertySheetPage tabbedPropertySheetPage;

    private GraphicalViewer viewer;

    private WorkflowDescription workflowDescription;

    private ZoomManager zoomManager;

    public WorkflowEditor() {
        setEditDomain(new DefaultEditDomain(this));
    }

    @Override
    protected PaletteRoot getPaletteRoot() {
        final List<ComponentDescription> cds = new ArrayList<ComponentDescription>();
        IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
        try {
            service.run(false, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.fetchingComponents, 3);
                        monitor.worked(2);
                        cds.addAll(ComponentUtils.eliminateDuplicates(new SimpleComponentRegistry(Activator.getInstance()
                            .getUser()).getAllComponentDescriptions()));
                        monitor.worked(3);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);            
        }
        return new WorkflowPaletteFactory().createPalette(cds);
    }

    @Override
    protected void initializeGraphicalViewer() {

        viewer = getGraphicalViewer();

        viewer.setRootEditPart(new ScalableFreeformRootEditPart());
        viewer.setEditPartFactory(new EditorEditPartFactory());

        KeyHandler keyHandler = new KeyHandler();
        final int del = 127;
        keyHandler.put(KeyStroke.getPressed(SWT.DEL, del, 0),
            getActionRegistry().getAction(ActionFactory.DELETE.getId()));
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer).setParent(keyHandler));

        viewer.setContents(workflowDescription);

        viewer.addDropTargetListener(new TemplateTransferDropTargetListener(viewer));

        ContextMenuProvider cmProvider = new WorkflowEditorContextMenuProvider(viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);
        getSite().registerContextMenu(cmProvider, viewer);

        zoomManager = ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).
            getZoomManager();
        zoomManager.setZoomAnimationStyle(ZoomManager.ANIMATE_ZOOM_IN_OUT);
        viewer.getControl().addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent arg0) {
                if (arg0.stateMask == SWT.CONTROL){
                    int notches = arg0.count;
                    if (notches < 0) {
                        zoomManager.zoomOut();
                    } else {
                        zoomManager.zoomIn();
                    }
                }
            }
        });

        tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
    }

    @Override
    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain()) {

            protected void configurePaletteViewer(PaletteViewer v) {
                super.configurePaletteViewer(v);
                v.addDragSourceListener(new TemplateTransferDragSourceListener(v));
            }
        };
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);

        IFile file = ((IFileEditorInput) input).getFile();
        workflowDescription = loadWorkflowDescription(file);
        initializeWorkflowDescriptionListener();
        setPartName(file.getName());

    }

    protected WorkflowDescription loadWorkflowDescription(final IFile file) {

        try {
            WorkflowDescriptionPersistenceHandler wdHandler = new WorkflowDescriptionPersistenceHandler();
            final int workflowVersion = wdHandler.readWorkflowVersionNumer(file.getContents());
            if (workflowVersion < WorkflowConstants.CURRENT_WORKFLOW_VERSION_NUMBER
                && PersistentWorkflowDescriptionUpdater.isUpdateNeeded(file.getContents(), workflowVersion,
                    Activator.getInstance().getUser())) {
                final int doUpdate = 2;
                int decision = 3;
                IPreferenceStore prefs = Activator.getInstance().getPreferenceStore();
                if (!prefs.getString(PREFS_KEY_UPDATEAUTOMATICALLY).equals(String.valueOf(true))) {
                    MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.incompatibleVersionTitle,
                        Messages.incompatibleVersionMessage,
                        Messages.rememberIncompatibleVersionQuestionDecision,
                        false, prefs, PREFS_KEY_UPDATEAUTOMATICALLY);
                    prefs.putValue(PREFS_KEY_UPDATEAUTOMATICALLY, String.valueOf(dialog.getToggleState()));
                    decision = dialog.getReturnCode();
                }

                if (prefs.getString(PREFS_KEY_UPDATEAUTOMATICALLY).equals(String.valueOf(true)) || decision == doUpdate) {
                    updateWorkflow(workflowVersion, file);
                } else {
                    throw new IllegalArgumentException("incompatible workflow file format");
                }
            }
            return wdHandler.readWorkflowDescriptionFromStream(file.getContents(), Activator.getInstance().getUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateWorkflow(final int workflowVersion, final IFile file) {
        BusyIndicator.showWhile(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyFile(new File(file.getRawLocation().toOSString()),
                        new File(file.getRawLocation().removeFileExtension().toOSString() + "_backup.wf"));
                    file.getProject().refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
                    InputStream tempInputStream = PersistentWorkflowDescriptionUpdater
                        .updatePersistentWorkflowDescription(file.getContents(), workflowVersion,
                            Activator.getInstance().getUser());
                    file.setContents(tempInputStream, true, false, new NullProgressMonitor());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    /**
     * Makes the editor listen to changes in the underlying {@link WorkflowDescription}.
     */
    private void initializeWorkflowDescriptionListener() {
        workflowDescription.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                WorkflowEditor.this.updateDirty();
            }
        });
    }
    protected void updateDirty() {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            IFile file = ((IFileEditorInput) getEditorInput()).getFile();
            WorkflowDescriptionPersistenceHandler wdHandler = new WorkflowDescriptionPersistenceHandler();
            file.setContents(
                new ByteArrayInputStream(wdHandler.writeWorkflowDescriptionToStream(workflowDescription).toByteArray()),
                true, // keep saving, even if IFile is out of sync with the Workspace
                false, // dont keep history
                monitor); // progress monitor
            getCommandStack().markSaveLocation();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // set the dirty state relative to the current command stack position
        getCommandStack().markSaveLocation();
    }

    @Override
    public void doSaveAs() {
        FileDialog fd = new FileDialog(new Shell(), SWT.SAVE);
        fd.setText("Save As...");
        String[] filterExt = { "*.wf" };
        fd.setFilterExtensions(filterExt);
        fd.setFilterPath(System.getProperty("user.dir"));
        String selected = fd.open();
        if (!selected.substring(selected.lastIndexOf('.') + 1).toLowerCase().equals("wf")){
            selected += ".wf";
        }

        try {
            File file = new File(selected);
            FileWriter fw = new FileWriter(file);
            WorkflowDescriptionPersistenceHandler wdHandler = new WorkflowDescriptionPersistenceHandler();
            byte[] stream = wdHandler.writeWorkflowDescriptionToStream(workflowDescription).toByteArray();
            for (int i = 0; i < stream.length; i++){
                fw.append((char) stream[i]); // progress monitor
            }
            fw.flush();
            fw.close();
            getCommandStack().markSaveLocation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // set the dirty state relative to the current command stack position
        getCommandStack().markSaveLocation();
    }
    @Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    @Override
    public String getContributorId() {
        return getSite().getId();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IPropertySheetPage.class) {
            return tabbedPropertySheetPage;
        }  else if (type == IContextProvider.class) {
            return new WorkflowEditorHelpContextProvider(viewer);
        }
        return super.getAdapter(type);
    }

    @Override
    protected FlyoutPreferences getPalettePreferences() {
        FlyoutPreferences prefs = super.getPalettePreferences();
        prefs.setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
        return prefs;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    public GraphicalViewer getViewer() {
        return viewer;
    }

}
