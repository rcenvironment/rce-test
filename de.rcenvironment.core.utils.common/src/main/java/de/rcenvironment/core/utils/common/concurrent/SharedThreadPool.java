/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.utils.common.concurrent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility wrapper class that provides a shared ExecutorService. Its main purpose is to avoid the
 * redundant creation of thread pools for each service, which would needlessly increase the global
 * thread count. An additional benefit is the ability to reset the thread pools of the communication
 * layer, which is useful to ensure test isolation, and to measure maximum thread usage.
 * 
 * Thread monitoring and debugging is also simplified by providing recognizable thread names.
 * 
 * In addition, this method collects execution statistics about the processed {@link Callable}s and
 * {@link Runnable}s.
 * 
 * @author Robert Mischke
 */
public final class SharedThreadPool {

    private static final float NANOS_TO_MSEC_RATIO = 1000000f;

    /**
     * A simple holder for statistical data.
     * 
     * @author Robert Mischke
     */
    private final class StatisticsEntry {

        private int activeTasks;

        private int completedTasks;

        private int exceptionCount;

        private long maxNormalCompletionTime;

        private long totalCompletionTime;

        private synchronized void beforeExecution() {
            activeTasks++;
        }

        private synchronized void afterExecution(long duration, boolean exception) {
            totalCompletionTime += duration;
            completedTasks++;
            activeTasks--;
            if (exception) {
                exceptionCount++;
            } else {
                if (duration > maxNormalCompletionTime) {
                    maxNormalCompletionTime = duration;
                }
            }
        }

        /**
         * Adds a String representation of this entry to the given {@link StringBuilder}.
         * 
         * @param sb the {@link StringBuilder} to append to
         */
        private void printFormatted(StringBuilder sb) {
            int numCompleted = completedTasks;
            int numActive = activeTasks;
            sb.append("Completed: ");
            sb.append(numCompleted);
            if (numCompleted > 0) {
                long totalTimeNanos = totalCompletionTime;
                float avgTimeMsec = totalTimeNanos / NANOS_TO_MSEC_RATIO / numCompleted;
                sb.append(", AvgTime: ");
                sb.append(avgTimeMsec);
                sb.append(" msec, MaxTime: ");
                sb.append(maxNormalCompletionTime / NANOS_TO_MSEC_RATIO);
                sb.append(" msec");
            }
            if (exceptionCount > 0) {
                sb.append(", Exceptions: ");
                sb.append(exceptionCount);
            }
            sb.append(", Active: ");
            sb.append(numActive);
        }
    }

    /**
     * An internal wrapper for enqueued {@link Callable}s.
     * 
     * @param <T> the callback type of the {@link Callable}s
     * 
     * @author Robert Mischke
     */
    private class WrappedCallable<T> implements Callable<T> {

        private Callable<T> innerCallable;

        public WrappedCallable(Callable<T> callable) {
            this.innerCallable = callable;
        }

        @Override
        public T call() throws Exception {
            StatisticsEntry statisticsEntry = getStatisticsEntry(innerCallable.getClass());
            statisticsEntry.beforeExecution();
            T result;
            long startTime = System.nanoTime();
            boolean exception = false;
            try {
                try {
                    result = innerCallable.call();
                } catch (RuntimeException e) {
                    log.warn("Unhandled exception in Callable for task " + getTaskName(innerCallable.getClass()), e);
                    exception = true;
                    throw e;
                }
            } finally {
                long duration = System.nanoTime() - startTime;
                statisticsEntry.afterExecution(duration, exception);
            }
            return result;
        }

    }

    /**
     * An internal wrapper for enqueued {@link Runnable}s.
     * 
     * @author Robert Mischke
     */
    private class WrappedRunnable implements Runnable {

        private Runnable innerRunnable;

        public WrappedRunnable(Runnable runnable) {
            this.innerRunnable = runnable;
        }

