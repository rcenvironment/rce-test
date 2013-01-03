/*
 * Copyright (C) 2006-2012 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.start.gui;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.ChooseWorkspaceDialog;

import de.rcenvironment.core.start.gui.internal.ApplicationWorkbenchAdvisor;

/**
 * Starts the GUI for RCE.
 * @author Sascha Zur
 *
 */
@SuppressWarnings("restriction")
public final class GUIRunner {

    private GUIRunner(){

    }

    /**
     * Starts the RCE GUI.
     * 
     * @return exit code
     * @throws Exception : URL
     */
    public static Integer runGUI() throws Exception {
        Integer result = 0 - 1;
        // initialize the GUI
        Display display = PlatformUI.createDisplay();
        Location loc = Platform.getInstanceLocation();
        // start the workbench - returns as soon as the workbench is closed
        try {
            // Show workspace chooser dialog if not suppressed
            ChooseWorkspaceData cwd = new ChooseWorkspaceData("");
            cwd.readPersistedData();
            ChooseWorkspaceDialog wd = new ChooseWorkspaceDialog(null, cwd, false, true);
            int cwdReturnCode = 0 - 1;
            if (cwd.getShowDialog()){
                cwdReturnCode = wd.open();
            }
            if (!(cwd.getShowDialog() && cwdReturnCode == Dialog.CANCEL)){
                String path = null;
                if (cwd.getSelection() != null){
                    path = new File(cwd.getSelection()).getAbsolutePath().replace(
                        File.separatorChar, '/');
                } else {
                    path = new File(cwd.getRecentWorkspaces()[0]).getAbsolutePath().replace(
                        File.separatorChar, '/');
                }
                URL userWSURL = new URL("file", null, path);
                cwd.writePersistedData();

                loc.set(userWSURL, true);
                int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
                if (returnCode == PlatformUI.RETURN_RESTART) {
                    result = IApplication.EXIT_RESTART;
                } else {
                    result = IApplication.EXIT_OK;
                }
            } else {
                if (cwdReturnCode == Dialog.CANCEL){
                    return IApplication.EXIT_OK;
                }
            }
        } finally {
            display.dispose();
        }
        return result;
    }


}
