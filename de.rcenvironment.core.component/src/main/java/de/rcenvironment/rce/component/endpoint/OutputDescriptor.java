/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;

import de.rcenvironment.rce.component.ComponentInstanceDescriptor;


/**
 * Container holding information describing an {@link Output}.
 *
 * @author Doreen Seider
 */
public class OutputDescriptor implements Serializable {

    private static final long serialVersionUID = -6948222368399977632L;

    private ComponentInstanceDescriptor compInstDescr;

    private String name;

    public OutputDescriptor(ComponentInstanceDescriptor newCompInstDesc, String newName) {
        compInstDescr = newCompInstDesc;
        name = newName;
    }

    public ComponentInstanceDescriptor getComponentInstanceDescriptor() {
        return compInstDescr;
    }

    public String getName() {
        return name;
    }
}
