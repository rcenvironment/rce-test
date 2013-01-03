/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Represents the output of a {@link Component} and provide an API for sending
 * data.
 * 
 * @author Doreen Seider
 */
public class Output {

    private final String name;

    private final String compInstanceId;

    private final Class<? extends Serializable> type;
    
    private final PlatformIdentifier platform;
    
    private DistributedNotificationService notificationService;
    
    private final String notificationId;
    
    public Output(OutputDescriptor newOutputDesc, Class<? extends Serializable> newType,
        DistributedNotificationService newNotificationService) {

        name = newOutputDesc.getName();
        compInstanceId = newOutputDesc.getComponentInstanceDescriptor().getIdentifier();
        type = newType;
        platform = newOutputDesc.getComponentInstanceDescriptor().getPlatform();
        notificationService = newNotificationService;
        
        notificationId = ComponentConstants.OUTPUT_NOTIFICATION_ID_PREFIX + compInstanceId + name;
    }
    
    /**
     * Returns the name of this {@link Output}.
     * 
     * @return the name of this {@link Output}.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the identifier of the associated component instance.
     * 
     * @return the identifier of the associated component instance.
     */
    public String getComponentInstanceIdentifier() {
        return compInstanceId;
    }
    
    /**
     * Returns the type of this {@link Output}'s value.
     * 
     * @return the type of this {@link Output}'s value.
     */
    public Class<? extends Serializable> getType() {
        return type;
    }

    /**
     * Returns the platform represented by its {@link PlatformIdentifier} this {@link Output}'s
     * component is running.
     * 
     * @return the platform represented by its {@link PlatformIdentifier} this {@link Output}'s
     *         component is running.
     */
    public PlatformIdentifier getPlatform() {
        return platform;
    }
    
    /**
     * Sends new values. The value is propagated to all connected {@link Input}s.
     * 
     * @param output the value to send.
     */
    public void write(Serializable output) {
        notificationService.send(notificationId, output);
    }
}
