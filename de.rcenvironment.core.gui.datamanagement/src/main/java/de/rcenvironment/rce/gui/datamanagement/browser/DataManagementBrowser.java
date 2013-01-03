/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.Session;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.SimpleFileDataService;
import de.rcenvironment.rce.datamanagement.SimpleQueryService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeType;
import de.rcenvironment.rce.gui.datamanagement.commons.DataManagementWorkbenchUtils;

/**
 * A visual tree-based navigator for data represented in the RCE data management.
 * 
 * @author Markus Litz
 * @author Robert Mischke
 */
public class DataManagementBrowser extends ViewPart implements DMBrowserNodeContentAvailabilityHandler {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "de.rcenvironment.rce.gui.datamanagement.browser.DataManagementBrowser";

    private static final String ROOT_NODE_TITLE = "<root>";

    protected final Log log = LogFactory.getLog(getClass());

    private final Set<DMBrowserNode> expandedElements = new HashSet<DMBrowserNode>();

    private TreeViewer viewer;

    private DrillDownAdapter drillDownAdapter;

    private DMContentProvider contentProvider;

    private Action sortAscendingName;

    private Action actionRefreshAll;

    private Action actionOpenInEditor;

    private Action doubleClickAction;

    private Action deleteNodeAction;

    private Action saveNodeAction;

    private RefreshNodeAction refreshNodeAction;

    private CollapseAllNodesAction collapseAllNodesAction;

    private User user;

    /**
     * FileDataService for storing/loading resources to the data management.
     */
    private SimpleFileDataService dataService;

    private SimpleQueryService queryService;

    private IAction sortDescendingName;

    private IAction sortTimestampAsc;

    private int sortOrderType;

    private IAction compareAction;

    private Action sortTimestampDesc;

    /**
     * An {@link Action} to save data management entries to local files.
     * 
     * @author Christian Weiss
     * 
     */
    private final class CustomSaveAction extends SelectionProviderAction {

        private final List<DMBrowserNode> selectedNodes = new LinkedList<DMBrowserNode>();

        private Display display;

        private final List<DMBrowserNodeType> savableNodeTypes = new ArrayList<DMBrowserNodeType>();

        /*
         * Set all savable DMBrowserNodeTypes.
         */
        {
            savableNodeTypes.add(DMBrowserNodeType.Timeline);
            savableNodeTypes.add(DMBrowserNodeType.Components);
            savableNodeTypes.add(DMBrowserNodeType.Component);
            savableNodeTypes.add(DMBrowserNodeType.HistoryRoot);
            savableNodeTypes.add(DMBrowserNodeType.HistoryObject);
            savableNodeTypes.add(DMBrowserNodeType.Folder);
            savableNodeTypes.add(DMBrowserNodeType.DMFileResource);
            savableNodeTypes.add(DMBrowserNodeType.Resource);
        }

        private CustomSaveAction(ISelectionProvider provider, String text) {
            super(provider, text);
        }

        public void selectionChanged(final IStructuredSelection selection) {
            // clear the old selection
            selectedNodes.clear();
            // the 'save' action is only enabled, if a DataService is
            // connected to delegate the deletion request to and the
            // selected is not empty
            boolean enabled = dataService != null && !selection.isEmpty();
            if (enabled) {
                @SuppressWarnings("unchecked")
                final Iterator<DMBrowserNode> iter = selection.iterator();
                while (iter.hasNext()) {
                    DMBrowserNode selectedNode = iter.next();
                    if (savableNodeTypes.contains(selectedNode.getType())) {
                        selectedNodes.add(selectedNode);
                    }
                }
                // action is only enabled, if at least one node is deletable
                // according to the deletable DMBrowserNodeTypes list
                enabled &= !selectedNodes.isEmpty();
                // action is only enabled if a potential content node is
                // selected
                enabled = mightHaveContent(selectedNodes);
                // store the Display to show the DirectoryDialog in 'run'
                display = Display.getCurrent();
            }
            setEnabled(enabled);
            if (selection.size() == 2){
                @SuppressWarnings("unchecked")
                final Iterator<DMBrowserNode> iter = selection.iterator();
                boolean compareEnabled = true;
                while (iter.hasNext()){
                    if (iter.next().getNumChildren() != 0){
                        compareEnabled = false;
                    }
                }
                compareAction.setEnabled(compareEnabled);
            } else {
                compareAction.setEnabled(false);
            }
        }

