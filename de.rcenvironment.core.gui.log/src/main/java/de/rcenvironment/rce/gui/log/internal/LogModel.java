/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.log.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.gui.log.LogListener;
import de.rcenvironment.rce.log.SerializableLogEntry;
import de.rcenvironment.rce.log.SimpleLogReaderService;

/**
 * Provides central local access to the whole logging data (log entries, remote platforms) to
 * display.
 * 
 * @author Doreen Seider
 * @author Enrico Tappert
 */
public final class LogModel {

    private static final int LOG_POOL_SIZE = 7000;

    private static LogModel instance;

    private final List<Listener> listeners = new LinkedList<Listener>();

    private Set<PlatformIdentifier> platforms;

    private PlatformIdentifier currentPlatform;

    private Map<PlatformIdentifier, Map<Integer, SortedSet<SerializableLogEntry>>> allLogEntries;

    private LogModel() {
        allLogEntries = new ConcurrentHashMap<PlatformIdentifier, Map<Integer, SortedSet<SerializableLogEntry>>>();
        IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
        try {
            service.run(false, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.fetchingPlatforms, 3);
                        monitor.worked(2);
                        platforms = new SimpleCommunicationService().getAvailableNodes();
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
    }

    /**
     * Singleton to enable central access to the data.
     * 
     * @return Singleton instance.
     */
    public static synchronized LogModel getInstance() {
        if (null == instance) {
            instance = new LogModel();
        }
        return instance;
    }

    /**
     * Returns a list of {@link LogEntry} for the specified {@link PlatformIdentifier} set by
     * {@link LogModel#setCurrentPlatform(String)}.
     * 
     * @return {@link SortedSet} of {@link LogEntry}.
     */
    public SortedSet<SerializableLogEntry> getLogEntries() {

        SortedSet<SerializableLogEntry> entries = new TreeSet<SerializableLogEntry>();

        if (currentPlatform != null && !allLogEntries.containsKey(currentPlatform)) {
            retrieveLogEntries(currentPlatform);

            // set the listener to recognize new message in future
            LogListener logListener = new LogListener(currentPlatform);
            new SimpleLogReaderService().addLogListener(logListener, currentPlatform);
        }

        for (Integer level : allLogEntries.get(currentPlatform).keySet()) {
            Map<Integer, SortedSet<SerializableLogEntry>> platformEntries = allLogEntries.get(currentPlatform);
            SortedSet<SerializableLogEntry> levelEntries = platformEntries.get(level);
            synchronized (levelEntries) {
                entries.addAll(levelEntries);
            }
        }
        return entries;
    }

    /**
     * Adds a {@link LogEntry} to the whole list of the specified {@link PlatformIdentifier}.
     * 
     * @param logEntry The {@link LogEntry} to add.
     */
    public void addLogEntry(SerializableLogEntry logEntry) {
        PlatformIdentifier platformIdentifier = logEntry.getPlatformIdentifer();

        if (!allLogEntries.get(platformIdentifier).containsKey(logEntry.getLevel())) {
            allLogEntries.get(platformIdentifier).put(logEntry.getLevel(),
                Collections.synchronizedSortedSet(new TreeSet<SerializableLogEntry>()));
        }

        SortedSet<SerializableLogEntry> logEntries = allLogEntries.get(platformIdentifier).get(logEntry.getLevel());
        while (logEntries.size() >= LOG_POOL_SIZE) {
            final SerializableLogEntry logEntryToRemove = logEntries.first();
            logEntries.remove(logEntryToRemove);
            for (final Listener listener : listeners) {
                listener.handleLogEntryRemoved(logEntryToRemove);
            }
        }
        logEntries.add(logEntry);

        for (final Listener listener : listeners) {
            listener.handleLogEntryAdded(logEntry);
        }
    }

    /**
     * Lets identify the current platform for which logging messages has to be shown.
     * 
     * @param platform The current platform identifier to set.
     */
    public void setCurrentPlatform(String platform) {
        currentPlatform = null;
        for (PlatformIdentifier platformIdentifier : platforms) {
            // search relevant platform

            // TODO searching by dynamically-generated string is brittle; rework
            if (platformIdentifier.getAssociatedDisplayName().equals(platform)) {
                currentPlatform = platformIdentifier;
                break;
            }
        }
    }

    /**
     * @return current platform.
     */
    public String getCurrentPlatform() {
        return currentPlatform.toString();
    }

    /**
     * Gathers all platform identifiers and provides them in array.
     * 
     * @return Array of platform identifiers.
     */
    public String[] getPlatforms() {
        platforms = new SimpleCommunicationService().getAvailableNodes();
        platforms.toArray();
        List<String> platformsAsStringList = new ArrayList<String>();

        String localPlatform = null;
        for (PlatformIdentifier pi : platforms) {
            if (new SimpleCommunicationService().isLocalPlatform(pi)) {
                localPlatform = pi.getAssociatedDisplayName();
            } else {
                platformsAsStringList.add(pi.getAssociatedDisplayName());
            }
        }

        Collections.sort(platformsAsStringList);

        if (localPlatform != null) {
            platformsAsStringList.add(0, localPlatform);
        }

        return platformsAsStringList.toArray(new String[platformsAsStringList.size()]);
    }

    /** Removes log entries. **/
    public void clear() {
        if (currentPlatform == null) {
            for (PlatformIdentifier pi : allLogEntries.keySet()) {
                allLogEntries.get(pi).clear();
            }
        } else {
            allLogEntries.get(currentPlatform).clear();
        }
    }

    private synchronized void retrieveLogEntries(final PlatformIdentifier platformId) {

        if (allLogEntries.get(platformId) == null) {
            // first call for this platform -> get all the entries
            IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
            try {
                service.run(false, false, new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            monitor.beginTask(Messages.fetchingLogs, 6);
                            monitor.worked(2);
                            SimpleLogReaderService simpleLogReaderService = new SimpleLogReaderService();
                            List<SerializableLogEntry> retrievedLogEntries = simpleLogReaderService.getLog(platformId);
                            monitor.worked(4);
                            Map<Integer, SortedSet<SerializableLogEntry>> logEntries = new ConcurrentHashMap<Integer,
                                SortedSet<SerializableLogEntry>>();
                            for (SerializableLogEntry retrievedLogEntry : retrievedLogEntries) {
                                if (!logEntries.containsKey(retrievedLogEntry.getLevel())) {
                                    logEntries.put(retrievedLogEntry.getLevel(),
                                        Collections.synchronizedSortedSet(new TreeSet<SerializableLogEntry>()));
                                }
                                retrievedLogEntry.setPlatformIdentifer(platformId);
                                logEntries.get(retrievedLogEntry.getLevel()).add(retrievedLogEntry);
                            }
                            monitor.worked(5);
                            allLogEntries.put(platformId, logEntries);
                            monitor.worked(6);
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
        }
    }

    /**
     * Adds a {@link Listener}.
     * 
     * @param listener the {@link Listener} to add
     */
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a {@link Listener}.
     * 
     * @param listener the {@link Listener} to remove
     */
    public void removeListener(final Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Listener interface to listen to {@link LogModel} changes.
     * 
     * @author Christian Weiss
     */
    public interface Listener {

        /**
         * Handle the addition of a {@link LogEntry}.
         * 
         * @param logEntry the newly added {@link LogEntry}
         */
        void handleLogEntryAdded(SerializableLogEntry logEntry);

        /**
         * Handle the removal of a {@link LogEntry}.
         * 
         * @param logEntry the removed {@link LogEntry}
         */
        void handleLogEntryRemoved(SerializableLogEntry logEntry);

    }

}
