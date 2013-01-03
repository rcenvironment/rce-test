/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for Windows registry query.
 *
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public final class RegistryUtil {

    /**
     * Bla.
     */
    private static final String QUERYING_THE_WINDOWS_REGISTRY_FAILED = "Querying the Windows registry failed.";

    /**
     * Our logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(FilteredStderrPipe.class);

    /**
     * Executors.
     */
    private static ExecutorService threadPool;
    
    static {
        threadPool = Executors.newCachedThreadPool();
    }
    
    /**
     * Hide the constructor.
     */
    private RegistryUtil() {}
    
    /**
     * This code works only in Windows and shouldn't be called in other os.
     * 
     * @return The path to the installed default Python executable (python.exe)
     * (at)throws IllegalStateException if current system is antother one than Windows
     */
    public static String getPythonExecutableLocations() {
        String path = null;
        final String os = System.getProperty("os.name", /* fallback */ "Linux");
        if (!os.startsWith("Windows")) {
            throw new IllegalStateException("Cannot query registry on non-Windows operating systems!");
        }
        try {
            final Process process = Runtime.getRuntime().exec("reg query "
                    + "\"HKEY_CLASSES_ROOT\\Applications\\python.exe\\shell\\open\\command\"");
            
            final Future<String> futureStreamContent = threadPool.submit(new StreamReader(process.getInputStream()));
            String streamContent;
            process.waitFor();
            streamContent = futureStreamContent.get();

            int p = streamContent.indexOf("REG_SZ");
            final int eos = -1;
            if (p != eos) {
                streamContent = streamContent.substring(p + "REG_SZ".length()).trim();
                streamContent = streamContent.replace("\"%0\"", "");
                streamContent = streamContent.replace("\"%1\"", "");
                streamContent = streamContent.replace(" %*", "");
                streamContent = streamContent.replace("\"", "");
                streamContent = streamContent.trim();
                if ((new File(streamContent).exists())) {
                    path = streamContent;
                }
            }
        } catch (final IOException e) {
            LOGGER.error(QUERYING_THE_WINDOWS_REGISTRY_FAILED, e);
        } catch (final InterruptedException e) {
            LOGGER.error(QUERYING_THE_WINDOWS_REGISTRY_FAILED, e);
        } catch (ExecutionException e) {
            LOGGER.error(QUERYING_THE_WINDOWS_REGISTRY_FAILED, e);
        }
        
        return path;
    }

}
