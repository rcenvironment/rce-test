/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.textstream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class to read and forward line-based text data from an {@link InputStream}. An internal
 * thread is created that reads the stream and produces the appropriate {@link TextOutputReceiver}
 * events.
 * 
 * TODO add matching unit test
 * 
 * @author Robert Mischke
 * 
 */
public class TextStreamWatcher {

    private final TextOutputReceiver[] receivers;

    private InputStream inputStream;

    private BufferedReader bufferedReader;

    private Thread watcherThread;

    /**
     * An optional file where the input stream is mirrored to.
     */
    private File logFile;

    private volatile boolean streamClosed = false;

    private FileOutputStream logFileStream;

    /**
     * The actual {@link Runnable} that performs the output capture. Uses the outer class fields for
     * delegation.
     * 
     * @author Robert Mischke
     */
    private final class WatcherRunnable implements Runnable {

        @Override
        public void run() {
            for (TextOutputReceiver receiver : receivers) {
                receiver.onStart();
            }
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    for (TextOutputReceiver receiver : receivers) {
                        receiver.processLine(line);
                    }
                }
                for (TextOutputReceiver receiver : receivers) {
                    receiver.onEndOfStream();
                }
            } catch (IOException e) {
                for (TextOutputReceiver receiver : receivers) {
                    try {
                        receiver.onException(e);
                    } catch (RuntimeException e1) {
                        // catch this to make sure the other "onException" handlers are called
                        LogFactory.getLog(getClass()).error("Exception in onException() callback", e1);
                    }
                }
            }

            IOUtils.closeQuietly(bufferedReader);
            streamClosed = true;
        }
    }

    /**
     * Creates an instance (which is not started automatically; see {@link #start()}).
     * 
     * @param input the {@link InputStream} to read from
     * @param receivers the {@link TextOutputReceiver} to send the generated events to
     */
    public TextStreamWatcher(InputStream input, TextOutputReceiver... receivers) {
        this.receivers = receivers;
        this.inputStream = input;

        // guard against "null" listeners
        for (TextOutputReceiver r : receivers) {
            if (r == null) {
                throw new IllegalArgumentException("A 'null' receiver was passed as an argument");
            }
        }
    }

    /**
     * Defines that the raw output should be mirrored to the given file. The target file will be
     * created, or overwritten if it already exists. This method is meant to be called no more than
     * once; repeated calls will cause a {@link IllegalStateException}.
     * 
     * @param file the target file to write to
     * 
     * @throws IOException on I/O errors while setting up the log file
     */
    public void enableLogFile(File file) throws IOException {
        if (watcherThread != null) {
            throw new IllegalStateException("Already started");
        }
        // sanity check
        if (logFile != null) {
            throw new IllegalStateException("Log file was already defined");
        }
        logFile = file;
        logFileStream = new FileOutputStream(logFile);
        // true = auto close stream
        inputStream = new TeeInputStream(inputStream, logFileStream, true);
    }

    /**
     * Returns the file set by {@link #enableLogFile(File)}, or null if none was set.
     * 
     * @return the defined log file
     */
    public File getLogFile() {
        return logFile;
    }

    /**
     * Starts the watcher thread.
     * 
     * @return the "self" instance for convenient chaining
     */
    public TextStreamWatcher start() {
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        watcherThread = new Thread(new WatcherRunnable());
        watcherThread.start();
        return this;
    }

    /**
     * Blocks until the stream watcher thread has terminated.
     * 
     */
    public void waitForTermination() {
        try {
            watcherThread.join();
        } catch (InterruptedException e) {
            LogFactory.getLog(getClass()).debug("Interrupted while waiting for stream watcher thread to end");
        }

        if (logFileStream != null) {
            IOUtils.closeQuietly(logFileStream);
        }
    }

    /**
     * Interrupts the stream watcher thread.
     * 
     */
    public void cancel() {
        watcherThread.interrupt();
    }

    /**
     * Returns whether the watched output stream has ended, either by reaching EOF or because an
     * exception has occurred.
     * 
     * @return true if the watched stream has ended
     */
    public boolean isStreamClosed() {
        return streamClosed;
    }
}
