/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.start.headless;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Logger;

import org.eclipse.equinox.app.IApplication;

import de.rcenvironment.core.start.common.ConsoleLineArguments;
import de.rcenvironment.core.start.common.Platform;
import de.rcenvironment.core.start.headless.internal.HeadlessConstants;
import de.rcenvironment.core.start.headless.internal.HeadlessNotificationSubscriber;
import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.Session;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.SimpleComponentRegistry;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowExecutionConfigurationHelper;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * Start class for headless run.
 * 
 * @author Sascha Zur
 *
 */
public final class HeadlessRunner{

    private static PlatformIdentifier localPlatform;
    private static boolean windows  = (System.getProperty("os.name").toLowerCase().indexOf("windows") > 0 - 1);

    private HeadlessRunner(){

    }

    /**
     * Runs the headless mode.
     * @return state
     * @throws AuthenticationException : no user
     * @throws InterruptedException : interrupted
     */
    public static Integer runHeadless() throws AuthenticationException, InterruptedException{
        Integer result;
        String filename = ConsoleLineArguments.getWorkflowPath();

        if (filename != null){
            result = runHeadlessWorkflow(filename);
        } else {
            // headless mode with OSGI console
            Platform.awaitShutdown();
            result = IApplication.EXIT_OK;
        }
        return result;
    }

    /**
     * runs a headless workflow.
     * @param filename : workflow file
     * @return state
     * @throws AuthenticationException : no user
     */
    public static Integer runHeadlessWorkflow(String filename) throws AuthenticationException{


        WorkflowDescription wd = null;
        final SimpleComponentRegistry scr = new SimpleComponentRegistry(Session.getInstance().getUser());
        final SimpleCommunicationService scs = new SimpleCommunicationService();
        final SimpleWorkflowRegistry swr = new SimpleWorkflowRegistry(Session.getInstance().getUser());
        final SimpleNotificationService notificationService = new SimpleNotificationService();

        if (filename.startsWith("\\") || filename.startsWith(" ") 
                || (windows  
                        && (filename.startsWith("/")))){
            filename = filename.substring(1);
        }
        filename = new File(filename).getAbsolutePath();
        File workflowFile = new File(filename);
        File logDirectory = createLogDirectory(filename);
        if (workflowFile.exists() && workflowFile.isFile()){
            

            // add complete log for workflow
            FileWriter completeLog = createLog(logDirectory, HeadlessConstants.completeLogName);
            if (completeLog != null) {
                NotificationSubscriber subscriber = new HeadlessNotificationSubscriber(completeLog);
                notificationService.subscribeToAllPlatforms(".*", subscriber);
            }
            WorkflowExecutionConfigurationHelper executionWizard = new WorkflowExecutionConfigurationHelper(scr, scs, swr);
            // instantiate the WorkflowLaunchConfigurationHelper
            if (localPlatform == null) {
                for (PlatformIdentifier platform : scs.getAvailableNodes()) {
                    if (scs.isLocalPlatform(platform)) {
                        localPlatform = platform;
                    }
                }
            }

            wd = executionWizard.loadWorkflow(workflowFile);

            // Set null platforms to localPlatform
            for (WorkflowNode node : wd.getWorkflowNodes()) {
                // replace null (representing localhost) with the actual host name
                if (node.getComponentDescription().getPlatform() == null) {
                    node.getComponentDescription().setPlatform(localPlatform);
                }
            }

            if (wd.getTargetPlatform() ==  null) {
                wd.setTargetPlatform(localPlatform);
            }

            if (executionWizard.isValid(wd)){
                final WorkflowInformation wi = swr.createWorkflowInstance(wd, wd.getName(), new HashMap<String, Object>());
                if (wi == null) {
                    RuntimeException e = new RuntimeException("workflow instance could not be created");
                    throw e;
                }

                /* Add log for components */
                FileWriter componentstatesLog = createLog(logDirectory, HeadlessConstants.componentsStateLogName);
                // object to call back if notification was received
                NotificationSubscriber subscriberComponents = new HeadlessNotificationSubscriber(componentstatesLog);

                // subscribe to a specified notification on all known RCE platforms
                for (ComponentInstanceDescriptor cid : wi.getComponentInstanceDescriptors()){
                    notificationService.subscribeToAllPlatforms("rce.component.state:" + cid.getIdentifier(), subscriberComponents);
                }

                /* Add log for workflow */
                FileWriter workflowLog = createLog(logDirectory, HeadlessConstants.workflowStateLogName);
                // object to call back if notification was received
                NotificationSubscriber subscriberWorkflow = new HeadlessNotificationSubscriber(workflowLog, true);
                notificationService.subscribeToAllPlatforms("rce.component.workflow.state:" + wi.getIdentifier(), subscriberWorkflow);

                /* Add log for components */
                FileWriter componentConsoleLog = createLog(logDirectory, HeadlessConstants.componentsConsoleLogName);
                // object to call back if notification was received
                NotificationSubscriber subscriberComponentConsole = new HeadlessNotificationSubscriber(componentConsoleLog);

                // subscribe to a specified notification on all known RCE platforms
                for (ComponentInstanceDescriptor cid : wi.getComponentInstanceDescriptors()){
                    notificationService.subscribeToAllPlatforms(cid.getIdentifier() + ":rce.component.console", subscriberComponentConsole);
                }

                // Start the workflow
                swr.startWorkflow(wi);

                try {
                    Platform.awaitShutdown(); // Wait till finished
                } catch (InterruptedException e) {
                    Platform.shutdown();
                }

                try {
                    if (completeLog != null){
                        completeLog.close();
                    }
                    if (componentstatesLog != null){
                        componentstatesLog.close();
                    }
                    if (workflowLog != null){
                        workflowLog.close();
                    }
                    if (componentConsoleLog != null){
                        componentConsoleLog.close();
                    }
                } catch (IOException e){
                    Platform.shutdown();
                }
            }
        } else {
            try {
                FileWriter fw = createLog(logDirectory, "headlessError");
                fw.append("Workflow file not valid : " + filename);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                Logger.getLogger(HeadlessRunner.class.toString()).warning("Could not create log directory : " + logDirectory);
            }
            Logger.getLogger(HeadlessRunner.class.toString()).warning("Could not run workflow file : " + filename);
        }
        return IApplication.EXIT_OK;
    }

    private static File createLogDirectory(String filename) {
        int pathEnd = 0 - 1;
        if (windows) {
            pathEnd =  filename.lastIndexOf("\\") + 1;
        } else {
            pathEnd =  filename.lastIndexOf("/") + 1;
        }
        String shortPath = filename.substring(0, pathEnd);
        long millis =  new GregorianCalendar().getTimeInMillis();
        Timestamp ts = new Timestamp(millis);

        String folder = filename.substring(pathEnd, filename.lastIndexOf('.')) + "_" 
                + ts.toString().replace('.', '-').replace(' ', '_').replace(':', '-');
        File newDir = new File(shortPath + "log_" + folder);
        newDir.mkdir();
        return newDir;
    }

    private static FileWriter createLog(File newDir, String logname) {
        File newLog  = new File(newDir.getAbsolutePath() + File.separatorChar + logname + "." + HeadlessConstants.logFileSuffix);
        FileWriter fw;
        try {
            newLog.createNewFile();
            fw = new FileWriter(newLog);

        } catch (IOException e) {
            fw = null;
        }
        return fw;
    }


}
