/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.log.DistributedLogReaderService;
import de.rcenvironment.rce.log.SerializableLogEntry;
import de.rcenvironment.rce.log.SerializableLogListener;
import de.rcenvironment.rce.log.SerializableLogReaderService;


/**
 * Implementation of the {@link DistributedLogReaderServiceImpl}.
 *
 * @author Doreen Seider
 */
public class DistributedLogReaderServiceImpl implements DistributedLogReaderService {

    private static final Log LOGGER = LogFactory.getLog(DistributedLogReaderServiceImpl.class);

    private CommunicationService communicationService;
    
    private BundleContext context;

    private List<SerializableLogListener> logListeners = new ArrayList<SerializableLogListener>();

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }
    
    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }
    
    @Override
    public void addLogListener(SerializableLogListener logListener, PlatformIdentifier platformIdentifier) {
        
        try {
            SerializableLogReaderService service = (SerializableLogReaderService) communicationService
                .getService(SerializableLogReaderService.class, platformIdentifier, context);
            
            service.addLogListener(logListener);
            logListeners.add(logListener);
        } catch (RuntimeException e) {
            LOGGER.warn("Can not add to remote log listener of platform: " + platformIdentifier, e);
        }
    }

    @Override
    public List<SerializableLogEntry> getLog(PlatformIdentifier platformIdentifier) {
        try {
            SerializableLogReaderService service = (SerializableLogReaderService) communicationService
                .getService(SerializableLogReaderService.class, platformIdentifier, context);
            return service.getLog();
        } catch (RuntimeException e) {
            LOGGER.warn("Can not get log from remote platform: " + platformIdentifier, e);
            return new LinkedList<SerializableLogEntry>();
        }
    }

    @Override
    public void removeLogListener(SerializableLogListener logListener, PlatformIdentifier platformIdentifier) {

        try {
            SerializableLogReaderService service = (SerializableLogReaderService) communicationService
                .getService(SerializableLogReaderService.class, platformIdentifier, context);
            service.removeLogListener(logListener);
            logListeners.remove(logListener);
        } catch (RuntimeException e) {
            LOGGER.warn("Can not remove from remote log listener: " + platformIdentifier, e);            
        }
        
    }

}
