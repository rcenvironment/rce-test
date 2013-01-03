/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.common;

/**
 * The operation mode of a pioneer component.
 * 
 * @author Christian Weiss
 */
public enum PioneerOperationMode {

    /** Actively triggering the output of the message. */
    ACTIVE,

    /** Passively reacting on input messages with output messages. */
    PASSIVE;

    /**
     * Returns the label of the {@link PioneerOperationMode}.
     * 
     * @return the label
     */
    public String getLabel() {
        final String name = name();
        final String label = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        return label;
    }

}
