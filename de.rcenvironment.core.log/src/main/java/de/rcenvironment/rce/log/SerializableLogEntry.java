/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.service.log.LogService;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Serializable version of {@link LogEntry}.
 *
 * @author Doreen Seider
 */
public class SerializableLogEntry implements Serializable, Comparable<SerializableLogEntry> {

    private static final long serialVersionUID = 1L;

    private final String bundleName;
    private final int level;
    private final String message;
    private final long time;
    private final Throwable exception;
    private PlatformIdentifier platformId;
    
    public SerializableLogEntry(String bundleName, int level, String message, long time, Throwable exception) {
        
        this.bundleName = bundleName;
        this.level = level;
        this.message = message;
        this.time = time;
        this.exception = exception;
    }

    public String getBundleName() {
        return bundleName;
    }

    public int getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
    
    public Throwable getException() {
        return exception;
    }

    public PlatformIdentifier getPlatformIdentifer() {
        return platformId;
    }

    public void setPlatformIdentifer(PlatformIdentifier newPlatformId) {
        this.platformId = newPlatformId;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss,SSS");
        
        String levelAsString = null;
        switch (level) {
        case LogService.LOG_ERROR:
            levelAsString = "ERROR";
            break;
        case LogService.LOG_WARNING:
            levelAsString = "WARNING";
            break;
        case LogService.LOG_INFO:
            levelAsString = "INFO";
            break;
        case LogService.LOG_DEBUG:
            levelAsString = "DEBUG";
            break;
        default:
            break;
        }
        
        return df.format(time) + " " + levelAsString + " - " + message;
    }

    @Override
    public int compareTo(SerializableLogEntry o) {
        
        int compResult = new Date(time).compareTo(new Date(o.getTime()));

        if (compResult == 0) {
            compResult = platformId.toString().compareTo(o.getPlatformIdentifer().toString());

            if (compResult == 0) {
                compResult = bundleName.compareTo(o.getBundleName());
    
                if (compResult == 0) {
                    compResult = new Integer(level).compareTo(new Integer(o.getLevel()));
    
                    if (compResult == 0) {
                        compResult = message.compareTo(o.getMessage());
    
                    }
                }
            }
        }
        
        if (compResult == 0 && exception != null && o.getException() != null) {
            compResult = exception.toString().compareTo(o.getException().toString());
        }

        return compResult;
    }
}
