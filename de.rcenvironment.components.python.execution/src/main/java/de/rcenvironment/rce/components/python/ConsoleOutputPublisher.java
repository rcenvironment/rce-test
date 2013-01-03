/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.python;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Responsible for announcing {@link ConsoleRow}s via {@link DistributedNotificationService}.
 * 
 * @author Arne Bachmann
 */
public class ConsoleOutputPublisher implements Runnable {

    private final ComponentInstanceInformation componentInformation;

    private final InputStream inputStream;
    
    private final ConsoleRow.Type consoleType;

    private final DistributedNotificationService notificationService;
    
    private final ByteArrayOutputStream baos;
    
    /**
     * Constructor.
     * 
     * @param ci the {@link ComponentInstanceInformation} containing needed information for announcing.
     * @param is the {@link InputStream} to read from
     * @param type the type of the {@link ConsoleRow}.
     * @param service the {@link DistributedNotificationService} for announcing.
     */
    public ConsoleOutputPublisher(final ComponentInstanceInformation ci, final InputStream is,
        ConsoleRow.Type type, DistributedNotificationService service) {
        componentInformation = ci;
        inputStream = is;
        consoleType = type;
        notificationService = service;
        baos = new ByteArrayOutputStream();
    }

    
    @Override
    public void run() {
        try {
            final int minusOne = -1;
            int c;
            while ((c = inputStream.read()) != minusOne) {
                baos.write(c);
                if (baos.toString().contains("\n")) {
                    notificationService.send(componentInformation.getIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX,
                        new ConsoleRow(componentInformation.getComponentContextName(),
                                 componentInformation.getName(),
                                 consoleType,
                                 baos.toString()));
                    baos.reset();
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException("Reading and publishing console out put failed", e);
        }
    }
}
