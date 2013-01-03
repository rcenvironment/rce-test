/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.console;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.gui.workflow.SubscriptionManager;

/**
 * Provides central local access to the workflow logging data (console rows, workflows, components)
 * to display.
 * 
 * @author Doreen Seider (initial version)
 * @author Robert Mischke (current)
 */
public final class ConsoleModel {
    
    /**
     * The sequence id to use for querying if no previous sequence id is available.
     */
    public static final int INITIAL_SEQUENCE_ID = 0;

    /**
     * The maximum total number of rows to keep.
     */
    // TODO check limiting details; sufficient or add more detail?
    private static final int MAX_UNFILTERED_ROWS_RETENTION = 35000;

    /**
     * The maximum number of (filtered) rows to return in a snapshot.
     */
    private static final int MAX_SNAPSHOT_SIZE = 25000;

    private static final String NOTIFICATION_ID = ".*" + ConsoleRow.NOTIFICATION_SUFFIX;
    
    private static ConsoleModel instance;

    private static SubscriptionManager subscriptionManager;

    private Deque<ConsoleRow> allRows;

    /**
     * Note: The current concept is based on a single client view using this model; if required,
     * this could be changed to a map of registered filters.
     */
    private ConsoleRowFilter currentFilter;

    private Deque<ConsoleRow> filteredRows;

    private SortedSet<String> workflows;

    private SortedSet<String> components;

    /**
     * Incremented on each model change; used for efficient change testing. Initialized with "+1" so
     * a query with INITIAL_SEQUENCE_ID as parameter will always signal an initial "change".
     * 
     * May be changed in the future to filter-specific sequence ids.
     */
    private int sequenceIdCounter = INITIAL_SEQUENCE_ID + 1;

    private int filteredListLastChanged;

    private int workflowListLastChanged;

    private int componentListLastChanged;

    private ConsoleModel() {
        // initialize internal model
        resetModel();
        // set default filter
        currentFilter = new ConsoleRowFilter();
    }

    /**
     * Ensures that the console model is registered to listen for console output.
     * 
     */
    public static void ensureConsoleCaptureIsInitialized() {
        // trigger model creation & subscription if not done yet
        getInstance();
    }

    /**
     * Singleton getter to provide central model access.
     * 
     * @return the singleton instance
     */
    public static synchronized ConsoleModel getInstance() {
        if (null == instance) {
            instance = new ConsoleModel();
            Job job = new Job(Messages.openConsoleOutputs) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        monitor.beginTask(Messages.fetchingConsoleOutputs, 3);
                        monitor.worked(1);
                        subscriptionManager = new SubscriptionManager(new ConsoleSubscriptionEventProcessor(instance));
                        monitor.worked(2);
                        subscriptionManager.initialize(new String[] { NOTIFICATION_ID });
                        monitor.worked(3);
                    } finally {
                        monitor.done();
                    }
                    return Status.OK_STATUS;
                };
            };
            job.setUser(true);
            job.schedule();
        }
        return instance;
    }

    /**
     * Updates subscriptions to known server instances.
     */
    public synchronized void updateSubscriptions() {
        subscriptionManager.updateSubscriptions(new String[] { NOTIFICATION_ID });
    }
    
    public synchronized ConsoleModelSnapshot getSnapshot() {
        return getSnapshotIfModifiedSince(INITIAL_SEQUENCE_ID);
    }

    /**
     * Returns a new {@link ConsoleModelSnapshot} of the current model state if the model was
     * modified since the given sequence id. The typical source of this sequence id is calling
     * getSequenceId() on a previously returned snapshot. If no change has occured, this method
     * returns null.
     * 
     * @param sequenceId the last sequence id known to the caller
     * @return a new model snapshot, or null if no change has occured since the given sequence id
     */
    public synchronized ConsoleModelSnapshot getSnapshotIfModifiedSince(int sequenceId) {

        // any change at all?
        if (sequenceId == sequenceIdCounter) {
            return null;
        }

        // any relevant change?
        if (sequenceId >= filteredListLastChanged && sequenceId >= workflowListLastChanged && sequenceId >= componentListLastChanged) {
            return null;
        }

        // create & return snapshot object
        ConsoleModelSnapshotImpl snapshot = new ConsoleModelSnapshotImpl();
        if (filteredListLastChanged > sequenceId) {
            // if modifed, set a copy of the filtered list
            snapshot.setFilteredRows(new ArrayList<ConsoleRow>(filteredRows));
        }
        // if modified, set a copy of the workflow list
        if (workflowListLastChanged > sequenceId) {
            snapshot.setWorkflowList(new ArrayList<String>(workflows));
        }
        // if modified, set a copy of the component list
        if (componentListLastChanged > sequenceId) {
            snapshot.setComponentList(new ArrayList<String>(components));
        }
        snapshot.setSequenceId(sequenceIdCounter);

        return snapshot;
    }

    /**
     * Batch version of {@link #addConsoleRow(ConsoleRow)} to reduce synchronization overhead.
     * 
     * @param rows the list of {@link ConsoleRow}s to add
     */
    public synchronized void addConsoleRows(List<ConsoleRow> rows) {
        sequenceIdCounter++;
        for (ConsoleRow row : rows) {
            // add unfiltered
            allRows.addLast(row);
            // add to filtered list if filter matches
            if (currentFilter.accept(row)) {
                filteredRows.addLast(row);
                filteredListLastChanged = sequenceIdCounter;
            }
            // add to the set of workflows
            // note: currently, workflows are only purged on clearAll
            if (workflows.add(row.getWorkflow())) {
                workflowListLastChanged = sequenceIdCounter;
            }
            // add to the set of components
            // note: currently, components are only purged on clearAll
            if (components.add(row.getComponent())) {
                componentListLastChanged = sequenceIdCounter;
            }
        }

        // trim model to retention limits
        trimUnfilteredModel();
        // trim filtered list to max capacity
        trimFilteredList();
    }

    /**
     * Set the new {@link ConsoleRowFilter} for building future snapshots. Null is not permitted;
     * set a permissive filter instead.
     * @param newFilter the new {@link ConsoleRowFilter}
     */
    public synchronized void setRowFilter(ConsoleRowFilter newFilter) {
        // mark modification
        sequenceIdCounter++;
        // use a clone to prevent external modification
        currentFilter = newFilter.clone();
        // rebuild filtered list with new filter
        filteredRows = new LinkedList<ConsoleRow>();
        for (ConsoleRow row : allRows) {
            // add to filtered list if filter matches
            if (currentFilter.accept(row)) {
                filteredRows.addLast(row);
            }
        }
        filteredListLastChanged = sequenceIdCounter;
        // trim filtered list to max capacity
        trimFilteredList();
    }

    private void resetModel() {
        allRows = new LinkedList<ConsoleRow>();
        filteredRows = new LinkedList<ConsoleRow>();
        filteredListLastChanged = sequenceIdCounter;
        workflows = new TreeSet<String>();
        workflowListLastChanged = sequenceIdCounter;
        components = new TreeSet<String>();
        componentListLastChanged = sequenceIdCounter;
    }

    private void trimUnfilteredModel() {
        // TODO could be expanded to retention limits per type etc.
        while (allRows.size() > MAX_UNFILTERED_ROWS_RETENTION) {
            allRows.removeFirst();
        }
    }

    private void trimFilteredList() {
        while (filteredRows.size() > MAX_SNAPSHOT_SIZE) {
            filteredRows.removeFirst();
        }
    }

    /**
     * Removes all console rows.
     **/
    public synchronized void clearAll() {
        sequenceIdCounter++;
        resetModel();
    }

}
