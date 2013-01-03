/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.console;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.gui.workflow.SubscriptionEventProcessor;
import de.rcenvironment.rce.notification.Notification;

/**
 * Subscriber for all console notifications in the overall system.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class ConsoleSubscriptionEventProcessor extends SubscriptionEventProcessor {

    private static final long serialVersionUID = 5521705555312627039L;

    private final transient ConsoleModel consoleModel;

    private transient ConsoleRowLogfileManager logfileManager;

    private transient Log log = LogFactory.getLog(getClass());

    public ConsoleSubscriptionEventProcessor(ConsoleModel consoleModel) {
        super();
        this.consoleModel = consoleModel;
        // set up background logging of console rows
        // TODO could (should?) be reworked to be independent of ConsoleView
        try {
            File logFile = File.createTempFile("rce.console.", ".log");
            // create log file manager; "true" = auto shutdown
            logfileManager = new ConsoleRowLogfileManager(logFile, true);
            log.info("Logging component console output to " + logFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create console log file", e);
        }
    }

    /**
     * Process all collected {@link ConsoleRow} updates and perform a single GUI update to improve
     * performance.
     */
    @Override
    protected void processNotifications() {
        List<Notification> readOnlyList = null;
        synchronized (notificationsToProcess) {
            if (!notificationsToProcess.isEmpty()) {
                // create read-only copy of pending notifications list
                readOnlyList = new ArrayList<Notification>(notificationsToProcess);
                // clear queue
                notificationsToProcess.clear();
            }
        }
        // process the list outside the synchronization block
        List<ConsoleRow> consoleRows = new ArrayList<ConsoleRow>();
        if (readOnlyList != null) {
            for (Notification notification : readOnlyList) {
                Serializable body = notification.getBody();
                if (body instanceof ConsoleRow) {
                    ConsoleRow consoleRow = ((ConsoleRow) notification.getBody()).clone();
                    consoleRow.setNumber(notification.getHeader().getNumber());
                    consoleRows.add(consoleRow);
                }
            }
            // update console model
            consoleModel.addConsoleRows(consoleRows);

            // send collected lines to file logger
            if (logfileManager != null) {
                logfileManager.append(consoleRows);
            }
        }
    }
}
