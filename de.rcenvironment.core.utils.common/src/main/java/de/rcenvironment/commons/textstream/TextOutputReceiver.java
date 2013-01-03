/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.textstream;

/**
 * An interface for receivers of line-based text output, for example the stdout/stderr output of
 * invoked programs.
 * 
 * @author Robert Mischke
 * 
 */
public interface TextOutputReceiver {

    /**
     * Initialization event; guaranteed to be fired before any {@link #processLine(String)} calls.
     */
    void onStart();

    /**
     * Provides the next line of text, as received from {@link BufferedReader#readLine()}.
     * 
     * @param line the received line
     */
    void processLine(String line);

    /**
     * Fired when the end of the stream is reached normally. This event is mutually exclusive with
     * {@link #onException(Exception)}.
     */
    void onEndOfStream();

    /**
     * Fired when an exception occurred during reading. Typical reasons are standard stream
     * {@link IOException}s, or an {@link InterruptedException} if the reading thread was
     * interrupted. This event is mutually exclusive with {@link #onEndOfStream()}.
     * 
     * @param e the Exception that has occurred
     */
    void onException(Exception e);

}
