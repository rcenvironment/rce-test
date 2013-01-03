/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.component.executor;

/**
 * Constants for executor components.
 * @author Doreen Seider
 */
public final class SshExecutorConstants {

    /** Configuration key constant. */
    public static final String CONFIG_KEY_HOST = "host";

    /** Configuration key constant. */
    public static final String CONFIG_KEY_PORT = "port";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_SANDBOXROOT = "sandbox root";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_UPLOAD = "upload";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_FILESTOUPLOAD = "files to upload";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_USAGEOFSCRIPT = "usage of script";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_LOCALSCRIPT = "local script";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_LOCALSCRIPTNAME = "local script name";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_REMOTEPATHOFSCRIPT = "remote path of script";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_NAMEOFNEWJOBSCRIPT = "remote upload path of new script";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_SCRIPT = "script";
    
    /** Configuration key constant. */
    public static final String EYMPTY_FILE_LIST_IN_JSON = "{}";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_DOWNLOAD = "download";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_DOWNLOADTARGETISRCE = "download target is rce";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_DOWNLOADTARGETISFILESYSTEM = "download target is file system";
    
    /** Configuration key constant. */
    public static final String CONFIG_KEY_FILESYSTEMPATH = "file system path";

    private SshExecutorConstants() {}
    
}
