/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.start.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Utilities class to govern the RCE platform.
 * 
 * @author Christian Weiss
 */
public class Platform {

    private static boolean isHeadless;
    
    private static CountDownLatch shutdownLatch = new CountDownLatch(1);

    protected Platform() {
        // do nothing
    }

    /**
     * Returns whether the RCE platform is started in headless mode.
     * 
     * @return the headless state
     */
    public static boolean isHeadless() {
        return isHeadless;
    }
    
    public static void setHeadless(boolean isHeadlessOn){
        isHeadless = isHeadlessOn;
    }

    /**
     * Waits for the RCE Platform to shut down.
     * 
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    /**
     * Waits for the RCE Platform to shut down.
     * 
     * @param timeout the amount of time to wait
     * @param unit the time unit of the timeout argument
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void awaitShutdown(final long timeout, final TimeUnit unit) throws InterruptedException {
        shutdownLatch.await(timeout, unit);
    }

    /**
     * Shuts down the RCE platform instance.
     * 
     */
    public static void shutdown() {
        shutdownLatch.countDown();
        if (!Platform.isHeadless()) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    PlatformUI.getWorkbench().close();
                }
            });
        }
    }
    
    /**
     * Return whether or not the RCE platform has been shut down.
     * 
     * @return true, if the RCE platform has been shut down.
     */
    public static boolean isShutdown() {
        return shutdownLatch.getCount() == 0;
    }
    
    /* default */ static void resetShutdown() {
        shutdownLatch = new CountDownLatch(1);
    }

}
