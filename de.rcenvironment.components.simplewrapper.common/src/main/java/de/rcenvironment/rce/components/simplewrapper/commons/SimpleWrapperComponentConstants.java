/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.simplewrapper.commons;

import java.util.regex.Pattern;

/**
 * Container holding constants for the basic wrapper component.
 * 
 * @author Christian Weiss
 */
public final class SimpleWrapperComponentConstants {

    /** The property key for the executable directory. */
    public static final String PROPERTY_EXECUTABLE_DIRECTORY = "executableDirectory";

    /** The property key for the executable directory. */
    public static final String PROPERTY_EXECUTABLE_DIRECTORY_CONTENT = "executableDirectoryContent";

    /** The property key for the command string. */
    public static final String PROPERTY_SEPARATE_EXECUTION_DIRECTORIES = "separateExecutionDirectories";

    /** The property key for the command string. */
    public static final String PROPERTY_INIT_COMMAND = "initCommand";

    /** The property key for the command string. */
    public static final String PROPERTY_DO_INIT_COMMAND = "doInitCommand";

    /** The property key for the command string. */
    public static final String PROPERTY_RUN_COMMAND = "runCommand";

    /** The property key for the file mapping. */
    public static final String PROPERTY_FILE_MAPPING = "fileMapping";

    /** The separator for file mappings. */
    public static final String SEPARATOR = ",";

    /** The sub separator for file mappings. */
    public static final String SUB_SEPARATOR = ":";

    /** The Pattern for file mappings. */
    public static final Pattern FILE_MAPPING_PATTERN = Pattern.compile("^([Input|Output]):([^:]*):(.*)$");

    /** The key for the context 'outputPath'. */
    public static final String CONTEXT_INPUT_PATH = "inputPath";

    /** The key for the context 'outputPath'. */
    public static final String CONTEXT_OUTPUT_PATH = "outputPath";

    private SimpleWrapperComponentConstants() {
        // do nothing
    }

}
