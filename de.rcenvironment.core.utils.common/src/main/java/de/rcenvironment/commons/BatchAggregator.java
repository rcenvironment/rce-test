/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A generic class to group sequentially-generated elements into ordered batches. A batch is
 * returned (to a given {@link BatchProcessor}) when a maximum number of elements is reached, or
 * after a specified time has elapsed since the batch was created. Batches are created implicitly
 * when an element is added while no batch is already active.
 * 
 * @param <T> the element type to aggregate
 * 
 * @author Robert Mischke
 */
public class BatchAggregator<T> {

    // Note: using Timer for a simple first implementation; could be replaced by Quartz if useful

    /**
     * The timer to check for batches that should be finished because the max latency since their
     * creation has elapsed.
     */
    private static Timer sharedMaxLatencyTimer = new Timer("Shared BatchAggregator Max Latency Timer", true);

    /**
     * The timer to process finished batches. Could be handled equivalently by an Executor because
     * currently, all enqueued tasks are scheduled to run as soon as possible.
     */
    private static Timer sharedBatchProcessingTimer = new Timer("Shared BatchAggregator Batch Processing Timer", true);

    /**
     * The TimerTask to check for maximum latency timeouts.
     * 
     * @author Robert Mischke
     */
    private class MaxLatencyTimerCallback extends TimerTask {

        private List<T> relevantBatch;

        public MaxLatencyTimerCallback(List<T> relevantBatch) {
            this.relevantBatch = relevantBatch;
        }

        @Override
        public void run() {
            // delegate for proper synchronization
            onMaxLatencyTimerCallback(relevantBatch);
        }
    }

    /**
     * The TimerTask to send out completed batches in order and from a single thread.
     * 
     * @author Robert Mischke
     */
    private class BatchProcessingTimerCallback extends TimerTask {

        @Override
        public void run() {
            // delegate for proper synchronization
            onSendCompletedBatchTimerCallback();
        }

    }

    // not made final as it needs to be replaced from a unit test
    private static Log logger = LogFactory.getLog(BatchAggregator.class);

    private List<T> currentBatch;

    private Deque<List<T>> outputQueue = new LinkedList<List<T>>();

    private int maxBatchSize;

    private long maxLatency;

    private BatchProcessor<T> processor;

    /**
     * Receiver interface for generated batches.
     * 
     * @param <TT> the element type of the received batches; should match the associated
     *        {@link BatchAggregator}
     * 
     * @author Robert Mischke
     */
    public interface BatchProcessor<TT> {

        /**
         * Callback method for a single generated batch.
         * 
         * @param batch the generated batch
         */
        void processBatch(List<TT> batch);
    }

    public BatchAggregator(int maxBatchSize, long maxLatency, BatchProcessor<T> processor) {
        this.maxBatchSize = maxBatchSize;
        this.maxLatency = maxLatency;
        this.processor = processor;
    }

    /**
     * Adds an element for aggregation. May trigger the internal creation of a new batch or the
     * sending of a finished batch when the the maximum size limit is reached.
     * 
     * @param element the element to add
     */
    public void enqueue(T element) {

        synchronized (this) {
            if (currentBatch == null) {
                startNewBatch();
            }

            currentBatch.add(element);

            int size = currentBatch.size();
            if (size >= maxBatchSize) {
                // sanity check
                Assertions.isFalse(size > maxBatchSize, "maxBatchSize exceeded?");
                // send current batch; the next incoming element will start a new one
                endCurrentBatchAndEnqueueForProcessing();
            }
        }
    }

    /**
     * Logger access for unit tests.
     */
    protected static synchronized void setLogger(Log logger) {
        BatchAggregator.logger = logger;
    }

    /**
     * Logger access for unit tests.
     */
    protected static synchronized Log getLogger() {
        return logger;
    }

    private void onMaxLatencyTimerCallback(List<T> relevantBatch) {
        synchronized (this) {
            if (currentBatch != relevantBatch) {
                // in this case, the batch associated with the calling TimerTask
                // was already sent out because max size was reached
                return;
            }
            endCurrentBatchAndEnqueueForProcessing();
        }
    }

    private void onSendCompletedBatchTimerCallback() {
        sendSingleEnqueuedBatch();
    }

    private void startNewBatch() {
        currentBatch = new ArrayList<T>();
        sharedMaxLatencyTimer.schedule(new MaxLatencyTimerCallback(currentBatch), maxLatency);
    }

    private void endCurrentBatchAndEnqueueForProcessing() {
        synchronized (outputQueue) {
            // enqueue batch
            outputQueue.addLast(currentBatch);
            // schedule asynchronous processing of this batch
            sharedBatchProcessingTimer.schedule(new BatchProcessingTimerCallback(), 0);
        }
        currentBatch = null;
    }

    private void sendSingleEnqueuedBatch() {
        // Note: using block synchronization instead of a BlockingDequeue
        // to prevent threads from polling and sending batches concurrently,
        // which could result in batches being sent out of order
        List<T> batch = null;
        synchronized (outputQueue) {
            batch = outputQueue.pollFirst();
            if (batch == null) {
                // synchronized so that the proper logger is always "seen"
                synchronized (BatchAggregator.class) {
                    logger.warn("sendSingleEnqueuedBatch() called, but output queue was empty");
                }
                return;
            }
        }
        // dispatch the acquired batch outside the "outputQueue" lock
        try {
            processor.processBatch(batch);
        } catch (RuntimeException e) {
            // the best we can do here is log the error and discard the batch
            // synchronized so that the proper logger is always "seen"
            synchronized (BatchAggregator.class) {
                logger.error("Uncaught exception in batch processor " + processor, e);
            }
        }
    }

}
