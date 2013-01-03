/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons.configuration.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a java bean class is used as a configuation bean representing a
 * {@link de.rcenvironment.commons.configuration.ConfigurationInfo}.
 * 
 * @author Christian Weiss
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

}
