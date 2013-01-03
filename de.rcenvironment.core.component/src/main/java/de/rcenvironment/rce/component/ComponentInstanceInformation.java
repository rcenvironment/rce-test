/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.endpoint.Output;


/**
 * Class holding information of a instantiated {@link Component}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class ComponentInstanceInformation implements Serializable {

    private static final long serialVersionUID = -6610792031806026368L;

    private String identifier;

    private String name;

    private String workingDirectory;

    private ComponentDescription componentDescription;

    private ComponentContext componentContext;

    private User user;

    private boolean inputConnected;

    private Map<String, Output> outputs;

    public ComponentInstanceInformation(String newIdentifier, String newName, String newWorkingDirectory,
        ComponentDescription newComponentDescription, ComponentContext newComponentContext,
        User newUser, boolean newInputConnected, Set<Output> newOutputs) {
        identifier = newIdentifier;
        name = newName;
        workingDirectory = newWorkingDirectory;
        componentDescription = newComponentDescription;
        componentContext = newComponentContext;
        user = newUser;
        inputConnected = newInputConnected;
        outputs = new HashMap<String, Output>();
        for (Output output : newOutputs) {
            outputs.put(output.getName(), output);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getComponentIdentifier() {
        return componentDescription.getIdentifier();
    }

    public String getComponentName() {
        return componentDescription.getName();
    }

    public PlatformIdentifier getPlatform() {
        return componentDescription.getPlatform();
    }

    public Map<String, Class<? extends Serializable>> getInputDefinitions() {
        return componentDescription.getInputDefinitions();
    }

    /**
     * @param inputName The name of the {@link Input} to get meta data for.
     * @return the {@link Input}'s meta data.
     */
    public Map<String, Serializable> getInputMetaData(String inputName) {
        return componentDescription.getInputMetaData(inputName);
    }

    public Map<String, Class<? extends Serializable>> getOutputDefinitions() {
        return componentDescription.getOutputDefinitions();
    }

    /**
     * @param outputName The name of the {@link Output} to get meta data for.
     * @return the {@link Output}'s meta data.
     */
    public Map<String, Serializable> getOutputMetaData(String outputName) {
        return componentDescription.getOutputMetaData(outputName);
    }

    /**
     * @return configuration as a name-type map.
     */
    public Map<String, Class<? extends Serializable>> getConfigurationDefinitions() {
        return componentDescription.getConfigurationDefinitions();
    }

    /**
     * @param key The key of the configuration value to get.
     * @return the configuration value associated with the given key.
     */
    public Serializable getConfigurationValue(String key) {
        Serializable result = componentDescription.getConfiguration().get(key);
        if (result instanceof String && ComponentUtils.isPlaceholder((String) result)){
            if (componentDescription.getPlaceholderMap() != null){
                result = componentDescription.getPlaceholderMap().get(
                    getNameOfPlaceholder((String) result));
            } 
        } 
        return result;
    }
    private String getNameOfPlaceholder(String fullPlaceholder) {
        return ComponentUtils.getMatcherForPlaceholder(fullPlaceholder).group(ComponentUtils.PLACEHOLDERNAME);
    }
    public String getComponentContextIdentifier() {
        return componentContext.getIdentifier();
    }

    public String getComponentContextName() {
        return componentContext.getName();
    }

    public PlatformIdentifier getComponentContextControllerPlatform() {
        return componentContext.getControllerPlatform();
    }

    /**
     * Specifies the default platform to store data management entries and history data on. Whether
     * overriding this setting will be allowed by the various data management methods is still an
     * open design issue (see https://www.sistec.dlr.de/mantis/view.php?id=5982).
     * 
     * @return the {@link PlatformIdentifier} of the default platform to create new data management
     *         entries on
     */
    public PlatformIdentifier getDefaultStoragePlatform() {
        return componentContext.getDefaultStoragePlatform();
    }

    public Set<PlatformIdentifier> getPlatformsInvolvedInComponentContext() {
        return componentContext.getInvolvedPlatforms();
    }

    public User getProxyCertificate() {
        return user;
    }

    public boolean isInputConnected() {
        return inputConnected;
    }

    /**
     * @param outputName The name of the {@link Output} to get.
     * @return the {@link Output} with the given name or null if there is none.
     */
    public Output getOutput(String outputName) {
        return outputs.get(outputName);
    }

}
