/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.start.headless.internal;

import java.io.FileWriter;
import java.io.IOException;

import de.rcenvironment.core.start.common.Platform;
import de.rcenvironment.rce.notification.DefaultNotificationSubscriber;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;

/**
 * Subscriber for the headless log files.
 * @author Sascha Zur
 *
 */
public class HeadlessNotificationSubscriber extends DefaultNotificationSubscriber{
    
    private static final long serialVersionUID = 1L;
    private FileWriter fileWriter;
    private boolean isWorkflowLogger;

    public HeadlessNotificationSubscriber(FileWriter fw) {
        super();
        fileWriter = fw;
        isWorkflowLogger = false;
    }
    
    public HeadlessNotificationSubscriber(FileWriter fw, boolean isWorkflowLogger) {
        super();
        fileWriter = fw;
        this.isWorkflowLogger = isWorkflowLogger;
    }

    @Override
    public Class<?> getInterface() {
        return NotificationSubscriber.class;
    }


    @Override
    public void notify(Notification n) {
        try {
            if (fileWriter != null){
                fileWriter.append((n.getHeader() + " : " + n.getBody()) + System.getProperty("line.separator"));
                fileWriter.flush();
            }
            if (isWorkflowLogger 
                    && (n.getHeader().toString().startsWith("rce.component.workflow.state") 
                    && (n.getBody().toString().endsWith("FINISHED") || n.getBody().toString().endsWith("ERROR")))){
                Platform.shutdown();
            }
        } catch (IOException e) {
            fileWriter = null;
        }
    }

}
