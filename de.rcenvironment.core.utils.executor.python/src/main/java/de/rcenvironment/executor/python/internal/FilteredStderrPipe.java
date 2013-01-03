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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.executor.python.PythonExecutor;

/**
 * Splitting the stderr stream to stream the part before and retrieve the part after the output variables separator.
 * 
 * @author Doreen Seider
 * @author Arne Bachmann
 */
public class FilteredStderrPipe implements Callable<String> {

    private static final int MS_100 = 100;

    private static final Log LOGGER = LogFactory.getLog(FilteredStderrPipe.class);

    private final LineNumberReader originalStdErr;
    private final PipedInputStream filtereStdErr;
    private final OutputStream writer;
        
    
    /**
     * Take the input stream and return a filtered inputstream only containing the variable information.
     * 
     * @param inputStdErr The original stderr stream to read from
     */
    public FilteredStderrPipe(final InputStream inputStdErr) {
        originalStdErr = new LineNumberReader(new InputStreamReader(inputStdErr));
        filtereStdErr = new PipedInputStream();
        try {
            writer = new PipedOutputStream(filtereStdErr);
        } catch (final IOException e) {
            LOGGER.error("Standard error could not be read and filtered");
            throw new RuntimeException("Standard error could not be read and filtered", e);
        }
    }
    
    /**
     * Here we can retrieve the filtered stream.
     * 
     * @return the std err stream without the variables
     */
    public InputStream getFilteredStdErrStream() {
        return filtereStdErr;
    }

    /**
     * Main method for the callable object.
     * Enables asynchronous streaming of the filtered stderr (without the variable separator and variables)
     * @return An array containing the original stderr and the extracted variable information
     * @throws Exception for any error
     */
    @Override
    public String call() throws Exception {
        final OutputStreamWriter osw = new OutputStreamWriter(writer);
        final StringBuilder varBuffer = new StringBuilder();
        String line;
        boolean unfinished = true;
        try {
            while (!originalStdErr.ready()) {
                Thread.sleep(MS_100);
            }
            while ((line = originalStdErr.readLine()) != null) {
                if (line.equals(PythonExecutor.VARS_SEPARATOR)) {
                    unfinished = false;
                    break;
                }
                osw.write(line + "\n");
            }
            if (!unfinished) { // if we had a separator, then maybe variable values are coming in!
                while ((line = originalStdErr.readLine()) != null) {
                    varBuffer.append(line).append("\n");
                }
            }
            osw.close(); // close the streams
        } catch (final IOException e) {
            LOGGER.error("I/O error while reading Standard error Stream", e);
        }
        
        return varBuffer.toString();
    }

}
