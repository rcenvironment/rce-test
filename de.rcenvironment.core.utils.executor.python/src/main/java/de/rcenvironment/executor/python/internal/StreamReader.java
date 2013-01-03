/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reads an {@link InputStream} and provide it as a {@link String} object in the end.
 * 
 * @author Arne Bachmann
 * @author Doreen Seider
 */
public class StreamReader implements Callable<String> {

    private static final Log LOGGER = LogFactory.getLog(StreamReader.class);

    private final InputStream input;

    private final StringWriter writer;

    
    public StreamReader(final InputStream inputStream) {
        input = inputStream;
        writer = new StringWriter();
    }

    @Override
    public String call() throws Exception {
        try {
            int c;
            final int eos = -1;
            while ((c = input.read()) != eos) {
                writer.write(c);
            }
        } catch (final IOException e) {
            LOGGER.error("Reading stream failed", e);
        }
        return writer.toString();
    }

}
