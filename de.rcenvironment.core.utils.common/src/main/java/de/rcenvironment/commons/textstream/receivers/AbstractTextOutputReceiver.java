/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.textstream.receivers;

import de.rcenvironment.commons.textstream.TextOutputReceiver;

/**
 * No-operation (NOP) implementation of {@link TextOutputReceiver} to avoid empty methods in
 * concrete implementations; subclasses only need to override the methods they use.
 * 
 * @author Robert Mischke
 */
public abstract class AbstractTextOutputReceiver implements TextOutputReceiver {

    @Override
    public void onEndOfStream() {}

    @Override
    public void onException(Exception e) {}

    @Override
    public void onStart() {}

    @Override
    public void processLine(String line) {}
}