        public void run() {
            final List<DMBrowserNode> browserNodesToSave = new LinkedList<DMBrowserNode>(
                selectedNodes);
            DirectoryDialog directoryDialog = new DirectoryDialog(
                display.getActiveShell());
            final String directoryPath = directoryDialog.open();
            if (directoryPath == null) {
                return;
            }
            final File directory = new File(directoryPath);
            final Job job = new Job(String.format("saving %d nodes: %s",
                browserNodesToSave.size(),
                browserNodesToSave.toString())) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    // delete the nodes recursively
                    for (final DMBrowserNode browserNodeToSave : browserNodesToSave) {
                        save(browserNodeToSave, directory);
                    }
                    return Status.OK_STATUS;
                }

                private void save(final DMBrowserNode browserNode,
                    final File directory) {
                    // get the current DataReference and delete it, if it is
                    // not null (null DataReferences are used for
                    // aggregating tree items)
                    final String dataReferenceId = browserNode
                        .getDataReferenceId();
                    String filename = browserNode.getAssociatedFilename();
                    if (filename == null) {
                        filename = browserNode.getTitle();
                    }
                    filename = filename.replaceAll(
                        "[^-\\s\\(\\)._a-zA-Z0-9]", "_");
                    final File nodeFile = findUniqueFilename(directory,
                        filename);
                    if (!browserNode.areChildrenKnown()
                        || browserNode.getNumChildren() > 0) {
                        nodeFile.mkdir();
                        // save children
                        for (final DMBrowserNode child : contentProvider
                            .getChildren(browserNode)) {
                            // recur
                            save(child, nodeFile);
                        }
                    } else {
                        save(dataReferenceId, nodeFile.getName(), directory);
                    }
                }

                private File findUniqueFilename(final File directory,
                    final String filename) {
                    File result = new File(directory, filename);
                    if (!result.exists()) {
                        return result;
                    }
                    String prefix = filename;
                    String postfix = "";
                    final Pattern pattern = Pattern
                        .compile("^(.*)\\.([a-zA-Z0-9]+)$");
                    final Matcher matcher = pattern.matcher(filename);
                    if (matcher.matches()) {
                        prefix = matcher.group(1);
                        postfix = "." + matcher.group(2);
                    }
                    int i = 0;
                    do {
                        ++i;
                        result = new File(directory, String.format(
                            "%s (%d)%s", prefix, i, postfix));
                    } while (result.exists());
                    return result;
                }

                private void save(final String dataReferenceId, final String filename,
                    final File directory) {
                    try {
                        DataManagementWorkbenchUtils.saveReferenceToFile(
                            dataReferenceId, //
                            new File(directory,
                                filename).getAbsolutePath(),
                                user);
                    } catch (NullPointerException e) {
                        // FIXME: log and warn
                        e = null;
                    } catch (AuthorizationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            };
            job.setUser(true);
            job.schedule();
        }
    }

    /**
     * An {@link Action} to delete data management entries.
     * 
     * @author Christian Weiss
     * 
     */
    private final class CustomDeleteAction extends SelectionProviderAction {

        private final List<DMBrowserNode> selectedNodes = new LinkedList<DMBrowserNode>();

        private Display display;

        private final List<DMBrowserNodeType> deletableNodeTypes = new ArrayList<DMBrowserNodeType>();

        /*
         * Set all deletable DMBrowserNodeTypes.
         */
        {
            deletableNodeTypes.add(DMBrowserNodeType.Workflow);
        }

        private CustomDeleteAction(ISelectionProvider provider, String text) {
            super(provider, text);
        }

