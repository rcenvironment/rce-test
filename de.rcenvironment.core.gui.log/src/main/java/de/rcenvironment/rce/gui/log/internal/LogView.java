/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.log.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.rce.log.SerializableLogEntry;

/**
 * The whole log view.
 * 
 * @author Enrico Tappert
 * @author Doreen Seider
 */
public class LogView extends ViewPart implements NetworkTopologyChangeListener {

    /** Identifier used to find the this view within the workbench. */
    public static final String ID = "de.rcenvironment.rce.gui.log.view"; //$NON-NLS-1$

    private static final String KEY_SCROLL_LOCK_DISABLED = "scrollLock_disabled";

    private static final String KEY_SCROLL_LOCK_ENABLED = "scrollLock_enabled";

    private static LogView myInstance;

    private static final boolean DEBUG_PRESELECTED = false;

    private static final boolean ERROR_PRESELECTED = true;

    private static final boolean INFO_PRESELECTED = true;

    private static final boolean WARN_PRESELECTED = true;

    private static final int COLUMN_WIDTH_BUNDLE = 250;

    private static final int COLUMN_WIDTH_PLATFORM = 250;

    private static final int COLUMN_WIDTH_LEVEL = 70;

    private static final int COLUMN_WIDTH_MESSAGE = 250;

    private static final int COLUMN_WIDTH_TIME = 140;

    private static final int NO_SPACE = 0;

    private static final int PLATFORM_WIDTH = 250;

    private static final int TEXT_WIDTH = PLATFORM_WIDTH;

    private Button myCheckboxDebug;

    private Button myCheckboxError;

    private Button myCheckboxInfo;

    private Button myCheckboxWarn;

    private Combo myPlatformCombo;

    private LogTableFilter myListenerAndFilter;

    private LogTableColumnSorter myTableColumnSorter;

    private TableViewer myLogEntryTableViewer;

    private Text myMessageTextArea;

    private Text mySearchTextField;

    private SerializableLogEntry displayedLogEntry;

    private boolean scrollLocked = false;

