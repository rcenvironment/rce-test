/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Provides static access to various {@link Image}s and {@link ImageDescriptor}s used in the
 * {@link DataManagementBrowser}.
 * 
 * @author Markus Litz
 * @author Robert Mischke
 */
public abstract class DMBrowserImages {

    /**
     * The plugin ID used to acquire {@link ImageDescriptor}s.
     */
    // Note: made public as checkstyle complains about order, but these are needed by other fields
    public static final String PLUGIN_ID = "de.rcenvironment.core.gui.datamanagement";

    /**
     * The path to custom image files.
     */
    // Note: made public as checkstyle complains about order, but these are needed by other fields
    public static final String ICONS_PATH_PREFIX = "resources/icons/";

    /**
     * "Workflow" icon.
     */
    public static final Image IMG_WORKFLOW =
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "workflow.gif").createImage();

    /**
     * "Folder" icon.
     */
    public static final ImageDescriptor IMG_DESC_FOLDER = PlatformUI.getWorkbench()
        .getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);

    /**
     * "Folder" icon.
     */
    public static final Image IMG_FOLDER = IMG_DESC_FOLDER.createImage();

    /**
     * "File" icon.
     */
    public static final Image IMG_FILE = PlatformUI.getWorkbench()
        .getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();
    
    /**
     * "Sort" icon.
     */
    public static final ImageDescriptor IMG_SORT_DOWN =  
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "sort.gif");
    /**
     * "Sort" icon.
     */
    public static final ImageDescriptor IMG_SORT_UP =  
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "sort_up.gif");
   
    /**
     * "Sort" icon.
     */
    public static final ImageDescriptor IMG_SORT_TIMESTAMP =  
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "waiting.gif");
    /**
     * "Sort" icon.
     */
    public static final ImageDescriptor IMG_SORT_TIMESTAMPUP =  
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "waitingup.gif");
    /**
     * "Sort" icon.
     */
    public static final ImageDescriptor IMG_SORT_TIMESTAMPDOWN =  
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "waitingdown.gif");
    /**
     * Generic default node/object icon.
     */
    public static final Image IMG_DEFAULT = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "default.gif")
        .createImage();

    /**
     * Warning icon.
     */
    public static final Image IMG_WARNING = PlatformUI.getWorkbench()
        .getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK).createImage();

    /**
     * Info icon.
     */
    public static final Image IMG_INFO = PlatformUI.getWorkbench()
        .getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK).createImage();

    /**
     * "Refresh" icon.
     */
    public static final ImageDescriptor IMG_DESC_REFRESH =
        AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH_PREFIX + "refresh.gif");

    /**
     * "Refresh" icon.
     */
    public static final Image IMG_REFRESH = IMG_DESC_REFRESH.createImage();

    /**
     * "Refresh" icon.
     */
    public static final ImageDescriptor IMG_DESC_COLLAPSE_ALL =
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL);

    /**
     * "Refresh" icon.
     */
    public static final Image IMG_COLLAPSE_ALL = IMG_DESC_COLLAPSE_ALL.createImage();
    
    private DMBrowserImages() {}

}
