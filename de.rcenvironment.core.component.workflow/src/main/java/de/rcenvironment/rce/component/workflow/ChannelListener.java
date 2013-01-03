/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import java.util.EventListener;

/**
 * Listener interface to listen to {@link InputChangeEvent}s.
 * 
 * @author Christian Weiss
 */
public interface ChannelListener extends EventListener {

    /**
     * Handle a {@link ChannelEvent}.
     * 
     * @param event the event to handle
     */
    void handleChannelEvent(ChannelEvent event);

}