        public void selectionChanged(final IStructuredSelection selection) {
            // clear the old selection
            selectedNodes.clear();
            // the 'delete' action is only enabled, if a DataService is
            // connected to delegate the deletion request to and the
            // selected is not empty
            boolean enabled = dataService != null && !selection.isEmpty();
            if (enabled) {
                @SuppressWarnings("unchecked")
                final Iterator<DMBrowserNode> iter = selection.iterator();
                while (iter.hasNext()) {
                    DMBrowserNode selectedNode = iter.next();
                    if (deletableNodeTypes.contains(selectedNode.getType())) {
                        selectedNodes.add(selectedNode);
                    }
                }
                // action is only enabled, if at least one node is deletable
                // according to the deletable DMBrowserNodeTypes list
                enabled &= !selectedNodes.isEmpty();
                // action is only enabled if a potential content node is
                // selected
                enabled &= mightHaveContent(selectedNodes);
                // store the Display to refresh the tree viewer in 'run'
                display = Display.getCurrent();
            }
            setEnabled(enabled);
        }

        public void run() {
            final List<DMBrowserNode> browserNodesToDelete = new LinkedList<DMBrowserNode>(
                selectedNodes);
            final Job job = new Job(String.format(Messages.jobTitleDelete,
                browserNodesToDelete.size(),
                browserNodesToDelete.toString())) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    // stores the parent nodes to update the tree and remove
                    // deleted child nodes
                    final List<DMBrowserNode> parentNodes = new LinkedList<DMBrowserNode>();
                    for (final DMBrowserNode browserNodeToDelete : browserNodesToDelete) {
                        // get the parent node of the node to update the tree
                        // and remove the deleted child node
                        final DMBrowserNode parentNode = browserNodeToDelete
                            .getParent();
                        parentNodes.add(parentNode);
                        // delete the node recursively
                        boolean deleted = delete(browserNodeToDelete);
                        // remove the node from its parent node if the deletion
                        // was successful
                        // otherwise abort the deletion
                        if (deleted) {
                            parentNode.removeChild(browserNodeToDelete);
                        } else {
                            break;
                        }
                    }
                    // update the tree
                    display.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            for (final DMBrowserNode parent : parentNodes) {
                                // complete refresh required to refresh the whole set of meta data
                                // the content provider builds the tree with
                                refresh();
                            }
                        }
                    });
                    // return OK as status
                    return Status.OK_STATUS;
                }

                private boolean delete(final DMBrowserNode browserNode) {
                    // stores the child nodes to remove them from the parent
                    // node after iterating over the child nodes (otherwise
                    // ~ ConcurrentModificationException)
                    final List<DMBrowserNode> childrenToRemove = new LinkedList<DMBrowserNode>();
                    
                    // delete children first to avoid inconsistencies due to
                    // the lack of transactions
                    for (final DMBrowserNode child : contentProvider.getChildren(browserNode, false)) {
                        // recur deletion
                        boolean childDeleted = delete(child);
                        // if the child was deleted also store the child
                        // node to have its reference removed from the
                        // parent DMBrowserNode so it is not displayed in
                        // the tree any more
                        if (childDeleted) {
                            childrenToRemove.add(child);
                        } else {
                            // in case the deletion was not successful,
                            // abort the deletion process
                            return false;
                        }
                    }
                    // remove the child node references of the deleted child
                    // nodes
                    for (final DMBrowserNode child : childrenToRemove) {
                        browserNode.removeChild(child);
                    }
                    // get the current DataReference and delete it, if it is
                    // not null (null DataReferences are used for
                    // aggregating tree items)
                    // - dataReferenceId is set for files
                    // - dataReference is set for component executions
                    // - otherwise none of both is set
                    final String dataReferenceId = browserNode
                        .getDataReferenceId();
                    DataReference dataReference = browserNode
                        .getDataReference();
                    if (dataReference == null && dataReferenceId != null) {
                        dataReference = queryService.getReference(UUID
                            .fromString(dataReferenceId));
                    }
                    if (dataReference != null) {
                        boolean dataDeleted = delete(dataReference);
                        if (!dataDeleted) {
                            return false;
                        }
                    }
                    return true;
                }

                private boolean delete(final DataReference dataReference) {
                    boolean deleted = true;
                    try {
                        dataService.deleteReference(dataReference);
                    } catch (RuntimeException e) {
                        log.error("Error while trying to delete a data entry", e);
                        deleted = false;
                    }
                    return deleted;
                }

            };
            // job is a UI task
            job.setUser(true);
            // schedule deletion, if all nodes have no children (the nodes
            // themselves are a data entities) or the user confirms the
            // recursive deletion
            boolean recursive = false;
            for (final DMBrowserNode browserNodeToDelete : browserNodesToDelete) {
                if (!browserNodeToDelete.areChildrenKnown()
                    || browserNodeToDelete.getNumChildren() > 0) {
                    recursive = true;
                }
            }
            boolean schedule = true;
            if (recursive) {
                final Shell shell = Display.getCurrent().getActiveShell();
                if (!MessageDialog.openConfirm(shell, Messages.dialogTitleDelete, Messages.dialogMessageDelete)) {
                    schedule = false;
                }
            }
            if (schedule) {
                job.schedule();
            }
        }
    }

    /**
     * An {@link Action} that opens the data associated with the selected node in a read-only
     * editor.
     */
    private final class OpenInEditorAction extends SelectionProviderAction {

        private OpenInEditorAction(ISelectionProvider provider, String text) {
            super(provider, text);
        }

        @Override
        public void selectionChanged(IStructuredSelection selection) {
            Object obj = selection.getFirstElement();
            if (obj instanceof DMBrowserNode) {
                DMBrowserNode node = (DMBrowserNode) obj;
                String dataReferenceId = node.getDataReferenceId();
                if (node.getType() != DMBrowserNodeType.HistoryObject && dataReferenceId != null) {
                    setEnabled(true);
                    return;
                }
            }
            setEnabled(false);
        }

        public void run() {
            ISelection selection = viewer.getSelection();
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof DMBrowserNode) {
                DMBrowserNode node = (DMBrowserNode) obj;
                String dataReferenceId = node.getDataReferenceId();
                String associatedFilename = node.getAssociatedFilename();
                if (dataReferenceId == null) {
                    // TODO improve
                    showMessage("Not a valid entry for 'open' action");
                    return;
                }

                if (associatedFilename == null) {
                    associatedFilename = "default";
                }

                Exception exception;
                try {
                    // get default certificate
                    User proxyCertificate = Session.getInstance().getUser();
                    // try to open in editor
                    DataManagementWorkbenchUtils.tryOpenDataReferenceInReadonlyEditor(dataReferenceId, associatedFilename,
                        proxyCertificate);
                    // ok -> return
                    return;
                } catch (AuthenticationException e) {
                    exception = e;
                }
                showMessage("Failed to open entry in editor: " + exception.toString());
            }
        }
    }

    /**
     * An {@link Action} that triggers a refresh of the selected node.
     * 
     */
    private final class RefreshNodeAction extends SelectionProviderAction {

        private final List<DMBrowserNode> selectedNodes = new LinkedList<DMBrowserNode>();

        private RefreshNodeAction(ISelectionProvider provider, String text) {
            super(provider, text);
        }
        public void selectionChanged(final IStructuredSelection selection) {
            // clear the old selection
            selectedNodes.clear();
            @SuppressWarnings("unchecked")
            final Iterator<DMBrowserNode> iter = selection.iterator();
            while (iter.hasNext()) {
                DMBrowserNode selectedNode = iter.next();
                selectedNodes.add(selectedNode);
            }
            setEnabled(!selectedNodes.isEmpty());
        }

        public void run() {
            for (final DMBrowserNode node : selectedNodes) {
                refresh(node);
            }
        }
    }

    /**
     * An {@link Action} that collapses all nodes.
     *
     * @author Christian Weiss
     */
    private final class CollapseAllNodesAction extends Action {

        @Override
        public void run() {
            viewer.collapseAll();
        }

    }



    /**
     * The constructor.
     */
    public DataManagementBrowser() { 
        sortOrderType = 0;
    }

    private void refresh(final DMBrowserNode node) {
        // clear children of selected node
        node.clearChildren();
        // refresh node in viewer
        viewer.refresh(node);
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        try {
            contentProvider = new DMContentProvider();
            contentProvider.addContentAvailabilityHandler(this);
            viewer.setContentProvider(contentProvider);
        } catch (AuthenticationException e) {
            // FIXME
            log.error(e);
        }
        viewer.setLabelProvider(new DMLabelProvider());
        // viewer.setSorter(new NameSorter());

        getSite().setSelectionProvider(viewer);

        // FIXME: re-enable?
        // Create the help context id for the viewer's control
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),"FIXME");
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();

        initialize();
    }

    private void initialize() {
        user = null;
        try {
            user = de.rcenvironment.rce.authentication.Session
                .getInstance().getUser();
            dataService = new SimpleFileDataService(user);
            queryService = new SimpleQueryService(user);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
        sortOrderType = DMTreeSorter.SORT_BY_TIMESTAMP_DESC;
        refresh();
    }

    private DMBrowserNode createRootNode() {
        DMBrowserNode rootNode = new DMBrowserNode(ROOT_NODE_TITLE);
        rootNode.setType(DMBrowserNodeType.HistoryRoot);
        return rootNode;
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                DataManagementBrowser.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(actionRefreshAll);
        manager.add(new Separator());
        manager.add(actionOpenInEditor);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(new Separator());    
        manager.add(actionOpenInEditor);
        manager.add(refreshNodeAction);
        manager.add(actionRefreshAll);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(new Separator());
        manager.add(saveNodeAction);
        manager.add(new Separator());
        manager.add(deleteNodeAction);
        manager.add(new Separator());
        manager.add(compareAction);
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(sortAscendingName);
        manager.add(sortDescendingName);
        manager.add(sortTimestampAsc);
        manager.add(sortTimestampDesc);
        manager.add(new Separator());
        manager.add(actionRefreshAll);
        manager.add(collapseAllNodesAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions() {

        final ISelectionProvider selectionProvider = getSite().getSelectionProvider();
        sortAscendingName = new Action(Messages.sortUp, IAction.AS_CHECK_BOX) {
            public void run(){
                sortOrderType = DMTreeSorter.SORT_BY_NAME_ASC;
                sortTimestampAsc.setChecked(false);
                sortTimestampDesc.setChecked(false);
                sortDescendingName.setChecked(false);
                refresh();
            }
        };
        sortAscendingName.setImageDescriptor(DMBrowserImages.IMG_SORT_DOWN);
        sortDescendingName = new Action(Messages.sortDown, IAction.AS_CHECK_BOX) {
            public void run(){
                sortOrderType = DMTreeSorter.SORT_BY_NAME_DESC;   
                sortTimestampAsc.setChecked(false);
                sortTimestampDesc.setChecked(false);
                sortAscendingName.setChecked(false);
                refresh();
            }
        };

        sortDescendingName.setImageDescriptor(DMBrowserImages.IMG_SORT_UP);

        sortTimestampAsc = new Action(Messages.sortTime, IAction.AS_CHECK_BOX) {
            public void run(){
                sortOrderType = DMTreeSorter.SORT_BY_TIMESTAMP;
                sortTimestampDesc.setChecked(false);
                sortDescendingName.setChecked(false);
                sortAscendingName.setChecked(false);
                refresh();
            }
        };
        sortTimestampAsc.setImageDescriptor(DMBrowserImages.IMG_SORT_TIMESTAMPUP);
        sortTimestampDesc = new Action(Messages.sortTimeDesc, IAction.AS_CHECK_BOX) {
            public void run(){
                sortOrderType = DMTreeSorter.SORT_BY_TIMESTAMP_DESC;
                sortTimestampAsc.setChecked(false);
                sortDescendingName.setChecked(false);
                sortAscendingName.setChecked(false);
                refresh();

            }
        };
        sortTimestampDesc.setImageDescriptor(DMBrowserImages.IMG_SORT_TIMESTAMPDOWN);
        compareAction = new Action(){
            @SuppressWarnings("deprecation")
            public void run(){
                Iterator<DMBrowserNode> it = ((IStructuredSelection) viewer.getSelection()).iterator();
                Object obj = it.next();
                Object obj2 = it.next();
                if (obj instanceof DMBrowserNode && obj2 instanceof DMBrowserNode) {
                    DMBrowserNode node = (DMBrowserNode) obj;
                    DMBrowserNode node2 = (DMBrowserNode) obj2;
                    String dataReferenceId = node.getDataReferenceId();
                    String associatedFilename = node.getAssociatedFilename();
                    String dataReferenceId2 = node2.getDataReferenceId();
                    String associatedFilename2 = node2.getAssociatedFilename();
                    if (dataReferenceId == null || dataReferenceId2 == null) {
                        return;
                    } else {
                        Exception exception;

                        try {
                            final File left = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(associatedFilename);
                            final File right = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(associatedFilename2);
                            try {
                                DataManagementWorkbenchUtils.dataManagementService.copyReferenceToLocalFile(
                                    Session.getInstance().getUser(), dataReferenceId, left);
                                DataManagementWorkbenchUtils.dataManagementService.copyReferenceToLocalFile(
                                    Session.getInstance().getUser(), dataReferenceId2, right);

                            } catch (AuthorizationException e) {
                                exception = e;
                            } catch (AuthenticationException e) {
                                exception = e;
                            }


                            final CompareConfiguration cc = new CompareConfiguration();
                            cc.setLeftLabel(left.getName());
                            cc.setRightLabel(right.getName());
                            CompareUI.openCompareEditor(new FileCompareInput(cc, left, right));
                        } catch (IOException e) {
                            exception = e;
                        }
                    }
                }
            }
        };
        compareAction.setText(Messages.compareMsg);
        compareAction.setEnabled(false);
        // an action to refresh all view contents
        actionRefreshAll = new Action() {

            public void run() {
                refresh();
            }
        };
        actionRefreshAll.setText(Messages.refreshAllNodesActionContextMenuLabel);
        actionRefreshAll.setImageDescriptor(DMBrowserImages.IMG_DESC_REFRESH);

        // an action to open a selected entry in a read-only editor
        actionOpenInEditor = new OpenInEditorAction(selectionProvider, "Open in Editor (Read-Only)");
        actionOpenInEditor.setToolTipText("Action 2 tooltip");
        actionOpenInEditor.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        // default double-click action
        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                showMessage("Double-click detected on " + obj.toString());
            }
        };

        doubleClickAction = actionOpenInEditor;

        deleteNodeAction = new CustomDeleteAction(selectionProvider, Messages.deleteNodeActionContextMenuLabel);

        saveNodeAction = new CustomSaveAction(selectionProvider, Messages.saveNodeActionContextMenuLabel);

        refreshNodeAction = new RefreshNodeAction(selectionProvider, Messages.refreshNodeActionContextMenuLabel);

        //        collapseAllNodesAction = new CollapseAllNodesAction(selectionProvider, Messages.collapseAllNodesActionContextMenuLabel);
        collapseAllNodesAction = new CollapseAllNodesAction();
        collapseAllNodesAction.setImageDescriptor(DMBrowserImages.IMG_DESC_COLLAPSE_ALL);
    }
   
    private void refresh() {
        contentProvider.clear();
        // disable the widget
        viewer.getTree().setEnabled(false);
        viewer.setSorter(new DMTreeSorter(sortOrderType));
        // disable the action
        // FIXME: ensure re-enabling upon errors
        actionRefreshAll.setEnabled(false);
        DMBrowserNode rootNode = (DMBrowserNode) viewer.getInput();
        if (rootNode == null) {
            rootNode = createRootNode();
            viewer.setInput(rootNode);
        } else {
            rootNode.clearChildren();
        }

        expandedElements.clear();
        for (final Object nodeObject : viewer.getExpandedElements()) {
            if (nodeObject instanceof DMBrowserNode) {
                final DMBrowserNode node = (DMBrowserNode) nodeObject;
                expandedElements.add(node);
            }
        }

        // remove all expanded Elements a parent of which is not expanded
        // (hidden expanded elements)
        final List<DMBrowserNode> hiddenExpandedElements = new LinkedList<DMBrowserNode>();
        for (final DMBrowserNode node : expandedElements) {
            DMBrowserNode parent = node.getParent();
            while (parent != null && parent.getParent() != null) {
                if (!expandedElements.contains(parent)) {
                    hiddenExpandedElements.add(node);
                }
                parent = parent.getParent();
            }
        }
        for (final DMBrowserNode node : hiddenExpandedElements) {
            expandedElements.remove(node);
        }
        if (sortOrderType == DMTreeSorter.SORT_BY_TIMESTAMP_DESC){
            sortTimestampAsc.setChecked(false);
            sortDescendingName.setChecked(false);
            sortAscendingName.setChecked(false);
            sortTimestampDesc.setChecked(true);
        }
        viewer.refresh();

    }
    
    private boolean mightHaveContent(final DMBrowserNode node) {
        if (node.getDataReference() != null
            || node.getDataReferenceId() != null) {
            return true;
        }
        if (node.areChildrenKnown()) {
            return mightHaveContent(node.getChildren());
        } else {
            // if the child nodes are unknown, the current node *might* have
            // content
            return true;
        }
    }

    private boolean mightHaveContent(final Collection<DMBrowserNode> nodes) {
        boolean result = false;
        for (final DMBrowserNode node : nodes) {
            if (mightHaveContent(node)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(),
            "Data Management Browser", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void handleContentAvailable(final DMBrowserNode node) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                viewer.refresh(node);
                if (node == viewer.getInput()) {
                    viewer.getTree().setEnabled(true);
                    actionRefreshAll.setEnabled(true);
                }
                for (final DMBrowserNode child : node.getChildren()) {
                    if (expandedElements.contains(child)) {
                        viewer.expandToLevel(child, TreeViewer.ALL_LEVELS);
                    }
                }
            }
        });
    }

    @Override
    public void handleContentRetrievalError(final DMBrowserNode node, final Exception cause) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                /* 
                 * No refresh of the node as a refresh would trigger another
                 * fetch which might result in the very same error.
                 */
                if (node == viewer.getInput()) {
                    viewer.getTree().setEnabled(true);
                    actionRefreshAll.setEnabled(true);
                }
                DataManagementBrowser.this.log.error(String.format("Failed to retrieve content for node '%s':", node), cause);
                // report error to GUI
                final ISafeRunnable runnable = new SafeRunnable() {

                    @Override
                    public void run() throws Exception {
                        throw cause;
                    }
                };
                SafeRunnable.run(runnable);
            }

        });
    };

  
    /**
     * 
     * An Item to compare.
     *
     * @author Sascha Zur
     */
    class FileCompareInput extends CompareEditorInput{
        private File left;
        private File right;
        private CompareConfiguration cc;

        public FileCompareInput(CompareConfiguration cc, File left, File right) {
            super(cc);
            this.cc = cc;
            this.left = left;
            this.right = right;
        }

        @Override
        protected Object prepareInput(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
            DiffNode result = new DiffNode(Differencer.CONFLICTING);
            result.setAncestor(new CompareItem(left));
            result.setLeft(new CompareItem(left));
            result.setRight(new CompareItem(right));   
            
            return result;
        }
   
    }
    /**
     * 
     * One item for the comparison.
     *
     * @author Sascha Zur
     */
    class CompareItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
        private File contents;
       

        CompareItem(File f) {
            this.contents = f;
        }

        public InputStream getContents() throws CoreException {
            try {
                return new ByteArrayInputStream(FileUtils.readFileToString(contents).getBytes());
            } catch (IOException e) {
                return null;            
            }
        }
        @Override
        public long getModificationDate() {
            return 0;
        }
        @Override
        public Image getImage() {
            return null;
        }
        @Override
        public String getName() {
            return null;
        }
        @Override
        public String getType() {
            return null;
        }
      
     
    }
}
