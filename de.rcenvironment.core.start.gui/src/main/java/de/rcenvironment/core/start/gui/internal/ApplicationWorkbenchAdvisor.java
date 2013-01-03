/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.start.gui.internal;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import de.rcenvironment.core.start.common.Platform;
import de.rcenvironment.core.start.common.validation.PlatformValidationManager;

/**
 * This class advises the creation of the workbench of the {@link Application}.
 * 
 * @author Christian Weiss
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return "de.rcenvironment.rce";
    }
    
    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
        WorkbenchAdvisorDelegate.declareWorkbenchImages(getWorkbenchConfigurer());
    }
    
    @Override
    public void preStartup() {
        super.preStartup();
        // validate the platform and exit if not valid
        if (!(new PlatformValidationManager()).validate()) {
            Platform.shutdown();
            throw new RuntimeException("RCE platform validation failed.");
        }
        // required to be able to use the Resource view
        IDE.registerAdapters();
    }

    @Override
    public void postStartup() {
        super.postStartup();
        // refreshes the Resource Explorer, otherwise projects will not be shown
        IWorkbenchWindow[] workbenchs =
            PlatformUI.getWorkbench().getWorkbenchWindows();
        ProjectExplorer view = null;
        for (IWorkbenchWindow workbench : workbenchs) {
            for (IWorkbenchPage page : workbench.getPages()) {
                view = (ProjectExplorer)
                    page.findView("org.eclipse.ui.navigator.ProjectExplorer");
                break;
            }
        }
        if (view == null) {
            return;
        }
        view.getCommonViewer().setInput(ResourcesPlugin.getWorkspace().getRoot());       
    }

}
