/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log;

import java.util.List;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Convenient class abstracting the OSGi API. It provides access to all {@link LogReaderService}s of
 * the whole distributed system.
 * 
 * @author Doreen Seider
 */
public class SimpleLogReaderService {

    private static DistributedLogReaderService nullLogReaderService = ServiceUtils.createNullService(DistributedLogReaderService.class);

    private static DistributedLogReaderService logReaderService = nullLogReaderService;

    protected void bindDistributedLogReaderService(DistributedLogReaderService newLogReaderService) {
        logReaderService = newLogReaderService;
    }

    protected void unbindDistributedLogReaderService(DistributedLogReaderService oldLogReaderService) {
        logReaderService = nullLogReaderService;
    }

    /**
     * Subscribes to LogEntry objects.
     * 
     * This method registers a {@link LogListener} object with a {@link LogReaderService} of the
     * given platform. The LogListener.logged(LogEntry) method will be called for each
     * {@link LogEntry} object placed into the log.
     * 
     * @param logListener The {@link LogListener} object to register.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform to register.
     * 
     * @see LogReaderService#addLogListener(LogListener).
     */
    public void addLogListener(SerializableLogListener logListener, PlatformIdentifier platformIdentifier) {
        logReaderService.addLogListener(logListener, platformIdentifier);
    }

    /**
     * Returns an {@link Enumeration} of all {@link LogEntry} objects in the log.
     * 
     * Each element of the enumeration is a {@link LogEntry} object, ordered with the most recent
     * entry first. Whether the enumeration is of all {@link LogEntry} objects since the
     * {@link LogService} was started or some recent past is implementation-specific. Also
     * implementation-specific is whether informational and debug {@link LogEntry} objects are
     * included in the enumeration.
     * 
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform to get the log from.
     * @return The {@link List} of {@link SerializableLogEntry} objects.
     * 
     * @see LogReaderService#getLog().
     */
    public List<SerializableLogEntry> getLog(PlatformIdentifier platformIdentifier) {
        return logReaderService.getLog(platformIdentifier);
    }

    /**
     * Unsubscribes to LogEntry objects.
     * 
     * This method unregisters a LogListener object from the Log Reader Service.
     * 
     * @param logListener The {@link LogListener} object to unregister.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform to unregister.
     * 
     * @see LogReaderService#removeLogListener().
     */
    public void removeLogListener(SerializableLogListener logListener, PlatformIdentifier platformIdentifier) {
        logReaderService.removeLogListener(logListener, platformIdentifier);

    }
}