        @Override
        public void run() {
            StatisticsEntry statisticsEntry = getStatisticsEntry(innerRunnable.getClass());
            statisticsEntry.beforeExecution();
            long startTime = System.nanoTime();
            boolean exception = false;
            try {
                try {
                    innerRunnable.run();
                } catch (RuntimeException e) {
                    log.warn("Unhandled exception in Runnable for task " + getTaskName(innerRunnable.getClass()), e);
                    exception = true;
                }
            } finally {
                long duration = System.nanoTime() - startTime;
                statisticsEntry.afterExecution(duration, exception);
            }
        }
    }

    /**
     * Default implementation of {@link CallablesGroup}.
     * 
     * @author Robert Mischke
     */
    private final class CallablesGroupImpl<T> implements CallablesGroup<T> {

        private List<Callable<T>> tasks = new ArrayList<Callable<T>>();

        @Override
        public void add(Callable<T> task) {
            tasks.add(task);
        }

        @Override
        public List<T> executeParallel(AsyncExceptionListener exceptionListener) {
            List<Future<T>> futures = new ArrayList<Future<T>>();
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }
            List<T> results = new ArrayList<T>();
            // note: this approach matches the order of results to the order of added tasks
            for (Future<T> future : futures) {
                try {
                    results.add(future.get());
                } catch (InterruptedException e) {
                    results.add(null);
                    if (exceptionListener != null) {
                        exceptionListener.onAsyncException(e);
                    }
                } catch (ExecutionException e) {
                    results.add(null);
                    if (exceptionListener != null) {
                        exceptionListener.onAsyncException(e);
                    }
                }
            }
            return results;
        }
    }

    private static final String THREAD_NAME_PREFIX = "SharedThreadPool-";

    private static final SharedThreadPool SHARED_INSTANCE = new SharedThreadPool();

    private ExecutorService executorService;

    private AtomicInteger poolIndex = new AtomicInteger(0);

    private AtomicInteger threadIndex = new AtomicInteger(0);

    private ThreadGroup currentThreadGroup;

    private Map<Class<?>, StatisticsEntry> statisticsMap;

    private ScheduledExecutorService schedulerService;

    private final Log log = LogFactory.getLog(getClass());

    private SharedThreadPool() {
        initialize();
    }

    public static SharedThreadPool getInstance() {
        return SHARED_INSTANCE;
    }

    /**
     * @see ExecutorService#execute(Runnable)
     * 
     * @param command the {@link Runnable} to execute
     */
    public void execute(Runnable command) {
        executorService.execute(new WrappedRunnable(command));
    }

    /**
     * @see ExecutorService#submit(Callable)
     * 
     * @param task the {@link Callable} to execute
     * @param <T> the return type of the {@link Callable}
     * @return the result of the {@link Callable}
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(new WrappedCallable<T>(task));
    }

    /**
     * A simplified version of
     * {@link ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)} for
     * scheduling periodic background tasks. The {@link TimeUnit} is always
     * {@link TimeUnit#MILLISECONDS}, and the initial and repetition delays are set to the same
     * value.
     * 
     * {@link Runnable}s scheduled with this method are included in the thread pool statistics.
     * 
     * @param runnable the {@link Runnable} to execute periodically
     * @param repetitionDelayMsec the delay before the first and between subsequent executions
     * @return a {@link ScheduledFuture} that can be used to cancel the task
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long repetitionDelayMsec) {
        return schedulerService.scheduleAtFixedRate(new WrappedRunnable(runnable), repetitionDelayMsec, repetitionDelayMsec,
            TimeUnit.MILLISECONDS);
    }

    /**
     * Intended for unit tests; shuts down the internal executor and replaces it with a new one.
     * This should terminate all well-behaved threads and tasks (ie, those that properly react to
     * interruption).
     * 
     * Note that no synchronization is performed when replacing the internal executor; it is up to
     * the caller to ensure proper thread visibility.
     * 
     * @return the number of enqueued tasks that were never started; should usually be zero
     */
    public int reset() {
        List<Runnable> queued = executorService.shutdownNow();
        executorService = null;
        schedulerService.shutdown();
        schedulerService = null;
        initialize();
        return queued.size();
    }

    /**
     * @return the approximate thread count of the current pool
     * 
     * @see {@link ThreadGroup#activeCount()}.
     */
    public int getCurrentThreadCount() {
        return currentThreadGroup.activeCount();
    }

    /**
     * Creates a {@link CallablesGroup} that uses the internal thread pool.
     * 
     * @param clazz the return type of the {@link Callable}s to execute
     * @param <T> the return type of the {@link Callable}s to execute
     * @return the {@link CallablesGroup} instance
     */
    public <T> CallablesGroup<T> createCallablesGroup(Class<T> clazz) {
        return new CallablesGroupImpl<T>();
    }

    /**
     * Returns a human-readable String representation of the collected statistics.
     * 
     * @return a String representation of the collected statistics
     */
    public String getFormattedStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Asynchronous tasks:\n");
        synchronized (statisticsMap) {
            // TODO sort?
            for (Entry<Class<?>, StatisticsEntry> entry : statisticsMap.entrySet()) {
                Class<?> taskClass = entry.getKey();
                String taskName = getTaskName(taskClass);
                StatisticsEntry statsEntry = entry.getValue();
                synchronized (statsEntry) {
                    sb.append("  ");
                    sb.append(taskName);
                    sb.append("\n    ");
                    statsEntry.printFormatted(sb);
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    private String getTaskName(Class<?> taskClass) {
        Method runMethod;
        try {
            runMethod = taskClass.getMethod("run");
        } catch (NoSuchMethodException e) {
            try {
                runMethod = taskClass.getMethod("call");
            } catch (NoSuchMethodException e2) {
                throw new IllegalStateException("Task is neither Runnable nor Callable?");
            }
        }
        for (Annotation annotation : runMethod.getDeclaredAnnotations()) {
            if (annotation.annotationType() == TaskDescription.class) {
                return ((TaskDescription) annotation).value();
            }
        }
        return "<" + taskClass.getName() + ">";
    }

    private void initialize() {
        final ThreadGroup threadGroup = new ThreadGroup(THREAD_NAME_PREFIX + "ThreadGroup");
        final String threadNamePrefix = THREAD_NAME_PREFIX + poolIndex.incrementAndGet() + "-";
        threadIndex.set(0);
        currentThreadGroup = threadGroup;
        ThreadFactory threadFactory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(threadGroup, r, threadNamePrefix + threadIndex.incrementAndGet());
            }
        };
        executorService = Executors.newCachedThreadPool(threadFactory);
        schedulerService = Executors.newScheduledThreadPool(1, threadFactory);
        statisticsMap = Collections.synchronizedMap(new HashMap<Class<?>, StatisticsEntry>());
    }

    private StatisticsEntry getStatisticsEntry(Class<?> r) {
        StatisticsEntry statisticsEntry = statisticsMap.get(r);
        if (statisticsEntry == null) {
            // NOTE: while this looks similar to the double-checked locking anti-pattern,
            // it should be safe as statisticsMap is a synchronizedMap; the synchronized block only
            // serves to prevent race conditions <b>between</b> the already-synchronized calls
            synchronized (statisticsMap) {
                statisticsEntry = statisticsMap.get(r);
                statisticsEntry = createEntryIfNotPresent(r, statisticsEntry);
            }
        }
        return statisticsEntry;
    }

    /**
     * A workaround method to circumvent the (well-intentioned) CheckStyle double-checked locking
     * prevention.
     */
    private StatisticsEntry createEntryIfNotPresent(Class<?> r, StatisticsEntry statisticsEntry) {
        if (statisticsEntry == null) {
            statisticsEntry = new StatisticsEntry();
            statisticsMap.put(r, statisticsEntry);
        }
        return statisticsEntry;
    }

}
