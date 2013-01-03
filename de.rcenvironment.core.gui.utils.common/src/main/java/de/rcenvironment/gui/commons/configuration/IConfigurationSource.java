/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.gui.commons.configuration;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * The Interface IConfigurationSource.
 * 
 * @author Christian Weiss
 */
public interface IConfigurationSource extends IPropertySource2 {

    /**
     * Returns the {@IPropertyDescriptor}s for the
     * <code>ConfigurationProperty</code>s.
     * 
     * @return the configuration property descriptors
     */
    IPropertyDescriptor[] getConfigurationPropertyDescriptors();

}
