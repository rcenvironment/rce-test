/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;


/**
 * Interface for classes that emmit {@link ChannelEvent}s.
 * 
 * @author Christian Weiss
 */
public interface ChannelEventSource {

    /**
     * Adds the specified {@link ChannelListener}.
     * 
     * @param listener the {@link ChannelListener} to add
     */
    void addChannelListener(ChannelListener listener);

    /**
     * Removes the specified {@link ChannelListener}.
     * 
     * @param listener the {@link ChannelListener} to remove
     */
    void removeChannelListener(ChannelListener listener);

}
