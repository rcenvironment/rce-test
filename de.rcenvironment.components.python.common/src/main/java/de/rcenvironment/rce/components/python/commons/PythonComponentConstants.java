/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.python.commons;

/**
 * Constants shared by GUI and component.
 *
 * @author Markus Litz
 * @author Arne Bachmann
 */
public final class PythonComponentConstants {

    /** Component id (=package name). */
    public static final String COMPONENT_ID = "de.rcenvironment.rce.components.python.internal";

    /** Configuration key denoting the script contents to execute. */
    public static final String SCRIPT = "script";

    /** Configuration key denoting which Python version should be used. */
    public static final String PYTHON_INSTALLATION = "pythonInstallation";

    /**
     * Hide the constructor.
     */
    private PythonComponentConstants() {}

}
