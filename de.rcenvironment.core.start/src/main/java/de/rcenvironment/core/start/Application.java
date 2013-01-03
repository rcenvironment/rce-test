/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.start;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import de.rcenvironment.core.start.common.ConsoleLineArguments;
import de.rcenvironment.core.start.common.Platform;
import de.rcenvironment.core.start.gui.GUIRunner;
import de.rcenvironment.core.start.headless.HeadlessRunner;
/**
 * This class represents the default application.
 * 
 * @author Sascha Zur
 */
public class Application implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {

        Integer result;
        ConsoleLineArguments.parseArguments(context);
        
        Platform.setHeadless(ConsoleLineArguments.isHeadless());
        if (ConsoleLineArguments.isHeadless()) {
            context.applicationRunning();
            result = HeadlessRunner.runHeadless();    
        } else {
            result = GUIRunner.runGUI();
        }
        return result;
    }

   
    @Override
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }

}
