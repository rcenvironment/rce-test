/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log.internal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.log.SerializableLogEntry;
import de.rcenvironment.rce.log.SerializableLogListener;
import de.rcenvironment.rce.log.SerializableLogReaderService;

/**
 * Implementation of {@link SerializableLogReaderService}.
 * 
 * @author Doreen Seider
 */
public class SerializableLogReaderServiceImpl implements SerializableLogReaderService {

    private static final long serialVersionUID = -7406557933348370062L;

    private LogReaderService logReaderService;
    
    private Map<SerializableLogListener, LogListener> logListener = new HashMap<SerializableLogListener, LogListener>();

    protected void bindLogReaderService(LogReaderService newLogReaderService) {
        logReaderService = newLogReaderService;
    }

    @Override
    @AllowRemoteAccess
    public void addLogListener(final SerializableLogListener listener) {
        LogListener originalListener = new LogListener() {

            @Override
            public void logged(LogEntry entry) {
                listener.logged(new SerializableLogEntry(
                    entry.getBundle().getSymbolicName(),
                    entry.getLevel(),
                    entry.getMessage(),
                    entry.getTime(),
                    entry.getException()));
            }
        };
        logListener.put(listener, originalListener);
        logReaderService.addLogListener(originalListener);

    }
    
    @Override
    @AllowRemoteAccess
    public List<SerializableLogEntry> getLog() {
        List<SerializableLogEntry> entries = new LinkedList<SerializableLogEntry>();
        Enumeration<LogEntry> retrievedEntries = logReaderService.getLog();

        while (retrievedEntries.hasMoreElements()) {
            LogEntry entry = retrievedEntries.nextElement();
            entries.add(entries.size(), new SerializableLogEntry(
                entry.getBundle().getSymbolicName(),
                entry.getLevel(),
                entry.getMessage(),
                entry.getTime(),
                entry.getException()));
        }
        return entries;
    }

    @Override
    @AllowRemoteAccess
    public void removeLogListener(SerializableLogListener listener) {
        logReaderService.removeLogListener(logListener.get(listener));
        logListener.remove(listener);
    }

}
