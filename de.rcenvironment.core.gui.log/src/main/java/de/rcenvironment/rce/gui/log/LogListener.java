/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.log;

import java.io.Serializable;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.callback.Callback;
import de.rcenvironment.rce.gui.log.internal.LogModel;
import de.rcenvironment.rce.log.SerializableLogEntry;
import de.rcenvironment.rce.log.SerializableLogListener;

/**
 * Implementation of {@link SerializableLogListener} in order to register for log events.
 * 
 * @author Doreen Seider
 */
public class LogListener implements SerializableLogListener {

    private static final long serialVersionUID = 1L;

    private PlatformIdentifier platformId;

    public LogListener(PlatformIdentifier aPlatformId) {
        platformId = aPlatformId;
    }

    @Callback
    @Override
    @AllowRemoteAccess
    public void logged(SerializableLogEntry logEntry) {
        logEntry.setPlatformIdentifer(platformId);
        LogModel.getInstance().addLogEntry(logEntry);
    }
    
    @Override
    public Class<? extends Serializable> getInterface() {
        return SerializableLogListener.class;
    }

}