    private final Action clearAction = new Action(Messages.clear, ImageDescriptor.createFromImage(
        PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE))) {

        public void run() {
            clear();
        }
    };

    private final ImageRegistry imageRegistry = new ImageRegistry();

    {
        URL url;
        ImageDescriptor imageDescriptor;
        url = LogView.class.getResource("/resources/icons/scrollLock_enabled.gif");
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(KEY_SCROLL_LOCK_ENABLED, imageDescriptor);
        url = LogView.class.getResource("/resources/icons/scrollLock_disabled.gif");
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(KEY_SCROLL_LOCK_DISABLED, imageDescriptor);
    }

    private final Action scrollLockAction = new Action(Messages.scrollLock, SWT.TOGGLE) {

        {
            setImageDescriptor(getScrollLockImageDescriptor());
        }

        public void run() {
            setScrollLocked(!isScrollLocked());
            setImageDescriptor(getScrollLockImageDescriptor());
        }
    };

    private final LogModel.Listener listener = new LogModel.Listener() {

        @Override
        public void handleLogEntryAdded(final SerializableLogEntry logEntry) {
            myLogEntryTableViewer.getTable().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    if (!myLogEntryTableViewer.getTable().isDisposed()) {
                        if (logEntry.getPlatformIdentifer().toString().equals(getPlatform())) {
                            myLogEntryTableViewer.add(logEntry);
                            if (!LogView.this.isScrollLocked()) {
                                myLogEntryTableViewer.reveal(logEntry);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void handleLogEntryRemoved(final SerializableLogEntry logEntry) {
            /*
             * Log entries are also removed, because otherwise upon a resort the log entries would
             * just vanish ... removing them thus implements the principle of least astonishment.
             */
            myLogEntryTableViewer.getTable().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    if (!myLogEntryTableViewer.getTable().isDisposed()) {
                        if (logEntry.getPlatformIdentifer().toString().equals(getPlatform())) {
                            myLogEntryTableViewer.remove(logEntry);
                        }
                    }
                }
            });
            /**
             * If (the message of) the deleted log entry is displayed, clear the displayed message.
             */
            if (logEntry.equals(getDisplayedLogEntry())) {
                myLogEntryTableViewer.getTable().getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        if (!myMessageTextArea.isDisposed()) {
                            LogView.this.displayLogEntry(null);
                        }
                    }
                });
            }
        }
    };

    private ServiceRegistration topologyListenerRegistration;

    private Display display;

    public LogView() {
        myInstance = this;
    }

    public static LogView getInstance() {
        return myInstance;
    }

    @Override
    public void createPartControl(final Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        // filter = level selection, platform selection, and search text field
        Composite filterComposite = new Composite(parent, SWT.NONE);
        filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        filterComposite.setLayout(new RowLayout());

        createLevelArrangement(filterComposite);
        createPlatformListingArrangement(filterComposite);
        createSearchArrangement(filterComposite);

        // sash = table and text display
        Composite sashComposite = new Composite(parent, SWT.NONE);
        sashComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sashComposite.setLayout(new GridLayout(1, false));

        SashForm sashForm = new SashForm(sashComposite, SWT.VERTICAL | SWT.SMOOTH);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createTableArrangement(sashForm);
        createTableDynamics();
        createTextAreaArrangement(sashForm);

        sashForm.setWeights(new int[] { 3, 1 });
        sashForm.setSashWidth(7);

        // add sorting functionality
        myTableColumnSorter = new LogTableColumnSorter();
        myLogEntryTableViewer.setSorter(myTableColumnSorter);

        // add change listeners and filter mechanism
        myListenerAndFilter = new LogTableFilter(this, myLogEntryTableViewer);

        mySearchTextField.addKeyListener(myListenerAndFilter);

        myCheckboxDebug.addSelectionListener(myListenerAndFilter);
        myCheckboxError.addSelectionListener(myListenerAndFilter);
        myCheckboxInfo.addSelectionListener(myListenerAndFilter);
        myCheckboxWarn.addSelectionListener(myListenerAndFilter);
        myPlatformCombo.addSelectionListener(myListenerAndFilter);

        myLogEntryTableViewer.addFilter(myListenerAndFilter);

        // add toolbar actions (right top of view)
        for (Action action : createToolbarActions()) {
            getViewSite().getActionBars().getToolBarManager().add(action);
        }

        getSite().setSelectionProvider(myLogEntryTableViewer);

        // store display reference for access by topology listener
        display = parent.getShell().getDisplay();
        registerTopologyChangeListener();
    }

    /**
     * Registers an event listener for network changes as an OSGi service (whiteboard pattern).
     * 
     * @param display
     */
    private void registerTopologyChangeListener() {
        BundleContext bundleContext = Activator.getInstance().getBundle().getBundleContext();
        topologyListenerRegistration =
            bundleContext.registerService(NetworkTopologyChangeListener.class.getName(), this, null);
    }

    /**
     * Triggers asynchronous refresh (worker thread calls UI thread) of the view only for the given
     * platform.
     * 
     * @param platform Current selected platform.
     */
    private void asyncRefresh(final String platform) {
        myLogEntryTableViewer.getTable().getDisplay().asyncExec(new Runnable() {

            public void run() {
                if (!myLogEntryTableViewer.getTable().isDisposed()) {
                    try {
                        if (platform.equals(getPlatform())) {
                            displayLogEntry(null);
                            myLogEntryTableViewer.getTable().clearAll();
                            myLogEntryTableViewer.refresh();
                        }
                    } catch (SWTException e) {
                        // re-throw the exception if the disposal state is NOT the error
                        if (!myLogEntryTableViewer.getTable().isDisposed()) {
                            throw e;
                        }
                    }
                }
            }
        });
    }

    public boolean getDebugSelection() {
        return myCheckboxDebug.getSelection();
    }

    public boolean getErrorSelection() {
        return myCheckboxError.getSelection();
    }

    public boolean getInfoSelection() {
        return myCheckboxInfo.getSelection();
    }

    public boolean getWarnSelection() {
        return myCheckboxWarn.getSelection();
    }

    public String getPlatform() {
        return myPlatformCombo.getItem(myPlatformCombo.getSelectionIndex());
    }

    public String getSearchText() {
        return mySearchTextField.getText();
    }

    @Override
    public void setFocus() {
        myLogEntryTableViewer.getControl().setFocus();
    }

    /**
     * 
     * Create the composite structure with level selection options.
     * 
     * @param filterComposite Parent composite for the level selection options.
     */
    private void createLevelArrangement(Composite filterComposite) {
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = NO_SPACE;
        filterComposite.setLayout(rowLayout);

        // ERROR checkbox
        myCheckboxError = new Button(filterComposite, SWT.CHECK);
        myCheckboxError.setText(Messages.error);
        myCheckboxError.setSelection(ERROR_PRESELECTED);

        // WARN checkbox
        myCheckboxWarn = new Button(filterComposite, SWT.CHECK);
        myCheckboxWarn.setText(Messages.warn);
        myCheckboxWarn.setSelection(WARN_PRESELECTED);

        // INFO checkbox
        myCheckboxInfo = new Button(filterComposite, SWT.CHECK);
        myCheckboxInfo.setText(Messages.info);
        myCheckboxInfo.setSelection(INFO_PRESELECTED);

        // DEBUG checkbox
        myCheckboxDebug = new Button(filterComposite, SWT.CHECK);
        myCheckboxDebug.setText(Messages.debug);
        myCheckboxDebug.setSelection(DEBUG_PRESELECTED);

    }

    /**
     * 
     * Create the composite structure with platform selection options.
     * 
     * @param platformComposite Parent composite for the platform selection options.
     */
    private void createPlatformListingArrangement(Composite platformComposite) {
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = NO_SPACE;
        platformComposite.setLayout(rowLayout);

        // platform listing combo box
        myPlatformCombo = new Combo(platformComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        myPlatformCombo.setItems(LogModel.getInstance().getPlatforms());
        myPlatformCombo.select(0);
        myPlatformCombo.setLayoutData(new RowData(PLATFORM_WIDTH, SWT.DEFAULT));

        LogModel.getInstance().setCurrentPlatform(myPlatformCombo.getItem(myPlatformCombo.getSelectionIndex()));
    }

    /**
     * 
     * Create the composite structure with search options.
     * 
     * @param searchComposite Parent composite for the search options.
     */
    private void createSearchArrangement(Composite searchComposite) {
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = 7;
        searchComposite.setLayout(rowLayout);

        // search text field
        mySearchTextField = new Text(searchComposite, SWT.SEARCH);
        mySearchTextField.setMessage(Messages.search);
        mySearchTextField.setSize(TEXT_WIDTH, SWT.DEFAULT);
        mySearchTextField.setLayoutData(new RowData(TEXT_WIDTH, SWT.DEFAULT));
    }

    /**
     * 
     * Create the composite structure with log display and selection options.
     * 
     * @param platformComposite Parent composite for log display and selection options.
     */
    private void createTableArrangement(Composite tableComposite) {
        tableComposite.setLayout(new GridLayout());

        // create table viewer
        myLogEntryTableViewer = new TableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        myLogEntryTableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // create table header and column styles
        String[] columnTitles = new String[] {
            Messages.level,
            Messages.message,
            Messages.bundle,
            Messages.platform,
            Messages.timestamp };
        int[] bounds = { COLUMN_WIDTH_LEVEL, COLUMN_WIDTH_MESSAGE, COLUMN_WIDTH_BUNDLE, COLUMN_WIDTH_PLATFORM, COLUMN_WIDTH_TIME };

        for (int i = 0; i < columnTitles.length; i++) {
            // for all columns

            final int index = i;
            final TableViewerColumn viewerColumn = new TableViewerColumn(myLogEntryTableViewer, SWT.NONE);
            final TableColumn column = viewerColumn.getColumn();

            // set column properties
            column.setText(columnTitles[i]);
            column.setWidth(bounds[i]);
            column.setResizable(true);
            column.setMoveable(true);

            column.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    myTableColumnSorter.setColumn(index);
                    int direction = myLogEntryTableViewer.getTable().getSortDirection();

                    if (myLogEntryTableViewer.getTable().getSortColumn() == column) {
                        if (direction == SWT.UP) {
                            direction = SWT.DOWN;
                        } else {
                            direction = SWT.UP;
                        }
                    } else {
                        direction = SWT.UP;
                    }
                    myLogEntryTableViewer.getTable().setSortDirection(direction);
                    myLogEntryTableViewer.getTable().setSortColumn(column);

                    myLogEntryTableViewer.getTable().clearAll();
                    myLogEntryTableViewer.refresh();
                }
            });
        }

        // set table content
        myLogEntryTableViewer.setContentProvider(new LogContentProvider());
        myLogEntryTableViewer.setLabelProvider(new LogLabelProvider());
        myLogEntryTableViewer.setInput(LogModel.getInstance());

        // set table layout data
        final Table table = myLogEntryTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // for the selection of a table item
        table.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                TableItem[] selection = table.getSelection();
                if (selection[0].getData() instanceof SerializableLogEntry) {
                    SerializableLogEntry log = (SerializableLogEntry) selection[0].getData();
                    displayLogEntry(log);
                }
            }
        });

        // create copy context menu
        Menu contextMenu = new Menu(tableComposite);
        MenuItem copyItem = new MenuItem(contextMenu, SWT.PUSH);
        copyItem.setText(Messages.copy);
        copyItem.addSelectionListener(new CopyToClipboardListener(myLogEntryTableViewer));
        myLogEntryTableViewer.getControl().setMenu(contextMenu);
    }

    private void createTableDynamics() {
        LogModel.getInstance().addListener(listener);
    }

    private SerializableLogEntry getDisplayedLogEntry() {
        return displayedLogEntry;
    }

    protected void displayLogEntry(final SerializableLogEntry logEntry) {
        StringBuffer message = new StringBuffer();
        if (logEntry != null) {
            message.append(logEntry.getMessage());
            if (logEntry.getException() != null) {
                message.append("\n\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                logEntry.getException().printStackTrace(pw);
                message.append(sw.toString());
            }
        }
        myMessageTextArea.setText(message.toString());
        displayedLogEntry = logEntry;
    }

    @Override
    public void dispose() {
        LogModel.getInstance().removeListener(listener);
        super.dispose();
        // unregister OSGi listener service
        if (topologyListenerRegistration != null) {
            topologyListenerRegistration.unregister();
        }
    }

    /**
     * 
     * Create the composite structure for expansive text displaying.
     * 
     * @param textAreaComposite Parent composite for expansive text displaying.
     */
    private void createTextAreaArrangement(Composite textAreaComposite) {
        textAreaComposite.setLayout(new GridLayout(1, false));

        myMessageTextArea = new Text(textAreaComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        myMessageTextArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    private Action[] createToolbarActions() {
        return new Action[] {
            scrollLockAction, clearAction
        };
    }

    protected void clear() {
        LogModel.getInstance().clear();
        asyncRefresh(getPlatform());
    }

    protected void setScrollLocked(final boolean scrollLocked) {
        this.scrollLocked = scrollLocked;
    }

    public boolean isScrollLocked() {
        return scrollLocked;
    }

    protected ImageDescriptor getScrollLockImageDescriptor() {
        final ImageDescriptor result;
        if (isScrollLocked()) {
            result = imageRegistry.getDescriptor(KEY_SCROLL_LOCK_ENABLED);
        } else {
            result = imageRegistry.getDescriptor(KEY_SCROLL_LOCK_DISABLED);
        }
        return result;
    }

    private void refreshPlatformCombo() {
        myPlatformCombo.setItems(LogModel.getInstance().getPlatforms());
        // select platform (previously selected)
        String currentPlatform = LogModel.getInstance().getCurrentPlatform();
        if (currentPlatform != null) {
            String[] items = myPlatformCombo.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i].equals(currentPlatform)) {
                    myPlatformCombo.select(i);
                    return;
                }
            }
        }
        myPlatformCombo.select(0);
    }

    @Override
    public void onNetworkTopologyChanged() {
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                refreshPlatformCombo();
            }
        });
    }

}
