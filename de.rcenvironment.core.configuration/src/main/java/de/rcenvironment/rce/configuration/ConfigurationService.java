/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration;

import java.io.File;
import java.util.Map;

/**
 * Service that can be used to retrieve configuration values, based on simple Java POJO mapping.
 * 
 * @author Heinrich Wendel
 * @author Robert Mischke
 */
public interface ConfigurationService {

    /**
     * Registers key-value pairs for variable substitution in configuration data (usually,
     * configuration files). The given properties are added to previously-registered properties. All
     * subsequent {@link #getConfiguration(String, Class)} calls will use the registered properties,
     * usually by applying them to the configuration data before the calling bundle processes its
     * content. Therefore, all variables must be registered before the target bundle is configured.
     * 
     * @param namespace a qualifier for the given properties; how this qualifier is used depends on
     *        the concrete {@link ConfigurationService} implementation
     * @param properties the property map to add to the existing set of properties
     */
    void addSubstitutionProperties(String namespace, Map<String, String> properties);

    /**
     * Retrieves the configuration for the given identifier as Java type of clazz. Usage: CustomType
     * config = getConfiguration("de.rcenvironment.bundle", CustomType.class);
     * 
     * @param identifier The identifier to retrieve the configuration for.
     * @param clazz Type of the object to return.
     * @param <T> Type of the object to return.
     * @return A custom configuration object.
     */
    <T> T getConfiguration(String identifier, Class<T> clazz);

    /**
     * Resolves a path relative to the configuration folder of this bundle to an absolute one. If it
     * is already an absolute one it will be returned as it is.
     * 
     * @param identifier The bundleSymbolicName
     * @param path The path to convert.
     * @return The absolute path.
     */
    String getAbsolutePath(String identifier, String path);

    /**
     * Returns the given host of the local RCE platform.
     * 
     * @return the host.
     */
    @Deprecated
    String getPlatformHost();

    /**
     * Returns the given host of the local RCE platform.
     * 
     * @return the host.
     */
    String getPlatformName();

    /**
     * Determines whether this node may act as a "server", which means that it may provide
     * components, act as a workflow controller, allow remote access etc.
     * 
     * @return true if this node is a workflow host
     */
    boolean getIsWorkflowHost();

    /**
     * Returns the instance identifier of the local RCE platform.
     * 
     * @return the instance identifier.
     */
    int getPlatformNumber();

    /**
     * Returns the directory representing the home of the local RCE platform.
     * 
     * @return the path to the directory.
     */
    String getPlatformHome();

    /**
     * Returns the temporary directory associated with the local RCE platform where
     * {@link Component}s can store their data.
     * 
     * @return the path to the directory.
     */
    // TODO review: why not return a File instance instead? - misc_ro
    String getPlatformTempDir();

    /**
     * Returns the directory where log files should be placed.
     * 
     * @return the path to the directory.
     */
    File getPlatformLogFilesDir();

    /**
     * Returns the configuration area where the json configuration files are located.
     * 
     * @return absolute path to the directory which serves as configuration area for the json
     *         configuration files.
     */
    String getConfigurationArea();

    /**
     * Adds a {@link ConfigurationServiceMessageEventListener} to this {@link ConfigurationService}.
     * 
     * @param listener the listener to add
     */
    void addErrorListener(ConfigurationServiceMessageEventListener listener);

    /**
     * Removes the given {@link ConfigurationServiceMessageEventListener} from this
     * {@link ConfigurationService}.
     * 
     * @param listener the listener to remove
     */
    void removeErrorListener(ConfigurationServiceMessageEventListener listener);

}
