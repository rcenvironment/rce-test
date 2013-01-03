/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.start.common;

import java.util.regex.Pattern;

import org.eclipse.equinox.app.IApplicationContext;

/**
 * Parses and saves the RCE console line arguments.
 * @author Sascha Zur
 */
public final class ConsoleLineArguments {
    
    private static final Pattern WORKFLOW_FILENAME_PATTERN = Pattern.compile("^.*\\.wf$");

    private static boolean isHeadless;
    private static boolean openWf;
    private static String workflowPath;
    private static String outputPath;
    
    
    private ConsoleLineArguments(){}
    
    /**
     * Parses and saves the RCE console line arguments.
     * @param context : contains the program arguments
     */
    public static void parseArguments(IApplicationContext context){
        String[] arguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        for (int i = 0; i < arguments.length; i++){
            String s = arguments[i];
            if (s.equals("--headless")){
                isHeadless = true;
            }
            if (s.startsWith("--wf")){
                openWf = true;
            }
            if (s.startsWith("--run-wf") || s.startsWith("--wf")){
                workflowPath = arguments[i + 1];
            }
            if (s.startsWith("--output")){
                outputPath = arguments[i + 1];
            }
       
        }

        if (isHeadless && openWf){
            System.err.println("Invalid arguments!");
            System.exit(1);
        }
        
    }
    
    public static String getWorkflowPath() {
        return workflowPath;
    }
  
    public static String getOutputPath() {
        return outputPath;
    }
   
    public static boolean isHeadless() {
        return isHeadless;
    }
    /**
     * Getter.
     * @return boolean
     */
    public static boolean openWf() {
        return openWf;
    }
    
    
}
