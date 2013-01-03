/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.validators.internal;

/**
 * Configuration for the PermGenMinimumValidator.
 * @author Sascha Zur
 *
 */
public class PermGenConfiguration {
    
    private String minimumPermGenSize = "256m";

    public String getMinimumPermGenSize() {
        return minimumPermGenSize;
    }

    public void setMinimumPermGenSize(String minimumPermGenSize) {
        this.minimumPermGenSize = minimumPermGenSize;
    }

}
