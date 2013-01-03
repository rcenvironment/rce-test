/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.ConsoleRow;

/**
 * A class to log {@link ConsoleRow} entries in the background.
 * 
 * @author Robert Mischke
 */
public class ConsoleRowLogfileManager {

    /**
     * The number of characters that the log buffer may accumulate before a warning message is
     * logged. Added to check whether background buffering with a low-priority writer thread
     * consumes too much memory in long-running, high-CPU-load workflows.
     */
    private static final int BUFFERED_CHARACTER_COUNT_WARNING_THRESHOLD = 2 * 1024 * 1024; // arbitrary

    private LinkedBlockingQueue<ConsoleRow> outputQueue;

    private Writer fileWriter;

    private WriteToDiskThread writerThread;

    private AtomicInteger bufferedCharacterCount = new AtomicInteger();

    private Log log = LogFactory.getLog(getClass());

    /**
     * The low-priority background thread to write log output to disk.
     * 
     * @author Robert Mischke
     */
    private class WriteToDiskThread extends Thread {

        /**
         * Default constructor.
         */
        public WriteToDiskThread() {
            super("Console Row Log Writer");
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    ConsoleRow row = outputQueue.take();
                    // subtract lenght of contained test string
                    modifyCharacterCount(-row.getText().length());
                    try {
                        // TODO add an explicit flush mechanism to ensure rows are on disk after a
                        // given time?
                        fileWriter.append(String.format("[%s] [%s] [%s] %s%n", row.getWorkflow(), row.getComponent(), row.getTimestamp(),
                            row.getText()));
                    } catch (IOException e) {
                        log.error(e);
                        interrupt();
                    }
                }
            } catch (InterruptedException e) {
                // can happen normally on shutdown; not an error
                log.debug("Log writer thread interrupted");
            }
            try {
                fileWriter.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * Constructor.
     * 
     * @param filename the filename to log to
     * @param autoCloseOnShutdown if true, a shutdown hook will be registered that calls
     *        {@link #close()}
     * @throws IOException if creating the log file failed
     */
    public ConsoleRowLogfileManager(File filename, boolean autoCloseOnShutdown) throws IOException {
        fileWriter = new BufferedWriter(new FileWriter(filename));
        outputQueue = new LinkedBlockingQueue<ConsoleRow>();
        writerThread = new WriteToDiskThread();
        writerThread.start();
        if (autoCloseOnShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread("Console Row Log Shutdown") {

                /**
                 * Shuts down the log writer.
                 */
                @Override
                public void run() {
                    ConsoleRowLogfileManager.this.close();
                }
            });
        }
    }

    /**
     * Enqueues a {@link ConsoleRow} to log. This method is thread-safe.
     * 
     * @param row the {@link ConsoleRow} to log
     */
    public void append(ConsoleRow row) {
        // add the length of contained test string
        modifyCharacterCount(row.getText().length());
        outputQueue.add(row);
    }

    /**
     * Modifies the counter that keeps track of how many characters are stored in the background
     * buffer. Also checks against the defined size limit.
     * 
     * @param delta the "delta" to add to the counter; may be negative to decrement the counter
     */
    private void modifyCharacterCount(int delta) {
        int newTotal = bufferedCharacterCount.addAndGet(delta);
        if (delta > 0) {
            if (newTotal >= BUFFERED_CHARACTER_COUNT_WARNING_THRESHOLD) {
                log.warn(String.format("Background log buffer has grown to %d characters", newTotal));
            }
        } else {
            // consistency check
            if (newTotal < 0) {
                log.error("Integrity violation: buffer count decremented below zero");
            }
        }
    }

    /**
     * Enqueues {@link ConsoleRow} entries to log. This method is thread-safe, although there is no
     * guarantee that lists of {@link ConsoleRow}s passed by concurrent calls are appended as
     * uninterrupted sequences.
     * 
     * @param rows the {@link ConsoleRow}s to log
     */
    public void append(List<ConsoleRow> rows) {
        // add total string length to counter
        int charCount = 0;
        for (ConsoleRow row : rows) {
            charCount += row.getText().length();
        }
        modifyCharacterCount(charCount);
        // add to buffer
        outputQueue.addAll(rows);
    }

    /**
     * Stops logging and closes the output file.
     * 
     * Note: Closing the log file may happen asynchronously.
     */
    public void close() {
        writerThread.interrupt();
    }
}
